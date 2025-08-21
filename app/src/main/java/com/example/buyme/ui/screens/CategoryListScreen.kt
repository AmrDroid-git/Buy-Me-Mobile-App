package com.example.buyme.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.buyme.Graph
import com.example.buyme.data.CategoryEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    onOpenCategory: (Int, String) -> Unit
) {
    val categoryDao = remember { Graph.db.categoryDao() }
    val scope = rememberCoroutineScope()
    val categories by categoryDao.observeCategories().collectAsState(initial = emptyList())

    var name by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Categories") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("New category") },
                    placeholder = { Text("e.g., Fruits") }
                )
                Button(onClick = {
                    val t = name.text.trim()
                    if (t.isNotEmpty()) {
                        scope.launch {
                            categoryDao.insert(CategoryEntity(name = t))
                            name = TextFieldValue("")
                        }
                    }
                }) { Text("Add") }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(categories, key = { it.id }) { cat ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onOpenCategory(cat.id, cat.name) }
                            )
                            IconButton(onClick = { scope.launch { categoryDao.delete(cat) } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
