package com.shuhang.androidexamples.audio

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shuhang.androidexamples.audio.player.AudioPlayer
import com.shuhang.androidexamples.audio.player.PlayConfig
import com.shuhang.androidexamples.audio.player.PlayerContract
import com.shuhang.androidexamples.databinding.ActivityAudioRecordBinding
import java.io.File
import java.io.IOException

/**
 * @author vveng
 * @version version 1.0.0
 * @date 2018/7/24 16:03.
 * @email vvengstuggle@163.com
 * @instructions 说明
 * @descirbe 描述
 * @features 功能
 */
class AudioRecordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioRecordBinding

    //申请权限列表
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO
    )

    private var recordFile: File? = null

    private var audioPlayer: AudioPlayer? = null

    companion object {
        private const val TAG = "AudioRecordManager"
        private const val REQUEST_CODE = 1001
    }

    //拒绝权限列表
    private val refusePermissions: MutableList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        initData()
        checkPermission()
    }

    /**
     * 6.0以上要动态申请权限
     */
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (i in permissions.indices) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        permissions[i]
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    refusePermissions.add(permissions[i])
                }
            }
            if (refusePermissions.isNotEmpty()) {
                val permissions = refusePermissions.toTypedArray()
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            }
        }
    }

    /**
     * 权限结果回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                for (i in permissions.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "${permissions[i]} 被禁用")
                    }
                }
            }
        }
    }

    private fun initView() {
        binding.recordStart.setOnClickListener {
            //录音
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@setOnClickListener
            }
            try {
                val tempFile =
                    File.createTempFile("AUDIO_" + System.currentTimeMillis(), ".wav")
                AudioRecorder.instance.startRecording(
                    tempFile.path,
                    2,
                    44100,
                    128000,
                    10 * 1000
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        binding.recordStop.setOnClickListener {
            AudioRecorder.instance.stopRecording()
        }
        binding.recordPlay.setOnClickListener {
            val playConfig = PlayConfig.Builder().path(recordFile?.path)
                .looping(false)
                .intervalTime(1000)
                .build()
            audioPlayer = AudioPlayer()
            audioPlayer?.play(playConfig)
            audioPlayer?.setPlayerCallback(object : PlayerContract.PlayerCallback {
                override fun onStartPlay() {
                    Log.d(TAG, "onStartPlay: ")
                }

                override fun onPlayProgress(curPosition: Long, duration: Long) {
                    Log.d(TAG, "onPlayProgress: $curPosition, $duration")
                }

                override fun onPausePlay() {
                    Log.d(TAG, "onPausePlay: ")
                }

                override fun onSeek(mills: Long) {
                    Log.d(TAG, "onSeek: ")
                }

                override fun onStopPlay() {
                    Log.d(TAG, "onStopPlay: ")
                }

                override fun onError(throwable: Throwable) {
                    Log.d(TAG, "onError: ")
                }

            })
        }
        binding.recordNoplay.setOnClickListener {
            audioPlayer?.stop()
        }

    }

    private fun initData() {
        //初始化
        AudioRecorder.instance.setRecorderCallback(object : RecorderContract.RecorderCallback {
            override fun onStartRecord(output: File) {
                recordFile = output
                Log.d(TAG, "onStartRecord: ${output.path}")
            }

            override fun onPauseRecord() {

            }

            override fun onResumeRecord() {

            }

            override fun onRecordProgress(mills: Long, amp: Int) {
                binding.audioTime.text = (mills / 1000).toString()
                Log.d(TAG, "onRecordProgress: $mills, $amp")
            }

            override fun onStopRecord(output: File) {
                Log.d(TAG, "onStopRecord: ${output.path}")
            }

            override fun onError(throwable: Throwable) {
                Log.d(TAG, "onError: ${throwable.message}")
            }
        })
    }
}