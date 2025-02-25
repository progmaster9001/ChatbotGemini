package theweeb.dev.chatbotbiometricauth.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID


@Entity(tableName = "conversation")
data class Conversation(
    @PrimaryKey val conversationId: String = "",
    val modelId: Int = 0,
    val topic: String = "Unidentified",
    val date: String = ""
)

data class ConversationWithMessages(
    @Embedded val conversation: Conversation = Conversation(),
    @Relation(
        parentColumn = "conversationId",
        entityColumn = "conversationId"
    )
    val messages: List<Message> = emptyList()
)