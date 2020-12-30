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
package com.codelabs.accountsafetyfido.java;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import com.codelabs.accountsafetyfido.R;
import com.codelabs.accountsafetyfido.java.bean.UserBean;
import com.codelabs.accountsafetyfido.java.common.SPConstants;
import com.codelabs.accountsafetyfido.java.util.SPUtil;
import com.codelabs.accountsafetyfido.java.util.UserUtil;
import com.codelabs.accountsafetyfido.java.util.fingerprint.FingerprintHelper;
import com.codelabs.accountsafetyfido.java.util.fingerprint.KeyHelper;
import com.google.gson.Gson;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.safetydetect.UserDetectResponse;
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnCallback;
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnPrompt;
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnResult;
import com.huawei.hms.support.api.fido.bioauthn.CryptoObject;
import com.huawei.hms.support.api.safetydetect.SafetyDetect;
import com.huawei.hms.support.api.safetydetect.SafetyDetectClient;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import javax.crypto.Cipher;

/**
 * Created by CodeLab
 */
public class LoginAct extends AppCompatActivity {
    private static final String TAG = "--LoginAct--";

    private static final int REQUEST_CODE = 1001;

    private AppCompatTextView mTvFingerprint;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_act);
        onInitView();
        onInitialize();
    }

    /**
     * init view
     */
    private void onInitView() {
        mTvFingerprint = findViewById(R.id.login_fingerprint);
        mTvFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSafetyCheck(2);
            }
        });

        findViewById(R.id.login_huaweiAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSafetyCheck(1);
            }
        });
    }

    /**
     * init data
     */
    private void onInitialize() {
        initSafetyDetect();
        onFingerprintStatusCheck();
    }

    /**
     * init safety detect
     */
    private void initSafetyDetect() {
        // init SafetyDetect
        SafetyDetectClient client = SafetyDetect.getClient(this);
        client.initUserDetect().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // init success
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                // init fail
            }
        });
    }

    /**
     * fingerprint status check
     */
    private void onFingerprintStatusCheck() {
        boolean isOpenFingerprint = (boolean) SPUtil.get(this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, false);
        mTvFingerprint.setVisibility(isOpenFingerprint ? View.VISIBLE : View.VISIBLE);
        if (isOpenFingerprint) {
            onSafetyCheck(2);
        }
    }

    /**
     * Login Safety Check
     */
    private void onSafetyCheck(int type) {
        String app_Id = AGConnectServicesConfig.fromContext(this).getString("client/app_id");
        SafetyDetectClient client = SafetyDetect.getClient(this);
        client.userDetection(app_Id).addOnSuccessListener(new OnSuccessListener<UserDetectResponse>() {
            @Override
            public void onSuccess(UserDetectResponse userDetectResponse) {
                // String responseToken = userDetectResponse.getResponseToken();
                // if (!responseToken.isEmpty()) {
                // Send the response token to your app server, and call the cloud API of
                // HMS Core on your server to obtain the fake user detection result.
                // }
                onLoginSelected(type);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "user detect fail");
                onLoginSelected(type);
            }
        });
    }

    /**
     * login selected
     */
    private void onLoginSelected(int type) {
        if (type == 1) {
            onHuaweiAccountLogin();
        } else {
            onFingerprintLogin();
        }
    }

    /**
     * Huawei Account Login in
     */
    private void onHuaweiAccountLogin() {
        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken().createParams();
        HuaweiIdAuthService mAuthService = HuaweiIdAuthManager.getService(this, authParams);
        // silent SignIn
        Task<AuthHuaweiId> task = mAuthService.silentSignIn();
        task.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId authHuaweiId) {
                onHuaweiIdLoginSuccess(authHuaweiId);
                Log.i(TAG, "silent signIn success");
            }
        });
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ApiException) {
                    ApiException apiException = (ApiException) e;
                    Log.e(TAG, "silent signIn error : " + apiException.getMessage());
                    if (apiException.getStatusCode() == 2002) {
                        startActivityForResult(mAuthService.getSignInIntent(), REQUEST_CODE);
                    }
                }
            }
        });
    }

    /**
     * Login success
     */
    private void onHuaweiIdLoginSuccess(AuthHuaweiId authHuaweiId) {
        String openId = authHuaweiId.getOpenId();
        Log.i(TAG, "OpenId : " + openId);
        SPUtil.put(this, SPConstants.KEY_OPENID, openId);
        // save user info
        UserBean userBean = UserUtil.getLocalUser(this, openId);
        if (userBean == null) {
            userBean = new UserBean();
            userBean.setAvatar(authHuaweiId.getAvatarUriString());
            userBean.setDisplayName(authHuaweiId.getDisplayName());
            String localStr = new Gson().toJson(userBean);
            SPUtil.put(this, openId, localStr);
        }
        startActivity(new Intent(this, HomeAct.class));
        finish();
    }

    /**
     * Fingerprint login
     */
    private void onFingerprintLogin() {
        BioAuthnPrompt bioAuthnPrompt = new BioAuthnPrompt(this, ContextCompat.getMainExecutor(this), loginCallback);
        BioAuthnPrompt.PromptInfo.Builder builder =
                new BioAuthnPrompt.PromptInfo.Builder().setTitle("This is the title.")
                        .setSubtitle("This is the subtitle")
                        .setDescription("This is the description");
        builder.setNegativeButtonText("This is the 'Cancel' button.");
        BioAuthnPrompt.PromptInfo info = builder.build();
        // save IV
        String ivStr = FingerprintHelper.get(this, SPConstants.KEY_SAVE_IV);
        byte[] iv = Base64.decode(ivStr, Base64.URL_SAFE);
        CryptoObject cryptoObject = new CryptoObject(KeyHelper.getInstance().getDecryptCipher(iv));
        bioAuthnPrompt.auth(info, cryptoObject);
    }

    /**
     * fingerprint login success
     */
    private void onFingerprintLoginSuccess(String openId) {
        UserBean userBean = UserUtil.getLocalUser(this, openId);
        if (null == userBean) {
            onHuaweiAccountLogin();
            return;
        }
        SPUtil.put(this, SPConstants.KEY_OPENID, openId);
        startActivity(new Intent(this, HomeAct.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                //login success
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                onHuaweiIdLoginSuccess(huaweiAccount);
            } else {
                Log.e(TAG, "sign in failed : " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
            }
        }
    }

    // call back
    BioAuthnCallback loginCallback = new BioAuthnCallback() {
        @Override
        public void onAuthError(int errMsgId, @NonNull CharSequence errString) {
            // TODO Login Auth Error
            Log.e("loginCallback", "login auth error : " + errString);
        }

        @Override
        public void onAuthSucceeded(@NonNull BioAuthnResult result) {
            try {
                CryptoObject cryptoObject = result.getCryptoObject();
                if (null == cryptoObject) {
                    Log.e(TAG, "auth success cryptoObject is null");
                    return;
                }
                Cipher cipher = result.getCryptoObject().getCipher();
                if (null == cipher) {
                    Log.e(TAG, "cipher is null");
                    return;
                }
                String saveEncode = FingerprintHelper.get(LoginAct.this, SPConstants.KEY_SAVE_ENCODE);
                byte[] input = Base64.decode(saveEncode, Base64.URL_SAFE);
                byte[] bytes = cipher.doFinal(input);
                String saveString = new String(bytes);
                Log.i(TAG, "auth success openId：" + saveString);
                onFingerprintLoginSuccess(saveString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAuthFailed() {
            // TODO Auth Failed
            Log.e(TAG, "auth failed");
        }
    };
}