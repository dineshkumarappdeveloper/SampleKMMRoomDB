package ai.bnm.myapplication.presentation

import ai.bnm.myapplication.data.TodoEntity
import ai.bnm.myapplication.data.TodoRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class TodoUiState(
    val todos: List<TodoEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val newTodoTitle: String = "",
    val newTodoDescription: String = "",
    val isAddingTodo: Boolean = false
)

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    private val _newTodoTitle = MutableStateFlow("")
    private val _newTodoDescription = MutableStateFlow("")
    private val _isAddingTodo = MutableStateFlow(false)

    init {
        loadTodos()

        // Combine all state flows to create the final UI state
        viewModelScope.launch {
            combine(
                repository.getAllTodos(),
                _newTodoTitle,
                _newTodoDescription,
                _isAddingTodo
            ) { todos, title, description, isAdding ->
                _uiState.value.copy(
                    todos = todos,
                    newTodoTitle = title,
                    newTodoDescription = description,
                    isAddingTodo = isAdding
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun loadTodos() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load todos: ${e.message}"
                )
            }
        }
    }

    fun updateTodoTitle(title: String) {
        _newTodoTitle.value = title
    }

    fun updateTodoDescription(description: String) {
        _newTodoDescription.value = description
    }

    fun addTodo() {
        val title = _newTodoTitle.value.trim()
        val description = _newTodoDescription.value.trim()

        if (title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Title cannot be empty")
            return
        }

        viewModelScope.launch {
            try {
                _isAddingTodo.value = true
                repository.insertTodo(title, description)
                _newTodoTitle.value = ""
                _newTodoDescription.value = ""
                _uiState.value = _uiState.value.copy(errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to add todo: ${e.message}"
                )
            } finally {
                _isAddingTodo.value = false
            }
        }
    }

    fun toggleTodoCompletion(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                repository.toggleTodoCompletion(todo.id, !todo.isCompleted)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update todo: ${e.message}"
                )
            }
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                repository.deleteTodo(todo)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to delete todo: ${e.message}"
                )
            }
        }
    }

    fun updateTodo(todo: TodoEntity) {
        viewModelScope.launch {
            try {
                repository.updateTodo(todo)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to update todo: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
