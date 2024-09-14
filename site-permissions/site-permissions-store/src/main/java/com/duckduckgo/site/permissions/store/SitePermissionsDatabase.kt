

package com.duckduckgo.site.permissions.store

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.duckduckgo.site.permissions.store.drmblock.DrmBlockDao
import com.duckduckgo.site.permissions.store.drmblock.DrmBlockExceptionEntity
import com.duckduckgo.site.permissions.store.sitepermissions.SitePermissionsDao
import com.duckduckgo.site.permissions.store.sitepermissions.SitePermissionsEntity
import com.duckduckgo.site.permissions.store.sitepermissionsallowed.SitePermissionAllowedEntity
import com.duckduckgo.site.permissions.store.sitepermissionsallowed.SitePermissionsAllowedDao

@Database(
    exportSchema = true,
    version = 3,
    entities = [
        SitePermissionsEntity::class,
        SitePermissionAllowedEntity::class,
        DrmBlockExceptionEntity::class,
    ],
)

abstract class SitePermissionsDatabase : RoomDatabase() {
    abstract fun sitePermissionsDao(): SitePermissionsDao
    abstract fun sitePermissionsAllowedDao(): SitePermissionsAllowedDao
    abstract fun drmBlockDao(): DrmBlockDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE 'site_permissions' ADD COLUMN 'askDrmSetting' TEXT NOT NULL DEFAULT 'ASK_EVERY_TIME'")
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2)
