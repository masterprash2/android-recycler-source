package com.clumob.recyclerview.adapter

import android.os.Handler
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.clumob.listitem.controller.source.ItemControllerSource
import com.clumob.listitem.controller.source.ItemControllerSource.ViewInteractor
import com.clumob.listitem.controller.source.SourceUpdateEvent
import io.reactivex.observers.DisposableObserver
import java.util.*

class RvAdapter constructor(val viewHolderProvider: ViewHolderProvider?,
                            val itemControllerSource: ItemControllerSource<*>,
                            val lifecycleOwner: LifecycleOwner?) : RecyclerView.Adapter<RvViewHolder>() {


    private var itemClickListener: OnRecyclerItemClickListener? = null
    private var recyclerView: RecyclerView? = null
    private var adapterUpdateEventObserver: AdapterUpdateObserver? = null

    private val mHandler = Handler()

    init {
        setHasStableIds(this.itemControllerSource.hasStableIds())
        this.itemControllerSource.viewInteractor = createViewInteractor()
    }

    private val onClickListener = View.OnClickListener { view ->
        if (recyclerView != null && itemClickListener != null) {
            val childViewHolder = recyclerView!!.getChildViewHolder(view)
            itemClickListener!!.onRecyclerItemClick(recyclerView, childViewHolder.adapterPosition)
        }
    }


    private fun createViewInteractor(): ViewInteractor? {
        return object : ViewInteractor {
            var deque: Deque<Runnable?> = LinkedList()
            private var processingInProgress = false
            override fun processWhenSafe(runnable: Runnable) {
                deque.add(runnable)
                if (!processingInProgress) {
                    processingInProgress = true
                    processWhenQueueIdle()
                }
            }

            private fun processWhenQueueIdle() {
                mHandler.post(object : Runnable {
                    override fun run() {
                        if (isComputingLayout()) {
                            mHandler.post(this)
                        } else {
                            if (deque.peekFirst() != null) {
                                val runnable = deque.pollFirst()
                                runnable!!.run()
                                mHandler.post(this)
                            } else {
                                processingInProgress = false
                            }
                        }
                    }
                })
            }

            override fun cancelOldProcess(runnable: Runnable) {
                mHandler.removeCallbacks(runnable)
            }
        }
    }

    fun isComputingLayout(): Boolean {
        return recyclerView != null && recyclerView!!.isComputingLayout
    }

    fun setItemClickListener(itemClickListener: OnRecyclerItemClickListener?) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
        val rvViewHolder = viewHolderProvider!!.provideViewHolder(parent, viewType)
        rvViewHolder.setLifecycleOwner(lifecycleOwner!!)
        return rvViewHolder
    }


    override fun onViewAttachedToWindow(holder: RvViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.performAttachToWindow()
        itemControllerSource.onItemAttached(holder.adapterPosition)
    }

    override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
        val item = itemControllerSource.getItem(position)
        holder.itemView.setOnClickListener(onClickListener)
        holder.bind(item)
    }

    override fun getItemId(position: Int): Long {
        return itemControllerSource.getItemId(position)
    }


    override fun onViewDetachedFromWindow(holder: RvViewHolder) {
        holder.performDetachFromWindow()
        super.onViewDetachedFromWindow(holder)
    }

    override fun onViewRecycled(holder: RvViewHolder) {
        holder.unBind()
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int { //        Log.d("PAGINATEDIP"," "+position);
        return itemControllerSource.getItemType(position)
    }

    override fun getItemCount(): Int {
        return itemControllerSource.itemCount
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
        if (adapterUpdateEventObserver != null) {
            adapterUpdateEventObserver!!.dispose()
            adapterUpdateEventObserver = null
        }
        adapterUpdateEventObserver = AdapterUpdateObserver()
        itemControllerSource.observeAdapterUpdates().subscribe(adapterUpdateEventObserver!!)
        itemControllerSource.onAttachToView()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        itemControllerSource.onDetachFromView()
        this.recyclerView = null
        if (adapterUpdateEventObserver != null) {
            adapterUpdateEventObserver!!.dispose()
            adapterUpdateEventObserver = null
        }
        super.onDetachedFromRecyclerView(recyclerView)
    }


    inner class AdapterUpdateObserver : DisposableObserver<SourceUpdateEvent?>() {
        override fun onNext(sourceUpdateEvent: SourceUpdateEvent) {
            if (recyclerView == null) {
                return
            }
            when (sourceUpdateEvent.type) {
                SourceUpdateEvent.Type.UPDATE_BEGINS -> {
                }
                SourceUpdateEvent.Type.ITEMS_CHANGED -> notifyItemRangeChanged(sourceUpdateEvent.position, sourceUpdateEvent.itemCount)
                SourceUpdateEvent.Type.ITEMS_REMOVED -> notifyItemRangeRemoved(sourceUpdateEvent.position, sourceUpdateEvent.itemCount)
                SourceUpdateEvent.Type.ITEMS_ADDED -> notifyItemRangeInserted(sourceUpdateEvent.position, sourceUpdateEvent.itemCount)
                SourceUpdateEvent.Type.ITEMS_MOVED -> notifyItemMoved(sourceUpdateEvent.position, sourceUpdateEvent.itemCount)
                SourceUpdateEvent.Type.UPDATE_ENDS -> {
                }
                SourceUpdateEvent.Type.HAS_STABLE_IDS -> setHasStableIds(itemControllerSource.hasStableIds())
            }
        }

        override fun onError(e: Throwable) {
            Log.d("RvAdapter", "Observer Error ")
            e.printStackTrace()
        }

        override fun onComplete() {
            Log.d("RvAdapter", "Observer OnComplete ")
        }
    }

    interface OnRecyclerItemClickListener {
        fun onRecyclerItemClick(recyclerView: RecyclerView?, position: Int)
    }

}