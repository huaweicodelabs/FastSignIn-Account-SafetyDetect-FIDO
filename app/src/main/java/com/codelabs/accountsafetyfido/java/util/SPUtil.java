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
package com.codelabs.accountsafetyfido.java.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by CodeLab
 * Description: safe SharedPreferences read write
 */
public class SPUtil {

    // save file name
    public static final String JAVA_NAME = "java_data";

    private static final String TAG = "SPUtil";

    /**
     * save value
     *
     * @param context context
     * @param key     key
     * @param object  object
     */
    public static void put(Context context, String key, Object object) {
        put(context, JAVA_NAME, key, object);
    }

    /**
     * save value
     *
     * @param context context
     * @param spName  spName
     * @param key     key
     * @param object  object
     */
    public static void put(Context context, String spName, String key, Object object) {
        if (null == context) {
            Log.e(TAG, "put context is null");
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (object instanceof String) {
            editor.putString(key, (String) object);
        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);
        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);
        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);
        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);
        } else {
            editor.putString(key, object.toString());
        }

        SharedPreferencesCompat.apply(editor);
    }

    /**
     * get value
     *
     * @param context       context
     * @param key           key
     * @param defaultObject defaultObject
     * @return Object
     */
    public static Object get(Context context, String key, Object defaultObject) {
        return get(context, JAVA_NAME, key, defaultObject);
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
    public static Object get(Context context, String spName, String key, Object defaultObject) {
        if (null == context) {
            Log.e(TAG, "get context is null");
            return defaultObject;
        }
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);

        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);
        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);
        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);
        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);
        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        } else {
            return defaultObject;
        }
    }

    /**
     * remove value
     *
     * @param context context
     * @param key     key
     */
    public static void remove(Context context, String key) {
        remove(context, JAVA_NAME, key);
    }

    /**
     * remove value
     *
     * @param context context
     * @param spName  spName
     * @param key     key
     */
    public static void remove(Context context, String spName, String key) {
        SharedPreferences sp = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        SharedPreferencesCompat.apply(editor);
    }

    /**
     * create SharedPreferencesCompat.apply
     */
    private static class SharedPreferencesCompat {
        private static final Method APPLY_METHOD = findApplyMethod();

        /**
         * apply method
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = SharedPreferences.Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
                Log.e(TAG, "NoSuchMethod Exception");
            }

            return null;
        }

        /**
         * user apply or commit
         *
         * @param editor SharedPreferences.Editor
         */
        public static void apply(SharedPreferences.Editor editor) {
            try {
                if (APPLY_METHOD != null) {
                    APPLY_METHOD.invoke(editor);
                    return;
                }
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgument Exception");
            } catch (IllegalAccessException e) {
                Log.e(TAG, "IllegalAccess Exception");
            } catch (InvocationTargetException e) {
                Log.e(TAG, "InvocationTarget Exception");
            }
            editor.commit();
        }
    }
}