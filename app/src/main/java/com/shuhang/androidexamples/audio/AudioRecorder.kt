package com.shuhang.androidexamples.audio

import android.Manifest
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresPermission
import timber.log.Timber
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs

/**
 * {@link https://github.com/Dimowner/AudioRecorder}
 */
class AudioRecorder private constructor() : RecorderContract.Recorder {

    private var recorder: AudioRecord? = null

    private var recordFile: File? = null
    private var bufferSize = 0
    private var updateTime: Long = 0
    private var durationMills: Long = 0

    private var recordingThread: Thread? = null

    private val isRecording = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private val totalAudioLen = AtomicLong(0)
    private val handler = Handler(Looper.getMainLooper())

    /** Value for recording used visualisation.  */
    private var lastVal = 0

    private var sampleRate = RECORD_SAMPLE_RATE_44100
    private var channelCount = 1

    private var recorderCallback: RecorderContract.RecorderCallback? = null

    /** 设置的最大时长对应的最大音频长度（PCM_16BIT） */
    private var maxAudioLen: Long = 0

    companion object {
        private const val RECORD_SAMPLE_RATE_44100 = 44100
        private const val RECORDER_BPP = 16 //bits per sample

        @JvmStatic
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { AudioRecorder() }
    }

    override fun setRecorderCallback(callback: RecorderContract.RecorderCallback) {
        recorderCallback = callback
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    override fun startRecording(
        outputFile: String,
        channelCount: Int,
        sampleRate: Int,
        bitrate: Int,
        maxDuration: Int
    ) {
        this.sampleRate = sampleRate
        this.channelCount = channelCount
        recordFile = File(outputFile)
        if (recordFile!!.exists() && recordFile!!.isFile) {
            val channel =
                if (channelCount == 1) AudioFormat.CHANNEL_IN_MONO else AudioFormat.CHANNEL_IN_STEREO
            // 通过设置的最长录制时间计算录制的最大文件大小
            maxAudioLen = if (maxDuration <= 0) {
                Long.MAX_VALUE
            } else {
                sampleRate * channelCount * maxDuration * 16L / 8000L
            }

            try {
                bufferSize = AudioRecord.getMinBufferSize(
                    sampleRate,
                    channel,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channel,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )
            } catch (e: IllegalArgumentException) {
                Timber.e(
                    e,
                    "sampleRate = $sampleRate, channel = $channel, bufferSize = $bufferSize"
                )
                recorder?.release()
            }
            if (recorder != null && recorder!!.state == AudioRecord.STATE_INITIALIZED) {
                recorder?.startRecording()
                updateTime = System.currentTimeMillis()
                isRecording.set(true)
                recordingThread = Thread({ this.writeAudioDataToFile() }, "AudioRecorder Thread")
                recordingThread?.start()
                scheduleRecordingTimeUpdate()
                recorderCallback?.onStartRecord(recordFile)
                isPaused.set(false)
            } else {
                Timber.e("prepare() failed")
                recorderCallback?.onError(Throwable("无法初始化录制器"))
            }
        } else {
            recorderCallback?.onError(Throwable("无效的输出文件"))
        }
    }

    override fun resumeRecording() {
        if (recorder != null && recorder!!.state == AudioRecord.STATE_INITIALIZED) {
            if (isPaused.get()) {
                updateTime = System.currentTimeMillis()
                scheduleRecordingTimeUpdate()
                recorder?.startRecording()
                recorderCallback?.onResumeRecord()
                isPaused.set(false)
            }
        }
    }

    override fun pauseRecording() {
        if (recorder != null && isRecording.get()) {
            recorder?.stop()
            durationMills += System.currentTimeMillis() - updateTime
            pauseRecordingTimer()
            isPaused.set(true)
            recorderCallback?.onPauseRecord()
        }
    }

    override fun stopRecording() {
        if (recorder != null) {
            isRecording.set(false)
            isPaused.set(false)
            stopRecordingTimer()
            if (recorder!!.state == AudioRecord.STATE_INITIALIZED) {
                try {
                    recorder?.stop()
                } catch (e: IllegalStateException) {
                    Timber.e(e, "stopRecording() problems")
                }
            }
            durationMills = 0
            recorder?.release()
            recordingThread?.interrupt()
            recorderCallback?.onStopRecord(recordFile)
        }
    }

    override fun isRecording(): Boolean {
        return isRecording.get()
    }

    override fun isPaused(): Boolean {
        return isPaused.get()
    }

    private fun writeAudioDataToFile() {
        val data = ByteArray(bufferSize)
        val fos: FileOutputStream? = try {
            FileOutputStream(recordFile)
        } catch (e: FileNotFoundException) {
            Timber.e(e)
            null
        }
        if (null != fos) {
            var chunksCount = 0
            val shortBuffer = ByteBuffer.allocate(2)
            shortBuffer.order(ByteOrder.LITTLE_ENDIAN)
            while (isRecording.get()) {
                if (!isPaused.get()) {
                    chunksCount += recorder!!.read(data, 0, bufferSize)
                    if (AudioRecord.ERROR_INVALID_OPERATION != chunksCount) {
                        var sum: Long = 0
                        var i = 0
                        while (i < bufferSize) {
                            shortBuffer.put(data[i])
                            shortBuffer.put(data[i + 1])
                            sum += abs(shortBuffer.getShort(0).toInt()).toLong()
                            shortBuffer.clear()
                            i += 2
                        }
                        lastVal = (sum / (bufferSize / 16)).toInt()
                        totalAudioLen.set(chunksCount.toLong())
                        try {
                            fos.write(data)
                        } catch (e: IOException) {
                            Timber.e(e)
                            handler.post {
                                recorderCallback?.onError(Throwable("录制错误!"))
                                stopRecording()
                            }
                        }
                        // 达到设置的最大录制大小
                        if (totalAudioLen.get() >= maxAudioLen) {
                            stopRecording()
                        }
                    }
                }
            }
            try {
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                Timber.e(e)
            }
            setWaveFileHeader(recordFile!!, channelCount)
        }
    }

    private fun setWaveFileHeader(file: File, channels: Int) {
        val fileSize = file.length() - 8
        val totalSize = fileSize + 36
        val byteRate =
            (sampleRate * channels * (RECORDER_BPP / 8)).toLong() //2 byte per 1 sample for 1 channel.
        try {
            val wavFile: RandomAccessFile = randomAccessFile(file)
            wavFile.seek(0) // to the beginning
            wavFile.write(
                generateHeader(
                    fileSize,
                    totalSize,
                    sampleRate.toLong(),
                    channels,
                    byteRate
                )
            )
            wavFile.close()
        } catch (e: FileNotFoundException) {
            Timber.e(e)
        } catch (e: IOException) {
            Timber.e(e)
        }
    }

    private fun randomAccessFile(file: File): RandomAccessFile {
        val randomAccessFile: RandomAccessFile = try {
            RandomAccessFile(file, "rw")
        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        }
        return randomAccessFile
    }

    private fun generateHeader(
        totalAudioLen: Long, totalDataLen: Long, longSampleRate: Long, channels: Int,
        byteRate: Long
    ): ByteArray {
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte() // RIFF/WAVE header
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte() //WAVE
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte() //过渡字节
        header[16] = 16 //16 for PCM. 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        //编码方式 10H为PCM编码格式
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        //采样率，每个通道的播放速度
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (channels * (RECORDER_BPP / 8)).toByte() // block align
        header[33] = 0
        //每个样本的数据位数
        header[34] = RECORDER_BPP.toByte() // bits per sample
        header[35] = 0
        //Data chunk
        header[36] = 'd'.code.toByte() //data
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        return header
    }

    private fun scheduleRecordingTimeUpdate() {
        handler.postDelayed({
            if (recorderCallback != null && recorder != null) {
                val curTime = System.currentTimeMillis()
                durationMills += curTime - updateTime
                updateTime = curTime
                recorderCallback?.onRecordProgress(durationMills, lastVal)
                scheduleRecordingTimeUpdate()
            }
        }, 100)
    }

    private fun stopRecordingTimer() {
        handler.removeCallbacksAndMessages(null)
        updateTime = 0
    }

    private fun pauseRecordingTimer() {
        handler.removeCallbacksAndMessages(null)
        updateTime = 0
    }
}