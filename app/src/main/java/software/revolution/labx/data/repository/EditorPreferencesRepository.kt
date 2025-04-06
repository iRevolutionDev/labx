package software.revolution.labx.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import software.revolution.labx.domain.model.EditorPreferences
import software.revolution.labx.ui.theme.PrimaryLight
import javax.inject.Inject

interface EditorPreferencesRepository {
    val editorPreferences: Flow<EditorPreferences>

    suspend fun updateIsDarkMode(isDarkMode: Boolean)
    suspend fun updateFontSize(fontSize: Float)
    suspend fun updateShowLineNumbers(showLineNumbers: Boolean)
    suspend fun updateTabSize(tabSize: Int)
    suspend fun updateWordWrap(wordWrap: Boolean)
    suspend fun updateAutoSave(autoSave: Boolean)
    suspend fun updateAccentColor(accentColor: Long)
    suspend fun updateEditorTheme(theme: String)
}

class EditorPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : EditorPreferencesRepository {

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val FONT_SIZE = floatPreferencesKey("font_size")
        val SHOW_LINE_NUMBERS = booleanPreferencesKey("show_line_numbers")
        val TAB_SIZE = intPreferencesKey("tab_size")
        val WORD_WRAP = booleanPreferencesKey("word_wrap")
        val AUTO_SAVE = booleanPreferencesKey("auto_save")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val EDITOR_THEME = stringPreferencesKey("editor_theme")
    }

    override val editorPreferences: Flow<EditorPreferences> = dataStore.data.map { preferences ->
        EditorPreferences(
            isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] == true,
            fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 14f,
            showLineNumbers = preferences[PreferencesKeys.SHOW_LINE_NUMBERS] != false,
            tabSize = preferences[PreferencesKeys.TAB_SIZE] ?: 4,
            wordWrap = preferences[PreferencesKeys.WORD_WRAP] != false,
            autoSave = preferences[PreferencesKeys.AUTO_SAVE] == true,
            accentColor = PrimaryLight,
            editorTheme = preferences[PreferencesKeys.EDITOR_THEME] ?: "darcula"
        )
    }

    override suspend fun updateIsDarkMode(isDarkMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = isDarkMode
        }
    }

    override suspend fun updateFontSize(fontSize: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SIZE] = fontSize
        }
    }

    override suspend fun updateShowLineNumbers(showLineNumbers: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_LINE_NUMBERS] = showLineNumbers
        }
    }

    override suspend fun updateTabSize(tabSize: Int) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TAB_SIZE] = tabSize
        }
    }

    override suspend fun updateWordWrap(wordWrap: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WORD_WRAP] = wordWrap
        }
    }

    override suspend fun updateAutoSave(autoSave: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_SAVE] = autoSave
        }
    }

    override suspend fun updateAccentColor(accentColor: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCENT_COLOR] = accentColor
        }
    }

    override suspend fun updateEditorTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.EDITOR_THEME] = theme
        }
    }
}