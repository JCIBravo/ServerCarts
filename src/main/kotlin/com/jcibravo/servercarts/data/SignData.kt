package com.jcibravo.servercarts.data

data class SignData(
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val isATrainCartsSign: Boolean,
    val linesFront: Array<String>,
    val linesBack: Array<String>,
)