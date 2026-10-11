package ru.otvykaniye.tracker.domain

import ru.otvykaniye.tracker.ConsumptionEntry
import ru.otvykaniye.tracker.ProfileState
import java.util.Calendar

data class HealthStageInfo(
    val stageIdx: Int,
    val currentTitleKey: String,
    val currentDescKey: String,
    val nextTitleKey: String?,
    val progress: Float
)

data class SpentSummary(
    val spentToday: Int,
    val totalSpent: Int,
    val hasPrice: Boolean
)

data class DayData(
    val ts: Long,
    val count: Int
)

data class HourlyBreakdown(
    val morning: Int,
    val day: Int,
    val evening: Int,
    val night: Int
)


object StatsCalculator {

    fun getDayBounds(): Pair<Long, Long> {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfToday = Calendar.getInstance().apply {
            timeInMillis = startOfToday
            add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        return Pair(startOfToday, endOfToday)
    }

    fun calculateTodayCount(entries: List<ConsumptionEntry>): Int {
        val (startOfToday, endOfToday) = getDayBounds()
        return entries.count { it.ts in startOfToday until endOfToday }
    }

    fun calculateHealthStage(timeSinceLastMs: Long): HealthStageInfo {
        val hours = timeSinceLastMs.toFloat() / (1000f * 60f * 60f)
        val stageIdx = when {
            hours < 1f -> 0
            hours < 12f -> 1
            hours < 24f -> 2
            hours < 72f -> 3
            else -> 4
        }

        val currentTitleKey = "stage_$stageIdx"
        val currentDescKey = "stage_${stageIdx}_desc"

        val nextTitleKey = if (stageIdx < 4) "stage_${stageIdx + 1}" else null
        val maxHours = when (stageIdx) {
            0 -> 1
            1 -> 12
            2 -> 24
            3 -> 72
            else -> 72
        }.toFloat()
        val prevHours = when (stageIdx) {
            0 -> 0
            1 -> 1
            2 -> 12
            3 -> 24
            else -> 24
        }.toFloat()

        val progress = if (stageIdx < 4) {
            ((hours - prevHours) / (maxHours - prevHours)).coerceIn(0f, 1f)
        } else {
            1f
        }

        return HealthStageInfo(
            stageIdx = stageIdx,
            currentTitleKey = currentTitleKey,
            currentDescKey = currentDescKey,
            nextTitleKey = nextTitleKey,
            progress = progress
        )
    }

    fun calculateSpent(
        todayCount: Int,
        totalCount: Int,
        profile: ProfileState,
        activeKind: String
    ): SpentSummary {
        val effectiveReuse = if (activeKind == "snus" && profile.reuse > 0) profile.reuse else 1.0
        val totalUnits = profile.perPack * effectiveReuse
        val pricePerPiece = if (totalUnits > 0) profile.price / totalUnits else 0.0

        return if (pricePerPiece > 0) {
            SpentSummary(
                spentToday = (todayCount * pricePerPiece).toInt(),
                totalSpent = (totalCount * pricePerPiece).toInt(),
                hasPrice = true
            )
        } else {
            SpentSummary(spentToday = 0, totalSpent = 0, hasPrice = false)
        }
    }

    fun calculate14DayChart(entries: List<ConsumptionEntry>): List<DayData> {
        val startOfToday = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        return (0..13).map { index ->
            val start = (startOfToday.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -(13 - index))
            }
            val end = (start.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }
            val startMs = start.timeInMillis
            val endMs = end.timeInMillis
            val count = entries.count { it.ts in startMs until endMs }
            DayData(startMs, count)
        }
    }

    fun calculateHourlyDistribution(entries: List<ConsumptionEntry>): HourlyBreakdown {
        var morning = 0
        var day = 0
        var eve = 0
        var night = 0

        val cal = Calendar.getInstance()
        entries.forEach { entry ->
            cal.timeInMillis = entry.ts
            when (cal.get(Calendar.HOUR_OF_DAY)) {
                in 6..11 -> morning++
                in 12..17 -> day++
                in 18..23 -> eve++
                else -> night++
            }
        }
        return HourlyBreakdown(morning = morning, day = day, evening = eve, night = night)
    }
}

