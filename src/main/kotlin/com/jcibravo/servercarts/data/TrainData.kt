package com.jcibravo.servercarts.data

data class TrainData(
    val id: String,
    val name: String?,
    val destination: String?,
    val route: MutableList<String>,
    val nextDestinationRoute: String?,
    val tags: Array<String>,
    val comment: String?,
    val nextStop: String?,
    val estimatedTimeOfArrival: Int?,
    val isMoving: Boolean,
    val passengers: Int,
    val carriages: Int,
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val trainWagons: List<CartData>,
    val speed: Speed,
    val speedLimit: Speed,
)