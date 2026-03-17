package com.example.warehouse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.net.BindException
import java.net.DatagramPacket
import java.net.DatagramSocket
import kotlin.system.exitProcess

class WarehouseService(
    private val sink: MeasurementSink,
    private val temperaturePort: Int = 3344,
    private val humidityPort: Int = 3355
) {
    private val logger = LoggerFactory.getLogger(WarehouseService::class.java)

    private var parentJob: Job? = null
    private var temperatureSocket: DatagramSocket? = null
    private var humiditySocket: DatagramSocket? = null

    suspend fun start() {
        val tempSocket = try {
            DatagramSocket(temperaturePort)
        } catch (e: BindException) {
            logger.error("Failed to bind UDP socket on port $temperaturePort: ${e.message}")
            exitProcess(1)
        }

        val humSocket = try {
            DatagramSocket(humidityPort)
        } catch (e: BindException) {
            tempSocket.close()
            logger.error("Failed to bind UDP socket on port $humidityPort: ${e.message}")
            exitProcess(1)
        }

        temperatureSocket = tempSocket
        humiditySocket = humSocket

        coroutineScope {
            parentJob = coroutineContext[Job]

            launch {
                listenLoop(tempSocket, SensorType.TEMPERATURE)
            }

            launch {
                listenLoop(humSocket, SensorType.HUMIDITY)
            }
        }
    }

    fun stop() {
        parentJob?.cancel()
        temperatureSocket?.close()
        humiditySocket?.close()
    }

    private suspend fun listenLoop(socket: DatagramSocket, sensorType: SensorType) {
        val buffer = ByteArray(4096)
        while (!socket.isClosed) {
            val packet = DatagramPacket(buffer, buffer.size)
            try {
                withContext(Dispatchers.IO) {
                    socket.receive(packet)
                }
            } catch (e: Exception) {
                break
            }

            val payload = String(packet.data, 0, packet.length, Charsets.UTF_8)

            when (val result = MessageParser.parse(payload)) {
                is ParseFailure -> {
                    logger.warn("Failed to parse datagram payload: '${result.rawPayload}'")
                }
                is ParseSuccess -> {
                    val measurement = result.measurement.copy(sensorType = sensorType)
                    try {
                        sink.accept(measurement)
                    } catch (e: Exception) {
                        logger.error(
                            "Error forwarding measurement from sensor '${measurement.sensorId}': ${e.message}"
                        )
                    }
                }
            }
        }
    }
}
