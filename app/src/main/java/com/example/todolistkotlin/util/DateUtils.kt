package com.example.todolistkotlin.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class DateUtils {

    companion object {

        private val locale = Locale("pt", "br")

        fun getDayNameOfDate(date: Long): String {
            return try {
                SimpleDateFormat("EEEE", locale).format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
                ""
            }
        }

        fun getMonthNameOfDate(date: Long): String {
            return try {
                SimpleDateFormat("MMMM", locale).format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
                "-"
            }
        }

        fun getCompleteDate(date: Long): String {
            return try {
                SimpleDateFormat("EEEE, dd MMM yyyy", locale).format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
                "-"
            }
        }

        fun getCompleteDateWithoutYear(date: Long): String {
            return try {
                SimpleDateFormat("EEEE, dd MMM", locale).format(date)
            } catch (e: ParseException) {
                e.printStackTrace()
                "-"
            }
        }

        fun longIntoDate(milliseconds: Long): String {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", locale)
            return dateFormat.format(milliseconds)
        }

        fun longIntoYear(milliseconds: Long): String {
            val dateFormat = SimpleDateFormat("yyyy", locale)
            return dateFormat.format(milliseconds)
        }

        fun longIntoDay(milliseconds: Long): String {
            val dateFormat = SimpleDateFormat("dd", locale)
            return dateFormat.format(milliseconds)
        }

        fun longIntoTime(milliseconds: Long): String {
            val timeFormat = SimpleDateFormat("HH:mm", locale)
            return timeFormat.format(milliseconds)
        }

        fun monthYearOfDate(date: Long): String {
            val formatter = SimpleDateFormat("MMMM yyyy", locale)
            return formatter.format(date)
        }

        fun resetTimeInCalendar(calendar: Calendar) {
            calendar.set(Calendar.HOUR, 0)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }

        fun isTimeInMillsSameDay(
            baseTimeInMillis: Long,
            taskDateInMills: Long
        ): Boolean {
            return longIntoDate(taskDateInMills) == longIntoDate(baseTimeInMillis)
        }

        fun getTimeInMillisResetedTime(date: Calendar): Long {
            date[Calendar.HOUR] = 0
            date[Calendar.HOUR_OF_DAY] = 0
            date[Calendar.MINUTE] = 0
            date[Calendar.SECOND] = 0
            date[Calendar.MILLISECOND] = 0
            return date.timeInMillis
        }
    }

}