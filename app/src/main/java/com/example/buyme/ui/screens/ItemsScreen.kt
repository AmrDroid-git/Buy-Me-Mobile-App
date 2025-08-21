package com.example.buyme.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.buyme.Graph
import com.example.buyme.data.ItemEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemsScreen(
    categoryId: Int,
    categoryName: String,
    onBack: () -> Unit
) {
    val itemDao = remember { Graph.db.itemDao() }
    val scope = rememberCoroutineScope()
    val items by itemDao.observeItems(categoryId).collectAsState(initial = emptyList())

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Uncheck all") },
                            onClick = {
                                showMenu = false
                                scope.launch { itemDao.uncheckAll(categoryId) }
                            }
                        )
                    }
                }
            )
        }
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
                    label = { Text("New item") },
                    placeholder = { Text("e.g., Apple") }
                )
                Button(onClick = {
                    val t = name.text.trim()
                    if (t.isNotEmpty()) {
                        scope.launch {
                            itemDao.insert(ItemEntity(categoryId = categoryId, name = t))
                            name = TextFieldValue("")
                        }
                    }
                }) { Text("Add") }
            }

            Spacer(Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { itx ->
                    ItemRow(
                        item = itx,
                        onToggle = {
                            scope.launch { itemDao.update(itx.copy(checked = !itx.checked)) }
                        },
                        onDelete = { scope.launch { itemDao.delete(itx) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemRow(
    item: ItemEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.weight(1f)) {
                Checkbox(checked = item.checked, onCheckedChange = { onToggle() })
                Spacer(Modifier.width(8.dp))
                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
