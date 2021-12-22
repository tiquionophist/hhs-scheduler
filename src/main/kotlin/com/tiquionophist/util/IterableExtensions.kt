package com.tiquionophist.util

/**
 * Like [Iterable.flatten] but flattens to a [Set] to improve performance in some use cases.
 */
fun <T> Iterable<Iterable<T>>.flattenToSet(): Set<T> {
    val set = mutableSetOf<T>()
    for (element in this) {
        set.addAll(element)
    }
    return set
}
