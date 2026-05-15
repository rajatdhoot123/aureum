package kwiktwik.ratewatch.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import kwiktwik.ratewatch.app.R
import kwiktwik.ratewatch.app.ui.theme.*
import kwiktwik.ratewatch.app.util.Language

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val context = LocalContext.current

    var showLanguageDialog by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .sonarBackground()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
            }

            // Language
            item {
                SettingsItem(
                    emoji = "🌐",
                    title = stringResource(R.string.language),
                    subtitle = currentLanguage.nativeName,
                    onClick = { showLanguageDialog = true }
                )
            }

            // About
            item {
                SettingsItem(
                    emoji = "ℹ️",
                    title = stringResource(R.string.about),
                    subtitle = stringResource(R.string.about_subtitle),
                    onClick = {}
                )
            }

            item {
                SettingsItem(
                    emoji = "📡",
                    title = stringResource(R.string.data_source),
                    subtitle = stringResource(R.string.data_source_description),
                    onClick = {}
                )
            }

            item { 
                Spacer(Modifier.height(24.dp))
                Text(
                    stringResource(R.string.regional_language_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            current = currentLanguage,
            languages = viewModel.languageManager.supportedLanguages,
            onLanguageSelected = { lang ->
                viewModel.changeLanguage(context, lang.code)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun SettingsItem(
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = GlassMorphism.surfaceColor(isDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassMorphism.strokeColor(isDark)),
        modifier = Modifier.fillMaxWidth()
    ) {
        ListItem(
            leadingContent = { 
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, style = MaterialTheme.typography.titleLarge) 
                }
            },
            headlineContent = { Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium) },
            supportingContent = { Text(subtitle, style = MaterialTheme.typography.bodySmall) },
            modifier = Modifier.clickable(onClick = onClick),
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    current: Language,
    languages: List<Language>,
    onLanguageSelected: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                stringResource(R.string.change_language),
                fontWeight = FontWeight.ExtraBold
            ) 
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        text = {
            Column {
                languages.forEach { lang ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onLanguageSelected(lang) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = lang.code == current.code,
                            onClick = { onLanguageSelected(lang) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(lang.nativeName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                            Text(lang.englishName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), fontWeight = FontWeight.Bold)
            }
        }
    )
}

