package com.andre_max.tiktokclone.utils

import timber.log.Timber
import java.util.concurrent.TimeUnit

object TimeUtils {

    fun convertTimeToDisplayTime(timeInMillis: Long): String {
        val time = String.format("%d:%d",
            TimeUnit.MILLISECONDS.toMinutes(timeInMillis),
            TimeUnit.MILLISECONDS.toSeconds(timeInMillis) -
                    TimeUnit.MINUTES.toSeconds((TimeUnit.MILLISECONDS.toMinutes(timeInMillis)))
        )
        Timber.d("TimeInMillis is $timeInMillis and time is $time")
        return time
    }

}