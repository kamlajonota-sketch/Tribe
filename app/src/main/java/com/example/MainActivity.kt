package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.auth.model.AuthUser
import com.example.core.auth.viewmodel.AuthState
import com.example.core.auth.viewmodel.AuthViewModel
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalTribeStrings
import com.example.core.localization.TribeLanguageContext
import com.example.core.preferences.UserPreferencesViewModel
import com.example.ui.auth.ForgotPasswordSheet
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.OAuthConfigurationDialog
import com.example.ui.auth.RegisterScreen
import com.example.ui.components.TribeBottomNavigation
import com.example.ui.components.TribeCard
import com.example.ui.components.TribeCategoryChip
import com.example.ui.components.TribeFilterChip
import com.example.ui.components.TribeNavTab
import com.example.ui.components.TribeOutlinedButton
import com.example.ui.components.TribeStatusBadge
import com.example.ui.components.TribeTopBar
import com.example.ui.theme.PillShape
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.TribeTheme
import kotlinx.coroutines.launch

enum class AuthFlowScreen {
    LOGIN,
    REGISTER
}

class MainActivity : ComponentActivity() {

    private val preferencesViewModel: UserPreferencesViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent?.data?.let { uri ->
            authViewModel.handleOAuthUri(uri)
        }

        setContent {
            val themeMode by preferencesViewModel.themeMode.collectAsState()
            val currentLanguage by preferencesViewModel.appLanguage.collectAsState()
            val authState by authViewModel.authState.collectAsState()
            val strings = TribeLanguageContext.getStrings(currentLanguage)

            CompositionLocalProvider(LocalTribeStrings provides strings) {
                TribeTheme(themeMode = themeMode) {
                    TribeMainApp(
                        authState = authState,
                        authViewModel = authViewModel,
                        currentTheme = themeMode,
                        onThemeChange = { preferencesViewModel.updateTheme(it) },
                        currentLanguage = currentLanguage,
                        onLanguageChange = { preferencesViewModel.updateLanguage(it) }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            authViewModel.handleOAuthUri(uri)
        }
    }
}

@Composable
fun TribeMainApp(
    authState: AuthState,
    authViewModel: AuthViewModel,
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var currentAuthScreen by remember { mutableStateOf(AuthFlowScreen.LOGIN) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var showOAuthDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Handle OAuth config requirement notification
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.GoogleOAuthSetupRequired -> {
                showOAuthDialog = Pair(authState.title, authState.reason)
            }
            is AuthState.PasswordRecoverySent -> {
                snackbarHostState.showSnackbar("Password reset email sent to ${authState.email}")
                showForgotPassword = false
            }
            is AuthState.ConfirmationEmailSent -> {
                snackbarHostState.showSnackbar("Confirmation email sent to ${authState.email}. Please verify your inbox.")
                currentAuthScreen = AuthFlowScreen.LOGIN
            }
            else -> {}
        }
    }

    if (showOAuthDialog != null) {
        val (dialogTitle, dialogReason) = showOAuthDialog!!
        OAuthConfigurationDialog(
            title = dialogTitle,
            reason = dialogReason,
            onDismiss = {
                showOAuthDialog = null
                authViewModel.clearError()
            }
        )
    }

    if (showForgotPassword) {
        ForgotPasswordSheet(
            onDismiss = { showForgotPassword = false },
            onSubmit = { email ->
                authViewModel.recoverPassword(email)
            },
            isLoading = authState is AuthState.Loading
        )
    }
    when (authState) {
        is AuthState.Authenticated -> {
            AuthenticatedDashboardScreen(
                user = authState.user,
                onSignOutClick = { authViewModel.signOut() },
                currentTheme = currentTheme,
                onThemeChange = onThemeChange,
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
            )
        }
        else -> {
            // Unauthenticated state: Login or Register screen
            val isLoading = authState is AuthState.Loading
            val errorMessage = (authState as? AuthState.Error)?.message

            when (currentAuthScreen) {
                AuthFlowScreen.LOGIN -> {
                    LoginScreen(
                        onSignInClick = { email, password ->
                            authViewModel.signInWithEmail(email, password)
                        },
                        onGoogleSignInClick = {
                            authViewModel.signInWithGoogle(context)
                        },
                        onForgotPasswordClick = {
                            authViewModel.clearError()
                            showForgotPassword = true
                        },
                        onNavigateToRegister = {
                            authViewModel.clearError()
                            currentAuthScreen = AuthFlowScreen.REGISTER
                        },
                        isLoading = isLoading,
                        errorMessage = errorMessage
                    )
                }
                AuthFlowScreen.REGISTER -> {
                    BackHandler {
                        authViewModel.clearError()
                        currentAuthScreen = AuthFlowScreen.LOGIN
                    }
                    RegisterScreen(
                        onRegisterClick = { fullName, email, password, confirmPassword ->
                            authViewModel.signUpWithEmail(fullName, email, password, confirmPassword)
                        },
                        onNavigateToLogin = {
                            authViewModel.clearError()
                            currentAuthScreen = AuthFlowScreen.LOGIN
                        },
                        isLoading = isLoading,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthenticatedDashboardScreen(
    user: AuthUser,
    onSignOutClick: () -> Unit,
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val strings = TribeLanguageContext.current
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(TribeNavTab.HOME) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("authenticated_dashboard"),
        topBar = {
            TribeTopBar(
                title = "TRIBE",
                actions = {
                    // Active User Badge
                    Surface(
                        shape = PillShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Sign Out Button (Section 10 Requirement)
                    IconButton(
                        onClick = onSignOutClick,
                        modifier = Modifier.testTag("dashboard_sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = strings.signOut,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        bottomBar = {
            TribeBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onCreatePlanClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(strings.createPlanTitle)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Welcome Card with User Greeting
            TribeCard(elevation = 1.dp) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "${strings.greetingTraveler} ${user.displayName}!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = strings.greetingSubtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TribeStatusBadge(status = strings.filterUpcoming)
                        TribeStatusBadge(status = strings.filterPlanned)
                    }
                }
            }

            // Settings & Preferences Card (Phase 1 Persistence Preserved)
            TribeCard(elevation = 1.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${strings.appearance} & ${strings.languageLabel}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Theme Mode Selector
                    Text(
                        text = strings.themeLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TribeFilterChip(
                            text = strings.lightMode,
                            isSelected = currentTheme == ThemeMode.LIGHT,
                            onClick = { onThemeChange(ThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        TribeFilterChip(
                            text = strings.darkMode,
                            isSelected = currentTheme == ThemeMode.DARK,
                            onClick = { onThemeChange(ThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Language Selector
                    Text(
                        text = strings.languageLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TribeFilterChip(
                            text = "English",
                            isSelected = currentLanguage == AppLanguage.ENGLISH,
                            onClick = { onLanguageChange(AppLanguage.ENGLISH) },
                            modifier = Modifier.weight(1f)
                        )
                        TribeFilterChip(
                            text = "বাংলা",
                            isSelected = currentLanguage == AppLanguage.BANGLA,
                            onClick = { onLanguageChange(AppLanguage.BANGLA) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Sign Out Action Card
            TribeCard(elevation = 1.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Session Management",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Authenticated as ${user.email ?: user.id}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    TribeOutlinedButton(
                        text = strings.signOut,
                        onClick = onSignOutClick,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Logout,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        testTag = "session_sign_out_button"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
