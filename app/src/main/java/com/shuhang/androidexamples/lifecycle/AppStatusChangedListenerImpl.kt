package com.shuhang.androidexamples.lifecycle

import android.app.Activity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils

class AppStatusChangedListenerImpl : Utils.OnAppStatusChangedListener {

    override fun onForeground(activity: Activity?) {
        LogUtils.d("应用进入前台")
    }

    override fun onBackground(activity: Activity?) {
        LogUtils.d("应用进入后台")
    }
}