

package com.duckduckgo.autofill.store

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CredentialsSyncMetadataDao {

    @Query("select * from credentials_sync_meta where localId = :localId")
    fun getSyncMetadata(localId: Long): CredentialsSyncMetadataEntity?

    @Query("select * from credentials_sync_meta where syncId = :syncId")
    fun getSyncMetadata(syncId: String): CredentialsSyncMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: CredentialsSyncMetadataEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entities: List<CredentialsSyncMetadataEntity>)

    @Transaction
    fun initialize(entities: List<CredentialsSyncMetadataEntity>) {
        removeAll()
        entities.forEach {
            insert(it)
        }
    }

    @Query("select * from credentials_sync_meta where deleted_at >= :since")
    fun getRemovedIdsSince(since: String): List<CredentialsSyncMetadataEntity>

    @Delete
    fun delete(entity: CredentialsSyncMetadataEntity)

    @Query("select localId from credentials_sync_meta where syncId = :syncId")
    fun getLocalId(syncId: String): Long?

    @Query("Delete from credentials_sync_meta where deleted_at <= :before")
    fun removeDeletedEntities(before: String)

    @Query("Delete from credentials_sync_meta where localId = :localId")
    fun removeEntityWithLocalId(localId: Long)

    @Query("Delete from credentials_sync_meta where syncId = :syncId")
    fun removeEntityWithSyncId(syncId: String)

    @Query("select * from credentials_sync_meta where modified_at > :since")
    fun getChangesSince(since: String): List<CredentialsSyncMetadataEntity>

    @Query("select * from credentials_sync_meta")
    fun getAllObservable(): Flow<List<CredentialsSyncMetadataEntity>>

    @Query("select * from credentials_sync_meta")
    fun getAll(): List<CredentialsSyncMetadataEntity>

    @Query("Delete from credentials_sync_meta")
    fun removeAll()
}
