package ai.bnm.myapplication.data

import kotlinx.coroutines.flow.Flow

class TodoRepository(private val todoDao: TodoDao) {

    fun getAllTodos(): Flow<List<TodoEntity>> = todoDao.getAllAsFlow()

    suspend fun insertTodo(title: String, description: String): Long {
        val todo = TodoEntity(
            title = title,
            description = description
        )
        return todoDao.insert(todo)
    }

    suspend fun updateTodo(todo: TodoEntity) {
        todoDao.update(todo)
    }

    suspend fun deleteTodo(todo: TodoEntity) {
        todoDao.delete(todo)
    }

    suspend fun deleteTodoById(id: Long) {
        todoDao.deleteById(id)
    }

    suspend fun toggleTodoCompletion(id: Long, isCompleted: Boolean) {
        todoDao.updateCompletionStatus(id, isCompleted)
    }

    suspend fun getTodoById(id: Long): TodoEntity? {
        return todoDao.getById(id)
    }

    suspend fun getTodoCount(): Int {
        return todoDao.getCount()
    }
}
