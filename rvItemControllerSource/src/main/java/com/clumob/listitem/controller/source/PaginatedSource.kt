package com.clumob.listitem.controller.source

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.*

/**
 * Created by prashant.rathore on 03/07/18.
 */
class PaginatedSource<T : ItemController>(private val loadingItemItemController: T,
                                          private val threshHold: Int,
                                          private val callbacks: PagenatedCallbacks)
    : ItemControllerSource<T>() {

    private val sources: MutableList<PaginatedSourceItem> = LinkedList()
    private var hasMoreBottomPage = false
    private var hasMoreTopPage = false
    private var isAttached = false
    private val safeLimit = 20
    private var lastItemAttached = -1
    private var cachedLastItemAttached = -1
    private var trimPagesRunnable: Runnable? = null
    private val itemUpdatePublisher = ItemUpdatePublisher()

    override var viewInteractor: ViewInteractor? = null
        set(value) {
            field = value
            sources.onEach {
                it.source.viewInteractor = viewInteractor
            }
        }


    private fun bloatPagesOnContentChange() {
        val subscribe = observeAdapterUpdates().subscribe(object : Consumer<SourceUpdateEvent> {
            var lastIndex = 0
            @Throws(Exception::class)
            override fun accept(sourceUpdateEvent: SourceUpdateEvent) {
                when (sourceUpdateEvent.type) {
                    SourceUpdateEvent.Type.UPDATE_BEGINS -> lastIndex = cachedLastItemAttached
                    SourceUpdateEvent.Type.ITEMS_ADDED -> if (sourceUpdateEvent.position <= lastIndex) lastIndex += sourceUpdateEvent.itemCount
                    SourceUpdateEvent.Type.ITEMS_REMOVED -> if (sourceUpdateEvent.position <= lastIndex) {
                        lastIndex -= sourceUpdateEvent.itemCount
                    }
                    SourceUpdateEvent.Type.UPDATE_ENDS -> if (lastIndex >= 0) {
                        bloatPages(this.lastIndex)
                        lastIndex = -1
                    }
//                    SourceUpdateEvent.Type.ITEMS_CHANGED -> TODO("ITEMS_CHANGED")
                }
            }

        })
    }


    override fun onAttachToView() {
        loadingItemItemController.performCreate(itemUpdatePublisher)
        for (item in sources) {
            item.source.onAttachToView()
        }
        isAttached = true
    }

    override fun onItemAttached(position: Int) {
        lastItemAttached = position
        cachedLastItemAttached = lastItemAttached
        trimPagesSafely(position)
        bloatPages(position)
        if (position == itemCount - 1 && hasMoreBottomPage) {
            return
        } else if (hasMoreTopPage) {
            if (position == 0) {
                return
            }
        }
        val adapterAsItem = decodeAdapterItem(position)
        adapterAsItem?.source?.onItemAttached(position - adapterAsItem.startPosition)
    }

    private fun bloatPages(attachedIndex: Int) {
        if (hasMoreBottomPage && attachedIndex + threshHold > itemCount) {
            callbacks.loadNextBottomPage()
        }
        if (hasMoreTopPage && attachedIndex - threshHold < 0) {
            callbacks.loadNextTopPage()
        }
    }

    fun addPageOnTop(page: ItemControllerSource<T>) {
        addPageOnTopWhenSafe(page)
    }

    private fun addPageOnTopWhenSafe(page: ItemControllerSource<T>) {
        processWhenSafe(Runnable { addPageOnTopInternal(page) })
    }

    private fun addPageOnTopInternal(page: ItemControllerSource<T>) {
        val item = PaginatedSourceItem(page)
        item.source.viewInteractor = viewInteractor
        val oldHadMoreTopPage = hasMoreTopPage
        var startPosition = if (oldHadMoreTopPage) 1 else 0
        hasMoreTopPage = callbacks.hasMoreTopPage()
        if (hasMoreTopPage != oldHadMoreTopPage) {
            startPosition = if (oldHadMoreTopPage) {
                0
            } else {
                1
            }
        }
        item.startPosition = startPosition
        sources.add(0, item)
        updateIndexes(item)
        if (isAttached) {
            item.attach()
        }
        beginUpdates()
        if (hasMoreTopPage != oldHadMoreTopPage) {
            if (oldHadMoreTopPage) {
                if (page.itemCount > 0) {
                    notifyItemsChanged(0, 1)
                    notifyItemsInserted(1, page.itemCount - 1)
                } else {
                    notifyItemsRemoved(0, 1)
                }
            } else {
                notifyItemsInserted(0, page.itemCount + 1)
            }
        } else if (hasMoreTopPage) {
            notifyItemsChanged(0, 1)
            notifyItemsInserted(1, page.itemCount - 1)
            notifyItemsInserted(0, 1)
        } else if (oldHadMoreTopPage) {
            notifyItemsChanged(0, 1)
            notifyItemsInserted(1, page.itemCount - 1)
        } else {
            notifyItemsInserted(startPosition, page.itemCount)
        }
        endUpdates()
    }

    fun addPageInBottom(page: ItemControllerSource<T>) {
        addPagInBottomWhenSafe(page)
    }

    private fun addPagInBottomWhenSafe(page: ItemControllerSource<T>) {
        processWhenSafe(Runnable { addPageInBottomInternal(page) })
    }

    private fun addPageInBottomInternal(page: ItemControllerSource<T>) {
        val oldHadMoreBottomPages = hasMoreBottomPage
        hasMoreBottomPage = callbacks.hasMoreBottomPage()
        val item = PaginatedSourceItem(page)
        val startPosition = itemCount - if (oldHadMoreBottomPages) 1 else 0
        item.startPosition = startPosition
        item.source.viewInteractor = viewInteractor
        sources.add(item)
        if (isAttached) {
            item.attach()
        }
        beginUpdates()
        if (hasMoreBottomPage != oldHadMoreBottomPages) {
            if (oldHadMoreBottomPages) {
                if (page.itemCount > 0) {
                    notifyItemsChanged(startPosition, 1)
                    notifyItemsInserted(startPosition, page.itemCount - 1)
                } else {
                    notifyItemsRemoved(startPosition, 1)
                }
            } else {
                notifyItemsInserted(startPosition, page.itemCount + 1)
            }
        } else {
            notifyItemsInserted(startPosition, page.itemCount)
        }
        endUpdates()
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getItemPosition(item: T): Int {
        if (loadingItemItemController === item) {
            return if (hasMoreTopPage) {
                0
            } else {
                itemCount - 1
            }
        } else {
            for (source in sources) {
                val source1: ItemControllerSource<T> = source.source
                val itemPosition = source1.getItemPosition(item)
                if (itemPosition >= 0) {
                    return itemPosition + source.startPosition
                }
            }
        }
        return -1
    }

    override fun getItemForPosition(position: Int): T {
        return if (position == 0 && hasMoreTopPage) {
            loadingItemItemController
        } else if (position == itemCount - 1 && hasMoreBottomPage) {
            loadingItemItemController
        } else {
            val item = decodeAdapterItem(position)
            item!!.source.getItem(position - item.startPosition)
        }
    }

    private fun decodeAdapterItem(position: Int): PaginatedSourceItem? {
        var previous: PaginatedSourceItem? = null
        for (adapterAsItem in sources) {
            previous = if (adapterAsItem.startPosition > position) {
                return previous
            } else {
                adapterAsItem
            }
        }
        return previous
    }

    //    @Override
//    public void onItemDetached(int position) {
//        trimPagesSafely(position);
//        bloatPages(position);
//        if (position == getItemCount() - 1 && hasMoreBottomPage) {
//            return;
//        } else if (hasMoreTopPage) {
//            if (position == 0) {
//                return;
//            }
//        }
//        PaginatedSourceItem adapterAsItem = decodeAdapterItem(position);
//        if(adapterAsItem == null) {
//            System.out.print("");
//        }
//        adapterAsItem.source.onItemDetached(position);
//    }
    private fun trimPagesSafely(position: Int) {
        cancelOldProcess(trimPagesRunnable)
        trimPagesRunnable = Runnable { trimPages(position) }
        processWhenSafe(trimPagesRunnable!!)
    }

    private fun trimPages(dii: Int) {
        var detachedItemPosition = dii
        if (lastItemAttached < 0) {
            return
        }
        beginUpdates()
        detachedItemPosition = lastItemAttached
        lastItemAttached = -1
        val didTripBottomPages = trimBottomPage(detachedItemPosition)
        val didTrimTopPages = trimTopPage(detachedItemPosition)
        if (didTrimTopPages || didTripBottomPages) endUpdates()
    }

    private fun trimTopPage(detachedItemPosition: Int): Boolean {
        var success = false
        if (detachedItemPosition - safeLimit > 0) {
            val safePosition = detachedItemPosition - safeLimit
            var removedItems = 0
            val removed: MutableList<PaginatedSourceItem> = ArrayList()
            while (0 < sources.size) {
                val paginatedSourceItem = sources[0]
                if (paginatedSourceItem.startPosition + paginatedSourceItem.source.itemCount < safePosition) {
                    val remove = sources.removeAt(0)
                    removed.add(remove)
                    callbacks.unloadingTopPage(paginatedSourceItem.source)
                    success = true
                    removedItems += paginatedSourceItem.source.itemCount
                } else {
                    if (success) {
                        var startPosition = 0
                        if (hasMoreTopPage != callbacks.hasMoreTopPage()) {
                            hasMoreTopPage = !hasMoreTopPage
                            if (!hasMoreTopPage) {
                                startPosition = 0
                                removedItems++
                            } else {
                                startPosition = 1
                                removedItems--
                                notifyItemsChanged(0, 1)
                            }
                        } else if (hasMoreTopPage) {
                            startPosition = 1
                        }
                        resetIndexes(startPosition)
                        notifyItemsRemoved(startPosition, removedItems)
                    }
                    break
                }
            }
            for (item in removed) {
                item.destroy()
            }
        }
        return success
    }

    fun refreshTopPageAvailability() {
        viewInteractor!!.processWhenSafe(Runnable { refreshTopPageAvailabilityInternal() })
    }

    private fun refreshTopPageAvailabilityInternal() {
        if (hasMoreTopPage != callbacks.hasMoreTopPage()) {
            hasMoreTopPage = callbacks.hasMoreTopPage()
            val startPosition: Int
            beginUpdates()
            startPosition = if (hasMoreTopPage) {
                notifyItemsInserted(0, 1)
                1
            } else {
                notifyItemsRemoved(0, 1)
                0
            }
            resetIndexes(startPosition)
            endUpdates()
        }
    }

    fun refreshBottomPageAvailability() {
        viewInteractor!!.processWhenSafe(Runnable { refreshBottomPageAvailabilityInternal() })
    }

    private fun refreshBottomPageAvailabilityInternal() {
        val hasMoreBottomPage = callbacks.hasMoreBottomPage()
        if (this.hasMoreBottomPage != hasMoreBottomPage) {
            val count = itemCount
            this.hasMoreBottomPage = hasMoreBottomPage
            beginUpdates()
            if (this.hasMoreBottomPage) {
                notifyItemsInserted(count, 1)
            } else {
                notifyItemsRemoved(count - 1, 1)
            }
            endUpdates()
        }
    }

    private fun trimBottomPage(detachedItemPosition: Int): Boolean {
        var success = false
        if (detachedItemPosition + safeLimit < itemCount) {
            val safePosition = detachedItemPosition + safeLimit
            var removedItemsCount = 0
            var startPosition = 0
            val didHaveMoreBottomItems = hasMoreBottomPage
            val removedItems: MutableList<PaginatedSourceItem> = ArrayList()
            for (i in sources.indices.reversed()) {
                val paginatedSourceItem = sources[i]
                if (paginatedSourceItem.startPosition > safePosition) {
                    val remove = sources.removeAt(i)
                    removedItems.add(remove)
                    removedItemsCount += paginatedSourceItem.source.itemCount
                    callbacks.unloadingBottomPage(paginatedSourceItem.source)
                    success = true
                    startPosition = paginatedSourceItem.startPosition
                } else {
                    if (removedItemsCount > 0) {
                        if (didHaveMoreBottomItems != callbacks.hasMoreBottomPage()) {
                            hasMoreBottomPage = !hasMoreBottomPage
                            if (didHaveMoreBottomItems) {
                                removedItemsCount++
                            } else {
                                removedItemsCount--
                                notifyItemsChanged(startPosition + removedItemsCount, 1)
                            }
                        }
                        notifyItemsRemoved(startPosition, removedItemsCount)
                    }
                    break
                }
            }
            for (item in removedItems) {
                item.destroy()
            }
        }
        return success
    }

    override fun onDetachFromView() {
        loadingItemItemController.performDestroy()
        for (item in sources) {
            item.destroy()
        }
        isAttached = false
    }

    override fun computeItemCount(): Int {
        hasMoreTopPage = callbacks.hasMoreTopPage()
        var count = if (hasMoreTopPage) 1 else 0
        resetIndexes(count)
        if (sources.size > 0) {
            val paginatedSourceItem = sources[sources.size - 1]
            count += paginatedSourceItem.startPosition + paginatedSourceItem.source.itemCount
        }
        hasMoreBottomPage = callbacks.hasMoreBottomPage()
        if (hasMoreBottomPage) {
            count++
        }
        return count
    }

    interface PagenatedCallbacks {
        fun hasMoreBottomPage(): Boolean
        fun hasMoreTopPage(): Boolean
        fun loadNextBottomPage()
        fun loadNextTopPage()
        fun unloadingTopPage(source: ItemControllerSource<*>?)
        fun unloadingBottomPage(source: ItemControllerSource<*>?)
    }

    private fun resetIndexes(startIndex: Int) {
        if (sources.size > 0) {
            val item = sources[0]
            item.startPosition = startIndex
            updateIndexes(item)
        }
    }

    fun updateIndexes(mi: PaginatedSourceItem) {
        var modifiedItem = mi
        var continueUpdating = false
        for (item in sources) {
            if (continueUpdating) {
                item.startPosition = modifiedItem.startPosition + modifiedItem.source.itemCount
                modifiedItem = item
            } else if (item === modifiedItem) {
                continueUpdating = true
            }
        }
    }

    inner class PaginatedSourceItem(val source: ItemControllerSource<T>) {
        var startPosition = 0
        var isAttached = false
        val updateObserver: Disposable
        fun transformUpdateEvent(event: SourceUpdateEvent) {
            val actualStartPosition = startPosition + event.position
            when (event.type) {
                SourceUpdateEvent.Type.UPDATE_BEGINS -> beginUpdates()
                SourceUpdateEvent.Type.ITEMS_CHANGED -> notifyItemsChanged(actualStartPosition, event.itemCount)
                SourceUpdateEvent.Type.ITEMS_REMOVED -> notifyItemsRemoved(actualStartPosition, event.itemCount)
                SourceUpdateEvent.Type.ITEMS_ADDED -> notifyItemsInserted(actualStartPosition, event.itemCount)
                SourceUpdateEvent.Type.ITEMS_MOVED -> {
                }
                SourceUpdateEvent.Type.UPDATE_ENDS -> endUpdates()
                SourceUpdateEvent.Type.HAS_STABLE_IDS -> {
                }
            }
            updateIndexes(this)
        }

        fun attach() {
            if (!this.isAttached) {
                this.isAttached = true
                source.onAttachToView()
            }
        }

        fun destroy() {
            if (this.isAttached) {
                source.onDetachFromView()
                this.isAttached = false
            }
        }

        init {
            updateObserver = source.observeAdapterUpdates().subscribe { event: SourceUpdateEvent -> transformUpdateEvent(event) }
        }
    }

    init {
        val itemCount = computeItemCount()
        notifyItemsInserted(0, itemCount)
        bloatPagesOnContentChange()
    }
}