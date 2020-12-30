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
package com.codelabs.accountsafetyfido.java.weight;

import android.view.View;

import com.codelabs.accountsafetyfido.R;

/**
 * Created by CodeLab
 */
public class FpCloseDialogFg extends BaseDialogFg {
    private IFingerprintListener mListener;

    public static FpCloseDialogFg newInstance() {
        return new FpCloseDialogFg();
    }

    @Override
    protected int initLayoutContent() {
        return R.layout.dialog_fingerprint_close;
    }

    @Override
    protected void onInitView() {
        mView.findViewById(R.id.fingerprint_close_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null) {
                    mListener.onFingerprintListener(0);
                }
            }
        });

        mView.findViewById(R.id.fingerprint_close_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mListener != null) {
                    mListener.onFingerprintListener(1);
                }
            }
        });
    }

    /**
     * Listener
     */
    public void setListener(IFingerprintListener listener) {
        this.mListener = listener;
    }
}