

package com.duckduckgo.app.onboarding.store

import androidx.room.*

@Dao
interface UserStageDao {

    @Query("select * from $USER_STAGE_TABLE_NAME limit 1")
    suspend fun currentUserAppStage(): UserStage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userStage: UserStage)

    @Transaction
    fun updateUserStage(appStage: AppStage) {
        insert(UserStage(appStage = appStage))
    }
}
