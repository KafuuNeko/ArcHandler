package cc.kafuu.archandler.ui.utils

class Stack<T>(private val mMaxSize: Int = Int.MAX_VALUE) {

    private val mData = ArrayList<T>()

    constructor(other: Stack<T>, maxSize: Int = Int.MAX_VALUE) : this(maxSize) {
        mData.addAll(other.mData)
    }

    val size: Int get() = mData.size
    fun isEmpty(): Boolean = mData.isEmpty()

    fun push(item: T) {
        mData.add(item)
        if (mData.size > mMaxSize) mData.removeAt(0)
    }

    fun pop(): T = mData.removeAt(mData.size - 1)
    fun popOrNull(): T? = mData.lastOrNull()?.also { mData.removeAt(mData.size - 1) }

    fun peek(): T = mData.last()
    fun peekOrNull(): T? = mData.lastOrNull()

    fun popBottom(): T = mData.removeAt(0)
    fun popBottomOrNull(): T? = mData.firstOrNull()?.also { mData.removeAt(0) }

    fun clear() = mData.clear()
    fun toList(): List<T> = mData.toList()
}
