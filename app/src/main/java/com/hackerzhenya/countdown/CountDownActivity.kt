package com.hackerzhenya.countdown

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_count_down.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap


class CountDownActivity : AppCompatActivity() {
    companion object {
        const val Event = "10.10.2018 18:20:00"
        const val VK = "https://vk.com/hackerzhenya"
        const val GitHub = "https://github.com/HackerZhenya"
    }

    private var lang = ""
    private var _timer: CountDownTimer? = null
    private val timeWords = HashMap<TimeUnit, Array<String>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadLocale()
        setContentView(R.layout.activity_count_down)

        switchLang.setOnClickListener {
            switchLocale()
        }

        gotoVK.setOnClickListener {
            openUrl(VK)
        }

        gotoGitHub.setOnClickListener {
            openUrl(GitHub)
        }

        initTimeWords()
        onStart()
    }

    override fun onStart() {
        super.onStart()

        _timer = object : CountDownTimer(millisUntilEvent(), 1000) {
            override fun onTick(millisUntilFinished: Long) = onTimerTick(millisUntilFinished / 1000)
            override fun onFinish() = onTimerFinish()
        }.start()
    }

    override fun onPause() {
        super.onPause()
        _timer?.cancel()
    }

    private fun initTimeWords() {
        timeWords[TimeUnit.DAYS] = arrayOf(
                resources.getString(R.string.day_1),
                resources.getString(R.string.day_2),
                resources.getString(R.string.day_5)
        )

        timeWords[TimeUnit.HOURS] = arrayOf(
                resources.getString(R.string.hour_1),
                resources.getString(R.string.hour_2),
                resources.getString(R.string.hour_5)
        )

        timeWords[TimeUnit.MINUTES] = arrayOf(
                resources.getString(R.string.min_1),
                resources.getString(R.string.min_2),
                resources.getString(R.string.min_5)
        )

        timeWords[TimeUnit.SECONDS] = arrayOf(
                resources.getString(R.string.sec_1),
                resources.getString(R.string.sec_2),
                resources.getString(R.string.sec_5)
        )
    }

    private fun millisUntilEvent(): Long =
            SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US).parse(Event).time - System.currentTimeMillis()

    private fun loadLocale() {
        lang = getSharedPreferences("CountDown", Context.MODE_PRIVATE)
                .getString("locale", Locale.getDefault().language)!!

        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    private fun switchLocale() {
        lang = when (lang) {
            "en" -> "ru"
            "ru" -> "en"
            else -> "en"
        }

        Log.d("CountDown", "Now locale is \"$lang\"")
        getSharedPreferences("CountDown", Context.MODE_PRIVATE)
                .edit()
                .putString("locale", lang)
                .apply()

        recreate()
    }

    private fun openUrl(url: String) =
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    private fun pluralize(n: Long, words: Array<String>): String =
            if (n % 10 == 1L && n % 100 != 11L)
               words[0]
            else if (n % 10 in 2..4 && (n % 100 < 10 || n % 100 >= 20))
                words[1]
            else
                words[2]

    private fun onTimerTick(secondsUntilFinish: Long) {
        Log.d("Timer", "Tick: $secondsUntilFinish")

        var seconds = secondsUntilFinish

        val days = TimeUnit.SECONDS.toDays(seconds)
        seconds -= TimeUnit.DAYS.toSeconds(days)

        val hours = TimeUnit.SECONDS.toHours(seconds)
        seconds -= TimeUnit.HOURS.toSeconds(hours)

        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        seconds -= TimeUnit.MINUTES.toSeconds(minutes)

        val daysWord = pluralize(days, timeWords[TimeUnit.DAYS]!!)
        val hoursWord = pluralize(hours, timeWords[TimeUnit.HOURS]!!)
        val minutesWord = pluralize(minutes, timeWords[TimeUnit.MINUTES]!!)
        val secondsWord = pluralize(seconds, timeWords[TimeUnit.SECONDS]!!)

        val template = "$days $daysWord $hours $hoursWord $minutes $minutesWord $seconds $secondsWord"
        timer.text = template
    }

    private fun onTimerFinish() {
        timer.text = resources.getString(R.string.already_started)
    }
}
