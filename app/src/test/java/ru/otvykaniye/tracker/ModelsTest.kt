package ru.otvykaniye.tracker

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsTest {

    @Test
    fun testConsumptionEntrySerialization() {
        val entry = ConsumptionEntry("123", 1000L, "habit")
        val json = entry.toJson()
        val parsed = ConsumptionEntry.fromJson(json)
        
        assertEquals(entry.id, parsed.id)
        assertEquals(entry.ts, parsed.ts)
        assertEquals(entry.trigger, parsed.trigger)
    }

    @Test
    fun testTracklessStateDefaultSerialization() {
        val defaultState = TracklessState.defaultState()
        val jsonString = defaultState.toJson().toString()
        val parsedState = TracklessState.fromJson(jsonString)
        
        assertEquals(defaultState.onboarded, parsedState.onboarded)
        assertEquals(defaultState.language, parsedState.language)
        assertEquals(defaultState.activeKind, parsedState.activeKind)
    }
}
