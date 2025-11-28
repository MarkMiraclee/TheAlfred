package com.example.thealfred.data

enum class ThreatLevel(val xpValue: Int) {
    MINOR_THREAT(xpValue = 25),
    MEDIUM_THREAT(xpValue = 75),
    CRITICAL_THREAT(xpValue = 200)
}