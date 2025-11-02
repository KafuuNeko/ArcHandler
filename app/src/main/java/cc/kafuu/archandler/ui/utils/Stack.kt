package cc.kafuu.archandler.ui.utils

class Stack<T>() {
    private val data = ArrayList<T>()

    constructor(other: Stack<T>) : this() {
        data.addAll(other.data)
    }

    fun push(item: T) = data.add(item)
    fun pop(): T = data.removeAt(data.size - 1)
    fun peek(): T = data.last()
    fun popOrNull(): T? = if (isEmpty()) null else pop()
    fun peekOrNull(): T? = data.lastOrNull()
    fun isEmpty(): Boolean = data.isEmpty()
    val size: Int get() = data.size
}
