

package com.duckduckgo.app.onboarding.store

import com.duckduckgo.common.utils.DispatcherProvider
import javax.inject.Inject
import kotlinx.coroutines.withContext

interface UserStageStore {
    suspend fun getUserAppStage(): AppStage
    suspend fun stageCompleted(appStage: AppStage): AppStage
    suspend fun moveToStage(appStage: AppStage)
}

class AppUserStageStore @Inject constructor(
    private val userStageDao: UserStageDao,
    private val dispatcher: DispatcherProvider,
) : UserStageStore {

    override suspend fun getUserAppStage(): AppStage {
        return withContext(dispatcher.io()) {
            val userStage = userStageDao.currentUserAppStage()
            return@withContext userStage?.appStage ?: AppStage.NEW
        }
    }

    override suspend fun stageCompleted(appStage: AppStage): AppStage {
        return withContext(dispatcher.io()) {
            val newAppStage = when (appStage) {
                AppStage.NEW -> AppStage.DAX_ONBOARDING
                AppStage.DAX_ONBOARDING -> AppStage.ESTABLISHED
                AppStage.ESTABLISHED -> AppStage.ESTABLISHED
            }

            if (newAppStage != appStage) {
                userStageDao.updateUserStage(newAppStage)
            }

            return@withContext newAppStage
        }
    }

    override suspend fun moveToStage(appStage: AppStage) {
        userStageDao.updateUserStage(appStage)
    }
}

suspend fun UserStageStore.isNewUser(): Boolean {
    return this.getUserAppStage() == AppStage.NEW
}

suspend fun UserStageStore.daxOnboardingActive(): Boolean {
    return this.getUserAppStage() == AppStage.DAX_ONBOARDING
}
