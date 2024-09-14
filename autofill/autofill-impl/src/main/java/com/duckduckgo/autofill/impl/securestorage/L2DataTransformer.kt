

package com.duckduckgo.autofill.impl.securestorage

import com.duckduckgo.autofill.impl.securestorage.SecureStorageException.InternalSecureStorageException
import com.duckduckgo.autofill.impl.securestorage.encryption.EncryptionHelper
import com.duckduckgo.autofill.impl.securestorage.encryption.EncryptionHelper.EncryptedBytes
import com.duckduckgo.autofill.impl.securestorage.encryption.EncryptionHelper.EncryptedString
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import javax.inject.Inject
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString

interface L2DataTransformer {
    fun canProcessData(): Boolean

    @Throws(SecureStorageException::class)
    fun encrypt(data: String): EncryptedString

    @Throws(SecureStorageException::class)
    fun decrypt(
        data: String,
        iv: String,
    ): String
}

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class RealL2DataTransformer @Inject constructor(
    private val encryptionHelper: EncryptionHelper,
    private val secureStorageKeyProvider: SecureStorageKeyProvider,
) : L2DataTransformer {
    private val l2Key by lazy {
        secureStorageKeyProvider.getl2Key()
    }

    override fun canProcessData(): Boolean = secureStorageKeyProvider.canAccessKeyStore()

    // get ByteArray -> encrypt -> encode to String
    override fun encrypt(data: String): EncryptedString = encryptionHelper.encrypt(data.toByteArray(), l2Key).run {
        EncryptedString(
            this.data.transformToString(),
            this.iv.transformToString(),
        )
    }

    // decode to ByteArray -> decrypt -> get String
    override fun decrypt(
        data: String,
        iv: String,
    ): String = encryptionHelper.decrypt(
        EncryptedBytes(
            data = data.transformToByteArray(),
            iv = iv.transformToByteArray(),
        ),
        l2Key,
    ).run { String(this) }

    private fun ByteArray.transformToString(): String = this.toByteString().base64()

    private fun String.transformToByteArray(): ByteArray =
        this.decodeBase64()?.toByteArray() ?: throw InternalSecureStorageException("Error while decoding string data to Base64")
}
