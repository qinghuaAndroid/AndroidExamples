package com.shuhang.androidexamples

import android.app.Application
import android.graphics.Color
import android.net.http.HttpResponseCache
import android.view.Gravity
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.shuhang.androidexamples.lifecycle.AppStatusChangedListenerImpl
import com.shuhang.androidexamples.lifecycle.LifecycleChecker
import java.io.File

class App : Application(), ViewModelStoreOwner {

    private lateinit var mAppViewModelStore: ViewModelStore

    override fun onCreate() {
        super.onCreate()

        val cacheDir = File(applicationContext.cacheDir, "http")
        HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)

        DeviceUtils.getUniqueDeviceId()
        LogUtils.getConfig().globalTag = "LogUtils"

        ToastUtils.getDefaultMaker().setBgResource(R.drawable.bg_toast_view).setTextSize(13)
            .setTextColor(Color.WHITE).setDurationIsLong(false)
            .setGravity(Gravity.CENTER, 0, 0)

        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleChecker())
        AppUtils.registerAppStatusChangedListener(AppStatusChangedListenerImpl())

        mAppViewModelStore = ViewModelStore()
    }

    override fun getViewModelStore(): ViewModelStore {
        return mAppViewModelStore
    }
}