package ru.otvykaniye.tracker.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsumptionDao {

    @Query("SELECT * FROM consumption_entries WHERE kind = :kind ORDER BY ts ASC")
    fun observeEntries(kind: String): Flow<List<ConsumptionEntity>>

    @Query("SELECT * FROM consumption_entries ORDER BY ts ASC")
    fun observeAllEntries(): Flow<List<ConsumptionEntity>>

    @Query("SELECT * FROM consumption_entries WHERE kind = :kind ORDER BY ts ASC")
    suspend fun getEntriesForKind(kind: String): List<ConsumptionEntity>

    @Query("SELECT * FROM consumption_entries ORDER BY ts ASC")
    suspend fun getAllEntries(): List<ConsumptionEntity>

    @Query("SELECT * FROM consumption_entries WHERE kind = :kind ORDER BY ts DESC LIMIT 1")
    suspend fun getLastEntry(kind: String): ConsumptionEntity?

    @Query("SELECT * FROM consumption_entries WHERE kind = :kind ORDER BY ts DESC LIMIT 1")
    fun observeLastEntry(kind: String): Flow<ConsumptionEntity?>

    @Query("SELECT COUNT(*) FROM consumption_entries WHERE kind = :kind AND ts >= :startMs AND ts < :endMs")
    suspend fun getCountBetween(kind: String, startMs: Long, endMs: Long): Int

    @Query("SELECT COUNT(*) FROM consumption_entries WHERE kind = :kind AND ts >= :startMs AND ts < :endMs")
    fun observeCountBetween(kind: String, startMs: Long, endMs: Long): Flow<Int>

    @Query("SELECT * FROM consumption_entries WHERE kind = :kind AND ts >= :startMs AND ts < :endMs ORDER BY ts ASC")
    suspend fun getEntriesBetween(kind: String, startMs: Long, endMs: Long): List<ConsumptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ConsumptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ConsumptionEntity>)

    @Query("DELETE FROM consumption_entries WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM consumption_entries WHERE kind = :kind AND id = (SELECT id FROM consumption_entries WHERE kind = :kind ORDER BY ts DESC LIMIT 1)")
    suspend fun deleteLast(kind: String): Int

    @Query("DELETE FROM consumption_entries")
    suspend fun clearAll()
}
