package com.example.data

import kotlinx.coroutines.flow.Flow

class FlowRepository(private val flowSessionDao: FlowSessionDao) {
    val allSessions: Flow<List<FlowSession>> = flowSessionDao.getAllFlowSessions()

    suspend fun insert(session: FlowSession) {
        flowSessionDao.insertSession(session)
    }

    suspend fun delete(session: FlowSession) {
        flowSessionDao.deleteSession(session)
    }

    suspend fun clearAll() {
        flowSessionDao.clearAllSessions()
    }
}
