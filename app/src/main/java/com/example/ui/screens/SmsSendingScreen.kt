package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Customer
import com.example.ui.viewmodel.AppViewModel
import com.example.utils.Loc

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SmsSendingScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val selectedIds by viewModel.selectedCustomerIds.collectAsState()
    val lang by viewModel.language.collectAsState()

    // Settings
    val defaultSim by viewModel.defaultSim.collectAsState()
    val defaultDelay by viewModel.defaultDelay.collectAsState()
    val customDelayValue by viewModel.customDelayValue.collectAsState()

    // Sending queue states
    val isSending by viewModel.isSending.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val sentCount by viewModel.sentCount.collectAsState()
    val failedCount by viewModel.failedCount.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val totalToSendCount by viewModel.totalToSendCount.collectAsState()
    val currentProgress by viewModel.currentProgress.collectAsState()
    val progressText by viewModel.progressText.collectAsState()

    // Local configuration
    var targetOption by remember { mutableStateOf("selected") } // "selected" or "all"
    var sendMode by remember { mutableStateOf("sms") } // "sms", "whatsapp", "both"
    var useWaBusiness by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Resolve target customers
    val targetCustomers = remember(customers, selectedIds, targetOption) {
        if (targetOption == "selected") {
            customers.filter { selectedIds.contains(it.id) }
        } else {
            customers
        }
    }

    // Permissions launcher
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        val phoneStateGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: false
        if (smsGranted && phoneStateGranted) {
            showConfirmDialog = true
        } else {
            Toast.makeText(context, "Permissions Denied: SMS Sending requires SEND_SMS and READ_PHONE_STATE permissions.", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Loc.t("nav_send", lang),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (isSending) {
            // Live Progress Panel
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Loc.t("send_progress", lang),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Surface(
                            color = if (isPaused) Color(0xFFFBBF24) else Color(0xFF10B981),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (isPaused) "Paused" else "Running",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = currentProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outlineVariant
                    )

                    Text(
                        text = progressText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Sent", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(sentCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("Failed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(failedCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.Red)
                        }
                        Column {
                            Text("Pending", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(pendingCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column {
                            Text("Total", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                            Text(totalToSendCount.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Sending Controls Button Group
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isPaused) {
                            Button(
                                onClick = { viewModel.resumeSmsSendingQueue() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Loc.t("send_btn_resume", lang), fontSize = 12.sp)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.pauseSmsSendingQueue() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Icon(Icons.Rounded.Pause, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Loc.t("send_btn_pause", lang), fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.stopSmsSendingQueue() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Rounded.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Loc.t("send_btn_stop", lang), fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            // Setup & Configuration Panel
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Target customers count
                    Text(
                        text = "1. Target Customers",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedCard(
                            onClick = { targetOption = "selected" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (targetOption == "selected") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                if (targetOption == "selected") 2.dp else 1.dp,
                                if (targetOption == "selected") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Selected Customers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${selectedIds.size}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        OutlinedCard(
                            onClick = { targetOption = "all" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = if (targetOption == "all") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                if (targetOption == "all") 2.dp else 1.dp,
                                if (targetOption == "all") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("All Customers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${customers.size}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 2. Channel/Medium Choose
                    Text(
                        text = "2. " + Loc.t("wa_mode", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val channels = listOf("sms", "whatsapp")
                        channels.forEach { ch ->
                            val label = if (ch == "sms") Loc.t("wa_mode_sms", lang) else Loc.t("wa_mode_wa", lang)
                            val isSelected = sendMode == ch
                            FilterChip(
                                selected = isSelected,
                                onClick = { sendMode = ch },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    AnimatedVisibility(visible = sendMode == "whatsapp") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = useWaBusiness,
                                onCheckedChange = { useWaBusiness = it }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(Loc.t("wa_use_business", lang), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Show options if sending SMS
                    if (sendMode == "sms") {
                        // SIM cards selection
                        Text(
                            text = "3. " + Loc.t("send_sim_selection", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("0" to "SIM 1", "1" to "SIM 2").forEach { (valKey, name) ->
                                val isSelected = defaultSim == valKey
                                OutlinedButton(
                                    onClick = { viewModel.updateSetting("default_sim", valKey) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Rounded.SimCard, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(name, fontSize = 12.sp)
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Delay Selection
                        Text(
                            text = "4. " + Loc.t("send_delay_selection", lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        val delays = listOf("1" to "1s", "3" to "3s", "5" to "5s", "10" to "10s", "15" to "15s")
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            delays.forEach { (sec, label) ->
                                val isSelected = defaultDelay == sec
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.updateSetting("default_delay", sec) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Final Trigger Button
                    if (sendMode == "sms") {
                        Button(
                            onClick = {
                                if (targetCustomers.isEmpty()) {
                                    Toast.makeText(context, "No customers selected!", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val smsPermission = Manifest.permission.SEND_SMS
                                val phoneStatePermission = Manifest.permission.READ_PHONE_STATE
                                val hasSms = ContextCompat.checkSelfPermission(context, smsPermission) == PackageManager.PERMISSION_GRANTED
                                val hasPhone = ContextCompat.checkSelfPermission(context, phoneStatePermission) == PackageManager.PERMISSION_GRANTED

                                if (hasSms && hasPhone) {
                                    showConfirmDialog = true
                                } else {
                                    smsPermissionLauncher.launch(arrayOf(smsPermission, phoneStatePermission))
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${Loc.t("send_btn_start", lang)} (${targetCustomers.size})")
                        }
                    }
                }
            }

            // WhatsApp Individual Quick Sender Panel
            if (sendMode == "whatsapp") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Launch Personalized WhatsApp Chats",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "WhatsApp doesn't allow bulk background sending on standard offline devices. Tap each targeted customer below to launch personalized WhatsApp automatically.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        if (targetCustomers.isEmpty()) {
                            Text(
                                "No customers targeted. Select customers in customer tab first.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            targetCustomers.forEach { customer ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(customer.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(customer.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            viewModel.sendWhatsAppMessage(
                                                context = context,
                                                customer = customer,
                                                useBusiness = useWaBusiness,
                                                onSuccess = {
                                                    Toast.makeText(context, "Launched chat for ${customer.name}", Toast.LENGTH_SHORT).show()
                                                },
                                                onError = { err ->
                                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                                }
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Rounded.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open Chat", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirm dialog
    if (showConfirmDialog) {
        val count = targetCustomers.size
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(Loc.t("send_confirm_title", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text(Loc.t("send_confirm_msg", lang).replace("{Count}", count.toString())) },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.startSmsSendingQueue(context, targetCustomers)
                    }
                ) {
                    Text(Loc.t("gen_confirm", lang))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(Loc.t("gen_cancel", lang))
                }
            }
        )
    }
}
