package com.example.geotask

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

/**
 * [AppCompatActivity] that displays map with [TextInputEditText] for entering latitude
 * and longitude. It also has [Switch] for enabling Geofencing.
 *
 * @author Alexander Gorin
 */
class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val inputMethodManager: InputMethodManager by lazy {
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private val sharedPref by lazy { getSharedPreferences(PREF_FILE_KEY, Context.MODE_PRIVATE) }

    private lateinit var latitudeInput: TextInputEditText
    private lateinit var longitudeInput: TextInputEditText
    private lateinit var enableLocationSwitch: Switch
    private lateinit var mapLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initView()
        getLocationPermission()
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        if (checkLocationPermission()) {
            map.isMyLocationEnabled = true
            map.uiSettings.isMyLocationButtonEnabled = true
            getDeviceLocation()
        }
        init()
    }


    private fun init() {
        val (savedLatitude, savedLongitude) = getSavedLocation()
        if (savedLatitude.isNotEmpty() && savedLongitude.isNotEmpty()) {
            latitudeInput.setText(savedLatitude)
            longitudeInput.setText(savedLongitude)
            enableLocationSwitch.isChecked = true
            displayLocation(savedLatitude.toDouble(), savedLongitude.toDouble())
        }

        enableLocationSwitch.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked) {
                val latitude = latitudeInput.text.toString()
                val longitude = longitudeInput.text.toString()
                if (latitude.isNotEmpty() && longitude.isNotEmpty()) {
                    if ((latitude.toDouble() in VALID_LATITUDE_RANGE)
                        && (longitude.toDouble() in VALID_LONGITUDE_RANGE)
                    ) {
                        displayLocation(latitude.toDouble(), longitude.toDouble())
                        startGeofencing(latitude.toDouble(), longitude.toDouble())
                        hideSoftKeyboard()
                        saveLocation(latitude, longitude)
                    } else {
                        Snackbar.make(
                            mapLayout,
                            getString(R.string.set_location_wrong),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        button.isChecked = false
                    }
                } else {
                    Snackbar.make(
                        mapLayout,
                        getString(R.string.set_location_warning),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    button.isChecked = false
                }
            } else {
                stopService(Intent(this, GeofencingService::class.java))
                deleteSavedLocation()
            }
        }
    }

    private fun startGeofencing(latitude: Double, longitude: Double) {
        val intent = Intent(this, GeofencingService::class.java).apply {
            putExtra(LATITUDE_EXTRA, latitude)
            putExtra(LONGITUDE_EXTRA, longitude)
        }
        startService(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initMap()
            }
        }
    }

    private fun displayLocation(latitude: Double, longitude: Double) {
        with(map) {
            moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(latitude, longitude),
                    DEFAULT_ZOOM
                )
            )
            addCircle(
                CircleOptions()
                    .center(LatLng(latitude, longitude))
                    .radius(CIRCLE_RADIUS)
                    .fillColor(getColor(R.color.colorCircleMap))
                    .strokeWidth(CIRCLE_STROKE_WIDTH)
            )
        }
    }

    private fun getDeviceLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.latitude, it.longitude),
                            DEFAULT_ZOOM
                        )
                    )
                }
            }
        }
    }

    private fun getLocationPermission() {
        if (checkLocationPermission()) {
            initMap()
        } else {
            requestLocationPermission()
        }
    }

    private fun hideSoftKeyboard() {
        currentFocus?.let {
            it.clearFocus()
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun requestLocationPermission() = ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
        FINE_LOCATION_REQUEST_CODE
    )

    private fun checkLocationPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    private fun initMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initView() {
        latitudeInput = findViewById(R.id.latitude_input)
        longitudeInput = findViewById(R.id.longitude_input)
        enableLocationSwitch = findViewById(R.id.enable_switch)
        mapLayout = findViewById(R.id.map_layout)
    }

    private fun saveLocation(latitude: String, longitude: String) {
        sharedPref.edit {
            putString(PREF_LATITUDE, latitude)
            putString(PREF_LONGITUDE, longitude)
        }
    }

    private fun deleteSavedLocation() {
        sharedPref.edit {
            clear()
        }
    }

    private fun getSavedLocation() = Pair(
        sharedPref.getString(PREF_LATITUDE, "") ?: "",
        sharedPref.getString(PREF_LONGITUDE, "") ?: ""
    )

    companion object {
        private const val FINE_LOCATION_REQUEST_CODE = 222
        private const val DEFAULT_ZOOM = 15f
        private const val CIRCLE_RADIUS = 100.0
        private const val CIRCLE_STROKE_WIDTH = 0F
        const val LATITUDE_EXTRA = "LATITUDE_EXTRA"
        const val LONGITUDE_EXTRA = "LONGITUDE_EXTRA"
        private const val PREF_FILE_KEY = "com.example.geotast.PREF_FILE_KEY"
        private const val PREF_LATITUDE = "com.example.geotast.PREF_LATITUDE"
        private const val PREF_LONGITUDE = "com.example.geotast.PREF_LONGITUDE"
        private val VALID_LATITUDE_RANGE = -90.0..90.0
        private val VALID_LONGITUDE_RANGE = -180.0..180.0
    }
}