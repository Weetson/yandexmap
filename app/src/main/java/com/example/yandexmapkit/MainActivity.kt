package com.example.yandexmapkit

import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlin.text.clear

class MainActivity : AppCompatActivity() {
    private lateinit var mapView: MapView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var login: String
    private lateinit var id: String
    private lateinit var user: UserUnit

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar

    private lateinit var navMenu: Menu
    private lateinit var ghostMenuItem: MenuItem
    private lateinit var ghostSwitch: SwitchCompat

    private val dbrouter = DataBase()
    private val imageProvider by lazy { ImageProvider.fromResource(this, R.drawable.point_on_map) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapKitFactory.setApiKey("32c2c323-e6a4-472d-bbeb-c40e9ca8f79b")
        MapKitFactory.initialize(this)

        setContentView(R.layout.activity_main2)
        mapView = findViewById(R.id.mapview)

        // Проверка логина
        val sharedPref = getSharedPreferences("MyApp", MODE_PRIVATE)
        login = sharedPref.getString("login", null) ?: run {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        id = dbrouter.signUpUser(login)
        user = dbrouter.setUser(id)


        listenToLocations()
        startLocationUpdates()

        // центрирование на карте при окрытии приложения
        getLocationOnce { point ->
            if (point != null) {
                mapView.map.move(CameraPosition(point, 15f, 0f, 0f))
            }
        }



        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.nav_view)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.group_profile -> toggleGroup("ProfileActivity")
                R.id.group_comrades -> toggleGroup("comrades")
                R.id.group_settings -> toggleGroup("settings")
                R.id.group_find -> toggleGroup("find")
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
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
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
        //if (toggle.onOptionsItemSelected(item)) {return true}
    }

    private fun setupUserInDb() {
        dbrouter.signUpUser(login)
    }

    private fun listenToLocations() {
        mapView.map.mapObjects.clear()
        val geoPoint = dbrouter.getLocation()
        val placemark = mapView.map.mapObjects.addPlacemark().apply {
            geometry = Point(geoPoint.latitude, geoPoint.longitude)
            setIcon(imageProvider)
        }
        placemark.addTapListener(placemarkTapListener)

    }

    private fun startLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    dbrouter.updateUser(login, location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private val placemarkTapListener = MapObjectTapListener { _, point ->
        Toast.makeText(this, "Точка (${point.longitude}, ${point.latitude})", Toast.LENGTH_SHORT).show()
        true
    }

    private fun toggleGroup(group: String) {
        Toast.makeText(this, "went to $group", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, group::class.java)
        if (group == "ProfileActivity") { intent.putExtra("USER_ID", user.id.toString())}
        startActivity(intent)
        finish()
    }

    private fun toggleGhostMode() {
        Toast.makeText(this, "you are in ghost mode", Toast.LENGTH_SHORT).show()
    }


    private fun getLocationOnce(callback: (Point?) -> Unit) {
        // Единоразовое получение локации
        if (!checkLocationPermission()) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            callback(null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                callback(location?.let { Point(it.latitude, it.longitude) })
            }
            .addOnFailureListener {
                Toast.makeText(this, "Ошибка GPS", Toast.LENGTH_SHORT).show()
                callback(null)
            }

    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }


}
