package theweeb.dev.chatbotbiometricauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import kotlinx.coroutines.launch
import theweeb.dev.chatbotbiometricauth.components.NoteDrawer
import theweeb.dev.chatbotbiometricauth.data.DatabaseModel
import theweeb.dev.chatbotbiometricauth.presentation.AppViewModel
import theweeb.dev.chatbotbiometricauth.presentation.HomeRoute
import theweeb.dev.chatbotbiometricauth.presentation.NoteRoute
import theweeb.dev.chatbotbiometricauth.presentation.Route
import theweeb.dev.chatbotbiometricauth.ui.theme.ChatbotBiometricAuthTheme

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            DatabaseModel::class.java, "my-database"
        ).build()
    }

    private val viewModel by viewModels<AppViewModel> {
        object: ViewModelProvider.Factory{
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AppViewModel(db) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ChatbotBiometricAuthTheme {

                val conversationState by viewModel.state.collectAsStateWithLifecycle()
                val noteTitles by viewModel.notes.collectAsStateWithLifecycle(emptyList())
                val currentConversation by viewModel.currentConversation.collectAsStateWithLifecycle()
                val navController = rememberNavController()

                Scaffold(
                    contentWindowInsets = WindowInsets(0.dp),
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Route.HOME,
                        modifier = Modifier.padding(paddingValues)
                    ){
                        composable(
                            route = Route.HOME,
                            enterTransition = { fadeIn() },
                            exitTransition = { ExitTransition.None },
                        ){
                            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                            val scope = rememberCoroutineScope()
                            BackHandler(enabled = drawerState.isOpen) {
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                            NoteDrawer(
                                notes = noteTitles,
                                drawerState = drawerState,
                                noteEvent = viewModel::noteEvent,
                                onAddNote = { navController.navigate("${Route.NOTE}/$it")},
                                onNoteClick = { navController.navigate("${Route.NOTE}/$it") },
                            ) {
                                HomeRoute(
                                    modifier = Modifier.navigationBarsPadding(),
                                    currentConversation = currentConversation,
                                    setConversationId = viewModel::setConversationId,
                                    conversationState = conversationState,
                                    drawerState = drawerState,
                                    viewModel = viewModel,
                                    openDrawer = { scope.launch { drawerState.open() } }
                                )
                            }
                        }
                        composable(
                            route = "${Route.NOTE}/{noteId}",
                            arguments = listOf(navArgument("noteId") { type = NavType.StringType }),
                            enterTransition = { slideInHorizontally { it } },
                            exitTransition = { slideOutHorizontally { it } }
                        ){
                            NoteRoute(
                                noteId = it.arguments?.getString("noteId") ?: "",
                                viewModel = viewModel,
                                back = { navController.navigateUp() }
                            )
                        }
                    }
                }
            }
        }
    }
}