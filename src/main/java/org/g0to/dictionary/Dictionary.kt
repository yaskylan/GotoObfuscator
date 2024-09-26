package org.g0to.dictionary

import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow
import kotlin.random.asKotlinRandom

open class Dictionary(
    private val words: CharArray,
    private var length: Int
) {
    protected val blackList = HashSet<String>()
    private val usedList = HashSet<String>()
    private var expectedSize: Int = 0

    init {
        if (length < 1) {
            throw IllegalArgumentException("Length should be greater than 1")
        }

        for (i in words.indices) {
            if (i == words.lastIndex) {
                break
            }

            for (j in (i + 1) until words.size) {
                if (words[i] == words[j]) {
                    throw IllegalArgumentException("Duplicate character, index: $i and $j")
                }
            }
        }

        updateExpectedSize()
    }

    fun addToBlacklist(s: String) {
        blackList.add(s)
    }

    fun addToBlacklist(blackList: Collection<String>) {
        this.blackList.addAll(blackList)
    }

    private fun updateExpectedSize() {
        if (expectedSize == Int.MAX_VALUE) {
            throw RuntimeException()
        }

        var pow = words.size.toDouble().pow(length)

        if (pow > Int.MAX_VALUE) {
            pow = Int.MAX_VALUE.toDouble()
        }

        this.expectedSize = pow.toInt()
    }

    fun randString(): String {
        return randString(blackList)
    }

    protected fun randString(blackList: HashSet<String>): String {
        var s: String

        do {
            do {
                s = randString0()
            } while (usedList.contains(s))

            usedList.add(s)

            if (usedList.size == expectedSize) {
                length++
                updateExpectedSize()
                usedList.clear()
            }
        } while (blackList.contains(s))

        return s
    }

    private fun randString0(): String {
        val length = this.length
        val buffer = CharArray(length)

        for (i in 0 until length) {
            buffer[i] = words.random(ThreadLocalRandom.current().asKotlinRandom())
        }

        return String(buffer)
    }
}