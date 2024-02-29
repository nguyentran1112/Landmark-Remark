package tcnguyen.app.landmarkremark

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import tcnguyen.app.landmarkremark.data.model.LocationNote
import tcnguyen.app.landmarkremark.ui.theme.LandmarkRemarkTheme
import tcnguyen.app.landmarkremark.util.DevicePosture
import tcnguyen.app.landmarkremark.util.isBookPosture
import tcnguyen.app.landmarkremark.util.isSeparating

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val viewModel: RootViewModel by viewModels()
        val devicePostureFlow = WindowInfoTracker.getOrCreate(this).windowLayoutInfo(this)
            .flowWithLifecycle(this.lifecycle)
            .map { layoutInfo ->
                val foldingFeature =
                    layoutInfo.displayFeatures
                        .filterIsInstance<FoldingFeature>()
                        .firstOrNull()
                when {
                    isBookPosture(foldingFeature) ->
                        DevicePosture.BookPosture(foldingFeature.bounds)

                    isSeparating(foldingFeature) ->
                        DevicePosture.Separating(foldingFeature.bounds, foldingFeature.orientation)

                    else -> DevicePosture.NormalPosture
                }
            }
            .stateIn(
                scope = lifecycleScope,
                started = SharingStarted.Eagerly,
                initialValue = DevicePosture.NormalPosture
            )
        setContent {
            var locationText by remember { mutableStateOf("No location obtained :(") }
            var showPermissionResultText by remember { mutableStateOf(false) }
            var permissionResultText by remember { mutableStateOf("Permission Granted...") }
            RequestLocationPermission(
                onPermissionGranted = {
                    // Callback when permission is granted
                    showPermissionResultText = true
                    // Attempt to get the last known user location
                    getLastUserLocation(
                        onGetLastLocationSuccess = {
                            viewModel.setCurrentLocation(
                                LocationNote(
                                    latitude = it.first,
                                    longitude = it.second,
                                    note = "Say something",
                                    owner = "Your location"
                                )
                            )
                        },
                        onGetLastLocationFailed = { exception ->
                            showPermissionResultText = true
                            locationText =
                                exception.localizedMessage ?: "Error Getting Last Location"
                        },
                        onGetLastLocationIsNull = {
                            // Attempt to get the current user location
                            getCurrentLocation(
                                onGetCurrentLocationSuccess = {
                                    viewModel.setCurrentLocation(
                                        LocationNote(
                                            latitude = it.first,
                                            longitude = it.second
                                        )
                                    )
                                    locationText =
                                        "Location using CURRENT-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}"
                                },
                                onGetCurrentLocationFailed = {
                                    showPermissionResultText = true
                                    locationText =
                                        it.localizedMessage
                                            ?: "Error Getting Current Location"
                                }
                            )
                        }
                    )
                },
                onPermissionDenied = {
                    // Callback when permission is denied
                    showPermissionResultText = true
                    permissionResultText = "Permission Denied :("
                },
                onPermissionsRevoked = {
                    // Callback when permission is revoked
                    showPermissionResultText = true
                    permissionResultText = "Permission Revoked :("
                }
            )
            LandmarkRemarkTheme {
                val uiState = viewModel.uiState.collectAsState().value
                val windowSize = calculateWindowSizeClass(this)
                val devicePosture = devicePostureFlow.collectAsState().value
                LandmarkRemarkApp(windowSize.widthSizeClass, devicePosture, uiState)
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun RequestLocationPermission(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: () -> Unit,
        onPermissionsRevoked: () -> Unit
    ) {
        // Initialize the state for managing multiple location permissions.
        val permissionState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        )

        // Use LaunchedEffect to handle permissions logic when the composition is launched.
        LaunchedEffect(key1 = permissionState) {
            // Check if all previously granted permissions are revoked.
            val allPermissionsRevoked =
                permissionState.permissions.size == permissionState.revokedPermissions.size

            // Filter permissions that need to be requested.
            val permissionsToRequest = permissionState.permissions.filter {
                !it.status.isGranted
            }

            // If there are permissions to request, launch the permission request.
            if (permissionsToRequest.isNotEmpty()) permissionState.launchMultiplePermissionRequest()

            // Execute callbacks based on permission status.
            if (allPermissionsRevoked) {
                onPermissionsRevoked()
            } else {
                if (permissionState.allPermissionsGranted) {
                    onPermissionGranted()
                } else {
                    onPermissionDenied()
                }
            }
        }
    }

    /**
     * Retrieves the last known user location asynchronously.
     *
     * @param onGetLastLocationSuccess Callback function invoked when the location is successfully retrieved.
     *        It provides a Pair representing latitude and longitude.
     * @param onGetLastLocationFailed Callback function invoked when an error occurs while retrieving the location.
     *        It provides the Exception that occurred.
     */
    @SuppressLint("MissingPermission")
    private fun getLastUserLocation(
        onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetLastLocationFailed: (Exception) -> Unit,
        onGetLastLocationIsNull: () -> Unit
    ) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the last known location
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        // If location is not null, invoke the success callback with latitude and longitude
                        onGetLastLocationSuccess(Pair(it.latitude, it.longitude))
                    }?.run {
                        onGetLastLocationIsNull()
                    }
                }
                .addOnFailureListener { exception ->
                    // If an error occurs, invoke the failure callback with the exception
                    onGetLastLocationFailed(exception)
                }
        }
    }

    /**
     * Retrieves the current user location asynchronously.
     *
     * @param onGetCurrentLocationSuccess Callback function invoked when the current location is successfully retrieved.
     *        It provides a Pair representing latitude and longitude.
     * @param onGetCurrentLocationFailed Callback function invoked when an error occurs while retrieving the current location.
     *        It provides the Exception that occurred.
     * @param priority Indicates the desired accuracy of the location retrieval. Default is high accuracy.
     *        If set to false, it uses balanced power accuracy.
     */
    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(
        onGetCurrentLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetCurrentLocationFailed: (Exception) -> Unit,
        priority: Boolean = true
    ) {
        // Determine the accuracy priority based on the 'priority' parameter
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        // Check if location permissions are granted
        if (areLocationPermissionsGranted()) {
            // Retrieve the current location asynchronously
            fusedLocationProviderClient.getCurrentLocation(
                accuracy, CancellationTokenSource().token,
            ).addOnSuccessListener { location ->
                location?.let {
                    // If location is not null, invoke the success callback with latitude and longitude
                    onGetCurrentLocationSuccess(Pair(it.latitude, it.longitude))
                }?.run {
                    // Location null do something
                }
            }.addOnFailureListener { exception ->
                // If an error occurs, invoke the failure callback with the exception
                onGetCurrentLocationFailed(exception)
            }
        }
    }

    /**
     * Checks if location permissions are granted.
     *
     * @return true if both ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions are granted; false otherwise.
     */
    private fun areLocationPermissionsGranted(): Boolean {
        return (
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            )
    }
}

@Preview(showBackground = true)
@Composable
fun ReplyAppPreview() {
    LandmarkRemarkTheme {
        LandmarkRemarkApp(
            windowSize = WindowWidthSizeClass.Compact,
            foldingDevicePosture = DevicePosture.NormalPosture,
            landmarkRemarkHomeUIState = LandmarkRemarkHomeUIState()
        )
    }
}

@Preview(showBackground = true, widthDp = 700)
@Composable
fun ReplyAppPreviewTablet() {
    LandmarkRemarkTheme {
        LandmarkRemarkApp(
            windowSize = WindowWidthSizeClass.Medium,
            foldingDevicePosture = DevicePosture.NormalPosture,
            landmarkRemarkHomeUIState = LandmarkRemarkHomeUIState()
        )
    }
}

@Preview(showBackground = true, widthDp = 1000)
@Composable
fun ReplyAppPreviewDesktop() {
    LandmarkRemarkTheme {
        LandmarkRemarkApp(
            windowSize = WindowWidthSizeClass.Expanded,
            foldingDevicePosture = DevicePosture.NormalPosture,
            landmarkRemarkHomeUIState = LandmarkRemarkHomeUIState()
        )
    }
}
