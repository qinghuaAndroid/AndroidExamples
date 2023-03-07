package com.shuhang.androidexamples.exoplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSink
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.CacheDataSink
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.shuhang.androidexamples.databinding.ActivityExoPlayerBinding

open class ExoPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExoPlayerBinding

    private lateinit var player: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializePlayer("https://img2.funchatting.net/dc09de8716fe2e776be2b565884c25b0.mp4")
    }

    open fun initializePlayer(uri: String) {
        player = SimpleExoPlayer.Builder(this).build()
        binding.playerView.player = player
        // 获取缓存文件夹
        val cache: Cache = ExoPlayerCache.get().simpleCache
        // CacheDataSinkFactory 第二个参数为单个缓存文件大小，如果需要缓存的文件大小超过此限制，则会分片缓存，不影响播放
        val cacheWriteDataSinkFactory: DataSink.Factory = CacheDataSink.Factory()
            .setCache(cache)
            .setFragmentSize(CacheDataSink.DEFAULT_FRAGMENT_SIZE)
            .setBufferSize(CacheDataSink.DEFAULT_BUFFER_SIZE)
        val dataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(DefaultDataSourceFactory(this))
            .setCacheReadDataSourceFactory(FileDataSource.Factory())
            .setCacheWriteDataSinkFactory(cacheWriteDataSinkFactory)
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        val mediaItem = MediaItem.Builder().setUri(uri).build()
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
        player.setMediaSource(mediaSource)
        player.prepare()
        player.volume = 0f
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.playWhenReady = true
        player.addListener(object : Player.Listener {

        })
    }
}