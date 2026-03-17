package com.example.warehouse

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CentralMonitoringServiceTest : FunSpec({

    // Test double: capturing AlarmSink
    fun capturingSink(): Pair<AlarmSink, MutableList<Pair<Measurement, Double>>> {
        val captured = mutableListOf<Pair<Measurement, Double>>()
        val sink = AlarmSink { measurement, threshold -> captured.add(measurement to threshold) }
        return sink to captured
    }

    test("value equal to temperature threshold does NOT alarm") {
        val (sink, captured) = capturingSink()
        val service = CentralMonitoringService(alarmSink = sink)
        val measurement = Measurement("S1", SensorType.TEMPERATURE, 35.0)
        service.accept(measurement)
        captured.size shouldBe 0
    }

    test("value just above temperature threshold alarms exactly once") {
        val (sink, captured) = capturingSink()
        val service = CentralMonitoringService(alarmSink = sink)
        val measurement = Measurement("S1", SensorType.TEMPERATURE, 35.0 + 1.0)
        service.accept(measurement)
        captured.size shouldBe 1
    }

    test("value equal to humidity threshold does NOT alarm") {
        val (sink, captured) = capturingSink()
        val service = CentralMonitoringService(alarmSink = sink)
        val measurement = Measurement("H1", SensorType.HUMIDITY, 50.0)
        service.accept(measurement)
        captured.size shouldBe 0
    }

    test("value just above humidity threshold alarms exactly once") {
        val (sink, captured) = capturingSink()
        val service = CentralMonitoringService(alarmSink = sink)
        val measurement = Measurement("H1", SensorType.HUMIDITY, 50.0 + 1.0)
        service.accept(measurement)
        captured.size shouldBe 1
    }

    test("default ThresholdConfig has temperature threshold of 35.0") {
        ThresholdConfig().temperatureThreshold shouldBe 35.0
    }

    test("default ThresholdConfig has humidity threshold of 50.0") {
        ThresholdConfig().humidityThreshold shouldBe 50.0
    }

    test("alarm is emitted with correct threshold value for temperature") {
        val (sink, captured) = capturingSink()
        val config = ThresholdConfig(temperatureThreshold = 40.0)
        val service = CentralMonitoringService(config = config, alarmSink = sink)
        val measurement = Measurement("S1", SensorType.TEMPERATURE, 41.0)
        service.accept(measurement)
        captured.size shouldBe 1
        captured[0].second shouldBe 40.0
    }

    test("alarm is emitted with correct threshold value for humidity") {
        val (sink, captured) = capturingSink()
        val config = ThresholdConfig(humidityThreshold = 60.0)
        val service = CentralMonitoringService(config = config, alarmSink = sink)
        val measurement = Measurement("H1", SensorType.HUMIDITY, 61.0)
        service.accept(measurement)
        captured.size shouldBe 1
        captured[0].second shouldBe 60.0
    }

    test("value below threshold does NOT alarm") {
        val (sink, captured) = capturingSink()
        val service = CentralMonitoringService(alarmSink = sink)
        val measurement = Measurement("S1", SensorType.TEMPERATURE, 20.0)
        service.accept(measurement)
        captured.size shouldBe 0
    }
})
