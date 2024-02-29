package tcnguyen.app.landmarkremark.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import tcnguyen.app.landmarkremark.R
import tcnguyen.app.landmarkremark.RootViewModel
import tcnguyen.app.landmarkremark.data.model.LocationNote
import tcnguyen.app.landmarkremark.ui.components.AppSearchBar
import tcnguyen.app.landmarkremark.ui.components.AreaTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(
    modifier: Modifier,
    viewModel: RootViewModel,
    snackbarHostState: SnackbarHostState
) {
    val flowLocationNote by viewModel.currentLocationNote.collectAsState(initial = LocationNote())
    viewModel.addALocation(flowLocationNote)
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var showBottomSheet by remember { mutableStateOf(false) }
    val locationNotes by viewModel.locationNotesQuery.collectAsState()

    val isSearching by viewModel.isSearching.collectAsState()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomStart
    ) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(
                    flowLocationNote.latitude,
                    flowLocationNote.longitude
                ),
                10f
            )
        }
        AppSearchBar(
            modifier = Modifier
                .zIndex(2f)
                .graphicsLayer {
                    translationY = -1640f
                },
            viewModel
        )
        FloatingActionButton(
            modifier = Modifier
                .zIndex(2f)
                .graphicsLayer {
                    translationY = -200f
                    translationX = 1084f
                },
            onClick = {
                scope.launch {
                    snackbarHostState.showSnackbar("Your current location has been determined")
                }
                cameraPositionState.move(
                    update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(flowLocationNote.latitude, flowLocationNote.longitude),
                        50f
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(Icons.Filled.Navigation, "Floating action button.")
        }
        FloatingActionButton(
            modifier = Modifier
                .zIndex(2f)
                .graphicsLayer {
                    translationY = -340f
                    translationX = 1084f
                },
            onClick = {
                showBottomSheet = true
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Icon(Icons.Filled.EditNote, stringResource(id = R.string.current_location))
        }
        GoogleMap(
            cameraPositionState = cameraPositionState,
        ) {
            locationNotes.forEach() {
                if (it.latitude == flowLocationNote.latitude || it.longitude == flowLocationNote.longitude) {
                    var makerStateCurrent = rememberMarkerState(
                        position = LatLng(
                            flowLocationNote.latitude,
                            flowLocationNote.longitude
                        )
                    )
                    Marker(
                        state = makerStateCurrent,
                        snippet = stringResource(id = R.string.your_location),
                        title = it.note,
                    )
                    var markerState =
                        rememberMarkerState(position = LatLng(it.latitude, it.longitude))
                    Marker(
                        state = markerState,
                        snippet = it.owner,
                        title = it.note,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                    markerState.showInfoWindow()
                    makerStateCurrent.showInfoWindow()
                } else {
                    var markerState =
                        rememberMarkerState(position = LatLng(it.latitude, it.longitude))
                    Marker(
                        state = markerState,
                        snippet = it.owner,
                        title = it.note,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
                    )
                    var makerStateCurrent = rememberMarkerState(
                        position = LatLng(
                            flowLocationNote.latitude,
                            flowLocationNote.longitude
                        )
                    )
                    Marker(
                        state = makerStateCurrent,
                        snippet = stringResource(id = R.string.your_location),
                        title = stringResource(id = R.string.say_something),
                    )
                    makerStateCurrent.showInfoWindow()
                    markerState.showInfoWindow()
                }
            }
            if (locationNotes.size > 1) {
                cameraPositionState.move(
                    update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(flowLocationNote.latitude, flowLocationNote.longitude),
                        40f
                    )
                )
            } else if (locationNotes.isNotEmpty()) {
                cameraPositionState.move(
                    update = CameraUpdateFactory.newLatLngZoom(
                        LatLng(locationNotes.first().latitude, locationNotes.first().longitude),
                        40f
                    )
                )
            }
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState,

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AreaTextField(
                    maxLines = 1,
                    value = userName,
                    onValueChanged = { userName = it },
                    hintText = stringResource(id = R.string.your_name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(25))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                )

                AreaTextField(
                    maxLines = 4,
                    value = noteContent,
                    onValueChanged = { noteContent = it },
                    hintText = stringResource(id = R.string.say_something),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(25))
                        .background(MaterialTheme.colorScheme.background)
                        .padding(16.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),

                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        onClick = {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        onClick = {
                            viewModel.addLocationNote(
                                LocationNote(
                                    longitude = flowLocationNote.longitude,
                                    latitude = flowLocationNote.latitude,
                                    owner = userName,
                                    note = noteContent
                                )
                            )
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    showBottomSheet = false
                                }
                            }
                        }
                    ) {
                        Text(stringResource(id = R.string.save))
                    }
                }
            }
        }
    }
}
