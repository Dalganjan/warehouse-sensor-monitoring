package com.example.warehouse

fun interface MeasurementSink {
    suspend fun accept(measurement: Measurement)
}
