package com.example.warehouse

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class ChannelBroker(
    private val downstream: MeasurementSink,
    capacity: Int = Channel.UNLIMITED
) : MeasurementSink {

    private val channel = Channel<Measurement>(capacity)
    private var consumerJob: Job? = null

    override suspend fun accept(measurement: Measurement) {
        channel.send(measurement)
    }

    suspend fun start() {
        consumerJob = CoroutineScope(Dispatchers.Default).launch {
            for (measurement in channel) {
                downstream.accept(measurement)
            }
        }
    }

    fun stop() {
        consumerJob?.cancel()
        channel.close()
    }
}
