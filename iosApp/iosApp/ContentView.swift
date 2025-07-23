import SwiftUI
import shared

struct ContentView: View {
    let appContainer: AppContainer
    @StateObject private var viewModel: ObservableViewModel

    init(appContainer: AppContainer) {
        self.appContainer = appContainer
        self._viewModel = StateObject(wrappedValue: ObservableViewModel(
            todoViewModel: appContainer.createTodoViewModel()
        ))
    }

    var body: some View {
        NavigationView {
            VStack(spacing: 16) {
                // Add Todo Section
                VStack(alignment: .leading, spacing: 12) {
                    Text("Add New Todo")
                        .font(.headline)
                        .padding(.horizontal)

                    VStack(spacing: 8) {
                        TextField("Title", text: $viewModel.newTodoTitle)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .padding(.horizontal)

                        TextField("Description", text: $viewModel.newTodoDescription, axis: .vertical)
                            .textFieldStyle(RoundedBorderTextFieldStyle())
                            .lineLimit(3)
                            .padding(.horizontal)

                        Button(action: {
                            viewModel.addTodo()
                        }) {
                            HStack {
                                if viewModel.isAddingTodo {
                                    ProgressView()
                                        .scaleEffect(0.8)
                                } else {
                                    Text("Add Todo")
                                }
                            }
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(viewModel.newTodoTitle.isEmpty ? Color.gray : Color.blue)
                            .foregroundColor(.white)
                            .cornerRadius(8)
                        }
                        .disabled(viewModel.newTodoTitle.isEmpty || viewModel.isAddingTodo)
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
                .background(Color(.systemGray6))
                .cornerRadius(12)
                .padding(.horizontal)

                // Error message
                if let errorMessage = viewModel.errorMessage {
                    HStack {
                        Text(errorMessage)
                            .foregroundColor(.red)
                            .font(.caption)
                        Spacer()
                        Button("Dismiss") {
                            viewModel.clearError()
                        }
                        .font(.caption)
                    }
                    .padding()
                    .background(Color(.systemRed).opacity(0.1))
                    .cornerRadius(8)
                    .padding(.horizontal)
                }

                // Todo List
                VStack(alignment: .leading) {
                    Text("Todos (\(viewModel.todos.count))")
                        .font(.headline)
                        .padding(.horizontal)

                    if viewModel.todos.isEmpty {
                        VStack {
                            Text("No todos yet. Add one above!")
                                .foregroundColor(.gray)
                                .padding()
                        }
                        .frame(maxWidth: .infinity)
                        .background(Color(.systemGray6))
                        .cornerRadius(8)
                        .padding(.horizontal)
                    } else {
                        List {
                            ForEach(viewModel.todos, id: \.id) { todo in
                                TodoItemView(
                                    todo: todo,
                                    onToggleComplete: { viewModel.toggleTodoCompletion(todo: todo) },
                                    onDelete: { viewModel.deleteTodo(todo: todo) }
                                )
                            }
                        }
                        .listStyle(PlainListStyle())
                    }
                }

                Spacer()
            }
            .navigationTitle("Room KMP Todo App")
            .onAppear {
                viewModel.startObserving()
            }
            .onDisappear {
                viewModel.stopObserving()
            }
        }
    }
}

struct TodoItemView: View {
    let todo: TodoEntity
    let onToggleComplete: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack {
            Button(action: onToggleComplete) {
                Image(systemName: todo.isCompleted ? "checkmark.circle.fill" : "circle")
                    .foregroundColor(todo.isCompleted ? .green : .gray)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(todo.title)
                    .font(.body)
                    .foregroundColor(todo.isCompleted ? .gray : .primary)
                    .strikethrough(todo.isCompleted)

                if !todo.description_.isEmpty {
                    Text(todo.description_)
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }

            Spacer()

            Button(action: onDelete) {
                Image(systemName: "trash")
                    .foregroundColor(.red)
            }
        }
        .padding(.vertical, 4)
    }
}

// Observable wrapper for the Kotlin ViewModel
class ObservableViewModel: ObservableObject {
    private let todoViewModel: TodoViewModel

    @Published var todos: [TodoEntity] = []
    @Published var newTodoTitle: String = ""
    @Published var newTodoDescription: String = ""
    @Published var isAddingTodo: Bool = false
    @Published var errorMessage: String?

    private var timer: Timer?

    init(todoViewModel: TodoViewModel) {
        self.todoViewModel = todoViewModel
    }

    func startObserving() {
        // Poll the UI state periodically (simple approach for KMP)
        timer = Timer.scheduledTimer(withTimeInterval: 0.1, repeats: true) { [weak self] _ in
            guard let self = self else { return }
            if let uiState = self.todoViewModel.uiState.value as? TodoUiState {
                DispatchQueue.main.async {
                    self.todos = uiState.todos
                    // Don't overwrite user input for title and description while typing
                    // Only update when adding todo is in progress or completed
                    if self.isAddingTodo && !uiState.isAddingTodo {
                        // Todo was just added, clear the fields
                        self.newTodoTitle = uiState.newTodoTitle
                        self.newTodoDescription = uiState.newTodoDescription
                    }
                    self.isAddingTodo = uiState.isAddingTodo
                    self.errorMessage = uiState.errorMessage
                }
            }
        }
    }

    func stopObserving() {
        timer?.invalidate()
        timer = nil
    }

    func addTodo() {
        todoViewModel.updateTodoTitle(title: newTodoTitle)
        todoViewModel.updateTodoDescription(description: newTodoDescription)
        todoViewModel.addTodo()
    }

    func toggleTodoCompletion(todo: TodoEntity) {
        todoViewModel.toggleTodoCompletion(todo: todo)
    }

    func deleteTodo(todo: TodoEntity) {
        todoViewModel.deleteTodo(todo: todo)
    }

    func clearError() {
        todoViewModel.clearError()
    }
}