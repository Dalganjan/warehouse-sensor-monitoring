package com.example.warehouse

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

/**
 * Unit/integration tests for WarehouseService wiring.
 * Uses ephemeral ports to avoid conflicts with well-known ports.
 */
class WarehouseServiceTest : FunSpec({

    // Helper: find a free UDP port
    fun freePort(): Int = DatagramSocket(0).use { it.localPort }

    // Helper: send a UDP datagram to localhost
    fun sendDatagram(port: Int, payload: String) {
        DatagramSocket().use { socket ->
            val bytes = payload.toByteArray(Charsets.UTF_8)
            val packet = DatagramPacket(bytes, bytes.size, InetAddress.getLoopbackAddress(), port)
            socket.send(packet)
        }
    }

    test("valid datagram on temperature port reaches sink with SensorType.TEMPERATURE") {
        val tempPort = freePort()
        val humPort = freePort()

        val received = mutableListOf<Measurement>()
        val sink = MeasurementSink { received.add(it) }
        val service = WarehouseService(sink, temperaturePort = tempPort, humidityPort = humPort)

        withTimeout(3000) {
            val job = launch { service.start() }
            delay(200) // allow sockets to bind

            sendDatagram(tempPort, "sensor_id=TEMP-01; value=36.5")
            delay(300) // allow processing

            service.stop()
            job.join()
        }

        received.size shouldBe 1
        received[0].sensorId shouldBe "TEMP-01"
        received[0].value shouldBe 36.5
        received[0].sensorType shouldBe SensorType.TEMPERATURE
    }

    test("valid datagram on humidity port reaches sink with SensorType.HUMIDITY") {
        val tempPort = freePort()
        val humPort = freePort()

        val received = mutableListOf<Measurement>()
        val sink = MeasurementSink { received.add(it) }
        val service = WarehouseService(sink, temperaturePort = tempPort, humidityPort = humPort)

        withTimeout(3000) {
            val job = launch { service.start() }
            delay(200)

            sendDatagram(humPort, "sensor_id=HUM-A3; value=48.0")
            delay(300)

            service.stop()
            job.join()
        }

        received.size shouldBe 1
        received[0].sensorId shouldBe "HUM-A3"
        received[0].value shouldBe 48.0
        received[0].sensorType shouldBe SensorType.HUMIDITY
    }

    test("malformed datagram does NOT reach sink") {
        val tempPort = freePort()
        val humPort = freePort()

        val received = mutableListOf<Measurement>()
        val sink = MeasurementSink { received.add(it) }
        val service = WarehouseService(sink, temperaturePort = tempPort, humidityPort = humPort)

        withTimeout(3000) {
            val job = launch { service.start() }
            delay(200)

            sendDatagram(tempPort, "this is not a valid payload")
            delay(300)

            service.stop()
            job.join()
        }

        received.size shouldBe 0
    }

    test("sink exception is caught and does not propagate") {
        val tempPort = freePort()
        val humPort = freePort()

        val sink = MeasurementSink { throw RuntimeException("sink failure") }
        val service = WarehouseService(sink, temperaturePort = tempPort, humidityPort = humPort)

        // Should complete without throwing
        withTimeout(3000) {
            val job = launch { service.start() }
            delay(200)

            sendDatagram(tempPort, "sensor_id=S1; value=10.0")
            delay(300)

            service.stop()
            job.join()
        }
        // If we reach here, the exception was caught and did not propagate
    }
})
