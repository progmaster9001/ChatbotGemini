package theweeb.dev.chatbotbiometricauth.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import theweeb.dev.chatbotbiometricauth.model.Conversation
import theweeb.dev.chatbotbiometricauth.model.Model
import theweeb.dev.chatbotbiometricauth.model.Personality
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@Composable
fun PersonalityPicker(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    conversations: List<Conversation>,
    models: List<Model> = Model.getPersonalities(),
    onChosenPersonality: (Personality) -> Unit,
    createConversation: (Conversation) -> Unit
) {
    Box(modifier = modifier.padding(16.dp)) {
        Column(
            modifier = Modifier.verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            models.forEach { model ->
                val modelConversations = conversations.filter { it.modelId == model.modelId }
                PersonalityCard(
                    model = model,
                    modelConversations = modelConversations,
                    onChosenPersonality = onChosenPersonality,
                    createConversation = createConversation
                )
            }
        }
    }
}

@Composable
fun PersonalityCard(
    model: Model,
    modelConversations: List<Conversation>,
    modifier: Modifier = Modifier,
    onChosenPersonality: (Personality) -> Unit,
    createConversation: (Conversation) -> Unit
) {

    var isPersonalitySelected by remember {
        mutableStateOf(false)
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 10.dp
        ),
        onClick = {
            isPersonalitySelected = !isPersonalitySelected
        }
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(id = model.image),
                contentDescription = null,
                modifier = modifier
                    .height(150.dp)
                    .drawWithCache {
                        val gradient = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black),
                            startY = size.height / 2,
                            endY = size.height
                        )
                        onDrawWithContent {
                            drawContent()
                            drawRect(gradient, blendMode = BlendMode.Multiply)
                        }
                    },
                contentScale = ContentScale.FillWidth,
            )
            Text(
                text = model.modelName,
                modifier = modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )
        }
        AnimatedVisibility(isPersonalitySelected) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(modelConversations.isEmpty()){
                    Text("Conversation Empty")
                }else{
                    modelConversations.forEach { conversation ->
                        ConversationItem(
                            conversation = conversation
                        ){
                            onChosenPersonality(model.modelPersonality)
                        }
                    }
                }
                AddConversationButton(
                    onAddConversation = {
                        onChosenPersonality(model.modelPersonality)
                        createConversation(
                            Conversation(
                                conversationId = UUID.randomUUID().toString(),
                                modelId = model.modelId,
                                date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM HH:mm:ss"))
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    modifier: Modifier = Modifier,
    onSelectedConversation: () -> Unit,
) {
    TextButton(
        onClick = onSelectedConversation,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = conversation.topic
        )
    }
}

@Composable
fun AddConversationButton(
    modifier: Modifier = Modifier,
    onAddConversation: () -> Unit
) {
    OutlinedButton(
        onClick = onAddConversation
    ) {
        Text(text = "New Conversation")
    }
}