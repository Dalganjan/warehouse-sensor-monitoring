package com.example.warehouse

data class ThresholdConfig(
    val temperatureThreshold: Double = 35.0,
    val humidityThreshold: Double = 50.0
)
