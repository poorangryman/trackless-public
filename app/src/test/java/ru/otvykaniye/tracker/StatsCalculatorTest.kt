package ru.otvykaniye.tracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.otvykaniye.tracker.domain.StatsCalculator
import java.util.Calendar

class StatsCalculatorTest {

    @Test
    fun testCalculateTodayCount() {
        val now = System.currentTimeMillis()
        val yesterday = now - 24 * 60 * 60 * 1000L
        
        val entries = listOf(
            ConsumptionEntry("1", now, "habit"),
            ConsumptionEntry("2", yesterday, "habit"),
            ConsumptionEntry("3", now - 60000L, "craving")
        )

        val todayCount = StatsCalculator.calculateTodayCount(entries)
        assertEquals(2, todayCount)
    }

    @Test
    fun testCalculateHealthStage() {
        val halfHour = 30 * 60 * 1000L
        val stage0 = StatsCalculator.calculateHealthStage(halfHour)
        assertEquals(0, stage0.stageIdx)
        assertEquals("stage_0", stage0.currentTitleKey)
        assertEquals("stage_1", stage0.nextTitleKey)
        assertEquals(0.5f, stage0.progress, 0.01f)

        val fiveDays = 5 * 24 * 60 * 60 * 1000L
        val stage4 = StatsCalculator.calculateHealthStage(fiveDays)
        assertEquals(4, stage4.stageIdx)
        assertEquals(null, stage4.nextTitleKey)
        assertEquals(1f, stage4.progress, 0.01f)
    }

    @Test
    fun testCalculateSpentWithReuse() {
        val profile = ProfileState(
            price = 400.0,
            perPack = 20,
            reuse = 2.0 // effective total units = 40, price per piece = 10.0
        )

        val spent = StatsCalculator.calculateSpent(
            todayCount = 5,
            totalCount = 10,
            profile = profile,
            activeKind = "snus"
        )

        assertTrue(spent.hasPrice)
        assertEquals(50, spent.spentToday)
        assertEquals(100, spent.totalSpent)
    }

    @Test
    fun testCalculateHourlyDistribution() {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 8)
        val morningTs = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 14)
        val dayTs = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 20)
        val eveTs = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 2)
        val nightTs = cal.timeInMillis

        val entries = listOf(
            ConsumptionEntry("1", morningTs, "habit"),
            ConsumptionEntry("2", dayTs, "habit"),
            ConsumptionEntry("3", eveTs, "stress"),
            ConsumptionEntry("4", nightTs, "habit")
        )

        val breakdown = StatsCalculator.calculateHourlyDistribution(entries)
        assertEquals(1, breakdown.morning)
        assertEquals(1, breakdown.day)
        assertEquals(1, breakdown.evening)
        assertEquals(1, breakdown.night)
    }

    @Test
    fun testCalculate14DayChart() {
        val entries = listOf(
            ConsumptionEntry("1", System.currentTimeMillis(), "habit")
        )

        val chart = StatsCalculator.calculate14DayChart(entries)
        assertEquals(14, chart.size)
        assertEquals(1, chart.last().count)
    }
}
