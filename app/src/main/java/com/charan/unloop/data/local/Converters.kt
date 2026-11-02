package com.charan.unloop.data.local

import androidx.room.TypeConverter
import com.charan.unloop.data.local.model.enums.ScheduleType
import com.charan.unloop.data.local.model.enums.SessionEventType
import com.charan.unloop.data.local.model.enums.SessionStatus
import com.charan.unloop.data.local.model.enums.SessionType
import com.charan.unloop.data.local.model.enums.UnlockChallengeType
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
object Converters {

    @TypeConverter
    fun localTimeToSecondsOfDay(value : LocalTime) : Int {
        return value.toSecondOfDay()
    }

    @TypeConverter
    fun secondsOfDayToLocalTime(value : Int) : LocalTime {
        return LocalTime.fromSecondOfDay(value)
    }

    @TypeConverter
    fun localDateTimeToLong(value : LocalDateTime) : Long {
        return value.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }

    @TypeConverter
    fun longToLocalDateTime(value : Long) : LocalDateTime {
        return Instant.fromEpochMilliseconds(value).toLocalDateTime(TimeZone.currentSystemDefault())
    }

    @TypeConverter
    fun dayOfWeekListToString(value: List<DayOfWeek>): String {
        return value.joinToString(separator = ",")
    }

    @TypeConverter
    fun stringToDayOfWeekList(value: String): List<DayOfWeek> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",").map { DayOfWeek.valueOf(it) }
        }
    }

    @TypeConverter
    fun stringListToString(value: List<String>?): String? {
        return value?.joinToString(separator = ",")
    }

    @TypeConverter
    fun stringToStringList(value: String?): List<String>? {
        return value?.let {
            if (it.isEmpty()) emptyList() else it.split(",")
        }
    }

    @TypeConverter
    fun dayOfWeekToString(value: DayOfWeek): String {
        return value.name
    }

    @TypeConverter
    fun stringToDayOfWeek(value: String): DayOfWeek {
        return DayOfWeek.valueOf(value)
    }

    @TypeConverter
    fun localDateTimeToLocalDate(value: LocalDateTime): LocalDate {
        return value.date
    }

    @TypeConverter
    fun unlockChallengeTypeToString(value: UnlockChallengeType): String {
        return value.name
    }

    @TypeConverter
    fun stringToUnlockChallengeType(value: String): UnlockChallengeType {
        return UnlockChallengeType.valueOf(value)
    }

    @TypeConverter
    fun sessionStatusToString(value: SessionStatus): String {
        return value.name
    }

    @TypeConverter
    fun stringToSessionStatus(value: String): SessionStatus {
        return SessionStatus.valueOf(value)
    }

    @TypeConverter
    fun sessionTypeToString(value: SessionType): String {
        return value.name
    }

    @TypeConverter
    fun stringToSessionType(value: String): SessionType {
        return SessionType.valueOf(value)
    }

    @TypeConverter
    fun sessionEventTypeToString(value: SessionEventType): String {
        return value.name
    }

    @TypeConverter
    fun stringToSessionEventType(value: String): SessionEventType {
        return SessionEventType.valueOf(value)
    }

    @TypeConverter
    fun scheduleTypeToString(value: ScheduleType): String {
        return value.name
    }

    @TypeConverter
    fun stringToScheduleType(value: String): ScheduleType {
        return ScheduleType.valueOf(value)
    }
}
