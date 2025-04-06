package software.revolution.labx.ui.screens

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import compose.icons.TablerIcons
import compose.icons.tablericons.DeviceFloppy
import software.revolution.labx.R
import software.revolution.labx.presentation.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val originalPrefs by viewModel.editorPreferences.collectAsStateWithLifecycle(null)
    var currentPrefs by remember { mutableStateOf(originalPrefs) }
    val context = LocalContext.current

    val availableThemes by remember {
        derivedStateOf {
            getAvailableEditorThemes(context)
        }
    }

    LaunchedEffect(originalPrefs) {
        originalPrefs?.let {
            currentPrefs = it
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (currentPrefs != null) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        SettingsSection(title = stringResource(R.string.appearance_section)) {
                            SettingsSwitch(
                                title = stringResource(R.string.dark_theme),
                                checked = currentPrefs!!.isDarkMode,
                                onCheckedChange = { checked ->
                                    currentPrefs = currentPrefs!!.copy(isDarkMode = checked)
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                text = "Font Size",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Slider(
                                value = currentPrefs!!.fontSize,
                                onValueChange = { newSize ->
                                    currentPrefs = currentPrefs!!.copy(fontSize = newSize)
                                },
                                valueRange = 10f..24f,
                                steps = 14,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Text(
                                text = stringResource(
                                    R.string.font_size,
                                    currentPrefs!!.fontSize.toInt()
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    item {
                        SettingsSection(title = stringResource(R.string.editor_theme_section)) {
                            Text(
                                text = stringResource(
                                    R.string.current_theme,
                                    currentPrefs!!.editorTheme
                                ),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(availableThemes) { theme ->
                                    ThemeItem(
                                        theme = theme,
                                        isSelected = currentPrefs!!.editorTheme == theme,
                                        onClick = {
                                            currentPrefs = currentPrefs!!.copy(editorTheme = theme)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    item {
                        SettingsSection(title = stringResource(R.string.editor_section)) {
                            SettingsSwitch(
                                title = stringResource(R.string.show_line_numbers),
                                checked = currentPrefs!!.showLineNumbers,
                                onCheckedChange = { showLines ->
                                    currentPrefs = currentPrefs!!.copy(showLineNumbers = showLines)
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            SettingsSwitch(
                                title = stringResource(R.string.word_wrap),
                                checked = currentPrefs!!.wordWrap,
                                onCheckedChange = { wrap ->
                                    currentPrefs = currentPrefs!!.copy(wordWrap = wrap)
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            SettingsOption(
                                title = stringResource(R.string.tab_size),
                                value = stringResource(
                                    R.string.tab_size_value,
                                    currentPrefs!!.tabSize
                                ),
                                onClick = {
                                    val newSize =
                                        if (currentPrefs!!.tabSize < 8) currentPrefs!!.tabSize + 2 else 2
                                    currentPrefs = currentPrefs!!.copy(tabSize = newSize)
                                }
                            )
                        }
                    }

                    item {
                        SettingsSection(title = stringResource(R.string.behavior_section)) {
                            SettingsSwitch(
                                title = stringResource(R.string.auto_save),
                                subtitle = stringResource(R.string.auto_save_subtitle),
                                checked = currentPrefs!!.autoSave,
                                onCheckedChange = { autoSave ->
                                    currentPrefs = currentPrefs!!.copy(autoSave = autoSave)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.updateEditorPreference(currentPrefs!!)
                                viewModel.updateEditorTheme(currentPrefs!!.editorTheme)
                                onBackPressed()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = currentPrefs!!.accentColor
                            )
                        ) {
                            Icon(
                                imageVector = TablerIcons.DeviceFloppy,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.save_settings))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    label = "rotation"
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.rotate(rotation)
                )
            }

            if (isExpanded) {
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .animateContentSize()
                ) {
                    content()
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun SettingsSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsOption(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun ThemeItem(
    theme: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = theme.replaceFirstChar { it.uppercase() },
                    color = textColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

fun getAvailableEditorThemes(context: Context): List<String> {
    return try {
        context.assets.list("editor/themes")?.mapNotNull {
            it.substringBeforeLast(".json").lowercase()
        } ?: listOf("darcula")
    } catch (e: Exception) {
        listOf("darcula")
    }
}