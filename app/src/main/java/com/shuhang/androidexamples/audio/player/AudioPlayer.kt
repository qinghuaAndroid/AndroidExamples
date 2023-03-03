/*
 * Copyright 2020 Dmytro Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shuhang.androidexamples.audio.player

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log


class AudioPlayer : PlayerContract.Player, MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener,
    MediaPlayer.OnErrorListener {

    private var actionsListener: PlayerContract.PlayerCallback? = null
    private var mediaPlayer: MediaPlayer? = null
    private var playerState = PlayerState.STOPPED
    private var duration = 0
    private var intervalTime = 1000L
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        private const val TAG = "AudioPlayer"
    }

    override fun setPlayerCallback(callback: PlayerContract.PlayerCallback) {
        actionsListener = callback
    }

    @Throws(Exception::class)
    override fun play(playConfig: PlayConfig) {
        try {
            intervalTime = playConfig.mIntervalTime
            playerState = PlayerState.STOPPED
            mediaPlayer = MediaPlayer()
            setDataSource(playConfig)
            mediaPlayer?.isLooping = playConfig.mLooping
            mediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer?.setOnPreparedListener(this)
            mediaPlayer?.setOnCompletionListener(this)
            mediaPlayer?.setOnErrorListener(this)
            mediaPlayer?.prepareAsync()
        } catch (e: Exception) {
            Log.e(TAG, "play failed: ${e.message}")
            actionsListener?.onError(e)
            throw e
        }
    }

    @Throws(Exception::class)
    private fun setDataSource(playConfig: PlayConfig) {
        try {
            if (TextUtils.isEmpty(playConfig.mPath)) {
                val assetManager = playConfig.mContext.assets
                val fileDescriptor = assetManager.openFd(playConfig.mAssetFileName)
                mediaPlayer?.setDataSource(
                    fileDescriptor.fileDescriptor,
                    fileDescriptor.startOffset,
                    fileDescriptor.length
                )
            } else {
                mediaPlayer?.setDataSource(playConfig.mPath)
            }
        } catch (e: Exception) {
            Log.e(TAG, "setDataSource failed: ${e.message}")
            actionsListener?.onError(e)
            throw e
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (mediaPlayer != null) {
            try {
                duration = mediaPlayer?.duration ?: 0
                mediaPlayer?.start()
                playerState = PlayerState.PLAYING
                actionsListener?.onStartPlay()
                schedulePlaybackTimeUpdate()
            } catch (e: IllegalStateException) {
                Log.e(TAG, "onPrepared failed: ${e.message}")
                actionsListener?.onError(e)
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        stop()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        Log.e(TAG, "Player error: $what, $extra")
        actionsListener?.onError(Throwable("Player error: $what, $extra"))
        stop()
        return true
    }

    override fun seek(mills: Long) {
        if (mediaPlayer != null) {
            try {
                if (playerState == PlayerState.PLAYING) {
                    mediaPlayer?.seekTo(mills.toInt())
                    actionsListener?.onSeek(mills)
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "seek failed: ${e.message}")
                actionsListener?.onError(e)
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun pause() {
        if (mediaPlayer != null) {
            try {
                stopPlaybackTimeUpdate()
                if (playerState == PlayerState.PLAYING) {
                    mediaPlayer?.pause()
                    playerState = PlayerState.PAUSED
                    actionsListener?.onPausePlay()
                }
            } catch (e: IllegalStateException) {
                Log.e(TAG, "pause failed: ${e.message}")
                actionsListener?.onError(e)
                throw e
            }
        }
    }

    @Throws(IllegalStateException::class)
    override fun resume() {
        if (mediaPlayer != null) {
            if (playerState == PlayerState.PAUSED) {
                try {
                    mediaPlayer?.start()
                    playerState = PlayerState.PLAYING
                    actionsListener?.onStartPlay()
                    schedulePlaybackTimeUpdate()
                } catch (e: IllegalStateException) {
                    Log.e(TAG, "resume failed: ${e.message}")
                    actionsListener?.onError(e)
                    throw e
                }
            }
        }
    }

    @Throws(Exception::class)
    override fun stop() {
        if (mediaPlayer != null) {
            stopPlaybackTimeUpdate()
            try {
                mediaPlayer?.stop()
                mediaPlayer?.reset()
                mediaPlayer?.setOnPreparedListener(null)
                mediaPlayer?.setOnCompletionListener(null)
                mediaPlayer?.setOnErrorListener(null)
                actionsListener?.onStopPlay()
                playerState = PlayerState.STOPPED
            } catch (e: Exception) {
                Log.e(TAG, "stop failed: ${e.message}")
                actionsListener?.onError(e)
                throw e
            }
        }
    }

    override fun release() {
        if (mediaPlayer != null) {
            mediaPlayer?.release()
            mediaPlayer = null
            actionsListener = null
        }
    }

    override fun getCurrentPosition(): Int {
        return mediaPlayer?.currentPosition ?: 0
    }

    override fun isPaused(): Boolean {
        return playerState == PlayerState.PAUSED
    }

    override fun isPlaying(): Boolean {
        return playerState == PlayerState.PLAYING
    }

    private fun schedulePlaybackTimeUpdate() {
        handler.postDelayed({
            if (mediaPlayer != null) {
                if (playerState == PlayerState.PLAYING) {
                    if (actionsListener != null) {
                        val pos = mediaPlayer?.currentPosition ?: 0
                        actionsListener?.onPlayProgress(pos.toLong(), duration.toLong())
                    }
                }
                schedulePlaybackTimeUpdate()
            }
        }, intervalTime)
    }

    private fun stopPlaybackTimeUpdate() {
        handler.removeCallbacksAndMessages(null)
    }
}
