package com.jcibravo.servercarts.data

data class CartData(
    val parentTrain: String,
    val uuid: String,
    val entityUUID: String,
    val destination: String?,
    val passengers: Int,
    val passengerNames: Array<String>,
    val speed: Speed,
    val remainingCarSeats: Int,
    val totalCarSeats: Int,
)