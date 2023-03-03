package com.shuhang.androidexamples.exoplayer;

import android.content.Context;
import android.os.Environment;

import com.blankj.utilcode.util.Utils;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.cache.CacheEvictor;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;

/**
 * Created by Alexis.Shelton on 11/5/2021.
 */
public class ExoPlayerCache {

    public SimpleCache getSimpleCache() {
        return simpleCache;
    }

    private final SimpleCache simpleCache;

    private ExoPlayerCache() {
        // 获取缓存文件夹
        Context context = Utils.getApp().getApplicationContext();
        File file = context.getExternalFilesDir(Environment.DIRECTORY_DCIM);
        file = file == null ? context.getExternalCacheDir() : file;
        file = file == null ? context.getCacheDir() : file;
        CacheEvictor cacheEvictor = new LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024);
        simpleCache = new SimpleCache(file, cacheEvictor, new ExoDatabaseProvider(context));
    }

    public static ExoPlayerCache get() {
        return InstanceHolder.receivers;
    }

    private static class InstanceHolder {
        static ExoPlayerCache receivers = new ExoPlayerCache();
    }
}
