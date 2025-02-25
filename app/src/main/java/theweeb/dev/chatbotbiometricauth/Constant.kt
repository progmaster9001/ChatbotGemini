package theweeb.dev.chatbotbiometricauth

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "model")

object Constant {
    val conversationId = stringPreferencesKey("conversation_id")
    val modelPersonality = stringPreferencesKey("model_personality")
    const val API_KEY = "AIzaSyBsU9_b5fQYc979Q38Pywv9r0AiS8ztvHc"
}