

package com.duckduckgo.securestorage

import com.duckduckgo.autofill.impl.securestorage.SecureStorageKeyGenerator
import com.duckduckgo.autofill.impl.securestorage.encryption.EncryptionHelper
import com.duckduckgo.autofill.impl.securestorage.encryption.EncryptionHelper.EncryptedBytes
import com.duckduckgo.securestorage.store.SecureStorageKeyRepository
import java.security.Key
import okio.ByteString.Companion.decodeBase64

class FakeSecureStorageKeyRepository(private val canUseEncryption: Boolean) : SecureStorageKeyRepository {
    override var password: ByteArray? = null
    override var l1Key: ByteArray? = null
    override var passwordSalt: ByteArray? = null
    override var encryptedL2Key: ByteArray? = null
    override var encryptedL2KeyIV: ByteArray? = null
    override fun canUseEncryption(): Boolean = canUseEncryption
}

class FakeEncryptionHelper constructor(
    private val expectedEncryptedData: String,
    private val expectedEncryptedIv: String,
    private val expectedDecryptedData: String,
) : EncryptionHelper {
    override fun encrypt(
        raw: ByteArray,
        key: Key,
    ): EncryptedBytes = EncryptedBytes(
        expectedEncryptedData.decodeBase64()!!.toByteArray(),
        expectedEncryptedIv.decodeBase64()!!.toByteArray(),
    )

    override fun decrypt(
        toDecrypt: EncryptedBytes,
        key: Key,
    ): ByteArray = expectedDecryptedData.decodeBase64()!!.toByteArray()
}

class FakeSecureStorageKeyGenerator constructor(private val key: Key) : SecureStorageKeyGenerator {
    override fun generateKey(): Key = key

    override fun generateKeyFromKeyMaterial(keyMaterial: ByteArray): Key = key

    override fun generateKeyFromPassword(
        password: String,
        salt: ByteArray,
    ): Key = key
}
