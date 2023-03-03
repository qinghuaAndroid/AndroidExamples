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

interface PlayerContract {
    interface PlayerCallback {
        fun onStartPlay()
        fun onPlayProgress(curPosition: Long, duration: Long)
        fun onPausePlay()
        fun onSeek(mills: Long)
        fun onStopPlay()
        fun onError(throwable: Throwable)
    }

    interface Player {
        fun setPlayerCallback(callback: PlayerCallback)
        fun play(playConfig: PlayConfig)
        fun pause()
        fun resume()
        fun seek(mills: Long)
        fun stop()
        fun release()
        fun getCurrentPosition(): Int
        fun isPaused(): Boolean
        fun isPlaying(): Boolean
    }
}

enum class PlayerState {
    STOPPED,
    PLAYING,
    PAUSED
}
