//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.clumob.listitem.controller.source

class BatchingListUpdateCallback(val mWrapped: ListUpdateCallback) : ListUpdateCallback {
    var mLastEventType = 0
    var mLastEventPosition = -1
    var mLastEventCount = -1
    var mLastEventPayload: Any? = null
    fun dispatchLastEvent() {
        if (mLastEventType != 0) {
            when (mLastEventType) {
                1 -> mWrapped.onInserted(mLastEventPosition, mLastEventCount)
                2 -> mWrapped.onRemoved(mLastEventPosition, mLastEventCount)
                3 -> mWrapped.onChanged(mLastEventPosition, mLastEventCount, mLastEventPayload!!)
            }
            mLastEventPayload = null
            mLastEventType = 0
        }
    }

    override fun onInserted(position: Int, count: Int) {
        if (mLastEventType == 1 && position >= mLastEventPosition && position <= mLastEventPosition + mLastEventCount) {
            mLastEventCount += count
            mLastEventPosition = Math.min(position, mLastEventPosition)
        } else {
            dispatchLastEvent()
            mLastEventPosition = position
            mLastEventCount = count
            mLastEventType = 1
        }
    }

    override fun onRemoved(position: Int, count: Int) {
        if (mLastEventType == 2 && mLastEventPosition >= position && mLastEventPosition <= position + count) {
            mLastEventCount += count
            mLastEventPosition = position
        } else {
            dispatchLastEvent()
            mLastEventPosition = position
            mLastEventCount = count
            mLastEventType = 2
        }
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        dispatchLastEvent()
        mWrapped.onMoved(fromPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any) {
        if (mLastEventType == 3 && position <= mLastEventPosition + mLastEventCount && position + count >= mLastEventPosition && mLastEventPayload === payload) {
            val previousEnd = mLastEventPosition + mLastEventCount
            mLastEventPosition = Math.min(position, mLastEventPosition)
            mLastEventCount = Math.max(previousEnd, position + count) - mLastEventPosition
        } else {
            dispatchLastEvent()
            mLastEventPosition = position
            mLastEventCount = count
            mLastEventPayload = payload
            mLastEventType = 3
        }
    }

    companion object {
        private const val TYPE_NONE = 0
        private const val TYPE_ADD = 1
        private const val TYPE_REMOVE = 2
        private const val TYPE_CHANGE = 3
    }

}