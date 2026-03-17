package com.example.warehouse

data class Measurement(
    val sensorId: String,
    val sensorType: SensorType,
    val value: Double
)
