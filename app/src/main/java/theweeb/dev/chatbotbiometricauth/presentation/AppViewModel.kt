package theweeb.dev.chatbotbiometricauth.presentation

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.type.FunctionResponsePart
import com.google.ai.client.generativeai.type.InvalidStateException
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import theweeb.dev.chatbotbiometricauth.Constant
import theweeb.dev.chatbotbiometricauth.bitmapToByteArray
import theweeb.dev.chatbotbiometricauth.data.DatabaseModel
import theweeb.dev.chatbotbiometricauth.dataStore
import theweeb.dev.chatbotbiometricauth.model.Conversation
import theweeb.dev.chatbotbiometricauth.model.ConversationWithMessages
import theweeb.dev.chatbotbiometricauth.model.Message
import theweeb.dev.chatbotbiometricauth.model.Note
import theweeb.dev.chatbotbiometricauth.model.NoteSerializable
import theweeb.dev.chatbotbiometricauth.model.NoteTuple
import theweeb.dev.chatbotbiometricauth.model.Personality
import theweeb.dev.chatbotbiometricauth.model.ResponseType
import theweeb.dev.chatbotbiometricauth.model.toNote

class AppViewModel(
    db: DatabaseModel
): ViewModel() {

    private val noteDao = db.noteDao()
    private val conversationDao = db.conversationDao()

    private val conversations: Flow<List<Conversation>>
        get() = conversationDao.getAllConversations().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _currentConversationId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentConversation = _currentConversationId.flatMapLatest { id ->
        id?.let { conversationDao.getConversationWithMessages(it) } ?: emptyFlow()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ConversationWithMessages()
    )

    val notes: Flow<List<NoteTuple>>
        get() = noteDao.getNoteTitles().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _state = MutableStateFlow(ConversationState())
    val state = _state.asStateFlow()

    private val _noteState = MutableStateFlow(NoteState())
    val noteState = _noteState.asStateFlow()

    init {
        viewModelScope.launch {
            conversations.collect{ conversations ->
                Log.d("conversations", "$conversations")
                _state.update {
                    it.copy(
                        conversations = conversations
                    )
                }
            }
        }
    }

    fun setConversationId(conversationId: String){
        _currentConversationId.update { conversationId }
    }

    suspend fun getNote(id: String) {
        val note = noteDao.getNote(id)
        _noteState.update { it.copy(note = note ?: Note(noteId = id)) }
    }

    fun saveModel(context: Context, personality: Personality, conversationId: String) {
        viewModelScope.launch {
            context.dataStore.edit { pref ->
                pref[Constant.modelPersonality] = personality.name
                pref[Constant.conversationId] = conversationId
            }
            getModel(context)
        }
    }

    fun getModel(context: Context) {
        viewModelScope.launch {
            context.dataStore.data.collect { preferences ->
                val personality = preferences[Constant.modelPersonality]
                val conversationId = preferences[Constant.conversationId]
                if(personality != null && conversationId != null)
                    _state.update {
                        setConversationId(conversationId)
                        it.copy(
                            model = _state.value.createModel(
                                personality = Personality.valueOf(personality),
                                currentConversation = currentConversation.value
                            ),
                            isConversationLoading = false
                        )
                    }
                else
                    _state.update {
                        it.copy(
                            isConversationLoading = false
                        )
                    }
            }
        }
    }

    fun noteEvent(event: NoteEvent){
        when(event){
            is NoteEvent.UpsertNote -> {
                viewModelScope.launch { noteDao.upsertNote(event.note) }
            }
            is NoteEvent.DeleteNotes -> {
                viewModelScope.launch { noteDao.deleteNotes(event.ids)  }
            }
        }
    }

    fun conversationEvent(event: ConversationEvent){
        when(event){
            is ConversationEvent.ResetConversation -> {
                viewModelScope.launch {
                    _state.value.messageCollectionJob?.cancel()
                    _state.update { it.copy(messageCollectionJob = null) }
                    conversationDao.clearMessages(event.conversationId)
                }
            }
            is ConversationEvent.SendAudioMessage -> {
                sendMessage(
                    event.content,
                    event.conversationId
                )
            }
            is ConversationEvent.SendTextMessage -> {
                sendMessage(
                    event.content,
                    event.conversationId
                )
            }
            ConversationEvent.StopMessage -> {
                _state.value.messageCollectionJob?.cancel()
            }

            is ConversationEvent.CreateConversation -> {
                viewModelScope.launch {
                    conversationDao.createConversation(event.conversation)
                    setConversationId(event.conversation.conversationId)
                }
            }
        }
    }

    fun clearBitMap(){
        _state.update {
            it.copy(
                bitmap = null
            )
        }
    }

    fun getBitMapFromUri(bitmap: Bitmap?){
        viewModelScope.launch {
            _state.update {
                it.copy(
                    bitmap = bitmap
                )
            }
        }
    }

    private fun sendMessage(message: String, conversationId: String){
        val messageResponse = Message(
            conversationId = conversationId,
            responseType = ResponseType.MODEL.name
        )
        val functionResponse = Message(
            conversationId = conversationId,
            responseType = ResponseType.MODEL.name
        )
        _state.update { it.copy(currentMessage = "") }
        viewModelScope.launch {
            val byteArray = state.value.bitmap?.let { bitmapToByteArray(it) }
            conversationDao.sendMessage(
                Message(
                    content = message,
                    conversationId = conversationId,
                    imageData = byteArray,
                    responseType = ResponseType.USER.name
                )
            )
            _state.update { state ->
                state.copy(
                    messageCollectionJob = launch {
                        try{
                            if(state.bitmap != null){
                                val response = _state.value.model?.sendMessage(
                                    content{
                                        image(state.bitmap)
                                        text(message)
                                    }
                                )
                                conversationDao.sendMessage(Message(
                                    content = response?.text?.trimEnd('\r', '\n') ?: "no message",
                                    conversationId = conversationId,
                                    responseType = ResponseType.MODEL.name)
                                )
                            }
                            else{
                                val response = _state.value.model?.sendMessage(content { text(message) })
                                response?.text?.let {
                                    if(it.isNotBlank())
                                        conversationDao.sendMessage(
                                            messageResponse.copy(
                                                content = it.trimEnd('\r', '\n'))
                                        )
                                }
                                response?.functionCall?.let { functionCall ->
                                    Log.d("functionCallPartHello", functionCall.name)
                                    val matchedFunction = _state.value.model?.model?.tools
                                        ?.flatMap { it.functionDeclarations }
                                        ?.firstOrNull()
                                        ?: throw InvalidStateException("Invalid state or invalid function name")
                                    val apiResponse = matchedFunction.execute(functionCall)
                                    val note = Json.decodeFromString<NoteSerializable>(apiResponse.toString())
                                    noteDao.upsertNote(note.toNote().copy(responseType = _state.value.model!!.personality.modelName))
                                    _state.value.model!!.sendMessage(
                                        content { part(FunctionResponsePart(functionCall.name, apiResponse)) }
                                    ).text?.let { response ->
                                        conversationDao.sendMessage(functionResponse.copy(content = response.trimEnd('\r', '\n')))
                                        _state.update {
                                            it.copy(modelNoteResponse = response.trimEnd('\r', '\n'))
                                        }
                                    }
                                }
                            }
                            clearBitMap()
                        }catch (e: CancellationException){
                            Log.d("cancellationException", e.printStackTrace().toString())
                        }
                    }
                )
            }
        }
    }

    fun onMessageChange(message: String) {
        _state.update { it.copy(currentMessage = message) }
    }

    fun onNoteTitleChange(title: String) {
        _noteState.update { it.copy(note = it.note.copy(title = title)) }
    }

    fun onNoteContentChange(content: String) {
        _noteState.update { it.copy(note = it.note.copy(content = content)) }
    }

    fun clearSnackBarMessage(){
        _state.update {
            it.copy(modelNoteResponse = "")
        }
    }

    fun clearModel(context: Context){
        viewModelScope.launch {
            _state.update {
                it.copy(
                    model = null,
                    currentMessage = ""
                )
            }
            context.dataStore.edit { pref ->
                pref.clear()
            }
            _state.value.messageCollectionJob?.cancel()
            _state.update { it.copy(messageCollectionJob = null) }
        }
    }
}


