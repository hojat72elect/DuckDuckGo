package com.duckduckgo.autoconsent.impl.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class AutoconsentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertDisabledCmps(disabledCmps: List<DisabledCmpsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertExceptions(exceptions: List<AutoconsentExceptionEntity>)

    @Transaction
    open fun updateAll(
        exceptions: List<AutoconsentExceptionEntity>,
        disabledCmps: List<DisabledCmpsEntity>
    ) {
        deleteDisabledCmps()
        deleteExceptions()
        insertDisabledCmps(disabledCmps)
        insertExceptions(exceptions)
    }

    @Transaction
    open fun updateAllDisabledCMPs(disabledCMPs: List<DisabledCmpsEntity>) {
        deleteDisabledCmps()
        insertDisabledCmps(disabledCMPs)
    }

    @Transaction
    open fun updateAllExceptions(exceptions: List<AutoconsentExceptionEntity>) {
        deleteExceptions()
        insertExceptions(exceptions)
    }

    @Query("select * from autoconsent_exceptions")
    abstract fun getExceptions(): List<AutoconsentExceptionEntity>

    @Query("select * from autoconsent_disabled_cmps")
    abstract fun getDisabledCmps(): List<DisabledCmpsEntity>

    @Query("delete from autoconsent_disabled_cmps")
    abstract fun deleteDisabledCmps()

    @Query("delete from autoconsent_exceptions")
    abstract fun deleteExceptions()
}
