package com.example.yandexmapkit

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class MainActivity : ComponentActivity() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var login: String
    private val db = Firebase.firestore
    private val imageProvider by lazy { ImageProvider.fromResource(this, R.drawable.point_on_map) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey("32c2c323-e6a4-472d-bbeb-c40e9ca8f79b")
        MapKitFactory.initialize(this)

        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapview)

        // Проверка логина
        val sharedPref = getSharedPreferences("MyApp", MODE_PRIVATE)
        login = sharedPref.getString("login", null) ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupUserInDb()
        listenToLocations()
        startLocationUpdates()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    private fun setupUserInDb() {
        val userDoc = db.collection("locations").document(login)
        userDoc.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                userDoc.set(
                    mapOf(
                        "name" to login,
                        "location" to GeoPoint(0.0, 0.0)
                    )
                )
            }
        }
    }

    private fun listenToLocations() {
        db.collection("locations")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    Toast.makeText(this, "Ошибка загрузки точек", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                mapView.map.mapObjects.clear()
                for (doc in snapshots) {
                    val geoPoint = doc.getGeoPoint("location")
                    if (geoPoint != null) {
                        val placemark = mapView.map.mapObjects.addPlacemark().apply {
                            geometry = Point(geoPoint.latitude, geoPoint.longitude)
                            setIcon(imageProvider)
                        }
                        placemark.addTapListener(placemarkTapListener)
                    }
                }
            }
    }

    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    db.collection("locations")
                        .document(login)
                        .update(
                            "location", GeoPoint(location.latitude, location.longitude)
                        )
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private val placemarkTapListener = MapObjectTapListener { _, point ->
        Toast.makeText(this, "Точка (${point.longitude}, ${point.latitude})", Toast.LENGTH_SHORT).show()
        true
    }
}
