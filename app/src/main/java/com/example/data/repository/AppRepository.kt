package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {

    private val customerDao = db.customerDao()
    private val smsLogDao = db.smsLogDao()
    private val whatsappLogDao = db.whatsappLogDao()
    private val templateDao = db.templateDao()
    private val settingDao = db.settingDao()

    // Customers
    val allCustomersFlow: Flow<List<Customer>> = customerDao.getAllCustomersFlow()
    suspend fun getAllCustomers(): List<Customer> = customerDao.getAllCustomers()
    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    suspend fun insertCustomers(customers: List<Customer>) = customerDao.insertCustomers(customers)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)
    suspend fun deleteCustomers(customers: List<Customer>) = customerDao.deleteCustomers(customers)
    suspend fun getCustomerById(id: Int): Customer? = customerDao.getCustomerById(id)
    suspend fun getCustomerByPhoneAndProduct(phone: String, product: String): Customer? =
        customerDao.getCustomerByPhoneAndProduct(phone, product)
    suspend fun clearAllCustomers() = customerDao.clearAllCustomers()

    // SMS Logs
    val allSmsLogsFlow: Flow<List<SmsLog>> = smsLogDao.getAllSmsLogsFlow()
    suspend fun getAllSmsLogs(): List<SmsLog> = smsLogDao.getAllSmsLogs()
    suspend fun insertSmsLog(log: SmsLog): Long = smsLogDao.insertSmsLog(log)
    suspend fun clearAllSmsLogs() = smsLogDao.clearAllSmsLogs()

    // WhatsApp Logs
    val allWhatsAppLogsFlow: Flow<List<WhatsAppLog>> = whatsappLogDao.getAllWhatsAppLogsFlow()
    suspend fun getAllWhatsAppLogs(): List<WhatsAppLog> = whatsappLogDao.getAllWhatsAppLogs()
    suspend fun insertWhatsAppLog(log: WhatsAppLog): Long = whatsappLogDao.insertWhatsAppLog(log)
    suspend fun clearAllWhatsAppLogs() = whatsappLogDao.clearAllWhatsAppLogs()

    // Templates
    fun getTemplateFlow(id: String): Flow<Template?> = templateDao.getTemplateFlow(id)
    suspend fun getTemplate(id: String): Template? = templateDao.getTemplate(id)
    suspend fun insertTemplate(template: Template) = templateDao.insertTemplate(template)

    // Settings
    fun getSettingFlow(key: String): Flow<Setting?> = settingDao.getSettingFlow(key)
    suspend fun getSetting(key: String): Setting? = settingDao.getSetting(key)
    suspend fun insertSetting(setting: Setting) = settingDao.insertSetting(setting)
    suspend fun getAllSettings(): List<Setting> = settingDao.getAllSettings()

    // Prepopulate defaults if tables are empty
    suspend fun prepopulateDefaults() {
        // Prepopulate templates if missing
        if (getTemplate("sms_default") == null) {
            insertTemplate(
                Template(
                    id = "sms_default",
                    type = "sms",
                    content = "প্রিয় {Name},\n\nTitan Mart BD-এ আপনার {Product} অর্ডারটি সফলভাবে কনফার্ম হয়েছে।\n\nঅর্ডার আইডি:\n{OrderID}\n\nমোট বিল:\n৳{Total}\n\nডেলিভারি চার্জ:\n৳{Delivery}\n\nধন্যবাদ।\n\nTitan Mart BD"
                )
            )
        }
        if (getTemplate("whatsapp_default") == null) {
            insertTemplate(
                Template(
                    id = "whatsapp_default",
                    type = "whatsapp",
                    content = "প্রিয় {Name},\n\nTitan Mart BD-এ আপনার {Product} অর্ডারটি কনফার্ম হয়েছে।\n\nঅর্ডার আইডি:\n{OrderID}\n\nমোট বিল:\n৳{Total}\n\nডেলিভারি চার্জ:\n৳{Delivery}\n\nধন্যবাদ।\n\nTitan Mart BD"
                )
            )
        }

        // Prepopulate default settings if missing
        val defaults = mapOf(
            "dark_mode" to "system",
            "language" to "en",
            "default_sim" to "0", // SIM 1 by default
            "default_delay" to "3" // 3 seconds by default
        )
        for ((key, value) in defaults) {
            if (getSetting(key) == null) {
                insertSetting(Setting(key, value))
            }
        }
    }
}
