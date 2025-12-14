package com.example.todolist

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch

class DetailActivity : ComponentActivity() {
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "todo-database"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Lab 3: Get Data with Intents ---
        // Retrieve the ID passed from MainActivity
        val taskId = intent.getIntExtra("TASK_ID", -1)

        setContent {
            DetailScreen(taskId, db.taskDao()) {
                finish() // Closes activity when "Save" is clicked
            }
        }
    }
}

@Composable
fun DetailScreen(taskId: Int, dao: TaskDao, onSaveComplete: () -> Unit) {
    // Collect the specific task from the DB
    val taskState = dao.getTask(taskId).collectAsState(initial = null)
    val task = taskState.value
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Local state for editing fields
    var editName by remember(task) { mutableStateOf(task?.name ?: "") }
    var isDone by remember(task) { mutableStateOf(task?.isCompleted ?: false) }

    if (task != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Edit Task", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // --- Edit Name Field ---
            OutlinedTextField(
                value = editName,
                onValueChange = { editName = it },
                label = { Text("Task Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Status Checkbox ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { isDone = it }
                )
                Text(text = if (isDone) "Completed" else "Not Completed")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Save Button ---
            Button(
                onClick = {
                    if (editName.isNotEmpty()) {
                        scope.launch {
                            // --- Lab 5: Update Operation ---
                            val updatedTask = task.copy(name = editName, isCompleted = isDone)
                            dao.updateTask(updatedTask)

                            // Show feedback and close
                            Toast.makeText(context, "Task Updated!", Toast.LENGTH_SHORT).show()
                            onSaveComplete()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    } else {
        // Show loading if task is not yet fetched
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}