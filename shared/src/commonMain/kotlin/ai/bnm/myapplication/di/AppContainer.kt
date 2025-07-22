package ai.bnm.myapplication.di

import ai.bnm.myapplication.data.AppDatabase
import ai.bnm.myapplication.data.TodoRepository
import ai.bnm.myapplication.presentation.TodoViewModel

class AppContainer(
    private val database: AppDatabase
) {
    private val todoRepository by lazy {
        TodoRepository(database.todoDao())
    }

    fun createTodoViewModel(): TodoViewModel {
        return TodoViewModel(todoRepository)
    }
}
