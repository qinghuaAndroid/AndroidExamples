package com.shuhang.androidexamples

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.kunminx.architecture.ui.callback.UnPeekLiveData

class GlobalViewModel(application: Application) : AndroidViewModel(application) {

    private val mToast = UnPeekLiveData<String>()

    fun getToast(): UnPeekLiveData<String> {
        return mToast
    }

    fun toast(message: String) {
        mToast.postValue(message)
    }
}