package com.example.warehouse

import kotlinx.coroutines.runBlocking

fun main() {
    val cms = CentralMonitoringService()

    val brokerEnabled = System.getenv("BROKER_ENABLED")?.lowercase()
        ?: System.getProperty("BROKER_ENABLED")?.lowercase()
    val isBrokerEnabled = brokerEnabled == "true" || brokerEnabled == "1" || brokerEnabled == "yes"

    val sink: MeasurementSink
    val broker: ChannelBroker?

    if (isBrokerEnabled) {
        val b = ChannelBroker(cms)
        broker = b
        sink = b
    } else {
        broker = null
        sink = cms
    }

    val ws = WarehouseService(sink)

    Runtime.getRuntime().addShutdownHook(Thread {
        ws.stop()
        broker?.stop()
    })

    runBlocking {
        broker?.start()
        ws.start()
    }
}
