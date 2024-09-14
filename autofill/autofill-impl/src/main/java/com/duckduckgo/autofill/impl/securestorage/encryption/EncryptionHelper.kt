

package com.duckduckgo.autofill.impl.securestorage.encryption

import android.security.keystore.KeyProperties
import com.duckduckgo.autofill.impl.securestorage.SecureStorageException
import com.duckduckgo.autofill.impl.securestorage.SecureStorageException.InternalSecureStorageException
import com.duckduckgo.autofill.impl.securestorage.encryption.EncryptionHelper.EncryptedBytes
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import java.lang.Exception
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject

interface EncryptionHelper {
    @Throws(SecureStorageException::class)
    fun encrypt(
        raw: ByteArray,
        key: Key,
    ): EncryptedBytes

    @Throws(SecureStorageException::class)
    fun decrypt(
        toDecrypt: EncryptedBytes,
        key: Key,
    ): ByteArray

    class EncryptedBytes(
        val data: ByteArray,
        val iv: ByteArray,
    )

    class EncryptedString(
        val data: String,
        val iv: String,
    )
}

@ContributesBinding(AppScope::class)
class RealEncryptionHelper @Inject constructor() : EncryptionHelper {
    private val encryptionCipher = Cipher.getInstance(TRANSFORMATION)
    private val decryptionCipher = Cipher.getInstance(TRANSFORMATION)

    @Synchronized
    override fun encrypt(
        raw: ByteArray,
        key: Key,
    ): EncryptedBytes {
        val encrypted = try {
            encryptionCipher.init(Cipher.ENCRYPT_MODE, key)
            encryptionCipher.doFinal(raw)
        } catch (exception: Exception) {
            throw InternalSecureStorageException(message = "Error occurred while encrypting data", cause = exception)
        }
        val iv = encryptionCipher.iv

        return EncryptedBytes(encrypted, iv)
    }

    @Synchronized
    override fun decrypt(
        toDecrypt: EncryptedBytes,
        key: Key,
    ): ByteArray {
        return try {
            val ivSpec = GCMParameterSpec(GCM_PARAM_SPEC_LENGTH, toDecrypt.iv)
            decryptionCipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            decryptionCipher.doFinal(toDecrypt.data)
        } catch (exception: Exception) {
            throw InternalSecureStorageException(message = "Error occurred while decrypting data", cause = exception)
        }
    }

    companion object {
        private const val GCM_PARAM_SPEC_LENGTH = 128
        private const val TRANSFORMATION =
            "${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_GCM}/${KeyProperties.ENCRYPTION_PADDING_NONE}"
    }
}
