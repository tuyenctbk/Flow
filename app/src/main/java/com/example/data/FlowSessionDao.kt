package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlowSessionDao {
    @Query("SELECT * FROM flow_sessions ORDER BY timestamp DESC")
    fun getAllFlowSessions(): Flow<List<FlowSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FlowSession)

    @Delete
    suspend fun deleteSession(session: FlowSession)

    @Query("DELETE FROM flow_sessions")
    suspend fun clearAllSessions()
}
