package com.example.thealfred.data

/**
 * Data class для хранения результата агрегирующего запроса из Room.
 * Хранит суммарное время (в миллисекундах), потраченное на каждую категорию.
 */
data class CategoryDuration(
    val category: TaskCategory,
    val totalDuration: Long
)
