package com.shuhang.androidexamples.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.blankj.utilcode.util.LogUtils

class LifecycleChecker : LifecycleEventObserver {

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_START) LogUtils.d("应用进入前台")
        else if (event == Lifecycle.Event.ON_STOP) LogUtils.d("应用进入后台")
    }
}