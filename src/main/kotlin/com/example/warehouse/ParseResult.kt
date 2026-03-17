package com.example.warehouse

sealed class ParseResult

data class ParseSuccess(val measurement: Measurement) : ParseResult()

data class ParseFailure(val reason: String, val rawPayload: String) : ParseResult()
