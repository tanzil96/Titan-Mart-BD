package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import com.example.ui.viewmodel.AppViewModel
import com.example.utils.Loc

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TemplateScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val lang by viewModel.language.collectAsState()
    val smsTemplate by viewModel.smsTemplate.collectAsState()
    val whatsappTemplate by viewModel.whatsappTemplate.collectAsState()

    var activeSmsText by remember { mutableStateOf("") }
    var activeWaText by remember { mutableStateOf("") }

    // Synchronize local states with flow values on load
    LaunchedEffect(smsTemplate) {
        if (smsTemplate.isNotEmpty() && activeSmsText.isEmpty()) {
            activeSmsText = smsTemplate
        }
    }
    LaunchedEffect(whatsappTemplate) {
        if (whatsappTemplate.isNotEmpty() && activeWaText.isEmpty()) {
            activeWaText = whatsappTemplate
        }
    }

    val variables = listOf(
        Pair("{Name}", "Customer Name"),
        Pair("{Phone}", "Phone Number"),
        Pair("{Product}", "Product Name"),
        Pair("{Quantity}", "Quantity"),
        Pair("{Delivery}", "Delivery Charge"),
        Pair("{Total}", "Total Bill"),
        Pair("{OrderID}", "Order ID")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = Loc.t("nav_templates", lang),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        // Description Card about templates
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "These templates support dynamic field tags. When sending, tags are automatically replaced with the customer's specific order details.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // SMS Template Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = Loc.t("temp_sms_title", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = activeSmsText,
                    onValueChange = { activeSmsText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Write your SMS template here...") }
                )

                // Variable inserts for SMS
                Text(
                    text = Loc.t("temp_variables", lang),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    variables.forEach { (tag, desc) ->
                        SuggestionChip(
                            onClick = { activeSmsText = "$activeSmsText $tag" },
                            label = { Text(tag, fontSize = 11.sp) }
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.saveSmsTemplate(activeSmsText)
                        Toast.makeText(context, Loc.t("temp_saved", lang), Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Loc.t("gen_save", lang))
                }
            }
        }

        // WhatsApp Template Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Text(
                        text = Loc.t("temp_wa_title", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                OutlinedTextField(
                    value = activeWaText,
                    onValueChange = { activeWaText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = { Text("Write your WhatsApp message here...") }
                )

                // Variable inserts for WhatsApp
                Text(
                    text = Loc.t("temp_variables", lang),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    variables.forEach { (tag, desc) ->
                        SuggestionChip(
                            onClick = { activeWaText = "$activeWaText $tag" },
                            label = { Text(tag, fontSize = 11.sp) }
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.saveWhatsAppTemplate(activeWaText)
                        Toast.makeText(context, Loc.t("temp_saved", lang), Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(Loc.t("gen_save", lang))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}
