package com.example.yandexmapkit

import com.google.firebase.firestore.GeoPoint
import com.yandex.mapkit.geometry.Point

class UserUnit(val id: String, val name: String, val location: GeoPoint, val friends: Array<String>, val is_ghost: Boolean) {

}

