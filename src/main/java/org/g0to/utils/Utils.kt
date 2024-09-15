package org.g0to.utils

import java.util.Objects
import java.util.Random

object Utils {
    fun breakpoint() {
    }

    fun <T> isOneOfThese(target: T, vararg toCompareObjects: T): Boolean {
        for (toCompareObject in toCompareObjects) {
            if (Objects.equals(target, toCompareObject)) {
                return true
            }
        }

        return false
    }

    fun Random.nextNonZeroInt(): Int {
        var i: Int

        do {
            i = nextInt()
        } while (i == 0)

        return i
    }
}