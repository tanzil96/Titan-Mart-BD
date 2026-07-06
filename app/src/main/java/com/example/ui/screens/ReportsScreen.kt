package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.SmsLog
import com.example.data.model.WhatsAppLog
import com.example.data.parser.ReportExporter
import com.example.ui.viewmodel.AppViewModel
import com.example.utils.Loc
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val smsLogs by viewModel.smsLogs.collectAsState()
    val waLogs by viewModel.whatsappLogs.collectAsState()
    val lang by viewModel.language.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0 for SMS, 1 for WA
    var showExportMenu by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Title Row with Export and Clear buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = Loc.t("nav_reports", lang),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Clear Button
                IconButton(onClick = {
                    if (selectedTab == 0) {
                        viewModel.clearSmsLogs()
                        Toast.makeText(context, "SMS logs cleared.", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.clearWhatsAppLogs()
                        Toast.makeText(context, "WhatsApp logs cleared.", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Rounded.DeleteSweep, contentDescription = Loc.t("rep_clear_logs", lang), tint = Color.Red)
                }

                // Export Dropdown Trigger
                Box {
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Rounded.Share, contentDescription = Loc.t("rep_export", lang), tint = MaterialTheme.colorScheme.primary)
                    }
                    
                    DropdownMenu(
                        expanded = showExportMenu,
                        onDismissRequest = { showExportMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(Loc.t("rep_export_xlsx", lang), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Rounded.TableChart, contentDescription = null, tint = Color(0xFF059669)) },
                            onClick = {
                                showExportMenu = false
                                if (selectedTab == 0) {
                                    val uri = ReportExporter.exportSmsLogsToExcel(context, smsLogs)
                                    if (uri != null) {
                                        ReportExporter.shareExportedFile(context, uri, "Share SMS Excel Report")
                                    } else {
                                        Toast.makeText(context, "Export failed.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val uri = ReportExporter.exportWhatsAppLogsToExcel(context, waLogs)
                                    if (uri != null) {
                                        ReportExporter.shareExportedFile(context, uri, "Share WhatsApp Excel Report")
                                    } else {
                                        Toast.makeText(context, "Export failed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text(Loc.t("rep_export_csv", lang), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Article, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showExportMenu = false
                                if (selectedTab == 0) {
                                    val uri = ReportExporter.exportSmsLogsToCsv(context, smsLogs)
                                    if (uri != null) {
                                        ReportExporter.shareExportedFile(context, uri, "Share SMS CSV Report")
                                    } else {
                                        Toast.makeText(context, "Export failed.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    val uri = ReportExporter.exportWhatsAppLogsToCsv(context, waLogs)
                                    if (uri != null) {
                                        ReportExporter.shareExportedFile(context, uri, "Share WhatsApp CSV Report")
                                    } else {
                                        Toast.makeText(context, "Export failed.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(Loc.t("rep_tab_sms", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(Loc.t("rep_tab_wa", lang), fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // History list
        val currentLogsEmpty = if (selectedTab == 0) smsLogs.isEmpty() else waLogs.isEmpty()
        if (currentLogsEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Loc.t("rep_no_logs", lang),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) {
                    items(smsLogs, key = { it.id }) { log ->
                        LogCard(
                            phone = log.phone,
                            name = log.name,
                            message = log.message,
                            status = log.status,
                            dateStr = dateFormat.format(Date(log.timestamp)),
                            failureReason = log.failureReason
                        )
                    }
                } else {
                    items(waLogs, key = { it.id }) { log ->
                        LogCard(
                            phone = log.phone,
                            name = log.name,
                            message = log.message,
                            status = log.status,
                            dateStr = dateFormat.format(Date(log.timestamp)),
                            failureReason = log.failureReason
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LogCard(
    phone: String,
    name: String,
    message: String,
    status: String,
    dateStr: String,
    failureReason: String?
) {
    val statusColor = when (status.lowercase()) {
        "sent" -> Color(0xFF059669) // Green
        "failed" -> Color(0xFFDC2626) // Red
        else -> Color(0xFFD97706)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(14.dp)
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
                        text = name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = phone,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    contentColor = statusColor,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = message,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!failureReason.isNullOrEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Error: $failureReason",
                        color = Color(0xFF991B1B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = dateStr,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
