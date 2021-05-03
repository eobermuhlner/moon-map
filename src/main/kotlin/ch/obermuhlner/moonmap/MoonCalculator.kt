package ch.obermuhlner.moonmap

import java.time.*

object MoonCalculator {
    private val lunarZero = ZonedDateTime.of(LocalDateTime.of(2000, 1, 6, 18, 14, 0), ZoneId.of("UTC"))
    private val lunarCycle = 29.53058770576
    private val lunarCycleHalf = lunarCycle / 2

    fun phase(date: ZonedDateTime): Double {
        val delta = Duration.between(lunarZero, date).toSeconds().toDouble() / Duration.ofDays(1).toSeconds()
        val deltaDays = delta % lunarCycle
        return (deltaDays - lunarCycleHalf) / lunarCycleHalf
    }
}