package com.clumob.listitem.controller.source

import com.clumob.listitem.controller.source.SourceUpdateEvent.Companion.builder
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by prashant.rathore on 24/06/18.
 */
abstract class ItemControllerSource<Controller : ItemController> {
    private val updateEventPublisher = PublishSubject.create<SourceUpdateEvent>()
    var itemCount = 0
        private set
    private var maxCount = -1
    private var hasMaxLimit = false

    open var viewInteractor: ViewInteractor? = null

    private var lastItem: Controller? = null
    //    ItemControllerSource<Item,Controller> getRootAdapter(int position);
    var lastItemIndex = 0
        private set

    abstract fun onAttached()
    abstract fun onItemAttached(position: Int)
    abstract fun hasStableIds(): Boolean
    fun observeAdapterUpdates(): Observable<SourceUpdateEvent> {
        return updateEventPublisher
    }

    fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    fun getItemType(position: Int): Int {
        return getItem(position).type
    }

    fun setMaxLimit(limit: Int) {
        require(limit >= 0) { "Max Limit cannot be < 0" }
        processWhenSafe(Runnable { setMaxLimitWhenSafe(limit) })
    }

    private fun setMaxLimitWhenSafe(limit: Int) {
        hasMaxLimit = true
        maxCount = limit
        if (maxCount < itemCount) {
            notifyItemsRemoved(maxCount, itemCount - maxCount)
        }
    }

    fun removeMaxLimit() {
        processWhenSafe(Runnable { removeMaxLimitWhenSafe() })
    }

    private fun removeMaxLimitWhenSafe() {
        hasMaxLimit = false
        val oldItemCount = itemCount
        val newItemCount = computeItemCount()
        if (oldItemCount < newItemCount) {
            val diff = newItemCount - oldItemCount
            notifyItemsInserted(oldItemCount, diff)
        }
    }

    abstract fun getItemPosition(item: Controller): Int
    fun getItem(position: Int): Controller {
        return if (lastItemIndex == position) {
            lastItem!!
        } else {
            val item = getItemForPosition(position)
            lastItem = item
            lastItemIndex = position
            item
        }
    }

    abstract fun getItemForPosition(position: Int): Controller
    //    public abstract void onItemDetached(int position);
    abstract fun onDetached()

    fun notifyItemsInserted(startPosition: Int, itemsInserted: Int) {
        resetCachedItems(startPosition)
        val oldItemCount = itemCount
        itemCount = computeItemCountOnItemsInserted(startPosition, itemsInserted)
        val diff = itemCount - oldItemCount
        publishUpdateEvent(startPosition, SourceUpdateEvent.Type.ITEMS_ADDED, diff)
        resetCachedItems(startPosition)
    }

    private fun computeItemCountOnItemsInserted(startPosition: Int, itemCount: Int): Int {
        return if (hasMaxLimit) itemCountIfLimitEnabled(startPosition + itemCount) else this.itemCount + itemCount
    }

    fun notifyItemsRemoved(startPosition: Int, itemsRemoved: Int) {
        resetCachedItems(startPosition)
        val oldItemCount = itemCount
        itemCount = computeItemCountOnItemsRemoved(oldItemCount, itemsRemoved)
        val diff = oldItemCount - itemCount
        publishUpdateEvent(startPosition, SourceUpdateEvent.Type.ITEMS_REMOVED, diff)
        resetCachedItems(startPosition)
    }

    private fun computeItemCountOnItemsRemoved(oldItemCount: Int, itemCount: Int): Int {
        return if (hasMaxLimit) itemCountIfLimitEnabled(oldItemCount - itemCount) else oldItemCount - itemCount
    }

    private fun itemCountIfLimitEnabled(newItemCount: Int): Int {
        return Math.min(newItemCount, maxCount)
    }

    private fun resetCachedItems(startPosition: Int) {
        if (startPosition <= lastItemIndex) {
            lastItemIndex = -1
            lastItem = null
        }
    }

    protected abstract fun computeItemCount(): Int

    private fun publishUpdateEvent(startPosition: Int, type: SourceUpdateEvent.Type, itemCount: Int) {
        updateEventPublisher.onNext(builder()
                .setItemCount(itemCount)
                .setPosition(startPosition)
                .setType(type)
                .build())
    }

    fun notifyItemsChanged(startIndex: Int, itemCount: Int) {
        if (this.itemCount > startIndex) {
            resetCachedItems(startIndex)
            publishUpdateEvent(startIndex, SourceUpdateEvent.Type.ITEMS_CHANGED, Math.min(this.itemCount - startIndex, itemCount))
        }
    }

    fun endUpdates() {
        publishUpdateEvent(0, SourceUpdateEvent.Type.UPDATE_ENDS, 0)
    }

    fun beginUpdates() {
        publishUpdateEvent(0, SourceUpdateEvent.Type.UPDATE_BEGINS, 0)
    }

    protected fun processWhenSafe(runnable: Runnable) {
        viewInteractor?.processWhenSafe(runnable) ?: runnable.run()
    }

    protected fun cancelOldProcess(runnable: Runnable?) {
        if (runnable != null) viewInteractor?.cancelOldProcess(runnable)
    }

    fun notifyItemsMoved(fromPosition: Int, toPosition: Int) {}

    interface ViewInteractor {
        fun processWhenSafe(runnable: Runnable)
        fun cancelOldProcess(runnable: Runnable)
    }

}