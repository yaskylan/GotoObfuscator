package org.g0to.test

import org.g0to.dictionary.Dictionary
import java.util.HashSet

fun main() {
    val charList = ArrayList<Char>()

    for (c in 'a'..'z') {
        charList.add(c)
    }

    for (c in 'A'..'Z') {
        charList.add(c)
    }

    for (c in '0'..'9') {
        charList.add(c)
    }

    val dictionary = Dictionary(charList.toCharArray(), 1)
    dictionary.addToBlacklist("a")

    repeat(106588) {
        println("" + it + ":" + dictionary.randString())
    }
}