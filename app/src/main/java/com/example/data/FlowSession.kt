package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flow_sessions")
data class FlowSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val intent: String,
    val durationSeconds: Int,
    val flowScore: Int, // Metacognitive mirror rating (1 to 5)
    val challengeLevel: Int, // 1: Very Low, 3: Medium, 5: Very High
    val skillLevel: Int, // 1: Very Low, 3: Medium, 5: Very High
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
