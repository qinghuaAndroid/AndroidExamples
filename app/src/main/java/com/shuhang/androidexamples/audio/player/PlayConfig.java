package com.shuhang.androidexamples.audio.player;

import android.content.Context;

public class PlayConfig {
    final Context mContext;

    final String mPath;

    final String mAssetFileName;

    final boolean mLooping;

    final long mIntervalTime;

    private PlayConfig(Builder builder) {
        mContext = builder.mContext;
        mPath = builder.mPath;
        mAssetFileName = builder.mAssetFileName;
        mLooping = builder.mLooping;
        mIntervalTime = builder.mIntervalTime;
    }

    public static class Builder {
        Context mContext;

        String mPath;

        String mAssetFileName;

        boolean mLooping;

        long mIntervalTime;

        /**
         * @param context –
         */
        public Builder context(Context context) {
            mContext = context;
            return this;
        }

        /**
         * @param path – 本地文件的路径，或要播放的URL
         */
        public Builder path(String path) {
            mPath = path;
            return this;
        }

        /**
         * @param assetFileName – asset文件的名称
         */
        public Builder assetFileName(String assetFileName) {
            mAssetFileName = assetFileName;
            return this;
        }

        /**
         * @param looping 是否循环
         */
        public Builder looping(boolean looping) {
            mLooping = looping;
            return this;
        }

        /**
         * @param intervalTime 播放进度反馈间隔
         */
        public Builder intervalTime(long intervalTime) {
            mIntervalTime = intervalTime;
            return this;
        }

        public PlayConfig build() {
            return new PlayConfig(this);
        }
    }
}
