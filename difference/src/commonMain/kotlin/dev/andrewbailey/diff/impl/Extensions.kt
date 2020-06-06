package dev.andrewbailey.diff.impl

import kotlin.math.abs

internal fun Int.isEven() = abs(this) % 2 == 0

internal fun Int.isOdd() = abs(this) % 2 == 1
