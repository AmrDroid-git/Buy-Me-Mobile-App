package com.example.buyme.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.room.withTransaction
import com.example.buyme.Graph
import com.example.buyme.data.CategoryEntity
import com.example.buyme.data.ItemEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

// JSON models for import/export
private data class ExportItem(val name: String, val checked: Boolean)
private data class ExportCategory(val name: String, val items: List<ExportItem>)
private data class ExportPayload(val version: Int = 1, val categories: List<ExportCategory>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryOverviewScreen() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val categoryDao = remember { Graph.db.categoryDao() }
        val itemDao = remember { Graph.db.itemDao() }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        val gson = remember { Gson() }

        val categories by categoryDao.observeCategories().collectAsState(initial = emptyList())
        var newCategory by remember { mutableStateOf(TextFieldValue("")) }
        var showConfirmAll by remember { mutableStateOf(false) }

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        // --- collapse state map ---
        val collapsedMap = remember { mutableStateMapOf<Int, Boolean>() }
        categories.forEach { if (!collapsedMap.containsKey(it.id)) collapsedMap[it.id] = false }

        // Share checked items
        fun shareChecked() {
            scope.launch {
                val allCats = categoryDao.listCategories()
                val lines = buildList {
                    for (cat in allCats) {
                        val checked = itemDao.listChecked(cat.id)
                        if (checked.isNotEmpty()) {
                            add("${cat.name}: " + checked.joinToString("، ") { it.name })
                        }
                    }
                }
                val text = lines.joinToString("\n")
                if (text.isBlank()) {
                    Toast.makeText(context, "لا توجد عناصر محددة للمشاركة", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                    context.startActivity(Intent.createChooser(intent, "مشاركة القائمة"))
                }
            }
        }

        // Export / Import
        suspend fun exportToUri(uri: Uri) = withContext(Dispatchers.IO) {
            val cats = categoryDao.listCategories()
            val payload = ExportPayload(
                categories = cats.map { cat ->
                    val items = itemDao.listAllInCategory(cat.id)
                    ExportCategory(
                        name = cat.name,
                        items = items.map { ExportItem(name = it.name, checked = it.checked) }
                    )
                }
            )
            val json = gson.toJson(payload)
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.writer().use { it.write(json) }
            }
        }

        suspend fun importFromUri(uri: Uri) = withContext(Dispatchers.IO) {
            val json = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()?.use { it.readText() }
                ?: throw IllegalArgumentException("فشل قراءة الملف")

            val payload = gson.fromJson(json, ExportPayload::class.java)
                ?: throw IllegalArgumentException("صيغة الملف غير صحيحة")

            Graph.db.withTransaction {
                Graph.db.clearAllTables()
                payload.categories.forEach { c ->
                    val newId = categoryDao.insert(CategoryEntity(name = c.name)).toInt()
                    c.items.forEach { itx ->
                        itemDao.insert(
                            ItemEntity(
                                categoryId = newId,
                                name = itx.name,
                                checked = itx.checked
                            )
                        )
                    }
                }
            }
        }

        val openDocLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    runCatching { importFromUri(uri) }
                        .onSuccess { Toast.makeText(context, "تم الاستيراد بنجاح", Toast.LENGTH_SHORT).show() }
                        .onFailure { Toast.makeText(context, "فشل الاستيراد: ${it.message}", Toast.LENGTH_LONG).show() }
                }
            }
        }

        val createDocLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    runCatching { exportToUri(uri) }
                        .onSuccess { Toast.makeText(context, "تم التصدير بنجاح", Toast.LENGTH_SHORT).show() }
                        .onFailure { Toast.makeText(context, "فشل التصدير: ${it.message}", Toast.LENGTH_LONG).show() }
                }
            }
        }

        // Drawer + BackHandler
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp) // ~1/3 screen
                ) {
                    Text(
                        text = "BuyMe — القوائم والعناصر",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    Divider()

                    NavigationDrawerItem(
                        label = { Text("إلغاء تحديد الكل") },
                        selected = false,
                        onClick = {
                            showConfirmAll = true
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("استيراد") },
                        selected = false,
                        onClick = {
                            openDocLauncher.launch(arrayOf("application/json", "text/*"))
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("تصدير") },
                        selected = false,
                        onClick = {
                            createDocLauncher.launch("buyme_export.json")
                            scope.launch { drawerState.close() }
                        }
                    )
                    NavigationDrawerItem(
                        label = { Text("مشاركة") },
                        selected = false,
                        onClick = {
                            shareChecked()
                            scope.launch { drawerState.close() }
                        },
                        icon = { Icon(Icons.Default.Share, contentDescription = null) }
                    )
                }
            }
        ) {
            BackHandler(enabled = drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }

            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = { Text("BuyMe — القوائم والعناصر") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { padding ->
                if (showConfirmAll) {
                    AlertDialog(
                        onDismissRequest = { showConfirmAll = false },
                        title = { Text("تأكيد") },
                        text = { Text("هل أنت متأكد أنك تريد إلغاء تحديد كل العناصر؟") },
                        confirmButton = {
                            TextButton(onClick = {
                                showConfirmAll = false
                                scope.launch {
                                    itemDao.uncheckAllGlobal()
                                    Toast.makeText(context, "تم إلغاء تحديد جميع العناصر", Toast.LENGTH_SHORT).show()
                                }
                            }) { Text("نعم") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmAll = false }) { Text("إلغاء") }
                        }
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newCategory,
                                onValueChange = { newCategory = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("فئة جديدة") },
                                placeholder = { Text("مثلاً: فواكه") },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        val t = newCategory.text.trim()
                                        if (t.isNotEmpty()) {
                                            scope.launch {
                                                categoryDao.insert(CategoryEntity(name = t))
                                                newCategory = TextFieldValue("")
                                            }
                                        }
                                    }
                                )
                            )
                            Button(onClick = {
                                val t = newCategory.text.trim()
                                if (t.isNotEmpty()) {
                                    scope.launch {
                                        categoryDao.insert(CategoryEntity(name = t))
                                        newCategory = TextFieldValue("")
                                    }
                                }
                            }) { Text("إضافة") }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    items(categories, key = { it.id }) { cat ->
                        CategoryCard(
                            category = cat,
                            collapsed = collapsedMap[cat.id] ?: false,
                            onToggleCollapse = { collapsedMap[cat.id] = !(collapsedMap[cat.id] ?: false) },
                            onDeleteCategory = { scope.launch { categoryDao.delete(cat) } },
                            onUncheckAll = { scope.launch { itemDao.uncheckAll(cat.id) } },
                            onAddItem = { itemName ->
                                scope.launch {
                                    if (itemName.isNotBlank()) {
                                        itemDao.insert(
                                            ItemEntity(
                                                categoryId = cat.id,
                                                name = itemName.trim()
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: CategoryEntity,
    collapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onDeleteCategory: () -> Unit,
    onUncheckAll: () -> Unit,
    onAddItem: (String) -> Unit
) {
    val itemDao = remember { Graph.db.itemDao() }
    val items by itemDao.observeItems(category.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var isAdding by remember { mutableStateOf(false) }
    var newItem by remember { mutableStateOf(TextFieldValue("")) }

    val focusRequester = FocusRequester()
    val keyboard = LocalSoftwareKeyboardController.current
    var hadFocus by remember { mutableStateOf(false) }

    LaunchedEffect(isAdding) {
        if (isAdding) {
            yield()
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }

    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onToggleCollapse() }) {
                    if (collapsed) {
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Show")
                    } else {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Hide")
                    }
                }

                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Red,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        isAdding = !isAdding
                        if (isAdding) { newItem = TextFieldValue(""); hadFocus = false }
                    }) { Icon(Icons.Default.Add, contentDescription = "إضافة عنصر") }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "القائمة")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("إلغاء تحديد عناصر هذه الفئة") },
                            onClick = { showMenu = false; onUncheckAll() }
                        )
                        DropdownMenuItem(
                            text = { Text("حذف الفئة") },
                            onClick = { showMenu = false; onDeleteCategory() }
                        )
                    }
                }
            }

            if (!collapsed) {
                if (isAdding) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newItem,
                            onValueChange = { newItem = it },
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester)
                                .onFocusChanged { state ->
                                    if (state.isFocused) hadFocus = true
                                    if (hadFocus && !state.hasFocus && newItem.text.isBlank()) {
                                        isAdding = false
                                    }
                                },
                            singleLine = true,
                            label = { Text("عنصر جديد") },
                            placeholder = { Text("مثلاً: تفاح") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    val t = newItem.text.trim()
                                    if (t.isNotEmpty()) {
                                        onAddItem(t)
                                        newItem = TextFieldValue("")
                                        isAdding = false
                                    }
                                }
                            )
                        )
                        Button(onClick = {
                            val t = newItem.text.trim()
                            if (t.isNotEmpty()) {
                                onAddItem(t)
                                newItem = TextFieldValue("")
                                isAdding = false
                            }
                        }) { Text("إضافة") }
                    }
                }

                Spacer(Modifier.height(8.dp))
                items.forEach { itx ->
                    ItemRow(
                        item = itx,
                        onToggle = { scope.launch { itemDao.update(itx.copy(checked = !itx.checked)) } },
                        onDelete = { scope.launch { itemDao.delete(itx) } }
                    )
                    Spacer(Modifier.height(6.dp))
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
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Surface(tonalElevation = if (item.checked) 2.dp else 0.dp, shape = MaterialTheme.shapes.medium) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = item.checked, onCheckedChange = { onToggle() })
                    Spacer(Modifier.width(8.dp))
                    Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
                }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "حذف") }
            }
        }
    }
}
