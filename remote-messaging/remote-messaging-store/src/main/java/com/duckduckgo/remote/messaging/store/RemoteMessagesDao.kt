

package com.duckduckgo.remote.messaging.store

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.duckduckgo.remote.messaging.store.RemoteMessageEntity.Status
import kotlinx.coroutines.flow.Flow

@Dao
abstract class RemoteMessagesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(messageEntity: RemoteMessageEntity)

    @Query("select * from remote_message where id = :id")
    abstract fun messagesById(id: String): RemoteMessageEntity?

    @Query("select * from remote_message where status = \"DISMISSED\"")
    abstract fun dismissedMessages(): List<RemoteMessageEntity>

    @Query("update remote_message set status = :newState where id = :id")
    abstract fun updateState(id: String, newState: Status)

    @Query("select * from remote_message where status = \"SCHEDULED\"")
    abstract fun message(): RemoteMessageEntity?

    @Query("select * from remote_message where status = \"SCHEDULED\"")
    abstract fun messagesFlow(): Flow<RemoteMessageEntity?>

    @Query("DELETE FROM remote_message WHERE status = \"SCHEDULED\"")
    abstract fun deleteActiveMessages()

    @Transaction
    open fun newMessage(messageEntity: RemoteMessageEntity) {
        deleteActiveMessages()
        insert(messageEntity)
    }
}
