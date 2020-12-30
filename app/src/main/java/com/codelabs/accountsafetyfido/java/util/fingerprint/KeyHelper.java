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
package com.codelabs.accountsafetyfido.java.util.fingerprint;

import android.annotation.SuppressLint;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;

import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by CodeLab
 */
public class KeyHelper {
    private static final String JAVA_KEY_STORE = "AndroidKeyStore";
    private static final String JAVA_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES;
    private static final String JAVA_BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC;
    private static final String JAVA_PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7;
    private static final String JAVA_TRANSFORMATION = JAVA_ALGORITHM + "/" + JAVA_BLOCK_MODE + "/" + JAVA_PADDING;
    private final String KEY_ALIAS = "com.codelabs.accountsafetyfido.java.util.fingerprint.KeyHelper.Fingerprint";

    private KeyHelper() {
    }

    private static class KeyHolder {
        @SuppressLint("StaticFieldLeak")
        private static final KeyHelper INSTANCE = new KeyHelper();
    }

    public static KeyHelper getInstance() {
        return KeyHolder.INSTANCE;
    }

    /**
     * get Encrypt Cipher
     */
    public Cipher getEncryptCipher() {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(JAVA_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipher;
    }

    /**
     * get Decrypt Cipher
     */
    public Cipher getDecryptCipher(byte[] initializeVector) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(JAVA_TRANSFORMATION);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(initializeVector);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), ivParameterSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipher;
    }

    /**
     * get key
     */
    private SecretKey getKey() throws Exception {
        SecretKey secretKey;
        KeyStore keyStore = KeyStore.getInstance(JAVA_KEY_STORE);
        keyStore.load(null);
        if (keyStore.isKeyEntry(KEY_ALIAS)) {
            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null);
            secretKey = secretKeyEntry.getSecretKey();
        } else {
            secretKey = createKey();
        }
        return secretKey;
    }


    /**
     * create key
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey createKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(JAVA_ALGORITHM, JAVA_KEY_STORE);
        KeyGenParameterSpec spec = new KeyGenParameterSpec
                .Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(JAVA_BLOCK_MODE)
                .setEncryptionPaddings(JAVA_PADDING)
                .setUserAuthenticationRequired(true)
                .build();
        keyGenerator.init(spec);
        return keyGenerator.generateKey();
    }
}