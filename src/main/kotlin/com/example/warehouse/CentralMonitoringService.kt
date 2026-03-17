package com.example.warehouse

class CentralMonitoringService(
    private val config: ThresholdConfig = ThresholdConfig(),
    private val alarmSink: AlarmSink = LogAlarmSink()
) : MeasurementSink {

    override suspend fun accept(measurement: Measurement) {
        val threshold = when (measurement.sensorType) {
            SensorType.TEMPERATURE -> config.temperatureThreshold
            SensorType.HUMIDITY -> config.humidityThreshold
        }
        if (measurement.value > threshold) {
            alarmSink.emit(measurement, threshold)
        }
    }
}