//    fun createNoteWithModel(message: String){
//        if(_state.value.messageCollectionJob?.isActive == true){
//            _state.value.messageCollectionJob?.cancel()
//        }
//
//        viewModelScope.launch {
//            val response = _state.value.model?.receiveNoteFromModel(
//                content = message
//            )
//            val messageResponse = Message(responseType = ResponseType.MODEL.name)
//            if(response != null){
//                _state.update { state ->
//                    state.copy(
//                        messageCollectionJob = launch {
//                            conversationDao.sendMessage(Message(content = message, responseType = ResponseType.USER.name))
//                            response.first.scan(initial = messageResponse.content) { accumulator, value ->
//                                accumulator + value.text
//                            }.collect {
//                                conversationDao.sendMessage(messageResponse.copy(content = it))
//                            }
//                        }
//                    )
//                }
//                val note = response.second
//                Log.d("note", "$note")
//                note.toNote()?.let { noteDao.upsertNote(it.copy(responseType = _state.value.model!!.personality.modelName)) }
//            }
//        }
//    }

//    private fun getModelResponse(message: String) {
//        if(_state.value.messageCollectionJob?.isActive == true){
//            _state.value.messageCollectionJob?.cancel()
//        }
//
//        val messageResponse = Message(responseType = ResponseType.MODEL.name)
//
//        viewModelScope.launch {
//            val response = _state.value.model?.sendMessage(content { text(message) })
//            _state.update { state ->
//                state.copy(
//                    messageCollectionJob = launch {
//                        response?.scan(initial = messageResponse.content) { accumulator, value ->
//                            accumulator + value.text
//                        }?.collect {
//                            conversationDao.sendMessage(messageResponse.copy(content = it))
//                        }
//                    }
//                )
//            }
//        }
//    }