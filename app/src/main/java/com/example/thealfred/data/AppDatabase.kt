package com.example.thealfred.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Task::class, Progress::class, Template::class, WeeklySummary::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun templateDao(): TemplateDao
    abstract fun weeklySummaryDao(): WeeklySummaryDao

    companion object {

        /**
         * v1 -> v2: в Task добавлен флаг isMorningRitual (утренний ритуал
         * опознаётся по флагу, а не по названию задачи).
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isMorningRitual INTEGER NOT NULL DEFAULT 0")
            }
        }

        /**
         * v2 -> v3: недельный цикл. В Progress добавлен weekStart (граница текущей
         * недели), добавлена таблица weekly_summaries — снимки итогов завершённых
         * недель для журнала.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE progress ADD COLUMN weekStart INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `weekly_summaries` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`weekStart` INTEGER NOT NULL, " +
                        "`totalXp` INTEGER NOT NULL, " +
                        "`currentRank` TEXT NOT NULL, " +
                        "`operationalMissionsXp` INTEGER NOT NULL, " +
                        "`physicalConditioningXp` INTEGER NOT NULL, " +
                        "`intelligenceAndStrategyXp` INTEGER NOT NULL, " +
                        "`waynePersonaMaintenanceXp` INTEGER NOT NULL, " +
                        "`tasksCompleted` INTEGER NOT NULL, " +
                        "`savedAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_weekly_summaries_weekStart` " +
                        "ON `weekly_summaries` (`weekStart`)"
                )
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "the_alfred_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}