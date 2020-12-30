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
package com.codelabs.accountsafetyfido.kotlin.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * Created by CodeLab
 * Description: safe SharedPreferences read write
 */
object SPUtil {

    // save file name
    private const val KOTLIN_NAME = "kotlin_data"
    private const val TAG = "SPUtil"

    /**
     * save value
     *
     * @param context context
     * @param key     key
     * @param objectValue  object
     */
    fun put(context: Context?, key: String?, objectValue: Any) {
        if (null == context) {
            Log.e(TAG, "put context is null")
            return
        }
        val sp = context.getSharedPreferences(KOTLIN_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        when (objectValue) {
            is String -> editor.putString(key, objectValue)
            is Int -> editor.putInt(key, objectValue)
            is Boolean -> editor.putBoolean(key, objectValue)
            is Float -> editor.putFloat(key, objectValue)
            is Long -> editor.putLong(key, objectValue)
            else -> editor.putString(key, objectValue.toString())
        }
        SharedPreferencesCompat.apply(editor)
    }

    /**
     * get value
     *
     * @param context       context
     * @param key           key
     * @param defaultObject defaultObject
     * @return Object
     */
    operator fun get(context: Context?, key: String?, defaultObject: Any?): Any? {
        return SPUtil[context, KOTLIN_NAME, key, defaultObject]
    }

    /**
     * get value
     *
     * @param context       context
     * @param spName        spName
     * @param key           key
     * @param defaultObject defaultObject
     * @return Object
     */
    operator fun get(context: Context?, spName: String?, key: String?, defaultObject: Any?): Any? {
        if (null == context) {
            Log.e(TAG, "get context is null")
            return defaultObject
        }
        val sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
        return when (defaultObject) {
            is String -> sp.getString(key, defaultObject)
            is Int -> sp.getInt(key, defaultObject)
            is Boolean -> sp.getBoolean(key, defaultObject)
            is Float -> sp.getFloat(key, defaultObject)
            is Long -> sp.getLong(key, defaultObject)
            else -> sp.getString(key, defaultObject as String?)
        }
    }

    /**
     * remove value
     *
     * @param context context
     * @param key     key
     */
    fun remove(context: Context, key: String?) {
        val sp = context.getSharedPreferences(KOTLIN_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.remove(key)
        SharedPreferencesCompat.apply(editor)
    }

    /**
     * check key
     *
     * @param context context
     * @param key     key
     * @return boolean
     */
    fun contains(context: Context?, key: String?): Boolean {
        return contains(context, KOTLIN_NAME, key)
    }

    /**
     * check key
     *
     * @param context context
     * @param spName  spName
     * @param key     key
     * @return boolean
     */
    fun contains(context: Context?, spName: String?, key: String?): Boolean {
        if (null != context) {
            val sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE)
            if (null != sp) {
                return sp.contains(key)
            }
        }
        return false
    }

    /**
     * create SharedPreferencesCompat.apply
     */
    private object SharedPreferencesCompat {
        private val APPLY_METHOD = findApplyMethod()

        /**
         * apply method
         */
        private fun findApplyMethod(): Method? {
            try {
                val clz: Class<*> = SharedPreferences.Editor::class.java
                return clz.getMethod("apply")
            } catch (e: NoSuchMethodException) {
                Log.e(TAG, "NoSuchMethod Exception")
            }
            return null
        }

        /**
         * user apply or commit
         *
         * @param editor SharedPreferences.Editor
         */
        fun apply(editor: SharedPreferences.Editor) {
            try {
                if (APPLY_METHOD != null) {
                    APPLY_METHOD.invoke(editor)
                    return
                }
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, "IllegalArgument Exception")
            } catch (e: IllegalAccessException) {
                Log.e(TAG, "IllegalAccess Exception")
            } catch (e: InvocationTargetException) {
                Log.e(TAG, "InvocationTarget Exception")
            }
            editor.commit()
        }
    }
}