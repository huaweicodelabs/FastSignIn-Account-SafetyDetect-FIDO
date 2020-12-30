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
package com.codelabs.accountsafetyfido.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.codelabs.accountsafetyfido.R
import com.codelabs.accountsafetyfido.kotlin.bean.UserBean
import com.codelabs.accountsafetyfido.kotlin.common.SPConstants
import com.codelabs.accountsafetyfido.kotlin.util.SPUtil
import com.codelabs.accountsafetyfido.kotlin.util.UserUtil
import com.codelabs.accountsafetyfido.kotlin.util.fingerprint.FingerprintHelper
import com.codelabs.accountsafetyfido.kotlin.util.fingerprint.KeyHelper
import com.codelabs.accountsafetyfido.kotlin.weight.FpCloseDialogFg
import com.codelabs.accountsafetyfido.kotlin.weight.FpOpenDialogFg
import com.codelabs.accountsafetyfido.kotlin.weight.IFingerprintListener
import com.huawei.hms.support.api.fido.bioauthn.*
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import kotlinx.android.synthetic.main.home_act.*

/**
 * Created by CodeLab
 */
class HomeAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_act)
        home_exit.setOnClickListener { onExitLogin() }
        onInitialize()
    }

    /**
     * init data
     */
    private fun onInitialize() {
        val openId = SPUtil[this, SPConstants.KEY_OPENID, ""] as String
        if (TextUtils.isEmpty(openId)) {
            finish()
            return
        }
        hone_openid.text = openId
        val userBean: UserBean? = UserUtil.getLocalUser(this, openId)
        home_displayName.text = userBean!!.displayName
        // avatar
        Glide.with(this).asBitmap().skipMemoryCache(true)
            .load(userBean.avatar).error(R.mipmap.ic_launcher_round)
            .into(home_avatar)
        onFingerprintCheck()
    }

    /**
     * Fingerprint Check
     */
    private fun onFingerprintCheck() {
        // check fingerprint is support
        val bioAuthnManager = BioAuthnManager(this)
        val errorCode = bioAuthnManager.canAuth()
        if (errorCode != 0) {
            // not support
            home_fingerprint_content.visibility = View.GONE
            SPUtil.put(this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, false)
            return
        }
        val isOpen = SPUtil[this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, false] as Boolean
        home_fingerprint.isChecked = isOpen

        home_fingerprint.setOnClickListener {
            if (home_fingerprint.isChecked) {
                // open fingerprint
                onFingerprintOpenCheck();
            } else {
                // close fingerprint
                onFingerprintCloseCheck();
            }
        }
    }

    /**
     * open fingerprint check
     */
    private fun onFingerprintOpenCheck() {
        val fpOpenDialogFg = FpOpenDialogFg.newInstance()
        fpOpenDialogFg.setListener(object : IFingerprintListener {
            override fun onFingerprintListener(type: Int) {
                if (type == 0) {
                    onFingerprintOpenResult(false);
                } else {
                    onFingerprintOpen()
                }
            }
        })
        fpOpenDialogFg.show(supportFragmentManager, "fpOpenDialogFg")
    }

    /**
     * open fingerprint result
     */
    private fun onFingerprintOpenResult(isSuccess: Boolean) {
        SPUtil.put(this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, isSuccess)
        home_fingerprint.isChecked = isSuccess
    }

    /**
     * close fingerprint check
     */
    private fun onFingerprintCloseCheck() {
        val fpCloseDialogFg = FpCloseDialogFg.newInstance()
        fpCloseDialogFg.setListener(object : IFingerprintListener {
            override fun onFingerprintListener(type: Int) {
                if (type == 0) {
                    home_fingerprint.isChecked = true
                } else {
                    onCloseFingerprint()
                }
            }
        })
        fpCloseDialogFg.show(supportFragmentManager, "fpCloseDialogFg")
    }

    /**
     * close fingerprint
     */
    private fun onCloseFingerprint() {
        FingerprintHelper.delete(this, SPConstants.KEY_SAVE_IV)
        FingerprintHelper.delete(this, SPConstants.KEY_SAVE_ENCODE)
        SPUtil.remove(this, SPConstants.FINGER_PRINT_LOGIN_SWITCH)
        home_fingerprint.isChecked = false
    }

    /**
     * open fingerprint
     */
    private fun onFingerprintOpen() {
        val bioAuthnPrompt = BioAuthnPrompt(this, ContextCompat.getMainExecutor(this), authCallback)
        val builder = BioAuthnPrompt.PromptInfo.Builder()
            .setTitle("This is the title.")
            .setSubtitle("This is the subtitle")
            .setDescription("This is the description")
        builder.setNegativeButtonText("This is the 'Cancel' button.")
        val info = builder.build()
        // get cryptoObject
        val cryptoObject = KeyHelper.getInstance().getEncryptCipher()?.let { CryptoObject(it) }
        if (cryptoObject != null) {
            bioAuthnPrompt.auth(info, cryptoObject)
        }
    }

    /**
     * Exit Login
     */
    private fun onExitLogin() {
        val authParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setIdToken().createParams()
        val mAuthManager = HuaweiIdAuthManager.getService(this, authParams)
        val signOutTask = mAuthManager!!.signOut()
        signOutTask.addOnSuccessListener {
            Log.i("exitLogin", "signOut Success")
            onClearExitData()
        }.addOnFailureListener {
            Log.e("exitLogin", "signOut fail")
            onClearExitData()
        }
    }

    /**
     * clear exit data
     */
    private fun onClearExitData() {
        val openId = SPUtil[this, SPConstants.KEY_OPENID, ""] as String
        SPUtil.remove(this, openId)
        SPUtil.remove(this, SPConstants.KEY_OPENID)
        finish()
    }

    // call back
    private var authCallback: BioAuthnCallback = object : BioAuthnCallback() {
        override fun onAuthError(errMsgId: Int, errString: CharSequence) {
            // TODO Auth Error
            Log.e("authCallback", "auth error : $errString")
            onFingerprintOpenResult(false);
        }

        override fun onAuthSucceeded(result: BioAuthnResult) {
            // Auth Success
            try {
                val cryptoObject = result.cryptoObject
                if (null == cryptoObject) {
                    Log.e("authCallback", "auth success cryptoObject is null")
                    onFingerprintOpenResult(false);
                    return
                }
                val cipher = result.cryptoObject!!.cipher
                if (null == cipher) {
                    Log.e("authCallback", "auth success cipher is null")
                    onFingerprintOpenResult(false);
                    return
                }
                val openId = SPUtil[this@HomeAct, SPConstants.KEY_OPENID, ""] as String
                val bytes = cipher.doFinal(openId.toByteArray())
                // save Encode
                FingerprintHelper.put(this@HomeAct, SPConstants.KEY_SAVE_ENCODE, Base64.encodeToString(bytes, Base64.URL_SAFE))
                // save IV
                val iv = cipher.iv
                FingerprintHelper.put(this@HomeAct, SPConstants.KEY_SAVE_IV, Base64.encodeToString(iv, Base64.URL_SAFE))
                Log.i("authCallback", "auth success")
                onFingerprintOpenResult(true);
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onAuthFailed() {
            // TODO Auth Failed
            Log.e("authCallback", "auth failed")
            onFingerprintOpenResult(false);
        }
    }
}