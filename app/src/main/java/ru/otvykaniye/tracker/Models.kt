package ru.otvykaniye.tracker

import org.json.JSONArray
import org.json.JSONObject

data class ConsumptionEntry(
    val id: String,
    val ts: Long,
    val trigger: String
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("ts", ts)
            put("trigger", trigger)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): ConsumptionEntry {
            return ConsumptionEntry(
                id = json.optString("id", ""),
                ts = json.optLong("ts", 0L),
                trigger = json.optString("trigger", "habit")
            )
        }
    }
}

data class ProfileState(
    val baseline: Int = 10,
    val price: Double = 200.0,
    val perPack: Int = 20,
    val reuse: Double = 1.0,     // For snus
    val packSize: Int = 20,      // For cigarettes
    val mode: String = "reduce", // reduce, limit, track
    val quitDate: String? = null,
    val dailyLimit: Int = 6,
    val startDate: String = "",
    val wishlistTitle: String = "",
    val wishlistCost: Double = 15000.0,
    val entries: List<ConsumptionEntry> = emptyList()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("baseline", baseline)
            put("price", price)
            put("perPack", perPack)
            put("reuse", reuse)
            put("packSize", packSize)
            put("mode", mode)
            put("quitDate", quitDate ?: JSONObject.NULL)
            put("dailyLimit", dailyLimit)
            put("startDate", startDate)
            put("wishlistTitle", wishlistTitle)
            put("wishlistCost", wishlistCost)
            val entriesArray = JSONArray()
            entries.forEach { entriesArray.put(it.toJson()) }
            put("entries", entriesArray)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): ProfileState {
            val entriesArray = json.optJSONArray("entries")
            val entries = mutableListOf<ConsumptionEntry>()
            if (entriesArray != null) {
                for (i in 0 until entriesArray.length()) {
                    val obj = entriesArray.optJSONObject(i)
                    if (obj != null) {
                        entries.add(ConsumptionEntry.fromJson(obj))
                    }
                }
            }

            return ProfileState(
                baseline = json.optInt("baseline", 10),
                price = json.optDouble("price", 200.0),
                perPack = json.optInt("perPack", 20),
                reuse = json.optDouble("reuse", 1.0),
                packSize = json.optInt("packSize", 20),
                mode = json.optString("mode", "reduce"),
                quitDate = if (json.isNull("quitDate")) null else json.optString("quitDate"),
                dailyLimit = json.optInt("dailyLimit", 6),
                startDate = json.optString("startDate", ""),
                wishlistTitle = json.optString("wishlistTitle", ""),
                wishlistCost = json.optDouble("wishlistCost", 15000.0),
                entries = entries
            )
        }
    }
}

data class TracklessState(
    val onboarded: Boolean = false,
    val setupStep: Int = 1,
    val activeKind: String = "snus",
    val language: String = "ru",
    val currency: String = "RUB",
    val profiles: Map<String, ProfileState> = emptyMap()
) {
    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("onboarded", onboarded)
            put("setupStep", setupStep)
            put("activeKind", activeKind)
            put("language", language)
            put("currency", currency)
            
            val profilesObj = JSONObject()
            profiles.forEach { (key, profile) ->
                profilesObj.put(key, profile.toJson())
            }
            put("profiles", profilesObj)
        }
    }

    companion object {
        fun defaultState(): TracklessState {
            return TracklessState(
                profiles = mapOf(
                    "snus" to ProfileState(reuse = 3.0),
                    "cigarette" to ProfileState()
                )
            )
        }

        fun fromJson(json: String): TracklessState {
            if (json.isBlank()) return defaultState()
            try {
                val obj = JSONObject(json)
                val profilesObj = obj.optJSONObject("profiles")
                val profiles = mutableMapOf<String, ProfileState>()
                
                if (profilesObj != null) {
                    val keys = profilesObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val pObj = profilesObj.optJSONObject(key)
                        if (pObj != null) {
                            profiles[key] = ProfileState.fromJson(pObj)
                        }
                    }
                }

                if (!profiles.containsKey("snus")) profiles["snus"] = ProfileState(reuse = 3.0)
                if (!profiles.containsKey("cigarette")) profiles["cigarette"] = ProfileState()

                return TracklessState(
                    onboarded = obj.optBoolean("onboarded", false),
                    setupStep = obj.optInt("setupStep", 1),
                    activeKind = obj.optString("activeKind", "snus"),
                    language = obj.optString("language", "ru"),
                    currency = obj.optString("currency", "RUB"),
                    profiles = profiles
                )
            } catch (e: Exception) {
                e.printStackTrace()
                return defaultState()
            }
        }
    }
}

