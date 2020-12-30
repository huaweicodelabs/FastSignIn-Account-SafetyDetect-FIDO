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
package com.codelabs.accountsafetyfido.kotlin.weight

import com.codelabs.accountsafetyfido.R
import kotlinx.android.synthetic.main.dialog_fingerprint_close.*

/**
 * Created by CodeLab
 */
class FpCloseDialogFg : BaseDialogFg() {
    private var mListener: IFingerprintListener? = null
    override fun initLayoutContent(): Int {
        return R.layout.dialog_fingerprint_close
    }

    override fun onInitView() {
        fingerprint_close_cancel.setOnClickListener {
            dismiss()
            mListener!!.onFingerprintListener(0)
        }
        fingerprint_close_confirm.setOnClickListener {
            dismiss()
            mListener!!.onFingerprintListener(1)
        }
    }

    /**
     * Listener
     */
    fun setListener(listener: IFingerprintListener?) {
        mListener = listener
    }

    companion object {
        @JvmStatic
        fun newInstance(): FpCloseDialogFg {
            return FpCloseDialogFg()
        }
    }
}