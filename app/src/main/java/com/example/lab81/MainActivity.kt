package com.example.lab81

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var viewMinutes: EditText
    private lateinit var viewSeconds: EditText
    private lateinit var buttonToStart: Button
    private lateinit var buttonToStop: Button
    private lateinit var tvForCount: TextView
    private val compositeDisposable = CompositeDisposable()
    private var oneTime: Disposable? = null
    private lateinit var player: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewMinutes = findViewById(R.id.viewForMinutes)
        viewSeconds = findViewById(R.id.viewForSeconds)
        buttonToStart = findViewById(R.id.buttonToStart)
        buttonToStop = findViewById(R.id.buttonToStop)
        tvForCount = findViewById(R.id.tvCount)
        player = MediaPlayer.create(this, R.raw.sound)
        buttonToStart.setOnClickListener {
            val inputMinutes = viewMinutes.text.toString()
            val inputSeconds = viewSeconds.text.toString()
            val valueOfCountdown = dataForCount(inputMinutes, inputSeconds)
            if (valueOfCountdown > 0) {
                startCount(valueOfCountdown)
            } else {
                tvForCount.text = "Недійсне значення зворотного відліку"
            }
        }
        buttonToStop.setOnClickListener {
            stopCount()
        }
    }

    private fun dataForCount(inputMinutes: String, inputSeconds: String): Long {
        val minutes = inputMinutes.toLongOrNull()
        val seconds = inputSeconds.toLongOrNull()
        if (minutes == null || seconds == null) {
            return -1
        }
        return minutes * 60 + seconds
    }
    private fun startCount(countdownValue: Long) {
        stopCount()

        oneTime = Flowable.interval(1, TimeUnit.SECONDS)
            .take(countdownValue + 1)
            .map { countdownValue - it }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                { remainingTime ->
                    tvForCount.text = timeFormat(remainingTime)
                },
                { error ->
                    tvForCount.text = "Помилка: ${error.message}"
                },
                {
                    tvForCount.text = "Завершено"
                    player.start()
                }
            )
        oneTime?.let { compositeDisposable.add(it) }
    }
    private fun timeFormat(seconds: Long): String {
        val minutes = seconds / 60
        val balance = seconds % 60
        return String.format("%02d:%02d", minutes, balance)
    }
    private fun stopCount() {
        oneTime?.dispose()
        tvForCount.text = "Зупинено"
    }
    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
        player.release()
    }
}

