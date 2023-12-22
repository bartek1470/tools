package pl.bartek.scripts

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.chrono.ChronoLocalDate
import java.time.chrono.ChronoZonedDateTime

fun ZonedDateTime.isEqualOrBefore(other: ChronoZonedDateTime<*>): Boolean {
    return this.isEqual(other) || this.isBefore(other)
}

fun ZonedDateTime.isEqualOrAfter(other: ChronoZonedDateTime<*>): Boolean {
    return this.isEqual(other) || this.isAfter(other)
}

fun LocalDate.isEqualOrBefore(other: ChronoLocalDate): Boolean {
    return this.isEqual(other) || this.isBefore(other)
}

fun LocalDate.isEqualOrAfter(other: ChronoLocalDate): Boolean {
    return this.isEqual(other) || this.isAfter(other)
}
