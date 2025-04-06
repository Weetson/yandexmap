package com.example.yandexmapkit

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.yandexmapkit.ui.theme.YandexMapKitTheme
import com.google.android.gms.location.LocationServices
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class MainActivity : ComponentActivity() {
    private lateinit var mapView: MapView

    private val placemarkTapListener = MapObjectTapListener { _, point ->
        Toast.makeText(
            this@MainActivity,
            "Tapped the point (${point.longitude}, ${point.latitude})",
            Toast.LENGTH_SHORT
        ).show()
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey("32c2c323-e6a4-472d-bbeb-c40e9ca8f79b")
        MapKitFactory.initialize(this@MainActivity)
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapview)


        val imageProvider = ImageProvider.fromResource(this, R.drawable.point_on_map)

        requestLocation { point ->
            if (point != null) {
                val placemark = mapView.map.mapObjects.addPlacemark().apply {
                    geometry = point
                    setIcon(imageProvider)
                }
                placemark.addTapListener(placemarkTapListener)
            }
        }

    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun requestLocation(callback: (Point?) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            callback(null)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                callback(Point(location.latitude, location.longitude))
            } else {
                Toast.makeText(this, "Не удалось получить местоположение", Toast.LENGTH_SHORT).show()
                callback(null)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Ошибка при получении координат", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

}
