

package com.duckduckgo.site.permissions.store.drmblock

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class DrmBlockDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(domains: List<DrmBlockExceptionEntity>)

    @Transaction
    open fun updateAll(domains: List<DrmBlockExceptionEntity>) {
        deleteAll()
        insertAll(domains)
    }

    @Query("select * from drm_block_exceptions")
    abstract fun getAll(): List<DrmBlockExceptionEntity>

    @Query("delete from drm_block_exceptions")
    abstract fun deleteAll()
}
