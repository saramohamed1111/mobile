package com.example.todolist

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextDecoration

class MainActivity : ComponentActivity() {
    // Initialize Database
    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "todo-database"
        ).build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TodoApp(db.taskDao())
        }
    }
}

@Composable
fun TodoApp(dao: TaskDao) {
    // State to hold the list of tasks (Real-time updates from Room)
    val tasks by dao.getAllTasks().collectAsState(initial = emptyList())

    // State for the text input
    var textInput by remember { mutableStateOf("") }

    // Coroutine scope for database operations (Insert/Delete)
    val scope = rememberCoroutineScope()

    // Context needed for starting Activities (Intents)
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Input Area (Add New Task) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                label = { Text("Enter Task") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (textInput.isNotEmpty()) {
                    scope.launch {
                        // Insert new task into Database
                        dao.insertTask(Task(name = textInput))
                        textInput = "" // Clear input field
                    }
                }
            }) {
                Text("Add")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Task List (LazyColumn) ---
        LazyColumn {
            items(tasks) { task ->
                TaskItem(
                    task = task,
                    onDelete = {
                        scope.launch { dao.deleteTask(task) }
                    },
                    onShare = {
                        // --- Lab 3: Implicit Intent (Share text) ---
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "ToDo Task: ${task.name}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    onClick = {
                        // --- Lab 3: Explicit Intent (Navigate to DetailActivity) ---
                        val intent = Intent(context, DetailActivity::class.java)
                        // Pass the Task ID so DetailActivity knows what to edit
                        intent.putExtra("TASK_ID", task.id)
                        context.startActivity(intent)
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
fun TaskItem(
    task: Task,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // --- Lab 6: Modifier.clickable to detect taps ---
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Task Name
        Text(
            text = task.name,
            style = MaterialTheme.typography.bodyLarge,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,

            // --- NEW: Gray out text if completed ---
            color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        // Share Icon
        IconButton(onClick = onShare) {
            Icon(Icons.Default.Share, contentDescription = "Share")
        }

        // Delete Icon
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}