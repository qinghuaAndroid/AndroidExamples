package com.shuhang.androidexamples.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.shuhang.androidexamples.audio.AudioRecordActivity
import com.shuhang.androidexamples.databinding.ActivityMainBinding
import com.shuhang.androidexamples.exoplayer.ExoPlayerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val adapter by lazy { MainListAdapter() }

    private val items = arrayListOf<MainListBean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
        adapter.setOnItemClickListener { _, _, position ->
            val bean = adapter.items[position]
            startActivity(Intent(this, bean.clazz))
        }

        items.add(MainListBean("音视频录制与播放", AudioRecordActivity::class.java))
        items.add(MainListBean("ExoPlayer视频播放器", ExoPlayerActivity::class.java))

        adapter.submitList(items)
    }
}