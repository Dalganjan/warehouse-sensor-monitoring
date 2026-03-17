package com.example.warehouse

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class MessageParserTest {

    // --- parse: valid payloads ---

    @Test
    fun `parse returns ParseSuccess for integer value`() {
        val result = MessageParser.parse("sensor_id=S1; value=42")
        result.shouldBeInstanceOf<ParseSuccess>()
        (result as ParseSuccess).measurement.sensorId shouldBe "S1"
        result.measurement.value shouldBe 42.0
    }

    @Test
    fun `parse returns ParseSuccess for decimal value`() {
        val result = MessageParser.parse("sensor_id=TEMP-01; value=36.2")
        result.shouldBeInstanceOf<ParseSuccess>()
        (result as ParseSuccess).measurement.sensorId shouldBe "TEMP-01"
        result.measurement.value shouldBe 36.2
    }

    @Test
    fun `parse returns ParseSuccess for negative value`() {
        val result = MessageParser.parse("sensor_id=sensor_x; value=-1.5")
        result.shouldBeInstanceOf<ParseSuccess>()
        (result as ParseSuccess).measurement.value shouldBe -1.5
    }

    @Test
    fun `parse returns ParseSuccess for sensor ID with hyphens`() {
        val result = MessageParser.parse("sensor_id=HUM-A3; value=48")
        result.shouldBeInstanceOf<ParseSuccess>()
        (result as ParseSuccess).measurement.sensorId shouldBe "HUM-A3"
    }

    @Test
    fun `parse trims surrounding whitespace from payload`() {
        val result = MessageParser.parse("  sensor_id=S2; value=10  ")
        result.shouldBeInstanceOf<ParseSuccess>()
        (result as ParseSuccess).measurement.sensorId shouldBe "S2"
    }

    @Test
    fun `parse preserves sensor ID case exactly`() {
        val lower = MessageParser.parse("sensor_id=abc; value=1")
        val upper = MessageParser.parse("sensor_id=ABC; value=1")
        (lower as ParseSuccess).measurement.sensorId shouldBe "abc"
        (upper as ParseSuccess).measurement.sensorId shouldBe "ABC"
    }

    // --- parse: invalid payloads ---

    @Test
    fun `parse returns ParseFailure for empty string`() {
        MessageParser.parse("").shouldBeInstanceOf<ParseFailure>()
    }

    @Test
    fun `parse returns ParseFailure when sensor_id key is missing`() {
        MessageParser.parse("id=S1; value=10").shouldBeInstanceOf<ParseFailure>()
    }

    @Test
    fun `parse returns ParseFailure when value key is missing`() {
        MessageParser.parse("sensor_id=S1; reading=10").shouldBeInstanceOf<ParseFailure>()
    }

    @Test
    fun `parse returns ParseFailure for non-numeric value field`() {
        MessageParser.parse("sensor_id=S1; value=abc").shouldBeInstanceOf<ParseFailure>()
    }

    @Test
    fun `parse returns ParseFailure for extra fields`() {
        MessageParser.parse("sensor_id=S1; value=10; extra=foo").shouldBeInstanceOf<ParseFailure>()
    }

    @Test
    fun `parse returns ParseFailure when semicolon is missing`() {
        MessageParser.parse("sensor_id=S1 value=10").shouldBeInstanceOf<ParseFailure>()
    }

    @Test
    fun `parse returns ParseFailure for raw payload stored in ParseFailure`() {
        val raw = "bad payload"
        val result = MessageParser.parse(raw) as ParseFailure
        result.rawPayload shouldBe raw
    }

    // --- serialize ---

    @Test
    fun `serialize produces correct wire format`() {
        val m = Measurement("TEMP-01", SensorType.TEMPERATURE, 36.2)
        MessageParser.serialize(m) shouldBe "sensor_id=TEMP-01; value=36.2"
    }

    @Test
    fun `serialize round-trips through parse`() {
        val original = Measurement("HUM-A3", SensorType.HUMIDITY, 48.0)
        val wire = MessageParser.serialize(original)
        val result = MessageParser.parse(wire) as ParseSuccess
        result.measurement.sensorId shouldBe original.sensorId
        result.measurement.value shouldBe original.value
    }
}
