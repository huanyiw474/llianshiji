package com.lianshiji.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateTimeUtils {
    private val zone: ZoneId = ZoneId.systemDefault()
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun today(): LocalDate = LocalDate.now(zone)

    fun dayStartMillis(date: LocalDate): Long {
        return date.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun nextDayStartMillis(date: LocalDate): Long {
        return date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    fun dateTimeMillis(date: LocalDate, timeText: String): Long {
        val time = parseTime(timeText) ?: LocalTime.now(zone).withSecond(0).withNano(0)
        return date.atTime(time).atZone(zone).toInstant().toEpochMilli()
    }

    fun dateAtNoonMillis(date: LocalDate): Long {
        return date.atTime(12, 0).atZone(zone).toInstant().toEpochMilli()
    }

    fun formatDate(date: LocalDate): String = dateFormatter.format(date)

    fun formatDate(millis: Long): String {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(dateFormatter)
    }

    fun toLocalDate(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    }

    fun formatTime(millis: Long): String {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalTime().format(timeFormatter)
    }

    fun parseDate(text: String): LocalDate? {
        return try {
            LocalDate.parse(text.trim(), dateFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun parseTime(text: String): LocalTime? {
        return try {
            LocalTime.parse(text.trim(), timeFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
