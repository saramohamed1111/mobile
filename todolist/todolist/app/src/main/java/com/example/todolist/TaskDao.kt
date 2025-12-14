package com.example.todolist

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<Task>>

    // --- NEW: Get a single task to display in the Detail Activity ---
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun getTask(taskId: Int): Flow<Task>

    @Insert
    suspend fun insertTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    // --- NEW: Update an existing task ---
    @Update
    suspend fun updateTask(task: Task)
}