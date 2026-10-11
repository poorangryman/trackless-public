package ru.otvykaniye.tracker.data.local.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.otvykaniye.tracker.ConsumptionEntry

@Entity(
    tableName = "consumption_entries",
    indices = [
        Index(value = ["kind", "ts"]),
        Index(value = ["ts"])
    ]
)
data class ConsumptionEntity(
    @PrimaryKey
    val id: String,
    val kind: String, // "snus", "cigarette"
    val ts: Long,
    val trigger: String
) {
    fun toModel() = ConsumptionEntry(id = id, ts = ts, trigger = trigger)

    companion object {
        fun fromModel(model: ConsumptionEntry, kind: String) = ConsumptionEntity(
            id = model.id,
            kind = kind,
            ts = model.ts,
            trigger = model.trigger
        )
    }
}
