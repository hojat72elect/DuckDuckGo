

package com.duckduckgo.autofill.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class AutofillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(domains: List<AutofillExceptionEntity>)

    @Transaction
    open fun updateAll(domains: List<AutofillExceptionEntity>) {
        deleteAll()
        insertAll(domains)
    }

    @Query("select * from autofill_exceptions where domain = :domain")
    abstract fun get(domain: String): AutofillExceptionEntity

    @Query("select * from autofill_exceptions")
    abstract fun getAll(): List<AutofillExceptionEntity>

    @Query("delete from autofill_exceptions")
    abstract fun deleteAll()
}
