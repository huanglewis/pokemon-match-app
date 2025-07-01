package iss.nus.edu.sg.fragments.workshop.pokemonmemorygame

import android.content.Context
import android.media.MediaPlayer

object AudioPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentResId: Int? = null
    private var oneTimePlayed = mutableSetOf<Int>()

    fun playMusic(context: Context, resId: Int, loop: Boolean = true, playOnce: Boolean = false) {
        if (playOnce && oneTimePlayed.contains(resId)) return
        stopMusic()
        mediaPlayer = MediaPlayer.create(context, resId).apply {
            isLooping = loop
            start()
        }
        if (playOnce) oneTimePlayed.add(resId)
        currentResId = resId
    }

    fun stopMusic() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }

    fun playSoundEffect(context: Context, resId: Int) {
        MediaPlayer.create(context, resId).apply {
            setOnCompletionListener { release() }
            start()
        }
    }

    fun getCurrentMusicId(): Int? = currentResId
}
