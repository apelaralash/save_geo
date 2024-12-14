package com.ilya.savegeo.location_tracking

import android.os.Parcelable
import androidx.annotation.StringRes
import com.google.android.gms.location.Priority
import com.ilya.savegeo.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class LocationConfig(
    @StringRes val title: Int,
    val interval: Long,
    val minUpdateInterval: Long,
    val priority: Int
) : Parcelable {
    HIGH_ACCURACY(
        title = R.string.high_accuracy,
        interval = 5000,
        minUpdateInterval = 3000,
        priority = Priority.PRIORITY_HIGH_ACCURACY
    ),
    BALANCED_ACCURACY(
        title = R.string.balanced_accuracy,
        interval = 30000,
        minUpdateInterval = 10000,
        priority = Priority.PRIORITY_BALANCED_POWER_ACCURACY
    ),
    LOW_POWER(
        title = R.string.low_power,
        interval = 60000,
        minUpdateInterval = 20000,
        priority = Priority.PRIORITY_LOW_POWER
    )
}