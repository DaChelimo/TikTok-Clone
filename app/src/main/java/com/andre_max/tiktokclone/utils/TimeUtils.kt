package com.andre_max.tiktokclone.utils

import timber.log.Timber
import java.util.concurrent.TimeUnit

object TimeUtils {

    fun convertTimeToDisplayTime(timeInMillis: String): String {
        val longTime = timeInMillis.toLong()
        val time = String.format("%d:%d",
            TimeUnit.MILLISECONDS.toMinutes(longTime),
            TimeUnit.MILLISECONDS.toSeconds(longTime) -
                    TimeUnit.MINUTES.toSeconds((TimeUnit.MILLISECONDS.toMinutes(longTime)))
        )
        Timber.d("TimeInMillis is $timeInMillis and time is $time")
        return time
    }

}