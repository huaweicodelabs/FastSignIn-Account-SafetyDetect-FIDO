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

import android.content.Context
import java.io.*

/**
 * Created by CodeLab
 */
object FingerprintHelper {

    /**
     * save String data
     *
     * @param key   save key
     * @param value save value
     */
    @JvmStatic
    fun put(context: Context, key: String, value: String?) {
        val file = File(context.filesDir, key.hashCode().toString() + "")
        var out: BufferedWriter? = null
        try {
            out = BufferedWriter(FileWriter(file), 1024)
            out.write(value)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                out!!.flush()
                out.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * get data
     *
     * @param key save key
     * @return String value
     */
    @JvmStatic
    operator fun get(context: Context, key: String): String? {
        val file = File(context.filesDir, key.hashCode().toString() + "")
        if (!file.exists()) {
            return null
        }
        var bufferedReader: BufferedReader? = null
        return try {
            bufferedReader = BufferedReader(FileReader(file))
            val readString = StringBuilder()
            var currentLine: String?
            while (bufferedReader.readLine().also { currentLine = it } != null) {
                readString.append(currentLine)
            }
            readString.toString()
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            try {
                bufferedReader!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * delete data
     *
     * @param key save key
     */
    @JvmStatic
    fun delete(context: Context, key: String) {
        val file = File(context.filesDir, key.hashCode().toString() + "")
        if (file.exists()) {
            file.delete()
        }
    }
}