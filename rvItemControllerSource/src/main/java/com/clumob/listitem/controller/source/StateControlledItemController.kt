package com.clumob.listitem.controller.source

import java.util.*

/**
 * Created by prashant.rathore on 21/09/18.
 */
abstract class StateControlledItemController : ItemController {
    var itemUpdatePublisher: ItemUpdatePublisher? = null
        private set

    internal enum class State {
        IDLE, CREATED, ATTACHED, DESTROYED
    }

    private var currentState = State.IDLE
    private val attachedSources: MutableSet<Any> = HashSet()
    override fun onCreate(itemUpdatePublisher: ItemUpdatePublisher) {
        this.itemUpdatePublisher = itemUpdatePublisher
        when (currentState) {
            State.IDLE, State.DESTROYED -> {
                currentState = State.CREATED
                created()
            }
        }
    }

    abstract fun created()
    override fun onAttach(source: Any) {
        if (attachedSources.size > 0) {
            attachedSources.add(source)
            return
        }
        attachedSources.add(source)
        when (currentState) {
            State.IDLE -> {
                onCreate(itemUpdatePublisher!!)
                currentState = State.CREATED
                currentState = State.ATTACHED
                attached()
            }
            State.CREATED -> {
                currentState = State.ATTACHED
                attached()
            }
            State.DESTROYED -> {
                onCreate(itemUpdatePublisher!!)
                attached()
                currentState = State.ATTACHED
            }
        }
    }

    abstract fun attached()
    override fun onDetach(source: Any) {
        if (source != null && !attachedSources.contains(source)) {
            return
        }
        attachedSources.remove(source)
        if (attachedSources.size > 0) {
            return
        }
        when (currentState) {
            State.DESTROYED, State.IDLE -> {
                onCreate(itemUpdatePublisher!!)
                currentState = State.CREATED
            }
            State.CREATED -> {
            }
            State.ATTACHED -> {
                currentState = State.CREATED
                detached()
            }
        }
    }

    abstract fun detached()
    override fun onDestroy() {
        when (currentState) {
            State.DESTROYED, State.IDLE -> {
            }
            State.CREATED -> {
                currentState = State.DESTROYED
                destroyed()
            }
            State.ATTACHED -> {
                currentState = State.DESTROYED
                detached()
                destroyed()
            }
        }
    }

    abstract fun destroyed()
}