

package com.duckduckgo.securestorage.store

import com.duckduckgo.securestorage.store.keys.SecureStorageKeyStore

interface SecureStorageKeyRepository {
    /**
     * User / Programmatically generated password to be used for L2 encryption
     */
    var password: ByteArray?

    /**
     * Key used for L1 encryption
     */
    var l1Key: ByteArray?

    /**
     * Salt to be used when generating the key for l2 encryption from the password
     */
    var passwordSalt: ByteArray?

    /**
     * Encrypted key that can be decrypted to be used for L2 encryption
     */
    var encryptedL2Key: ByteArray?

    /**
     * Iv to be used for L2 key decryption
     */
    var encryptedL2KeyIV: ByteArray?

    /**
     * This method can be checked if the keystore has support for encryption
     *
     * @return `true` if keystore encryption is supported and `false` otherwise
     */
    fun canUseEncryption(): Boolean
}

class RealSecureStorageKeyRepository constructor(
    private val keyStore: SecureStorageKeyStore,
) : SecureStorageKeyRepository {
    override var password: ByteArray?
        get() = keyStore.getKey(KEY_GENERATED_PASSWORD)
        set(value) {
            keyStore.updateKey(KEY_GENERATED_PASSWORD, value)
        }

    override var l1Key: ByteArray?
        get() = keyStore.getKey(KEY_L1KEY)
        set(value) {
            keyStore.updateKey(KEY_L1KEY, value)
        }

    override var passwordSalt: ByteArray?
        get() = keyStore.getKey(KEY_PASSWORD_SALT)
        set(value) {
            keyStore.updateKey(KEY_PASSWORD_SALT, value)
        }

    override var encryptedL2Key: ByteArray?
        get() = keyStore.getKey(KEY_ENCRYPTED_L2KEY)
        set(value) {
            keyStore.updateKey(KEY_ENCRYPTED_L2KEY, value)
        }

    override var encryptedL2KeyIV: ByteArray?
        get() = keyStore.getKey(KEY_ENCRYPTED_L2KEY_IV)
        set(value) {
            keyStore.updateKey(KEY_ENCRYPTED_L2KEY_IV, value)
        }

    override fun canUseEncryption(): Boolean = keyStore.canUseEncryption()

    companion object {
        const val KEY_GENERATED_PASSWORD = "KEY_GENERATED_PASSWORD"
        const val KEY_L1KEY = "KEY_L1KEY"
        const val KEY_PASSWORD_SALT = "KEY_PASSWORD_SALT"
        const val KEY_ENCRYPTED_L2KEY = "KEY_ENCRYPTED_L2KEY"
        const val KEY_ENCRYPTED_L2KEY_IV = "KEY_ENCRYPTED_L2KEY_IV"
    }
}
