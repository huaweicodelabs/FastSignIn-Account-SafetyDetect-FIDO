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

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.codelabs.accountsafetyfido.R
import com.codelabs.accountsafetyfido.kotlin.bean.UserBean
import com.codelabs.accountsafetyfido.kotlin.common.SPConstants
import com.codelabs.accountsafetyfido.kotlin.util.SPUtil
import com.codelabs.accountsafetyfido.kotlin.util.UserUtil
import com.codelabs.accountsafetyfido.kotlin.util.fingerprint.FingerprintHelper
import com.codelabs.accountsafetyfido.kotlin.util.fingerprint.KeyHelper.Companion.getInstance
import com.google.gson.Gson
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.result.AuthAccount
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnCallback
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnPrompt
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnResult
import com.huawei.hms.support.api.fido.bioauthn.CryptoObject
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import kotlinx.android.synthetic.main.login_act.*
import java.util.*


/**
 * Created by CodeLab
 */
class LoginAct : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_act)
        login_huaweiAccount.setOnClickListener { onSafetyCheck(1) }
        login_fingerprint.setOnClickListener { onSafetyCheck(2) }
        onInitialize()
    }

    /**
     * init data
     */
    private fun onInitialize() {
        initSafetyDetect()
        onFingerprintStatusCheck()
    }

    /**
     * init safety detect
     */
    private fun initSafetyDetect() {
        // init SafetyDetect
        val client = SafetyDetect.getClient(this)
        client.initUserDetect().addOnSuccessListener {
            // init success
        }.addOnFailureListener {
            // init fail
        }
    }

    /**
     * fingerprint status check
     */
    private fun onFingerprintStatusCheck() {
        val isOpenFingerprint = SPUtil[this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, false] as Boolean
        login_fingerprint.visibility = if (isOpenFingerprint) View.VISIBLE else View.GONE
        if (isOpenFingerprint) {
            onSafetyCheck(2)
        }
    }

    /**
     * Login Safety Check
     */
    private fun onSafetyCheck(type: Int) {
        val appId = AGConnectServicesConfig.fromContext(this).getString("client/app_id")
        val client = SafetyDetect.getClient(this)
        client.userDetection(appId).addOnSuccessListener {
            // String responseToken = userDetectResponse.getResponseToken();
            // if (!responseToken.isEmpty()) {
            // Send the response token to your app server, and call the cloud API of
            // HMS Core on your server to obtain the fake user detection result.
            // }
            onLoginSelected(type)
        }.addOnFailureListener {
            Log.e(TAG, "user detect fail")
            onLoginSelected(type)
        }
    }

    /**
     * login selected
     */
    private fun onLoginSelected(type: Int) {
        if (type == 1) {
            onHuaweiAccountLogin()
        } else {
            onFingerprintLogin()
        }
    }

    /**
     * HUAWEI Account Login in
     */
    private fun onHuaweiAccountLogin() {
        val authParams: AccountAuthParams = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setIdToken().createParams()
        val mAuthService: AccountAuthService = AccountAuthManager.getService(this, authParams)
        startActivityForResult(mAuthService.signInIntent, REQUEST_CODE)
    }

    /**
     * HUAWEI Account Login success
     */
    private fun onHuaweiIdLoginSuccess(authAccount: AuthAccount) {
        val openId = authAccount.openId
        Log.i(TAG, "OpenId : $openId")
        // is login
        SPUtil.put(this, SPConstants.KEY_OPENID, openId)
        var userBean: UserBean? = UserUtil.getLocalUser(this, openId)
        if (userBean == null) {
            userBean = UserBean(authAccount.displayName, authAccount.avatarUriString)
            val localStr = Gson().toJson(userBean)
            SPUtil.put(this, openId, localStr)
        }
        startActivity(Intent(this, HomeAct::class.java))
        finish()
    }

    /**
     * Fingerprint login
     */
    private fun onFingerprintLogin() {
        val bioAuthnPrompt = BioAuthnPrompt(this, ContextCompat.getMainExecutor(this), loginCallback)
        val builder = BioAuthnPrompt.PromptInfo.Builder().setTitle("This is the title.")
            .setSubtitle("This is the subtitle")
            .setDescription("This is the description")
        builder.setNegativeButtonText("This is the 'Cancel' button.")
        val info = builder.build()
        // save IV
        val ivStr = FingerprintHelper[this, SPConstants.KEY_SAVE_IV]
        val iv = Base64.decode(ivStr, Base64.URL_SAFE)
        val cryptoObject = CryptoObject(getInstance().getDecryptCipher(iv)!!)
        bioAuthnPrompt.auth(info, cryptoObject)
    }

    /**
     * fingerprint login success
     */
    private fun onFingerprintLoginSuccess(openId: String) {
        val userBean = UserUtil.getLocalUser(this, openId)
        if (null == userBean) {
            onHuaweiAccountLogin()
            return
        }
        SPUtil.put(this, SPConstants.KEY_OPENID, openId)
        startActivity(Intent(this, HomeAct::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
            if (authAccountTask.isSuccessful) {
                //login success
                val authAccount = authAccountTask.result
                onHuaweiIdLoginSuccess(authAccount)
            } else {
                Log.e(TAG, "sign in failed : " + (authAccountTask.exception as ApiException).statusCode)
            }
        }
    }

    // call back
    private var loginCallback: BioAuthnCallback = object : BioAuthnCallback() {
        override fun onAuthError(errMsgId: Int, errString: CharSequence) {
            Log.e("loginCallback", "login auth error : $errString")
        }

        override fun onAuthSucceeded(result: BioAuthnResult) {
            try {
                val cryptoObject = result.cryptoObject
                if (null == cryptoObject) {
                    Log.e("loginCallback", "auth success cryptoObject is null")
                    return
                }
                val cipher = result.cryptoObject!!.cipher
                if (null == cipher) {
                    Log.e(TAG, "cipher is null")
                    return
                }
                val saveEncode = FingerprintHelper[this@LoginAct, SPConstants.KEY_SAVE_ENCODE]
                val input = Base64.decode(saveEncode, Base64.URL_SAFE)
                val bytes = cipher.doFinal(input)
                val saveString = String(bytes)
                Log.i(TAG, "auth success save openId：$saveString")
                onFingerprintLoginSuccess(saveString)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onAuthFailed() {
            Log.e("loginCallback", "auth failed")
        }
    }

    companion object {
        private const val TAG = "--LoginAct--"
        private const val REQUEST_CODE = 1001
    }
}