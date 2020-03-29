package com.clumob.listitem.controller.source

import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import java.util.*

/**
 * Created by prashant.rathore on 24/06/18.
 */
class MultiplexSource : ItemControllerSource<ItemController>() {
    private val adapters: MutableList<AdapterAsItem> = ArrayList()
    private var isAttached = false
    override fun onAttachToView() {
        isAttached = true
        for (item in adapters) {
            item.adapter.onAttachToView()
        }
    }

    override var viewInteractor: ViewInteractor? = null
        set(value) {
            field = value
            adapters.onEach {
                it.adapter.viewInteractor = viewInteractor
            }
        }


    override fun onItemAttached(position: Int) {
        val adapterAsItem = decodeAdapterItem(position)
        adapterAsItem!!.adapter.onItemAttached(position - adapterAsItem.startPosition)
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    fun addAdapter(adapter: ItemControllerSource<ItemController>) {
        addSource(adapters.size, adapter)
    }

    fun addSource(index: Int, adapter: ItemControllerSource<ItemController>) {
        val item = AdapterAsItem(adapter)
        adapter.viewInteractor = viewInteractor
        processWhenSafe(Runnable { addSourceImmediate(index, item) })
    }

    private fun addSourceImmediate(index: Int, item: AdapterAsItem) {
        if (adapters.size > index) {
            val previousItem = adapters[index]
            item.startPosition = previousItem.startPosition
        } else if (adapters.size > 0) {
            val lastItem = adapters[adapters.size - 1]
            item.startPosition = lastItem.startPosition + lastItem.adapter.itemCount
        }
        adapters.add(index, item)
        updateIndexes(item)
        if (isAttached) {
            item.adapter.onAttachToView()
        }
        beginUpdates()
        notifyItemsInserted(item.startPosition, item.adapter.itemCount)
        endUpdates()
    }

    override fun computeItemCount(): Int {
        if (adapters.size > 0) {
            val item = adapters[adapters.size - 1]
            return item.startPosition + item.adapter.itemCount
        }
        return 0
    }

    override fun getItemForPosition(position: Int): ItemController {
        val item = decodeAdapterItem(position)
        return item!!.adapter.getItem(position - item.startPosition)
    }

    //    @Override
//    public void onItemDetached(int position) {
//        AdapterAsItem adapterAsItem = decodeAdapterItem(position);
//        adapterAsItem.adapter.onItemDetached(position - adapterAsItem.startPosition);
//    }
    override fun onDetachFromView() {
        for (item in adapters) {
            item.adapter.onDetachFromView()
        }
        isAttached = false
    }

    private fun decodeAdapterItem(position: Int): AdapterAsItem? {
        var previous: AdapterAsItem? = null
        for (adapterAsItem in adapters) {
            previous = if (adapterAsItem.startPosition > position) {
                return previous
            } else {
                adapterAsItem
            }
        }
        return previous
    }

    override fun getItemPosition(item: ItemController): Int {
        val top = 0
        var itemPosition = -1
        for (adapterAsItem in adapters) {
            val foundPosition = adapterAsItem.adapter.getItemPosition(item)
            if (foundPosition >= 0) {
                itemPosition = top + foundPosition
                break
            }
        }
        return itemPosition
    }

    fun removeAdapter(removeAdapterAtPosition: Int) {
        processWhenSafe(Runnable { removeAdapterImmediate(removeAdapterAtPosition) })
    }

    private fun removeAdapterImmediate(removeAdapterAtPosition: Int) {
        val remove: AdapterAsItem = adapters.removeAt(removeAdapterAtPosition)
        val removePositionStart = remove.startPosition
        var nextAdapterStartPosition = removePositionStart
        for (index in removeAdapterAtPosition until adapters.size) {
            val adapterAsItem = adapters[index]
            adapterAsItem.startPosition = nextAdapterStartPosition
            nextAdapterStartPosition = adapterAsItem.startPosition + adapterAsItem.adapter.itemCount
        }
        beginUpdates()
        notifyItemsRemoved(removePositionStart, remove.adapter.itemCount)
        endUpdates()
        remove.adapter.viewInteractor = null
    }

    fun updateIndexes(modifiedItem: AdapterAsItem) {
        var modifiedItem = modifiedItem
        var continueUpdating = false
        for (item in adapters) {
            if (continueUpdating) {
                item.startPosition = modifiedItem.startPosition + modifiedItem.adapter.itemCount
                modifiedItem = item
            } else if (item === modifiedItem) {
                continueUpdating = true
            }
        }
    }

    inner class AdapterAsItem(val adapter: ItemControllerSource<ItemController>) {
        var startPosition = 0
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

        init {
            updateObserver = adapter.observeAdapterUpdates().subscribe(Consumer { event: SourceUpdateEvent -> transformUpdateEvent(event) } as Consumer<SourceUpdateEvent>)
        }
    }
}