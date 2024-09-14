

package com.duckduckgo.autofill.store.feature.email.incontext

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class EmailProtectionInContextDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAll(domains: List<EmailInContextExceptionEntity>)

    @Transaction
    open fun updateAll(domains: List<EmailInContextExceptionEntity>) {
        deleteAll()
        insertAll(domains)
    }

    @Query("select * from email_incontext_exceptions where domain = :domain")
    abstract fun get(domain: String): EmailInContextExceptionEntity

    @Query("select * from email_incontext_exceptions")
    abstract fun getAll(): List<EmailInContextExceptionEntity>

    @Query("delete from email_incontext_exceptions")
    abstract fun deleteAll()
}
