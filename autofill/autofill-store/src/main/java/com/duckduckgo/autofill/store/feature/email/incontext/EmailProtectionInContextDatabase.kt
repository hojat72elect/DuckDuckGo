

package com.duckduckgo.autofill.store.feature.email.incontext

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.migration.Migration

@Database(
    exportSchema = true,
    version = 1,
    entities = [
        EmailInContextExceptionEntity::class,
    ],
)
abstract class EmailProtectionInContextDatabase : RoomDatabase() {
    abstract fun emailInContextDao(): EmailProtectionInContextDao
}

@Entity(tableName = "email_incontext_exceptions")
data class EmailInContextExceptionEntity(
    @PrimaryKey val domain: String,
    val reason: String,
)

val ALL_MIGRATIONS = emptyArray<Migration>()
