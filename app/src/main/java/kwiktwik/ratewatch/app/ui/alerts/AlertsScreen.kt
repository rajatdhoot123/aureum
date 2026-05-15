package kwiktwik.ratewatch.app.ui.alerts

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.TrendingDown
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kwiktwik.ratewatch.app.data.model.AlertAssetType
import kwiktwik.ratewatch.app.data.model.AlertCondition
import kwiktwik.ratewatch.app.data.model.PriceAlert
import kwiktwik.ratewatch.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlertsScreen(viewModel: AlertsViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Request notification permission on Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied – no action needed */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .aureumBackground()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Text(
                    "Price Alerts",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Get notified when prices hit your targets",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }

            // Current prices card
            item {
                CurrentPricesCard(uiState)
            }

            // Create alert button
            item {
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Create Alert", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }

            // Active alerts header
            if (uiState.alerts.isNotEmpty()) {
                item {
                    Text(
                        "Active Alerts (${uiState.alerts.count { it.enabled }})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Alert items
            items(uiState.alerts, key = { it.id }) { alert ->
                AlertItem(
                    alert = alert,
                    onToggle = { viewModel.toggleAlert(alert.id) },
                    onDelete = { viewModel.deleteAlert(alert.id) }
                )
            }

            // Empty state
            if (uiState.alerts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No alerts yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White.copy(alpha = 0.4f)
                            )
                            Text(
                                "Create an alert to get notified",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            // Refresh info
            item {
                if (uiState.lastRefreshed > 0) {
                    val fmt = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    Text(
                        "Auto-refreshes every 5 min  •  Last: ${fmt.format(Date(uiState.lastRefreshed))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }

    if (showCreateDialog) {
        CreateAlertDialog(
            currentPrices = uiState.currentPrices,
            onDismiss = { showCreateDialog = false },
            onCreate = { assetType, condition, targetPrice ->
                viewModel.createAlert(assetType, condition, targetPrice)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun CurrentPricesCard(uiState: AlertsUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Current Prices",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading && uiState.currentPrices == null) {
                Box(Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GoldAccent, modifier = Modifier.size(24.dp))
                }
            } else {
                val prices = uiState.currentPrices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PricePill("Gold 24K", prices?.gold24kPer10g?.let { "₹${"%,d".format(it)}" } ?: "---", AureumGold)
                    PricePill("Gold 22K", prices?.gold22kPer10g?.let { "₹${"%,d".format(it)}" } ?: "---", GoldAccent)
                    PricePill("Silver", prices?.silverPerKg?.let { "₹${"%,d".format(it)}" } ?: "---", SilverAccent)
                }
            }
        }
    }
}

@Composable
private fun PricePill(label: String, price: String, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = accentColor)
        Spacer(Modifier.height(4.dp))
        Text(price, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun AlertItem(
    alert: PriceAlert,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val isTriggered = alert.lastTriggeredAt != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(20.dp),
        color = AureumCard,
        border = BorderStroke(1.dp, if (isTriggered) EmeraldGreen.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (alert.condition == AlertCondition.ABOVE)
                            EmeraldGreen.copy(alpha = 0.1f)
                        else
                            RubyRed.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (alert.condition == AlertCondition.ABOVE) Icons.Outlined.TrendingUp else Icons.Outlined.TrendingDown,
                    contentDescription = null,
                    tint = if (alert.condition == AlertCondition.ABOVE) EmeraldGreen else RubyRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alert.assetType.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "${alert.condition.displayName} ₹${"%,.0f".format(alert.targetPrice)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f)
                )
                if (isTriggered) {
                    Text(
                        "Triggered",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGreen
                    )
                }
            }

            // Toggle
            Switch(
                checked = alert.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GoldAccent,
                    checkedTrackColor = GoldAccent.copy(alpha = 0.3f)
                )
            )

            Spacer(Modifier.width(8.dp))

            // Delete
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RubyRed.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun CreateAlertDialog(
    currentPrices: kwiktwik.ratewatch.app.data.model.CityPrice?,
    onDismiss: () -> Unit,
    onCreate: (AlertAssetType, AlertCondition, Double) -> Unit
) {
    var selectedAsset by remember { mutableStateOf(AlertAssetType.GOLD_24K) }
    var selectedCondition by remember { mutableStateOf(AlertCondition.ABOVE) }
    var targetPriceText by remember { mutableStateOf("") }

    // Pre-fill with current price for selected asset
    LaunchedEffect(selectedAsset) {
        if (currentPrices != null && targetPriceText.isEmpty()) {
            val price = when (selectedAsset) {
                AlertAssetType.GOLD_24K -> currentPrices.gold24kPer10g
                AlertAssetType.GOLD_22K -> currentPrices.gold22kPer10g
                AlertAssetType.SILVER -> currentPrices.silverPerKg
            }
            if (price != null) targetPriceText = price.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AureumSurface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text("Create Alert", fontWeight = FontWeight.ExtraBold, color = Color.White)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Asset type selector
                Text("Asset", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertAssetType.entries.forEach { asset ->
                        val isSelected = selectedAsset == asset
                        Surface(
                            onClick = { selectedAsset = asset },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GoldAccent else Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, if (isSelected) GoldAccent else Color.White.copy(alpha = 0.1f))
                        ) {
                            Text(
                                when (asset) {
                                    AlertAssetType.GOLD_24K -> "Gold 24K"
                                    AlertAssetType.GOLD_22K -> "Gold 22K"
                                    AlertAssetType.SILVER -> "Silver"
                                },
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Condition selector
                Text("Condition", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertCondition.entries.forEach { cond ->
                        val isSelected = selectedCondition == cond
                        Surface(
                            onClick = { selectedCondition = cond },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                if (cond == AlertCondition.ABOVE) EmeraldGreen.copy(alpha = 0.2f) else RubyRed.copy(alpha = 0.2f)
                            } else Color.White.copy(alpha = 0.05f),
                            border = BorderStroke(1.dp, if (isSelected) {
                                if (cond == AlertCondition.ABOVE) EmeraldGreen else RubyRed
                            } else Color.White.copy(alpha = 0.1f))
                        ) {
                            Text(
                                if (cond == AlertCondition.ABOVE) "Goes Above" else "Goes Below",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                // Target price
                Text("Target Price (₹)", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.6f))
                OutlinedTextField(
                    value = targetPriceText,
                    onValueChange = { targetPriceText = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = GoldAccent
                    ),
                    placeholder = { Text("Enter price", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = targetPriceText.toDoubleOrNull()
                    if (price != null && price > 0) {
                        onCreate(selectedAsset, selectedCondition, price)
                    }
                },
                enabled = targetPriceText.toDoubleOrNull()?.let { it > 0 } == true
            ) {
                Text("Create", fontWeight = FontWeight.Bold, color = GoldAccent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.5f))
            }
        }
    )
}