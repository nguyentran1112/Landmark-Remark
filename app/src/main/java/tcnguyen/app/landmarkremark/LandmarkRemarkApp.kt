package tcnguyen.app.landmarkremark

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tcnguyen.app.landmarkremark.ui.navigations.LandmarkRemarkDestinations
import tcnguyen.app.landmarkremark.ui.screens.MapScreenContent
import tcnguyen.app.landmarkremark.ui.screens.NotesScreenContent
import tcnguyen.app.landmarkremark.util.ContentType
import tcnguyen.app.landmarkremark.util.DevicePosture
import tcnguyen.app.landmarkremark.util.NavigationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandmarkRemarkApp(
    windowSize: WindowWidthSizeClass,
    foldingDevicePosture: DevicePosture,
    landmarkRemarkHomeUIState: LandmarkRemarkHomeUIState
) {
    val navigationType: NavigationType
    val contentType: ContentType

    when (windowSize) {
        WindowWidthSizeClass.Compact -> {
            navigationType = NavigationType.BOTTOM_NAVIGATION
            contentType = ContentType.LIST_ONLY
        }
        WindowWidthSizeClass.Medium -> {
            navigationType = NavigationType.BOTTOM_NAVIGATION
            contentType = if (foldingDevicePosture != DevicePosture.NormalPosture) {
                ContentType.LIST_AND_DETAIL
            } else {
                ContentType.LIST_ONLY
            }
        }
        WindowWidthSizeClass.Expanded -> {
            navigationType = if (foldingDevicePosture is DevicePosture.BookPosture) {
                NavigationType.BOTTOM_NAVIGATION
            } else {
                NavigationType.PERMANENT_NAVIGATION_DRAWER
            }
            contentType = ContentType.LIST_AND_DETAIL
        }
        else -> {
            navigationType = NavigationType.BOTTOM_NAVIGATION
            contentType = ContentType.LIST_ONLY
        }
    }
    LandmarkRemarkWrapperUI(navigationType, contentType, landmarkRemarkHomeUIState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LandmarkRemarkWrapperUI(
    navigationType: NavigationType,
    contentType: ContentType,
    replyHomeUIState: LandmarkRemarkHomeUIState
) {

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val viewModel = viewModel<RootViewModel>()
    val flowTab by viewModel.uiState.collectAsState(initial = LandmarkRemarkHomeUIState())
    viewModel.loadLocationNotes()
    if (navigationType == NavigationType.PERMANENT_NAVIGATION_DRAWER) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet {
                    NavigationDrawerContent(flowTab.tab, viewModel)
                }
            }
        ) {
            LandmarkRemarkAppContent(flowTab.tab, viewModel, navigationType, contentType, replyHomeUIState)
        }
    } else {
        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet {
                    NavigationDrawerContent(
                        flowTab.tab,
                        viewModel,
                        onDrawerClicked = {
                            scope.launch {
                                drawerState.close()
                            }
                        },

                    )
                }
            },
            drawerState = drawerState
        ) {
            LandmarkRemarkAppContent(
                flowTab.tab,
                viewModel, navigationType, contentType, replyHomeUIState,
                onDrawerClicked = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandmarkRemarkAppContent(
    selectedDestination: String,
    viewModel: RootViewModel,
    navigationType: NavigationType,
    contentType: ContentType,
    landmarkRemarkHomeUIState: LandmarkRemarkHomeUIState,
    onDrawerClicked: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.inverseOnSurface)
        ) {
            if (selectedDestination == LandmarkRemarkDestinations.HOME) {
                MapScreenContent(
                    Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.inverseOnSurface),
                    viewModel,
                    snackbarHostState
                )
            } else {
                NotesScreenContent(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    viewModel
                )
            }
            AnimatedVisibility(visible = navigationType == NavigationType.BOTTOM_NAVIGATION) {
                AppBottomNavigationBar(selectedDestination, viewModel)
            }
        }
    }
}

@Composable
fun AppNavigationRail(
    selectedDestination: String,
    onDrawerClicked: () -> Unit = {},
) {
    NavigationRail(modifier = Modifier.fillMaxHeight()) {
        NavigationRailItem(
            selected = selectedDestination == LandmarkRemarkDestinations.HOME,
            onClick = onDrawerClicked,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(id = R.string.home)
                )
            }
        )
        NavigationRailItem(
            selected = selectedDestination == LandmarkRemarkDestinations.NOTES,
            onClick = { /*TODO*/ },
            icon = {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = stringResource(id = R.string.notes)
                )
            }
        )
    }
}

@Composable
fun AppBottomNavigationBar(selectedDestination: String, viewModel: RootViewModel) {
    NavigationBar(modifier = Modifier.fillMaxWidth()) {
        NavigationBarItem(
            selected = selectedDestination == LandmarkRemarkDestinations.HOME,
            onClick = { viewModel.switchTab(LandmarkRemarkDestinations.HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(id = R.string.home)
                )
            }
        )

        NavigationBarItem(
            selected = selectedDestination == LandmarkRemarkDestinations.NOTES,
            onClick = { viewModel.switchTab(LandmarkRemarkDestinations.NOTES) },
            icon = {
                Icon(
                    imageVector = Icons.Default.NoteAlt,
                    contentDescription = stringResource(id = R.string.notes)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    selectedDestination: String,
    viewModel: RootViewModel,
    modifier: Modifier = Modifier,
    onDrawerClicked: () -> Unit = {}
) {
    Column(
        modifier
            .wrapContentWidth()
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.inverseOnSurface)
            .padding(24.dp)
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(onClick = onDrawerClicked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = stringResource(id = R.string.add)
                )
            }
        }

        NavigationDrawerItem(
            selected = selectedDestination == LandmarkRemarkDestinations.HOME,
            label = {
                Text(
                    text = stringResource(id = R.string.home),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = stringResource(id = R.string.home)
                )
            },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
            onClick = { viewModel.switchTab(LandmarkRemarkDestinations.HOME) }
        )
        NavigationDrawerItem(
            selected = selectedDestination == LandmarkRemarkDestinations.NOTES,
            label = {
                Text(
                    text = stringResource(id = R.string.notes),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.NoteAlt,
                    contentDescription = stringResource(id = R.string.notes)
                )
            },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
            onClick = { viewModel.switchTab(LandmarkRemarkDestinations.NOTES) }
        )
    }
}
