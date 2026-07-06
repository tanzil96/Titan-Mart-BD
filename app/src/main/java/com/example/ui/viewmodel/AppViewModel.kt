package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.SmsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.parser.ExcelReader
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val repository: AppRepository
    
    // UI states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("All")
    val statusFilter = _statusFilter.asStateFlow()

    private val _selectedCustomerIds = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCustomerIds = _selectedCustomerIds.asStateFlow()

    // Database Flows
    val allCustomers = MutableStateFlow<List<Customer>>(emptyList())
    val smsLogs = MutableStateFlow<List<SmsLog>>(emptyList())
    val whatsappLogs = MutableStateFlow<List<WhatsAppLog>>(emptyList())

    // Settings States
    val smsTemplate = MutableStateFlow("")
    val whatsappTemplate = MutableStateFlow("")
    val darkMode = MutableStateFlow("system")
    val language = MutableStateFlow("en")
    val defaultSim = MutableStateFlow("0") // "0" for SIM 1, "1" for SIM 2
    val defaultDelay = MutableStateFlow("3") // "1", "3", "5", "10", "15", "custom"
    val customDelayValue = MutableStateFlow("3")

    // Sending Progress states
    private val _isSending = MutableStateFlow(false)
    val isSending = _isSending.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused = _isPaused.asStateFlow()

    private val _sentCount = MutableStateFlow(0)
    val sentCount = _sentCount.asStateFlow()

    private val _failedCount = MutableStateFlow(0)
    val failedCount = _failedCount.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount = _pendingCount.asStateFlow()

    private val _totalToSendCount = MutableStateFlow(0)
    val totalToSendCount = _totalToSendCount.asStateFlow()

    private val _currentProgress = MutableStateFlow(0f)
    val currentProgress = _currentProgress.asStateFlow()

    private val _progressText = MutableStateFlow("")
    val progressText = _progressText.asStateFlow()

    private var sendingJob: Job? = null

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AppRepository(db)

        viewModelScope.launch {
            repository.prepopulateDefaults()
            loadSettings()
            observeDatabase()
        }
    }

    private suspend fun loadSettings() {
        smsTemplate.value = repository.getTemplate("sms_default")?.content ?: ""
        whatsappTemplate.value = repository.getTemplate("whatsapp_default")?.content ?: ""
        
        darkMode.value = repository.getSetting("dark_mode")?.value ?: "system"
        language.value = repository.getSetting("language")?.value ?: "en"
        defaultSim.value = repository.getSetting("default_sim")?.value ?: "0"
        defaultDelay.value = repository.getSetting("default_delay")?.value ?: "3"
        customDelayValue.value = repository.getSetting("custom_delay")?.value ?: "3"
    }

    private fun observeDatabase() {
        viewModelScope.launch {
            repository.allCustomersFlow.collect {
                allCustomers.value = it
            }
        }
        viewModelScope.launch {
            repository.allSmsLogsFlow.collect {
                smsLogs.value = it
            }
        }
        viewModelScope.launch {
            repository.allWhatsAppLogsFlow.collect {
                whatsappLogs.value = it
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun toggleCustomerSelection(id: Int) {
        val current = _selectedCustomerIds.value.toMutableSet()
        if (current.contains(id)) {
            current.remove(id)
        } else {
            current.add(id)
        }
        _selectedCustomerIds.value = current
    }

    fun selectAllCustomers(customers: List<Customer>) {
        _selectedCustomerIds.value = customers.map { it.id }.toSet()
    }

    fun clearCustomerSelection() {
        _selectedCustomerIds.value = emptySet()
    }

    // Save and Update templates & settings
    fun saveSmsTemplate(content: String) {
        smsTemplate.value = content
        viewModelScope.launch {
            repository.insertTemplate(Template("sms_default", "sms", content))
        }
    }

    fun saveWhatsAppTemplate(content: String) {
        whatsappTemplate.value = content
        viewModelScope.launch {
            repository.insertTemplate(Template("whatsapp_default", "whatsapp", content))
        }
    }

    fun updateSetting(key: String, value: String) {
        when (key) {
            "dark_mode" -> darkMode.value = value
            "language" -> language.value = value
            "default_sim" -> defaultSim.value = value
            "default_delay" -> defaultDelay.value = value
            "custom_delay" -> customDelayValue.value = value
        }
        viewModelScope.launch {
            repository.insertSetting(Setting(key, value))
        }
    }

    // Customer imports
    fun importDataFromFile(context: Context, uri: Uri, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val list = ExcelReader.readImportedData(context, uri)
                if (list.isNotEmpty()) {
                    // Check duplicates and insert
                    var insertedCount = 0
                    for (customer in list) {
                        val existing = repository.getCustomerByPhoneAndProduct(customer.phone, customer.product)
                        if (existing == null) {
                            repository.insertCustomer(customer)
                            insertedCount++
                        }
                    }
                    onSuccess(insertedCount)
                } else {
                    onError("No valid records found in file. Make sure columns are matched and numbers are valid BD mobile numbers.")
                }
            } catch (e: Exception) {
                onError("Excel Import Error: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun importFromContacts(context: Context, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val list = mutableListOf<Customer>()
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    arrayOf(
                        android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER
                    ),
                    null,
                    null,
                    null
                )
                cursor?.use {
                    val nameIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneIndex = it.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                    while (it.moveToNext()) {
                        val name = if (nameIndex >= 0) it.getString(nameIndex) else "Contact"
                        val rawPhone = if (phoneIndex >= 0) it.getString(phoneIndex) else ""
                        val phone = ExcelReader.cleanAndValidatePhoneNumber(rawPhone)
                        if (phone != null) {
                            list.add(
                                Customer(
                                    name = name,
                                    phone = phone,
                                    product = "General Store",
                                    quantity = "1",
                                    deliveryCharge = "0",
                                    totalAmount = "0",
                                    orderId = "CT-${System.currentTimeMillis().toString().takeLast(5)}",
                                    status = "Pending",
                                    remarks = "Imported from Contacts",
                                    date = "Today"
                                )
                            )
                        }
                    }
                }
                if (list.isNotEmpty()) {
                    var insertedCount = 0
                    for (customer in list) {
                        val existing = repository.getCustomerByPhoneAndProduct(customer.phone, customer.product)
                        if (existing == null) {
                            repository.insertCustomer(customer)
                            insertedCount++
                        }
                    }
                    onSuccess(insertedCount)
                } else {
                    onError("No contacts found with valid BD mobile numbers.")
                }
            } catch (e: Exception) {
                onError("Permission or read error: ${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }

    fun importFromPastedText(text: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        if (text.trim().isEmpty()) {
            onError("Pasted text is empty.")
            return
        }
        viewModelScope.launch {
            try {
                val list = mutableListOf<Customer>()
                val lines = text.split("\n")
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isEmpty()) continue
                    
                    // Try parsing as comma, tab, or semicolon separated
                    val parts = when {
                        trimmed.contains(",") -> trimmed.split(",")
                        trimmed.contains("\t") -> trimmed.split("\t")
                        trimmed.contains(";") -> trimmed.split(";")
                        else -> listOf(trimmed)
                    }

                    var name = "Customer"
                    var phone: String? = null
                    var product = "General Product"
                    var total = "0"

                    if (parts.size == 1) {
                        // Just a phone number or mixed text
                        val foundPhone = ExcelReader.cleanAndValidatePhoneNumber(parts[0])
                        if (foundPhone != null) {
                            phone = foundPhone
                        }
                    } else if (parts.size >= 2) {
                        name = parts[0].trim()
                        val foundPhone = ExcelReader.cleanAndValidatePhoneNumber(parts[1])
                        if (foundPhone != null) {
                            phone = foundPhone
                        } else {
                            // If second part is not phone, maybe first part is phone?
                            val reversePhone = ExcelReader.cleanAndValidatePhoneNumber(parts[0])
                            if (reversePhone != null) {
                                phone = reversePhone
                                name = parts[1].trim()
                            }
                        }
                        if (parts.size >= 3) {
                            product = parts[2].trim()
                        }
                        if (parts.size >= 4) {
                            total = parts[3].trim()
                        }
                    }

                    if (phone != null) {
                        list.add(
                            Customer(
                                name = name,
                                phone = phone,
                                product = product,
                                quantity = "1",
                                deliveryCharge = "0",
                                totalAmount = total,
                                orderId = "PA-${System.currentTimeMillis().toString().takeLast(5)}",
                                status = "Pending",
                                remarks = "Imported from Pasted Text",
                                date = "Today"
                            )
                        )
                    }
                }

                if (list.isNotEmpty()) {
                    var insertedCount = 0
                    for (customer in list) {
                        val existing = repository.getCustomerByPhoneAndProduct(customer.phone, customer.product)
                        if (existing == null) {
                            repository.insertCustomer(customer)
                            insertedCount++
                        }
                    }
                    onSuccess(insertedCount)
                } else {
                    onError("No valid BD mobile numbers detected in pasted text. Each line should contain Name, Phone, Product, Total.")
                }
            } catch (e: Exception) {
                onError("Failed to parse: ${e.localizedMessage}")
            }
        }
    }

    fun addManualCustomer(customer: Customer, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val phone = ExcelReader.cleanAndValidatePhoneNumber(customer.phone)
            if (phone == null) {
                onError("Invalid BD Mobile Number! Must be 11 digits starting with 01.")
                return@launch
            }
            val existing = repository.getCustomerByPhoneAndProduct(phone, customer.product)
            if (existing != null) {
                onError("Duplicate Customer! A customer with this phone and product already exists.")
                return@launch
            }
            repository.insertCustomer(customer.copy(phone = phone))
            onSuccess()
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun clearAllCustomers() {
        viewModelScope.launch {
            repository.clearAllCustomers()
        }
    }

    fun clearSmsLogs() {
        viewModelScope.launch {
            repository.clearAllSmsLogs()
        }
    }

    fun clearWhatsAppLogs() {
        viewModelScope.launch {
            repository.clearAllWhatsAppLogs()
        }
    }

    // Dual SIM management
    fun getActiveSimCards(context: Context): List<SubscriptionInfo> {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.READ_PHONE_STATE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as? SubscriptionManager
                return subscriptionManager?.activeSubscriptionInfoList ?: emptyList()
            }
        }
        return emptyList()
    }

    // Template variables substitution
    fun compileTemplate(template: String, customer: Customer): String {
        return template
            .replace("{Name}", customer.name)
            .replace("{Phone}", customer.phone)
            .replace("{Product}", customer.product)
            .replace("{Quantity}", customer.quantity)
            .replace("{Delivery}", customer.deliveryCharge)
            .replace("{Total}", customer.totalAmount)
            .replace("{OrderID}", customer.orderId)
    }

    // SMS Sender logic
    private fun sendSms(context: Context, phone: String, message: String, simIndex: Int) {
        val simList = getActiveSimCards(context)
        val subId = if (simIndex in simList.indices) {
            simList[simIndex].subscriptionId
        } else {
            -1
        }

        val smsManager = if (subId != -1) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java).createForSubscriptionId(subId)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getSmsManagerForSubscriptionId(subId)
            }
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
        }

        val parts = smsManager.divideMessage(message)
        if (parts.size > 1) {
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
        } else {
            smsManager.sendTextMessage(phone, null, message, null, null)
        }
    }

    // WhatsApp Message trigger
    fun sendWhatsAppMessage(context: Context, customer: Customer, useBusiness: Boolean, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val message = compileTemplate(whatsappTemplate.value, customer)
        val cleanPhone = customer.phone.replace(Regex("[^0-9]"), "")
        val formattedPhone = if (cleanPhone.startsWith("01") && cleanPhone.length == 11) {
            "88$cleanPhone"
        } else {
            cleanPhone
        }
        
        val packageName = if (useBusiness) "com.whatsapp.w4b" else "com.whatsapp"
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=${Uri.encode(message)}")
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
            setPackage(packageName)
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        try {
            context.startActivity(intent)
            viewModelScope.launch {
                repository.insertWhatsAppLog(
                    WhatsAppLog(
                        phone = customer.phone,
                        name = customer.name,
                        message = message,
                        status = "Sent"
                    )
                )
                repository.updateCustomer(customer.copy(status = "Delivered"))
            }
            onSuccess()
        } catch (e: Exception) {
            // Try fallback
            try {
                val generalIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri).apply {
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(generalIntent)
                viewModelScope.launch {
                    repository.insertWhatsAppLog(
                        WhatsAppLog(
                            phone = customer.phone,
                            name = customer.name,
                            message = message,
                            status = "Sent"
                        )
                    )
                    repository.updateCustomer(customer.copy(status = "Delivered"))
                }
                onSuccess()
            } catch (ex: Exception) {
                viewModelScope.launch {
                    repository.insertWhatsAppLog(
                        WhatsAppLog(
                            phone = customer.phone,
                            name = customer.name,
                            message = message,
                            status = "Failed",
                            failureReason = "WhatsApp application is not installed."
                        )
                    )
                }
                onError(if (useBusiness) "WhatsApp Business is not installed!" else "WhatsApp Messenger is not installed!")
            }
        }
    }

    // Background Queue Send SMS Controller
    fun startSmsSendingQueue(context: Context, targetCustomers: List<Customer>) {
        if (targetCustomers.isEmpty()) return
        _isSending.value = true
        _isPaused.value = false
        _totalToSendCount.value = targetCustomers.size
        _sentCount.value = 0
        _failedCount.value = 0
        _pendingCount.value = targetCustomers.size
        _currentProgress.value = 0f
        _progressText.value = "Starting offline sending queue..."

        val simIndex = when (defaultSim.value) {
            "1" -> 1 // SIM 2
            else -> 0 // SIM 1
        }

        val delayMillis = when (defaultDelay.value) {
            "1" -> 1000L
            "3" -> 3000L
            "5" -> 5000L
            "10" -> 10000L
            "15" -> 15000L
            else -> {
                customDelayValue.value.toLongOrNull()?.times(1000L) ?: 3000L
            }
        }

        sendingJob = viewModelScope.launch {
            for (i in targetCustomers.indices) {
                while (_isPaused.value) {
                    _progressText.value = "Sending paused. (${_sentCount.value}/${_totalToSendCount.value} sent)"
                    delay(500)
                }
                if (!_isSending.value) {
                    _progressText.value = "Sending stopped manually."
                    break
                }

                val customer = targetCustomers[i]
                _progressText.value = "Sending to ${customer.name} (${customer.phone})..."

                val message = compileTemplate(smsTemplate.value, customer)
                try {
                    sendSms(context, customer.phone, message, simIndex)
                    
                    repository.updateCustomer(customer.copy(status = "Sent"))
                    repository.insertSmsLog(
                        SmsLog(
                            phone = customer.phone,
                            name = customer.name,
                            message = message,
                            status = "Sent"
                        )
                    )
                    _sentCount.value++
                } catch (e: Exception) {
                    repository.updateCustomer(customer.copy(status = "Failed"))
                    repository.insertSmsLog(
                        SmsLog(
                            phone = customer.phone,
                            name = customer.name,
                            message = message,
                            status = "Failed",
                            failureReason = e.localizedMessage ?: "SIM error or lack of SMS balance"
                        )
                    )
                    _failedCount.value++
                }

                _pendingCount.value = _totalToSendCount.value - (_sentCount.value + _failedCount.value)
                _currentProgress.value = (_sentCount.value + _failedCount.value).toFloat() / _totalToSendCount.value

                if (i < targetCustomers.size - 1 && _isSending.value) {
                    val seconds = delayMillis / 1000L
                    for (s in seconds downTo 1) {
                        if (!_isSending.value) break
                        while (_isPaused.value) {
                            delay(500)
                        }
                        _progressText.value = "Delaying next message: ${s}s... (${_sentCount.value}/${_totalToSendCount.value} sent)"
                        delay(1000L)
                    }
                }
            }
            _isSending.value = false
            _progressText.value = "Completed! Sent: ${_sentCount.value}, Failed: ${_failedCount.value}"
        }
    }

    fun pauseSmsSendingQueue() {
        if (_isSending.value) {
            _isPaused.value = true
        }
    }

    fun resumeSmsSendingQueue() {
        if (_isSending.value) {
            _isPaused.value = false
        }
    }

    fun stopSmsSendingQueue() {
        _isSending.value = false
        _isPaused.value = false
        sendingJob?.cancel()
        _progressText.value = "Sending queue stopped successfully."
    }
}
