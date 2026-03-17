package com.example.warehouse

import org.slf4j.LoggerFactory

fun interface AlarmSink {
    fun emit(measurement: Measurement, threshold: Double)
}

class LogAlarmSink : AlarmSink {
    private val logger = LoggerFactory.getLogger(LogAlarmSink::class.java)

    override fun emit(measurement: Measurement, threshold: Double) {
        logger.warn(
            "ALARM sensor_type={} sensor_id={} value={} threshold={}",
            measurement.sensorType,
            measurement.sensorId,
            measurement.value,
            threshold
        )
    }
}
