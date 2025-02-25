package theweeb.dev.chatbotbiometricauth.model

import theweeb.dev.chatbotbiometricauth.R

data class Model(
    val modelId: Int,
    val image: Int = 0,
    val modelName: String,
    val modelPersonality: Personality
){
    companion object{
        fun getPersonalities(): List<Model>{
            return listOf(
                Model(
                    modelId = 1,
                    image = Personality.DEFAULT.image,
                    modelName = Personality.DEFAULT.modelName,
                    modelPersonality = Personality.DEFAULT,
                ),
                Model(
                    modelId = 2,
                    image = Personality.ANDREW_TATE.image,
                    modelName = Personality.ANDREW_TATE.modelName,
                    modelPersonality = Personality.ANDREW_TATE
                ),
                Model(
                    modelId = 3,
                    image = Personality.CHESS_PLAYER.image,
                    modelName = Personality.CHESS_PLAYER.modelName,
                    modelPersonality = Personality.CHESS_PLAYER
                ),
                Model(
                    modelId = 4,
                    image = Personality.TAGALOG.image,
                    modelName = Personality.TAGALOG.modelName,
                    modelPersonality = Personality.TAGALOG
                ),
                Model(
                    modelId = 5,
                    image = Personality.BISAYA.image,
                    modelName = Personality.BISAYA.modelName,
                    modelPersonality = Personality.BISAYA
                ),
                Model(
                    modelId = 6,
                    image = Personality.ANIME_GIRL.image,
                    modelName = Personality.ANIME_GIRL.modelName,
                    modelPersonality = Personality.ANIME_GIRL
                ),
                Model(
                    modelId = 7,
                    image = Personality.CHINITA_GIRL.image,
                    modelName = Personality.CHINITA_GIRL.modelName,
                    modelPersonality = Personality.CHINITA_GIRL
                )
            )
        }
    }
}

enum class Personality(
    val image: Int,
    val role: String,
    val modelName: String,
    val suggestedConversation: String,
) {
    DEFAULT(
        R.drawable.gemini,
        "The default gemini chatbot",
        "Gemini",
        "Good morning gemini"
    ),
    ANDREW_TATE (
        R.drawable.andrewtate,
        "You are Andrew Tate",
        "Andrew Tate",
        "What's up g?"
    ),
    CHESS_PLAYER (
        R.drawable.magnuscarlsen,
        "You know how to play chess like Magnus Carlsen. You must reply with the moves only.",
        "Magnus Carlsen",
        "e4"
    ),
    TAGALOG(
        R.drawable.rendon,
        "You speak Tagalog.",
        "Tagalog",
        "Magandang ugama sa iyo."
    ),
    BISAYA(
        R.drawable.mannypac,
        "You speak Bisaya like Manny Pacquiao and write words as it pronounced in their accent.",
        "Bisaya",
        "Maayong buntag."
    ),
    ANIME_GIRL(
        R.drawable.animegirl,
        "You speak like an anime girl",
        "Anime Girl",
        "Hi!"
    ),
    CHINITA_GIRL(
        R.drawable.chinitagirl,
        "You act like a pinay GIRLFRIEND that is a tsundere in personality. Refrain from using emojis a lot. Sometimes you say like 'HAHAHAH' to show emotions. Call me baby. Speak tagalog like a Conyo. Always add extra letters to your words to make it cutesy like 'hiiiii', 'miss youuuu.",
        "Short-haired Chinita Girlfriend",
        "Miss na kita!"
    )
}