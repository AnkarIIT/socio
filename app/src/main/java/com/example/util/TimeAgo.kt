package com.example.util

import java.util.concurrent.TimeUnit

object TimeAgo {
    fun format(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 0 -> "now"
            diff < TimeUnit.MINUTES.toMillis(1) -> "now"
            diff < TimeUnit.MINUTES.toMillis(2) -> "1m"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m"
            diff < TimeUnit.HOURS.toMillis(2) -> "1h"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h"
            diff < TimeUnit.DAYS.toMillis(2) -> "1d"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d"
            diff < TimeUnit.DAYS.toMillis(30) -> "${TimeUnit.MILLISECONDS.toDays(diff) / 7}w"
            else -> "${TimeUnit.MILLISECONDS.toDays(diff) / 30}mo"
        }
    }
}
