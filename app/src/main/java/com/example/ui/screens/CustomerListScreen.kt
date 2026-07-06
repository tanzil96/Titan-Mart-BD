package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Customer
import com.example.ui.viewmodel.AppViewModel
import com.example.utils.Loc

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerListScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val selectedIds by viewModel.selectedCustomerIds.collectAsState()
    val lang by viewModel.language.collectAsState()

    // Dialog flags
    var showManualDialog by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }

    // File Import launcher
    val fileImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importDataFromFile(
                context = context,
                uri = uri,
                onSuccess = { count ->
                    Toast.makeText(context, "$count customers imported successfully!", Toast.LENGTH_LONG).show()
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    // Contacts permission launcher
    val contactPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.importFromContacts(
                context = context,
                onSuccess = { count ->
                    Toast.makeText(context, "$count customers imported from Contacts!", Toast.LENGTH_LONG).show()
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                }
            )
        } else {
            Toast.makeText(context, "Permission Denied: Cannot access Contacts.", Toast.LENGTH_SHORT).show()
        }
    }

    // Filter customers
    val filteredCustomers = remember(customers, searchQuery, statusFilter) {
        customers.filter { customer ->
            val matchesQuery = customer.name.contains(searchQuery, ignoreCase = true) ||
                    customer.phone.contains(searchQuery, ignoreCase = true) ||
                    customer.orderId.contains(searchQuery, ignoreCase = true)

            val matchesFilter = if (statusFilter == "All") {
                true
            } else {
                customer.status.equals(statusFilter, ignoreCase = true)
            }

            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search text field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text(Loc.t("cust_search_placeholder", lang), fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )

        // Status Filter Chips
        val statusList = listOf("All", "Pending", "Sent", "Failed", "Delivered")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            statusList.forEach { filter ->
                val isSelected = statusFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.updateStatusFilter(filter) },
                    label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Selection & Action Bar
        if (selectedIds.isNotEmpty()) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${selectedIds.size} ${Loc.t("cust_selected", lang)}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 14.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { viewModel.clearCustomerSelection() }) {
                            Text(Loc.t("cust_clear_selection", lang), fontSize = 12.sp)
                        }
                        IconButton(onClick = {
                            val selectedCustList = customers.filter { selectedIds.contains(it.id) }
                            selectedCustList.forEach { viewModel.deleteCustomer(it) }
                            viewModel.clearCustomerSelection()
                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }
        } else {
            // Import buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { fileImportLauncher.launch("*/*") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.ImportExport, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Loc.t("cust_import", lang), fontSize = 11.sp)
                }
                
                OutlinedButton(
                    onClick = {
                        val permission = Manifest.permission.READ_CONTACTS
                        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                            viewModel.importFromContacts(
                                context = context,
                                onSuccess = { count ->
                                    Toast.makeText(context, "$count customers imported!", Toast.LENGTH_LONG).show()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        } else {
                            contactPermissionLauncher.launch(permission)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Contacts, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Loc.t("cust_import_contacts", lang), fontSize = 11.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showPasteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Loc.t("cust_pasted_text", lang), fontSize = 11.sp)
                }

                Button(
                    onClick = { showManualDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Loc.t("cust_add_manual", lang), fontSize = 11.sp)
                }
            }
        }

        // Customer count display
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${filteredCustomers.size} customers",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )
            
            if (filteredCustomers.isNotEmpty() && selectedIds.isEmpty()) {
                TextButton(onClick = { viewModel.selectAllCustomers(filteredCustomers) }) {
                    Text(Loc.t("cust_select_all", lang), fontSize = 12.sp)
                }
            }
        }

        // Customers list
        if (filteredCustomers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Loc.t("cust_empty", lang),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 14.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCustomers, key = { it.id }) { customer ->
                    val isSelected = selectedIds.contains(customer.id)
                    CustomerCard(
                        customer = customer,
                        isSelected = isSelected,
                        onSelect = { viewModel.toggleCustomerSelection(customer.id) },
                        onDelete = { viewModel.deleteCustomer(customer) }
                    )
                }
            }
        }
    }

    // Manual Entry Dialog
    if (showManualDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var product by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("1") }
        var deliveryCharge by remember { mutableStateOf("0") }
        var totalAmount by remember { mutableStateOf("") }
        var orderId by remember { mutableStateOf("TM-${System.currentTimeMillis().toString().takeLast(6)}") }
        var remarks by remember { mutableStateOf("") }
        var date by remember { mutableStateOf("Today") }

        AlertDialog(
            onDismissRequest = { showManualDialog = false },
            title = { Text(Loc.t("cust_add_manual", lang), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true)
                    OutlinedTextField(value = product, onValueChange = { product = it }, label = { Text("Product Name") }, singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Qty") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = deliveryCharge, onValueChange = { deliveryCharge = it }, label = { Text("Delivery") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    OutlinedTextField(value = totalAmount, onValueChange = { totalAmount = it }, label = { Text("Total Amount") }, singleLine = true)
                    OutlinedTextField(value = orderId, onValueChange = { orderId = it }, label = { Text("Order ID") }, singleLine = true)
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks") })
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date") }, singleLine = true)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isEmpty() || phone.isEmpty()) {
                            Toast.makeText(context, "Name and Phone cannot be empty!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addManualCustomer(
                            Customer(
                                name = name,
                                phone = phone,
                                product = product.ifEmpty { "General Store" },
                                quantity = quantity.ifEmpty { "1" },
                                deliveryCharge = deliveryCharge.ifEmpty { "0" },
                                totalAmount = totalAmount.ifEmpty { "0" },
                                orderId = orderId,
                                status = "Pending",
                                remarks = remarks,
                                date = date
                            ),
                            onSuccess = {
                                showManualDialog = false
                                Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text(Loc.t("gen_confirm", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualDialog = false }) {
                    Text(Loc.t("gen_cancel", lang))
                }
            }
        )
    }

    // Pasted Text Import Dialog
    if (showPasteDialog) {
        var pasteText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasteDialog = false },
            title = { Text(Loc.t("cust_pasted_text", lang), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste lines of data. Each line: Name, Phone, Product, Total", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    OutlinedTextField(
                        value = pasteText,
                        onValueChange = { pasteText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        placeholder = { Text("Tarek Hossain, 01812345678, Sunglass, 450\nRahman, 01911112222, Shoes, 1200") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importFromPastedText(
                            text = pasteText,
                            onSuccess = { count ->
                                showPasteDialog = false
                                Toast.makeText(context, "$count customers imported successfully!", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text(Loc.t("gen_confirm", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasteDialog = false }) {
                    Text(Loc.t("gen_cancel", lang))
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerCard(
    customer: Customer,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (customer.status.lowercase()) {
        "pending" -> Color(0xFFD97706) // Orange
        "sent" -> Color(0xFF2563EB) // Blue
        "failed" -> Color(0xFFDC2626) // Red
        "delivered" -> Color(0xFF059669) // Green
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onSelect,
                onLongClick = onSelect
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = customer.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = customer.phone,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    contentColor = statusColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = customer.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Product: ${customer.product} (x${customer.quantity})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "OrderID: ${customer.orderId}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "৳${customer.totalAmount}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Deliv: ৳${customer.deliveryCharge}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (customer.remarks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Note: ${customer.remarks}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
