package com.example.warehouse

object MessageParser {

    private val PAYLOAD_REGEX = Regex("""^sensor_id=([^;]+);\s*value=([+-]?\d+(\.\d+)?)$""")

    fun parse(payload: String): ParseResult {
        val trimmed = payload.trim()
        val match = PAYLOAD_REGEX.matchEntire(trimmed)
            ?: return ParseFailure("Payload does not match expected format", trimmed)

        val sensorId = match.groupValues[1]
        val valueStr = match.groupValues[2]

        val value = valueStr.toDoubleOrNull()
            ?: return ParseFailure("Invalid numeric value: $valueStr", trimmed)

        val measurement = Measurement(
            sensorId = sensorId,
            sensorType = SensorType.TEMPERATURE, // placeholder; caller assigns actual type
            value = value
        )
        return ParseSuccess(measurement)
    }

    fun serialize(measurement: Measurement): String =
        "sensor_id=${measurement.sensorId}; value=${measurement.value}"
}
