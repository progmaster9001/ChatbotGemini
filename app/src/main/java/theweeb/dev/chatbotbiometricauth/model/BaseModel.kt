package theweeb.dev.chatbotbiometricauth.model

import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse

class BaseModel(
    val model: GenerativeModel? = null,
    val chat: Chat? = null,
    val personality: Personality = Personality.DEFAULT
) {
    suspend fun sendMessage(content: Content): GenerateContentResponse {
        return chat!!.sendMessage(content)
    }
}