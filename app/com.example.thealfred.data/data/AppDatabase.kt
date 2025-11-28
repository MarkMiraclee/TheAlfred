package com.example.thealfred.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// 1. Аннотация Database: Указываем все Entity, версию и используемые TypeConverters
@Database(
    entities = [Task::class, Progress::class], // Наши сущности (Entity)
    version = 1,              // Версия базы данных
    exportSchema = false      // Не экспортировать схему в файл
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // 2. DAO: Абстрактная функция, которая возвращает наш DAO
    abstract fun taskDao(): TaskDao

    // 3. Companion Object: Паттерн Singleton для гарантированного создания только одного экземпляра БД
    companion object {

        // Volatile гарантирует, что значение этой переменной всегда актуально для всех потоков
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Функция получения экземпляра базы данных (основная точка доступа)
        fun getDatabase(context: Context): AppDatabase {
            // Если экземпляр уже существует, возвращаем его
            return INSTANCE ?: synchronized(this) {
                // В противном случае, создаем новый экземпляр в защищенном (синхронизированном) блоке
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "the_alfred_db" // Имя файла базы данных
                )
                    .fallbackToDestructiveMigration() // Полезно при разработке: при смене версии БД она сбрасывается
                    .build()

                INSTANCE = instance

                // Возвращаем только что созданный экземпляр
                instance
            }
        }
    }
}