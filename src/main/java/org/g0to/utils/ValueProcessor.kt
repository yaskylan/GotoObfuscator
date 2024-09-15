package org.g0to.utils

interface ValueProcessor<T> {
    fun process(value: T) : T
}