package com.example.thealfred.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Query("SELECT * FROM templates ORDER BY title ASC")
    fun getAllTemplates(): Flow<List<Template>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTemplate(template: Template)

    @Query("DELETE FROM templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: Int)
}