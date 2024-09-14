

package com.duckduckgo.networkprotection.store.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import com.duckduckgo.networkprotection.store.remote_config.NetPConfigToggle
import com.duckduckgo.networkprotection.store.remote_config.NetPConfigTogglesDao
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

@Database(
    exportSchema = true,
    version = 3,
    entities = [
        NetPManuallyExcludedApp::class,
        NetPConfigToggle::class,
        NetPGeoswitchingLocation::class,
    ],
)
@TypeConverters(NetpDatabaseConverters::class)
abstract class NetPDatabase : RoomDatabase() {
    abstract fun exclusionListDao(): NetPExclusionListDao
    abstract fun configTogglesDao(): NetPConfigTogglesDao
    abstract fun geoswitchingDao(): NetPGeoswitchingDao

    companion object {
        val ALL_MIGRATIONS: List<Migration>
            get() = emptyList()
    }
}

object NetpDatabaseConverters {

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter: JsonAdapter<List<String>> = Moshi.Builder().build().adapter(stringListType)

    @TypeConverter
    @JvmStatic
    fun toStringList(value: String): List<String> {
        return stringListAdapter.fromJson(value)!!
    }

    @TypeConverter
    @JvmStatic
    fun fromStringList(value: List<String>): String {
        return stringListAdapter.toJson(value)
    }
}
