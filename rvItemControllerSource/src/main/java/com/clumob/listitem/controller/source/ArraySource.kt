package com.clumob.listitem.controller.source

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import java.util.*

/**
 * Created by prashant.rathore on 24/06/18.
 */
class ArraySource<Controller : ItemController> : ItemControllerSource<Controller>() {
    private var controller: MutableList<Controller> = ArrayList()
    private var isAttached = false
    private val itemUpdatePublisher = ItemUpdatePublisher()
    private var compositeDisposable: CompositeDisposable? = null
    override fun onAttached() {
        isAttached = true
        compositeDisposable = CompositeDisposable()
        compositeDisposable!!.add(observeItemUpdates())
        for (item in controller) {
            item.onCreate(itemUpdatePublisher)
        }
    }

    override fun onItemAttached(position: Int) {}
    val items: List<Controller>
        get() = controller

    fun setItems(items: MutableList<Controller>?) {
        switchItems(items)
    }

    private fun switchItems(items: MutableList<Controller>?, useDiffProcess: Boolean) {
        val newItems: MutableList<Controller> = items ?: ArrayList()
        processWhenSafe(Runnable { switchItemImmediate(useDiffProcess, newItems) })
    }

    private fun switchItemImmediate(useDiffProcess: Boolean, newItems: MutableList<Controller>) {
        val oldCount = controller.size
        val newCount = newItems.size
        val retained: MutableSet<Controller> = HashSet()
        val diffResult = diffResults(controller, newItems, retained)
        val oldItems = controller
        controller = newItems
        beginUpdates()
        if (useDiffProcess) {
            diffResult.dispatchUpdatesTo(this@ArraySource)
        } else {
            val diff = newCount - oldCount
            if (diff > 0) {
                notifyItemsInserted(oldCount, diff)
                notifyItemsChanged(0, oldCount)
            } else if (diff < 0) {
                notifyItemsRemoved(newCount, diff * -1)
                notifyItemsChanged(0, newCount)
            } else {
                notifyItemsChanged(0, newCount)
            }
        }
        endUpdates()
        if (isAttached) {
            newItems.onEach { it.onCreate(itemUpdatePublisher) }
        }
        oldItems.removeAll(retained)
        oldItems.onEach { it.onDestroy() }
    }

    fun switchItems(items: MutableList<Controller>?) {
        switchItems(items, false)
    }

    fun switchItemsWithDiffRemovalAndInsertions(items: MutableList<Controller>?) {
        switchItems(items, true)
    }

    private fun diffResults(oldItems: List<Controller>, newItems: MutableList<Controller>, retained: MutableSet<Controller>): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldItems.size
            }

            override fun getNewListSize(): Int {
                return newItems.size
            }

            override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                val itemOld = oldItems[oldPosition]
                val itemNew = newItems[newPosition]
                val equals = itemOld === itemNew || itemOld.hashCode() == itemNew.hashCode() && itemOld == itemNew
                if (equals) {
                    newItems[newPosition] = itemOld
                    retained.add(itemOld)
                }
                return equals
            }

            override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
                return areItemsTheSame(oldPosition, newPosition)
            }
        }, false)
    }

    fun replaceItem(index: Int, item: Controller) {
        processWhenSafe(Runnable { replaceItemWhenSafe(index, item) })
    }

    private fun replaceItemWhenSafe(index: Int, item: Controller) {
        val set = controller.set(index, item)
        set.onDestroy()
        notifyItemsChanged(index, 1)
        if (isAttached) {
            item.onCreate(itemUpdatePublisher)
        }
    }

    override fun getItemPosition(item: Controller): Int {
        return controller.indexOf(item)
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun computeItemCount(): Int {
        return controller.size
    }

    override fun getItemForPosition(position: Int): Controller {
        return controller[position]
    }

    //    @Override
//    public void onItemDetached(int position) {
//
//    }
    private fun observeItemUpdates(): Disposable {
        return itemUpdatePublisher.observeEvents().subscribe { itemController: ItemController -> postItemUpdate(itemController) }
    }

    private fun postItemUpdate(itemController: ItemController) {
        processWhenSafe(Runnable {
            val index = controller.indexOf(itemController)
            if (index >= 0) notifyItemsChanged(index, 1)
        })
    }

    override fun onDetached() {
        compositeDisposable!!.dispose()
        isAttached = false
        controller.onEach { it.onDestroy() }
    }
}