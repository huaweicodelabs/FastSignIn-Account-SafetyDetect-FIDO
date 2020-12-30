/*
 *  Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.codelabs.accountsafetyfido.kotlin.util.fingerprint

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * Created by CodeLab
 */
class KeyHelper private constructor() {

    companion object {
        private const val KOTLIN_ALIAS = "com.codelabs.accountsafetyfido.kotlin.util.fingerprint.KeyHelper.Fingerprint"
        private const val KOTLIN_KEY_STORE = "AndroidKeyStore"
        private const val KOTLIN_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val KOTLIN_BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
        private const val KOTLIN_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val KOTLIN_TRANSFORMATION = "$KOTLIN_ALGORITHM/$KOTLIN_BLOCK_MODE/$KOTLIN_PADDING"

        @JvmStatic
        fun getInstance(): KeyHelper {
            return KeyHolder.mInstance
        }
    }

    object KeyHolder {
        val mInstance: KeyHelper = KeyHelper()
    }

    /**
     * get Encrypt Cipher
     */
    fun getEncryptCipher(): Cipher? {
        var cipher: Cipher? = null
        try {
            cipher = Cipher.getInstance(KOTLIN_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getKey())
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return cipher
    }

    /**
     * get Decrypt Cipher
     */
    fun getDecryptCipher(initializeVector: ByteArray?): Cipher? {
        var cipher: Cipher? = null
        try {
            cipher = Cipher.getInstance(KOTLIN_TRANSFORMATION)
            val ivParameterSpec = IvParameterSpec(initializeVector)
            cipher.init(Cipher.DECRYPT_MODE, getKey(), ivParameterSpec)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return cipher
    }

    /**
     * get key
     */
    @Throws(java.lang.Exception::class)
    private fun getKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(KOTLIN_KEY_STORE)
        keyStore.load(null)
        return if (keyStore.isKeyEntry(KOTLIN_ALIAS)) {
            val secretKeyEntry = keyStore.getEntry(KOTLIN_ALIAS, null) as KeyStore.SecretKeyEntry
            secretKeyEntry.secretKey
        } else {
            createKey()
        }
    }

    /**
     * create key
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Throws(Exception::class)
    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KOTLIN_ALGORITHM, KOTLIN_KEY_STORE)
        val spec = KeyGenParameterSpec.Builder(KOTLIN_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KOTLIN_BLOCK_MODE)
            .setEncryptionPaddings(KOTLIN_PADDING)
            .setUserAuthenticationRequired(true)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}