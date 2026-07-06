package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppViewModel
import com.example.utils.Loc

@Composable
fun DashboardScreen(
    viewModel: AppViewModel,
    onNavigate: (String) -> Unit
) {
    val customers by viewModel.allCustomers.collectAsState()
    val smsLogs by viewModel.smsLogs.collectAsState()
    val lang by viewModel.language.collectAsState()

    // Calculations
    val totalCustomers = customers.size
    val totalSent = smsLogs.filter { it.status == "Sent" }.size
    val totalFailed = smsLogs.filter { it.status == "Failed" }.size
    val totalPending = customers.filter { it.status == "Pending" }.size

    // Today's SMS calculations
    val todayMillis = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
    val todaySms = smsLogs.filter { it.timestamp >= todayMillis }.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Brand Banner Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Column {
                Text(
                    text = Loc.t("app_title", lang),
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Loc.t("db_subtitle", lang),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // Stats Title
        Text(
            text = Loc.t("nav_dashboard", lang),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Grid of Stats
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = Loc.t("db_total_customers", lang),
                    value = totalCustomers.toString(),
                    icon = Icons.Rounded.People,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("customers") }
                )
                StatCard(
                    title = Loc.t("db_sms_sent", lang),
                    value = totalSent.toString(),
                    icon = Icons.Rounded.Send,
                    color = Color(0xFFD1FAE5),
                    iconColor = Color(0xFF059669),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("reports") }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = Loc.t("db_sms_failed", lang),
                    value = totalFailed.toString(),
                    icon = Icons.Rounded.ErrorOutline,
                    color = Color(0xFFFEE2E2),
                    iconColor = Color(0xFFDC2626),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("reports") }
                )
                StatCard(
                    title = Loc.t("db_pending", lang),
                    value = totalPending.toString(),
                    icon = Icons.Rounded.HourglassEmpty,
                    color = Color(0xFFFEF3C7),
                    iconColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate("customers") }
                )
            }

            StatCard(
                title = Loc.t("db_today_sms", lang),
                value = todaySms.toString(),
                icon = Icons.Rounded.Today,
                color = MaterialTheme.colorScheme.surfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                onClick = { onNavigate("reports") }
            )
        }

        // Quick Actions Section
        Text(
            text = Loc.t("db_quick_actions", lang),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                label = Loc.t("cust_import", lang),
                icon = Icons.Rounded.UploadFile,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("customers") }
            )
            QuickActionButton(
                label = Loc.t("nav_templates", lang),
                icon = Icons.Rounded.TextSnippet,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("templates") }
            )
            QuickActionButton(
                label = Loc.t("nav_send", lang),
                icon = Icons.Rounded.Sms,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("sms_sending") }
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = iconColor
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = iconColor.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
