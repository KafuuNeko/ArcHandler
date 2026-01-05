package cc.kafuu.archandler.libs.utils

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.RecursiveAction
import kotlin.math.log2
import kotlin.math.max

private const val INSERTION_SORT_THRESHOLD = 32
private const val PARALLEL_THRESHOLD = 1 shl 14 // 16384

fun <T> MutableList<T>.parallelSortWith(
    comparator: Comparator<in T>
) {
    if (size <= 1) return

    val processors = Runtime.getRuntime().availableProcessors()
    val maxDepth = max(1, log2(processors.toDouble()).toInt() * 2)

    ForkJoinPool.commonPool().invoke(
        QuickSortTask(
            list = this,
            left = 0,
            right = lastIndex,
            comparator = comparator,
            depth = maxDepth
        )
    )
}

private class QuickSortTask<T>(
    private val list: MutableList<T>,
    private val left: Int,
    private val right: Int,
    private val comparator: Comparator<in T>,
    private val depth: Int
) : RecursiveAction() {

    override fun compute() {
        val size = right - left + 1

        if (size <= INSERTION_SORT_THRESHOLD) {
            insertionSort(list, left, right, comparator)
            return
        }

        if (depth <= 0 || size < PARALLEL_THRESHOLD) {
            quickSortSequential(list, left, right, comparator)
            return
        }

        val pivot = partition(list, left, right, comparator)

        val leftTask = if (left < pivot - 1)
            QuickSortTask(list, left, pivot - 1, comparator, depth - 1)
        else null

        val rightTask = if (pivot + 1 < right)
            QuickSortTask(list, pivot + 1, right, comparator, depth - 1)
        else null

        when {
            leftTask != null && rightTask != null -> invokeAll(leftTask, rightTask)
            leftTask != null -> leftTask.invoke()
            rightTask != null -> rightTask.invoke()
        }
    }
}

private fun <T> quickSortSequential(
    list: MutableList<T>,
    left: Int,
    right: Int,
    comparator: Comparator<in T>
) {
    var l = left
    var r = right

    while (l < r) {
        if (r - l < INSERTION_SORT_THRESHOLD) {
            insertionSort(list, l, r, comparator)
            return
        }
        val p = partition(list, l, r, comparator)
        if (p - l < r - p) {
            quickSortSequential(list, l, p - 1, comparator)
            l = p + 1
        } else {
            quickSortSequential(list, p + 1, r, comparator)
            r = p - 1
        }
    }
}

private fun <T> partition(
    list: MutableList<T>,
    left: Int,
    right: Int,
    comparator: Comparator<in T>
): Int {
    val mid = (left + right) ushr 1
    val pivotIndex = medianOfThree(list, left, mid, right, comparator)
    swap(list, pivotIndex, right)

    val pivot = list[right]
    var i = left

    for (j in left until right) {
        if (comparator.compare(list[j], pivot) <= 0) {
            swap(list, i, j)
            i++
        }
    }
    swap(list, i, right)
    return i
}

private fun <T> insertionSort(
    list: MutableList<T>,
    left: Int,
    right: Int,
    comparator: Comparator<in T>
) {
    for (i in left + 1..right) {
        val v = list[i]
        var j = i - 1
        while (j >= left && comparator.compare(list[j], v) > 0) {
            list[j + 1] = list[j]
            j--
        }
        list[j + 1] = v
    }
}

private fun <T> medianOfThree(
    list: List<T>,
    a: Int,
    b: Int,
    c: Int,
    comparator: Comparator<in T>
): Int {
    val x = list[a]
    val y = list[b]
    val z = list[c]
    return when {
        comparator.compare(x, y) <= 0 && comparator.compare(y, z) <= 0 -> b
        comparator.compare(x, z) <= 0 && comparator.compare(z, y) <= 0 -> c
        else -> a
    }
}

private fun <T> swap(list: MutableList<T>, i: Int, j: Int) {
    val tmp = list[i]
    list[i] = list[j]
    list[j] = tmp
}
