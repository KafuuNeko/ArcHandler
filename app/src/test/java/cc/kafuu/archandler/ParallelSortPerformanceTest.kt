package cc.kafuu.archandler

import cc.kafuu.archandler.libs.utils.parallelSortWith
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class ParallelSortPerformanceTest {

    companion object {
        private const val WARMUP_ROUNDS = 2
        private const val TEST_ROUNDS = 5
    }

    @Test
    fun testParallelSortPerformance() {
        println("\n========== ParallelSortWith Performance Test ==========\n")

        val testSizes = listOf(1000, 10000, 50000, 100000, 500000)

        for (size in testSizes) {
            println("Testing with $size elements...")
            testWithSize(size)
            println()
        }
    }

    private fun testWithSize(size: Int) {
        val origin = generateRandomData(size).toList()
        // 预热
        repeat(WARMUP_ROUNDS) {
            val data = origin.toMutableList()
            runBlocking {
                data.parallelSortWith(compareBy { it })
            }
        }

        // 并行排序性能测试
        val parallelTimes = mutableListOf<Long>()
        repeat(TEST_ROUNDS) {
            val data = origin.toMutableList()
            val time = measureTimeMillis {
                runBlocking {
                    data.parallelSortWith(compareBy { it })
                }
            }
            parallelTimes.add(time)

            // 验证排序正确性
            assert(isSorted(data)) { "Parallel sort failed - data not sorted correctly!" }
        }

        // 单线程排序性能测试
        val singleThreadTimes = mutableListOf<Long>()
        repeat(TEST_ROUNDS) {
            val data = origin.toMutableList()
            val time = measureTimeMillis {
                data.sortWith(compareBy { it })
            }
            singleThreadTimes.add(time)

            // 验证排序正确性
            assert(isSorted(data)) { "Single thread sort failed - data not sorted correctly!" }
        }

        // 计算统计数据
        val avgParallel = parallelTimes.average()
        val avgSingle = singleThreadTimes.average()
        val minParallel = parallelTimes.minOrNull() ?: 0L
        val minSingle = singleThreadTimes.minOrNull() ?: 0L
        val speedup = avgSingle / avgParallel

        println("  Single-threaded sort:")
        println("    Average: ${avgSingle.toInt()} ms")
        println("    Best: $minSingle ms")
        println("    All times: $singleThreadTimes")

        println("  Parallel sort:")
        println("    Average: ${avgParallel.toInt()} ms")
        println("    Best: $minParallel ms")
        println("    All times: $parallelTimes")

        println("  Speedup: ${String.format("%.2f", speedup)}x")

        if (speedup > 1.0) {
            println("  ✓ Parallel sort is ${String.format("%.1f", (speedup - 1) * 100)}% faster")
        } else {
            println("  ✗ Parallel sort is slower (may need larger dataset or adjust threshold)")
        }
    }

    @Test
    fun testThreadUtilization() {
        println("\n========== Thread Utilization Test ==========\n")

        val size = 100000
        println("Testing thread utilization with $size elements...")

        // 获取可用处理器数量
        val processors = Runtime.getRuntime().availableProcessors()
        println("Available processors: $processors")

        // 创建测试数据
        val data = generateRandomData(size)

        // 使用自定义比较器来追踪比较操作
        val comparisons = java.util.concurrent.atomic.AtomicInteger(0)
        val threadIds = java.util.concurrent.ConcurrentHashMap.newKeySet<Long>()

        val comparator = Comparator<Int> { a, b ->
            comparisons.incrementAndGet()
            threadIds.add(Thread.currentThread().id)
            a.compareTo(b)
        }

        // 执行并行排序
        val time = measureTimeMillis {
            runBlocking {
                data.parallelSortWith(comparator)
            }
        }

        println("\nResults:")
        println("  Time: $time ms")
        println("  Total comparisons: ${comparisons.get()}")
        println("  Unique threads used: ${threadIds.size}")
        println("  Thread IDs: ${threadIds.sorted()}")

        // 验证是否使用了多线程
        if (threadIds.size > 1) {
            println("  ✓ Successfully utilized multiple threads")
            println("  ✓ Thread utilization: ${String.format("%.1f", threadIds.size.toDouble() / processors * 100)}% of available processors")
        } else {
            println("  ✗ WARNING: Only used single thread!")
        }

        // 验证排序正确性
        assert(isSorted(data)) { "Sort failed - data not sorted correctly!" }
        println("  ✓ Sort correctness verified")
    }

    @Test
    fun testWorstCaseScenario() {
        println("\n========== Worst Case Scenario Test ==========\n")

        val sizes = listOf(10000, 50000, 100000)

        for (size in sizes) {
            println("Testing with $size elements...")

            // 已排序数据（最坏情况对某些快排实现）
            val sortedData = (1..size).toMutableList()
            val reversedData = (size downTo 1).toMutableList()

            // 测试已排序数据
            val sortedTime = measureTimeMillis {
                runBlocking {
                    sortedData.parallelSortWith(compareBy { it })
                }
            }

            // 测试逆序数据
            val reversedTime = measureTimeMillis {
                runBlocking {
                    reversedData.parallelSortWith(compareBy { it })
                }
            }

            println("  Sorted data: $sortedTime ms")
            println("  Reversed data: $reversedTime ms")

            assert(isSorted(sortedData)) { "Sorted data test failed!" }
            assert(isSorted(reversedData)) { "Reversed data test failed!" }
            println("  ✓ Both scenarios handled correctly")
            println()
        }
    }

    @Test
    fun testCorrectness() {
        println("\n========== Correctness Test ==========\n")

        val testCases = listOf(
            "Empty list" to mutableListOf<Int>(),
            "Single element" to mutableListOf(1),
            "Two elements" to mutableListOf(2, 1),
            "Already sorted" to mutableListOf(1, 2, 3, 4, 5),
            "Reverse sorted" to mutableListOf(5, 4, 3, 2, 1),
            "With duplicates" to mutableListOf(3, 1, 4, 1, 5, 9, 2, 6, 5, 3),
            "Random 1000" to generateRandomData(1000),
            "Random 10000" to generateRandomData(10000)
        )

        for ((name, data) in testCases) {
            val original = data.toList()
            runBlocking {
                data.parallelSortWith(compareBy { it })
            }

            val isCorrect = isSorted(data) && data.size == original.size
            println("  ${if (isCorrect) "✓" else "✗"} $name: ${if (isCorrect) "PASS" else "FAIL"}")

            if (!isCorrect) {
                println("    Original: $original")
                println("    Sorted: $data")
            }

            assert(isCorrect) { "Test failed for: $name" }
        }

        println("\n  All correctness tests passed!")
    }

    private fun generateRandomData(size: Int): MutableList<Int> {
        val random = Random(System.currentTimeMillis())
        return MutableList(size) { random.nextInt() }
    }

    private fun isSorted(list: List<Int>): Boolean {
        for (i in 0 until list.size - 1) {
            if (list[i] > list[i + 1]) {
                return false
            }
        }
        return true
    }
}
