package org.g0to.utils.extensions

import java.util.function.Predicate

inline fun <T> Iterable<T>.filteredForeach(predicate: Predicate<T>, block: (T) -> Unit) {
    forEach {
        if (predicate.test(it)) {
            block(it)
        }
    }
}

inline fun <T> Iterable<T>.reversedFilteredForeach(predicate: Predicate<T>, block: (T) -> Unit) {
    forEach {
        if (!predicate.test(it)) {
            block(it)
        }
    }
}