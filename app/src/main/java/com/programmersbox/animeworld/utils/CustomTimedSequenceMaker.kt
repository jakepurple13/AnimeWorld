package com.programmersbox.animeworld.utils

import android.content.Context
import android.os.CountDownTimer
import android.widget.Toast
import androidx.annotation.CallSuper

class CustomTimedSequenceMaker<T>(private val context: Context, sequence: List<T>, private val timeout: Long = 5000, sequenceAchieved: () -> Unit) :
    CustomSequenceMaker<T>(sequence, sequenceAchieved) {
    constructor(context: Context, vararg sequence: T, timeout: Long = 5000, sequenceAchieved: () -> Unit) : this(
        context,
        sequence.toList(),
        timeout,
        sequenceAchieved
    )

    private var toast: Toast? = null

    private val timeoutTimer: CountDownTimer? = if (timeout <= 0) null else object : CountDownTimer(timeout, 1000) {
        override fun onTick(millisUntilFinished: Long) = Unit
        override fun onFinish(): Unit = resetSequence().also { sequenceFailed() }
    }

    override fun nextItem(item: T) = timeoutTimer?.start().let { Unit }

    override fun internalAchieved() {
        super.internalAchieved()
        toast?.cancel()
        toast = Toast.makeText(
            context,
            "You have activated Developer Mode",
            Toast.LENGTH_LONG
        )
        toast?.show()
    }

    override fun addNewItem(item: T) {
        if (correctSequence().size - currentSequence().size < 5 && !context.developerModeActivated) {
            toast?.cancel()
            toast = Toast.makeText(
                context,
                "You are ${correctSequence().size - currentSequence().size} steps away from activating Developer Mode",
                Toast.LENGTH_SHORT
            )
            toast?.show()
        }
        timeoutTimer?.cancel()
        super.addNewItem(item)
    }
}

open class CustomSequenceMaker<T>(private val sequence: List<T>, private val sequenceAchieved: () -> Unit) {
    constructor(vararg sequence: T, sequenceAchieved: () -> Unit) : this(sequence.toList(), sequenceAchieved)

    protected var sequenceFailed: () -> Unit = {}
    private val currentSequence = mutableListOf<T>()
    fun sequenceReset(block: () -> Unit) = apply { sequenceFailed = block }
    fun resetSequence() = currentSequence.clear()
    private fun validateSequence() = currentSequence.lastIndex.let { currentSequence[it] == sequence[it] }
    private fun isAchieved() = currentSequence == sequence
    protected open fun nextItem(item: T) = Unit
    fun currentSequence() = currentSequence.toList()
    fun correctSequence() = sequence.toList()
    operator fun plusAssign(order: T) = add(order)
    operator fun plusAssign(list: Iterable<T>) = add(list)
    operator fun plusAssign(items: Array<T>) = add(*items)
    fun add(list: Iterable<T>) = list.forEach { add(it) }
    fun add(vararg items: T) = items.forEach(::addNewItem)

    @CallSuper
    protected open fun internalAchieved() = sequenceAchieved()

    @CallSuper
    protected open fun addNewItem(item: T) = addItem(item)

    private fun addItem(item: T) {
        currentSequence += item
        if (validateSequence()) {
            if (isAchieved()) {
                internalAchieved()
                resetSequence()
            } else nextItem(item)
        } else resetSequence().also { sequenceFailed() }
    }
}