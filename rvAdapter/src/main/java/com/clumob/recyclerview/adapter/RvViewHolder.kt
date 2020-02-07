package com.clumob.recyclerview.adapter

import android.view.View
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.clumob.listitem.controller.source.ItemController

/**
 * Created by prashant.rathore on 28/05/18.
 */
abstract class RvViewHolder<Controller : ItemController>(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var controller: Controller? = null
        private set
    private var isScreenInFocus = false
    private var isBounded = false
    var parentLifecycleOwner: LifecycleOwner? = null
        private set
    private var lifecycleObserver: LifecycleObserver? = null

    fun bind(itemController: ItemController) {
        bindWithType(itemController as Controller)
    }

    private fun bindWithType(controller: Controller) {
        if (isBounded) {
            unBind()
        }
        this.controller = controller
        bindView()
        if (lifecycleObserver == null) observeLifecycle()
        isBounded = true
    }

    protected abstract fun bindView()
    fun onAttach() {
        onAttached()
        controller!!.onAttach(this)
    }

    protected fun onAttached() {}
    fun onDetach() {
        controller!!.onDetach(this)
        onDetached()
    }

    fun onDetached() {}
    fun unBind() {
        unBindView()
        controller = null
        removeLifecycleObserver()
        isScreenInFocus = false
        isBounded = false
    }

    protected abstract fun unBindView()
    private fun observeLifecycle() {
        removeLifecycleObserver()
        lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {}
            override fun onStart(owner: LifecycleOwner) {}
            override fun onResume(owner: LifecycleOwner) {
                updateScreenFocus(true)
            }

            override fun onPause(owner: LifecycleOwner) {
                updateScreenFocus(false)
            }

            override fun onStop(owner: LifecycleOwner) {}
            override fun onDestroy(owner: LifecycleOwner) {}
        }
        parentLifecycleOwner!!.lifecycle.addObserver(lifecycleObserver!!)
    }

    private fun updateScreenFocus(isInFocus: Boolean) {
        isScreenInFocus = isInFocus
        if (isScreenInFocus) {
            onScreenIsInFocus()
        } else {
            onScreenIsOutOfFocus()
        }
    }

    protected fun onScreenIsInFocus() {}
    protected fun onScreenIsOutOfFocus() {}
    fun setLifecycleOwner(lifecycle: LifecycleOwner) {
        if (parentLifecycleOwner !== lifecycle) {
            removeLifecycleObserver()
            parentLifecycleOwner = lifecycle
            observeLifecycle()
        }
    }

    private fun removeLifecycleObserver() {
        if (parentLifecycleOwner != null && lifecycleObserver != null) {
            parentLifecycleOwner!!.lifecycle.removeObserver(lifecycleObserver!!)
            lifecycleObserver = null
        }
    }

}