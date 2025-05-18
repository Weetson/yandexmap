package com.example.yandexmapkit

import android.R
import android.location.Location
import com.google.firebase.Firebase
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.firestore
import kotlin.properties.Delegates

class DataBase {
    private val db = Firebase.firestore

    public fun signUpUser(login: String) : String {
        // Регистрация в БД
        val id = generateId()
        val userDoc = db.collection("locations").document(login)
        userDoc.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                userDoc.set(
                    mapOf(
                        "id" to id,
                        "name" to login,
                        "location" to GeoPoint(0.0, 0.0),
                        "is_ghost" to false,
                        "friends" to emptyList<String>()
                    )
                )
            }
        }
        return id
    }

    public fun updateUser(login: String, location: Location) {
        db.collection("locations")
            .document(login)
            .update(
                "location", GeoPoint(location.latitude, location.longitude)
            )
    }

    public fun getLocation(): GeoPoint {
        var geoPoint: GeoPoint = GeoPoint(0.0, 0.0);
        // Обновление карты
        db.collection("locations")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    return@addSnapshotListener
                }

                for (doc in snapshots) {
                    geoPoint = doc.getGeoPoint("location")!!
                }
            }
        return geoPoint
    }

    fun generateId(length: Int = 12): String {
        val chars = ('a'..'z') + ('A'..'Z') // только латинские буквы
        return (1..length)
            .map { chars.random() }
            .joinToString("")
    }

    public fun setUser(id: String) : UserUnit {
        lateinit var name: String;
        lateinit var location: GeoPoint;
        lateinit var friends: Array<String>;
        //var groups: Array<String>;
        var is_ghost by Delegates.notNull<Boolean>();

        db.collection("locations")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    return@addSnapshotListener
                }
                for (doc in snapshots) {
                    name = doc.getString("name")!!
                    location = doc.getGeoPoint("location")!!
                    friends = (doc.get("friends") as? List<String>)?.toTypedArray() ?: emptyArray()
                    is_ghost = doc.getBoolean("is_ghost")!!
                }
            }

        return UserUnit(id, name, location, friends, is_ghost)

    }

}