package com.duckduckgo.autofill.store.reporting

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.migration.Migration

@Database(
    exportSchema = true,
    version = 1,
    entities = [
        AutofillSiteBreakageReportingEntity::class,
    ],
)
abstract class AutofillSiteBreakageReportingDatabase : RoomDatabase() {
    abstract fun dao(): AutofillSiteBreakageReportingDao
}

@Entity(tableName = "autofill_site_breakage_reporting")
data class AutofillSiteBreakageReportingEntity(
    @PrimaryKey val domain: String,
    val reason: String,
)

val ALL_MIGRATIONS = emptyArray<Migration>()

@Dao
abstract class AutofillSiteBreakageReportingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(domains: List<AutofillSiteBreakageReportingEntity>)

    @Transaction
    open fun updateAll(domains: List<AutofillSiteBreakageReportingEntity>) {
        deleteAll()
        insertAll(domains)
    }

    @Query("select * from autofill_site_breakage_reporting")
    abstract fun getAll(): List<AutofillSiteBreakageReportingEntity>

    @Query("delete from autofill_site_breakage_reporting")
    abstract fun deleteAll()
}
