package theweeb.dev.chatbotbiometricauth.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import theweeb.dev.chatbotbiometricauth.model.Conversation
import theweeb.dev.chatbotbiometricauth.model.ConversationWithMessages
import theweeb.dev.chatbotbiometricauth.model.Message

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversation")
    fun getAllConversations(): Flow<List<Conversation>>

    @Upsert
    suspend fun createConversation(conversation: Conversation)

    @Transaction
    @Query("SELECT * FROM conversation WHERE conversationId = :conversationId")
    fun getConversationWithMessages(conversationId: String): Flow<ConversationWithMessages>

    @Query("SELECT * FROM message WHERE conversationId = :conversationId ORDER BY date")
    fun getMessages(conversationId: String): Flow<List<Message>>

    @Query("DELETE FROM message WHERE conversationId = :conversationId")
    suspend fun clearMessages(conversationId: String)

    @Upsert
    suspend fun sendMessage(message: Message)

    @Delete
    suspend fun deleteConversation(conversation: Conversation)

    @Transaction
    suspend fun deleteConversationWithMessages(conversation: Conversation) {
        clearMessages(conversation.conversationId)
        deleteConversation(conversation)
    }
}