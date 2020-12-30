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

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.codelabs.accountsafetyfido.R;
import com.codelabs.accountsafetyfido.java.bean.UserBean;
import com.codelabs.accountsafetyfido.java.common.SPConstants;
import com.codelabs.accountsafetyfido.java.util.SPUtil;
import com.codelabs.accountsafetyfido.java.util.UserUtil;
import com.codelabs.accountsafetyfido.java.util.fingerprint.FingerprintHelper;
import com.codelabs.accountsafetyfido.java.util.fingerprint.KeyHelper;
import com.codelabs.accountsafetyfido.java.weight.FpCloseDialogFg;
import com.codelabs.accountsafetyfido.java.weight.FpOpenDialogFg;
import com.codelabs.accountsafetyfido.java.weight.IFingerprintListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnCallback;
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnManager;
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnPrompt;
import com.huawei.hms.support.api.fido.bioauthn.BioAuthnResult;
import com.huawei.hms.support.api.fido.bioauthn.CryptoObject;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import javax.crypto.Cipher;

/**
 * Created by CodeLab
 */
public class HomeAct extends AppCompatActivity {
    private static final String TAG = "--HomeAct--";

    // avatar imageView
    private AppCompatImageView mIvAvatar;
    // display textView
    private AppCompatTextView mTvDisplayName;
    // openId textView
    private AppCompatTextView mTvOpenId;
    // Fingerprint content
    private LinearLayoutCompat mLlFingerprint;
    // Fingerprint switch button
    private SwitchCompat mSwitchCompat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_act);
        onInitView();
        onInitialize();
    }

    /**
     * init view
     */
    private void onInitView() {
        mIvAvatar = findViewById(R.id.home_avatar);
        mTvDisplayName = findViewById(R.id.home_displayName);
        mTvOpenId = findViewById(R.id.hone_openid);
        mLlFingerprint = findViewById(R.id.home_fingerprint_content);
        mSwitchCompat = findViewById(R.id.home_fingerprint);
        findViewById(R.id.home_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onExitLogin();
            }
        });
    }

    /**
     * init data
     */
    private void onInitialize() {
        String openId = (String) SPUtil.get(this, SPConstants.KEY_OPENID, "");
        if (TextUtils.isEmpty(openId)) {
            finish();
            return;
        }
        mTvOpenId.setText(openId);
        UserBean userBean = UserUtil.getLocalUser(this, openId);
        if (userBean == null) {
            return;
        }
        mTvDisplayName.setText(userBean.getDisplayName());
        // avatar
        Glide.with(this)
                .asBitmap()
                .skipMemoryCache(true)
                .load(userBean.getAvatar())
                .error(R.mipmap.ic_launcher_round)
                .into(mIvAvatar);

        onFingerprintCheck();
    }

    /**
     * Fingerprint Check
     */
    private void onFingerprintCheck() {
        // check fingerprint is support
        BioAuthnManager bioAuthnManager = new BioAuthnManager(this);
        int errorCode = bioAuthnManager.canAuth();
        if (errorCode != 0) {
            // not support
            mLlFingerprint.setVisibility(View.GONE);
            SPUtil.put(HomeAct.this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, false);
            return;
        }
        mLlFingerprint.setVisibility(View.VISIBLE);
        boolean isOpen = (boolean) SPUtil.get(HomeAct.this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, false);
        mSwitchCompat.setChecked(isOpen);
        mSwitchCompat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSwitchCompat.isChecked()) {
                    // open fingerprint
                    onFingerprintOpenCheck();
                } else {
                    // close fingerprint
                    onFingerprintCloseCheck();
                }
            }
        });
    }

    /**
     * open fingerprint check
     */
    private void onFingerprintOpenCheck() {
        FpOpenDialogFg fpOpenDialogFg = FpOpenDialogFg.newInstance();
        fpOpenDialogFg.setListener(new IFingerprintListener() {
            @Override
            public void onFingerprintListener(int type) {
                if (type == 0) {
                    onFingerprintOpenResult(false);
                } else {
                    onFingerprintOpen();
                }
            }
        });
        fpOpenDialogFg.show(getSupportFragmentManager(), "fpOpenDialogFg");
    }

    /**
     * open fingerprint
     */
    private void onFingerprintOpen() {
        BioAuthnPrompt bioAuthnPrompt = new BioAuthnPrompt(this, ContextCompat.getMainExecutor(this), authCallback);
        BioAuthnPrompt.PromptInfo.Builder builder = new BioAuthnPrompt.PromptInfo.Builder()
                .setTitle("This is the title.")
                .setSubtitle("This is the subtitle")
                .setDescription("This is the description");
        builder.setNegativeButtonText("This is the 'Cancel' button.");
        BioAuthnPrompt.PromptInfo info = builder.build();
        // get cryptoObject
        CryptoObject cryptoObject = new CryptoObject(KeyHelper.getInstance().getEncryptCipher());
        bioAuthnPrompt.auth(info, cryptoObject);
    }

    /**
     * open fingerprint result
     */
    private void onFingerprintOpenResult(boolean isSuccess) {
        SPUtil.put(HomeAct.this, SPConstants.FINGER_PRINT_LOGIN_SWITCH, isSuccess);
        mSwitchCompat.setChecked(isSuccess);
    }

    /**
     * close fingerprint check
     */
    private void onFingerprintCloseCheck() {
        FpCloseDialogFg fpCloseDialogFg = FpCloseDialogFg.newInstance();
        fpCloseDialogFg.setListener(new IFingerprintListener() {
            @Override
            public void onFingerprintListener(int type) {
                if (type == 0) {
                    mSwitchCompat.setChecked(true);
                } else {
                    onCloseFingerprint();
                }
            }
        });
        fpCloseDialogFg.show(getSupportFragmentManager(), "fpCloseDialogFg");
    }

    /**
     * close fingerprint
     */
    private void onCloseFingerprint() {
        FingerprintHelper.delete(this, SPConstants.KEY_SAVE_IV);
        FingerprintHelper.delete(this, SPConstants.KEY_SAVE_ENCODE);
        SPUtil.remove(HomeAct.this, SPConstants.FINGER_PRINT_LOGIN_SWITCH);
        mSwitchCompat.setChecked(false);
    }

    /**
     * Exit Login
     */
    private void onExitLogin() {
        HuaweiIdAuthParams authParams = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                .setIdToken().createParams();
        HuaweiIdAuthService mAuthManager = HuaweiIdAuthManager.getService(this, authParams);

        Task<Void> signOutTask = mAuthManager.signOut();
        signOutTask.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i(TAG, "signOut Success");
                onClearExitData();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "signOut fail");
                onClearExitData();
            }
        });
    }

    /**
     * clear exit data
     */
    private void onClearExitData() {
        String openId = (String) SPUtil.get(this, SPConstants.KEY_OPENID, "");
        SPUtil.remove(this, openId);
        SPUtil.remove(this, SPConstants.KEY_OPENID);
        finish();
    }

    // call back
    BioAuthnCallback authCallback = new BioAuthnCallback() {
        @Override
        public void onAuthError(int errMsgId, @NonNull CharSequence errString) {
            // TODO Auth Error
            Log.e(TAG, "auth error : " + errString);
            onFingerprintOpenResult(false);
        }

        @Override
        public void onAuthSucceeded(@NonNull BioAuthnResult result) {
            // Auth Success
            try {
                CryptoObject cryptoObject = result.getCryptoObject();
                if (null == cryptoObject) {
                    Log.e(TAG, "auth success cryptoObject is null");
                    onFingerprintOpenResult(false);
                    return;
                }
                Cipher cipher = result.getCryptoObject().getCipher();
                if (null == cipher) {
                    Log.e(TAG, "auth success cipher is null");
                    onFingerprintOpenResult(false);
                    return;
                }
                String openId = (String) SPUtil.get(HomeAct.this, SPConstants.KEY_OPENID, "");
                byte[] bytes = cipher.doFinal(openId.getBytes());
                // save Encode
                FingerprintHelper.put(HomeAct.this, SPConstants.KEY_SAVE_ENCODE, Base64.encodeToString(bytes, Base64.URL_SAFE));
                // save IV
                byte[] iv = cipher.getIV();
                FingerprintHelper.put(HomeAct.this, SPConstants.KEY_SAVE_IV, Base64.encodeToString(iv, Base64.URL_SAFE));
                Log.i(TAG, "auth success");
                onFingerprintOpenResult(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAuthFailed() {
            // TODO Auth Failed
            Log.e(TAG, "auth failed");
            onFingerprintOpenResult(false);
        }
    };
}