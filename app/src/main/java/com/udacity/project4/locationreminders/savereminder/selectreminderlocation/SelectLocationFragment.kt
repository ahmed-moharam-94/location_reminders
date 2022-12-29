package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val TAG = "SelectLocationFragment"
private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap

    private val REQUEST_LOCATION_PERMISSION = 1

    private lateinit var selectedMarker: Marker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        checkDeviceLocationSettings()
        val mapFragment = childFragmentManager.findFragmentById(R.id.selectLocationFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.saveButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }


    // onMapReady() will only be called when the map is ready and is not null
    // and the user have installed google play services
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        // define lat and long variable
        val lat = 30.763238
        val long = 30.702940
        // create a lat, long object
        val homeLatLong = LatLng(lat, long)
        // move the camera to homeLatLong
        val zoomLevel = 15f // zoom level from 1 to 20
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLong, zoomLevel))
        // add marker to homeLatLong
        val homeMarker = MarkerOptions().position(homeLatLong)
        map.addMarker(homeMarker)
        // call setMapLongClicked()
        setMapLongClicked(map)
        // call setPoiClicked()
        setPoiClick(map)
        // call setMapStyle()
        setMapStyle(map)
        // enable location tracking
        enableMyLocation()


    }

    // onMapLongClicked() called when the user long press on the map
    // it will only be invoked when none of the overlays of the map handled the gesture
    // it will always be invoked in the Android UI thread
    private fun setMapLongClicked(map: GoogleMap) {

        map.setOnMapLongClickListener {
            // Cleat the map when long click before add a new one
            map.clear()
            // define a snippet to display when click the marker
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                it.latitude,
                it.longitude
            )
            // add marker when long press
           selectedMarker = map.addMarker(
                MarkerOptions()
                    .position(it)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    // change color of the marker to blue
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            selectedMarker.showInfoWindow()
        }
    }

    // set a POI(point of interest)
    private fun setPoiClick(map: GoogleMap) {

        map.setOnPoiClickListener {
            // clear previous marker
            map.clear()
            // add marker in POI
             selectedMarker = map.addMarker(
                MarkerOptions()
                    .position(it.latLng)
                    .title(it.name)
            )
            // show poi info window
            selectedMarker.showInfoWindow()
        }
    }

    // create a function to set map style
    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            // if the style is unsuccessful print a log
            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        }
        // catch not found resource exception
        catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

    // enable tracking location
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        // check if permission is granted
        if (isPermissionGranted()) {
            // if permission granted enable location tracking
            map.isMyLocationEnabled = true
        } else {
            // request permission
            this.requestPermissions(
                // array of permissions
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }



    private fun onLocationSelected() {
        if (this::selectedMarker.isInitialized) {
            _viewModel.latitude.value = selectedMarker.position.latitude
            _viewModel.longitude.value = selectedMarker.position.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedMarker.title
            findNavController().popBackStack()

        } else {
            val toast = Toast.makeText(context, resources.getString(R.string.select_location), Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    private fun checkDeviceLocationSettings(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        activity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    this.requireView(),
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted(): Boolean {
        return context?.let {
            ContextCompat.checkSelfPermission(
                it,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        } == PackageManager.PERMISSION_GRANTED
    }

    // Check if the user grant permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If permissions granted enable my location
        if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == REQUEST_LOCATION_PERMISSION)) {
            enableMyLocation()
        }
        else {
            // If not grant permission show a snackBar with action to grant permissions
            Snackbar.make(
                binding.selectLocationFragment,
                R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
            ).setAction(android.R.string.ok) {
                requestPermissions(
                    arrayOf<String>(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
                )
            }.show()
        }
    }
}
