package theweeb.dev.chatbotbiometricauth.presentation

import theweeb.dev.chatbotbiometricauth.model.Conversation
import theweeb.dev.chatbotbiometricauth.model.Message
import theweeb.dev.chatbotbiometricauth.model.Note

sealed interface ConversationEvent{
    data class CreateConversation(val conversation: Conversation): ConversationEvent
    data class SendTextMessage(val content: String, val conversationId: String): ConversationEvent
    data class SendAudioMessage(val content: String, val conversationId: String): ConversationEvent
    data class ResetConversation(val conversationId: String): ConversationEvent
    data object StopMessage: ConversationEvent
}

sealed interface NoteEvent{
    data class DeleteNotes(val ids: List<String>): NoteEvent
    data class UpsertNote(val note: Note): NoteEvent
}

object Route{
    const val HOME = "home"
    const val NOTE = "note"
}