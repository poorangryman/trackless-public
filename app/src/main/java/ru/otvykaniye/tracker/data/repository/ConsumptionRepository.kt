package ru.otvykaniye.tracker.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.otvykaniye.tracker.ConsumptionEntry
import ru.otvykaniye.tracker.data.local.db.ConsumptionDao
import ru.otvykaniye.tracker.data.local.db.ConsumptionEntity

interface ConsumptionRepository {
    fun observeEntries(kind: String): Flow<List<ConsumptionEntry>>
    fun observeAllEntries(): Flow<Map<String, List<ConsumptionEntry>>>
    suspend fun getEntries(kind: String): List<ConsumptionEntry>
    suspend fun getAllEntries(): Map<String, List<ConsumptionEntry>>
    suspend fun getLastEntry(kind: String): ConsumptionEntry?
    suspend fun recordUse(kind: String, trigger: String, customTimestamp: Long? = null): ConsumptionEntry
    suspend fun undoLastUse(kind: String): Boolean
    suspend fun deleteEntry(id: String)
    suspend fun clearAll()
    suspend fun importEntries(entriesByKind: Map<String, List<ConsumptionEntry>>)
}

class ConsumptionRepositoryImpl(
    private val dao: ConsumptionDao
) : ConsumptionRepository {

    override fun observeEntries(kind: String): Flow<List<ConsumptionEntry>> {
        return dao.observeEntries(kind).map { list -> list.map { it.toModel() } }
    }

    override fun observeAllEntries(): Flow<Map<String, List<ConsumptionEntry>>> {
        return dao.observeAllEntries().map { list ->
            list.groupBy(
                keySelector = { it.kind },
                valueTransform = { it.toModel() }
            )
        }
    }

    override suspend fun getEntries(kind: String): List<ConsumptionEntry> {
        return dao.getEntriesForKind(kind).map { it.toModel() }
    }

    override suspend fun getAllEntries(): Map<String, List<ConsumptionEntry>> {
        return dao.getAllEntries().groupBy(
            keySelector = { it.kind },
            valueTransform = { it.toModel() }
        )
    }

    override suspend fun getLastEntry(kind: String): ConsumptionEntry? {
        return dao.getLastEntry(kind)?.toModel()
    }

    override suspend fun recordUse(kind: String, trigger: String, customTimestamp: Long?): ConsumptionEntry {
        val now = customTimestamp ?: System.currentTimeMillis()
        val entry = ConsumptionEntry(
            id = "$now-${trigger.hashCode()}",
            ts = now,
            trigger = trigger
        )
        dao.insert(ConsumptionEntity.fromModel(entry, kind))
        return entry
    }

    override suspend fun undoLastUse(kind: String): Boolean {
        val rowsAffected = dao.deleteLast(kind)
        return rowsAffected > 0
    }

    override suspend fun deleteEntry(id: String) {
        dao.deleteById(id)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }

    override suspend fun importEntries(entriesByKind: Map<String, List<ConsumptionEntry>>) {
        val entities = entriesByKind.flatMap { (kind, entries) ->
            entries.map { ConsumptionEntity.fromModel(it, kind) }
        }
        dao.insertAll(entities)
    }
}
