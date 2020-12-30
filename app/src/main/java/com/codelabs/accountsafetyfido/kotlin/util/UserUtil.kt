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
import android.text.TextUtils
import android.util.Log
import com.codelabs.accountsafetyfido.kotlin.bean.UserBean
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

/**
 * Created by CodeLab
 */
object UserUtil {

    @JvmStatic
    fun getLocalUser(context: Context?, openId: String?): UserBean? {
        val localStr = SPUtil[context, openId, ""] as String?
        if (TextUtils.isEmpty(localStr)) {
            return null
        }
        try {
            return Gson().fromJson(localStr, UserBean::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e("UserUtil", "onInit LocalUser json format error")
        }
        return null
    }
}