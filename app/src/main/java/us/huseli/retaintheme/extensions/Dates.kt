@file:Suppress("unused")

package us.huseli.retaintheme.extensions

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration

enum class DateTimePrecision { DAY, HOUR, MINUTE, SECOND }

@RequiresApi(Build.VERSION_CODES.O)
fun Instant.isoDate(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    return formatter.format(this)
}

@RequiresApi(Build.VERSION_CODES.O)
fun Instant.isoDateTime(precision: DateTimePrecision = DateTimePrecision.SECOND): String {
    val pattern = when (precision) {
        DateTimePrecision.DAY -> "yyyy-MM-dd"
        DateTimePrecision.HOUR -> "yyyy-MM-dd HH"
        DateTimePrecision.MINUTE -> "yyyy-MM-dd HH:mm"
        DateTimePrecision.SECOND -> "yyyy-MM-dd HH:mm:ss"
    }
    val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneId.systemDefault())
    return formatter.format(this)
}

fun Duration.sensibleFormat() = toComponents { hours, minutes, seconds, _ ->
    if (hours > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    else String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
}

fun Iterable<Duration>.sum(): Duration = this.plus(Duration.ZERO).reduce { d1, d2 -> d1 + d2 }
