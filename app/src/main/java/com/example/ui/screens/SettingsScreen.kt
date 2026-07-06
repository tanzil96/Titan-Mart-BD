package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import com.example.ui.viewmodel.AppViewModel
import com.example.utils.Loc

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val lang by viewModel.language.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val defaultSim by viewModel.defaultSim.collectAsState()
    val defaultDelay by viewModel.defaultDelay.collectAsState()
    val customDelayValue by viewModel.customDelayValue.collectAsState()

    var customDelayInput by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(customDelayValue) {
        if (customDelayValue.isNotEmpty() && customDelayInput.isEmpty()) {
            customDelayInput = customDelayValue
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
            text = Loc.t("nav_settings", lang),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Theme setting
        SettingCard(
            title = Loc.t("set_dark_mode", lang),
            icon = Icons.Rounded.Palette
        ) {
            val themes = listOf("system" to "System Default", "light" to "Light Mode", "dark" to "Dark Mode")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                themes.forEach { (key, label) ->
                    val isSelected = darkMode == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateSetting("dark_mode", key) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Language setting
        SettingCard(
            title = Loc.t("set_lang", lang),
            icon = Icons.Rounded.Language
        ) {
            val languages = listOf("en" to "English", "bn" to "বাংলা (Bangla)")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                languages.forEach { (key, label) ->
                    val isSelected = lang == key
                    Button(
                        onClick = { viewModel.updateSetting("language", key) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Default SIM selector
        SettingCard(
            title = Loc.t("set_def_sim", lang),
            icon = Icons.Rounded.SimCard
        ) {
            val sims = listOf("0" to "SIM 1 Default", "1" to "SIM 2 Default")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sims.forEach { (key, label) ->
                    val isSelected = defaultSim == key
                    OutlinedButton(
                        onClick = { viewModel.updateSetting("default_sim", key) },
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
                        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Default Delay & Custom input
        SettingCard(
            title = Loc.t("set_def_delay", lang),
            icon = Icons.Rounded.Timer
        ) {
            val delays = listOf("1" to "1s", "3" to "3s", "5" to "5s", "10" to "10s", "15" to "15s", "custom" to "Custom")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                delays.forEach { (key, label) ->
                    val isSelected = defaultDelay == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.updateSetting("default_delay", key) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            if (defaultDelay == "custom") {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = customDelayInput,
                        onValueChange = { customDelayInput = it },
                        label = { Text("Delay in seconds") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    Button(
                        onClick = {
                            val intVal = customDelayInput.toIntOrNull()
                            if (intVal == null || intVal <= 0) {
                                Toast.makeText(context, "Must be positive integer!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.updateSetting("custom_delay", customDelayInput)
                            Toast.makeText(context, "Custom delay saved.", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }

        // Clean utility card
        SettingCard(
            title = "Database Clean Utility",
            icon = Icons.Rounded.DeleteSweep
        ) {
            Button(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.DeleteSweep, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Delete All Imported Customers")
            }
        }

        // About block card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Text(
                    text = Loc.t("set_about", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = Loc.t("set_about_desc", lang),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete All Customers", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely sure you want to delete all customers and reset the list? This action is offline and cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.clearAllCustomers()
                        Toast.makeText(context, "All customers deleted.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(Loc.t("gen_cancel", lang))
                }
            }
        )
    }
}

@Composable
fun SettingCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            content()
        }
    }
}
