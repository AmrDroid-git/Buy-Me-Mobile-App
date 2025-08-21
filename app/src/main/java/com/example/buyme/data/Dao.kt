package com.example.buyme.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name")
    suspend fun listCategories(): List<CategoryEntity>

    // return new rowId so we can use it during import
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity): Long

    @Delete
    suspend fun delete(category: CategoryEntity)
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY name")
    fun observeItems(categoryId: Int): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE categoryId = :categoryId AND checked = 1 ORDER BY name")
    suspend fun listChecked(categoryId: Int): List<ItemEntity>

    @Query("SELECT * FROM items WHERE categoryId = :categoryId ORDER BY name")
    suspend fun listAllInCategory(categoryId: Int): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ItemEntity): Long

    @Update suspend fun update(item: ItemEntity)
    @Delete suspend fun delete(item: ItemEntity)

    @Query("UPDATE items SET checked = 0 WHERE categoryId = :categoryId")
    suspend fun uncheckAll(categoryId: Int)

    @Query("UPDATE items SET checked = 0")
    suspend fun uncheckAllGlobal()
}
