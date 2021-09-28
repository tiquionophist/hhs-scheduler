package com.tiquionophist.util

/**
 * Returns a string representing [count] with this String as a pluralizeable unit, e.g. "6 units" for
 * "unit".pluralizedCount(6).
 */
fun String.pluralizedCount(count: Int): String {
    return "$count ${this.pluralize(count)}"
}

/**
 * Returns a pluralized version of this [String], i.e. adding "s" if [count] is not singular.
 */
fun String.pluralize(count: Int): String {
    return if (count == 1) this else this + "s"
}
