package ai.bnm.myapplication.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Insert
    suspend fun insert(todo: TodoEntity): Long

    @Update
    suspend fun update(todo: TodoEntity)

    @Delete
    suspend fun delete(todo: TodoEntity)

    @Query("DELETE FROM TodoEntity WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM TodoEntity ORDER BY createdAt DESC")
    fun getAllAsFlow(): Flow<List<TodoEntity>>

    @Query("SELECT * FROM TodoEntity WHERE id = :id")
    suspend fun getById(id: Long): TodoEntity?

    @Query("SELECT COUNT(*) FROM TodoEntity")
    suspend fun getCount(): Int

    @Query("UPDATE TodoEntity SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)
}
