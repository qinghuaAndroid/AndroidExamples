/*
 * Copyright 2018 Dmytro Ponomarenko
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

package com.shuhang.androidexamples.audio;

import java.io.File;

public interface RecorderContract {

	interface RecorderCallback {
		void onStartRecord(File output);
		void onPauseRecord();
		void onResumeRecord();
		void onRecordProgress(long mills, int amp);
		void onStopRecord(File output);
		void onError(Throwable throwable);
	}

	interface Recorder {
		void setRecorderCallback(RecorderCallback callback);

		/**
		 *
		 * @param outputFile 输出文件路径
		 * @param channelCount 声道数 Channel
		 * @param sampleRate 采样频率
		 * @param bitrate bitel
		 * @param maxDuration 最大时长-毫秒
		 */
		void startRecording(String outputFile, int channelCount, int sampleRate, int bitrate, int maxDuration);
		void resumeRecording();
		void pauseRecording();
		void stopRecording();
		boolean isRecording();
		boolean isPaused();
	}
}
