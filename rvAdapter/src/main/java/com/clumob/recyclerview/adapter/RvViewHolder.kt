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
abstract class RvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private var controller: ItemController? = null

    fun <T : ItemController> getController(): T = controller as T

    private var isScreenResumed = false
    private var isScreenStared = false
    private var isBounded = false
    var parentLifecycleOwner: LifecycleOwner? = null
        private set
    private var lifecycleObserver: LifecycleObserver? = null

    internal fun bind(controller: ItemController) {
        if (isBounded) {
            unBind()
        }
        this.controller = controller
        bindView()
        if (lifecycleObserver == null) observeLifecycle()
        isBounded = true
    }

    protected abstract fun bindView()

    internal fun performAttachToWindow() {
        onAttachedToWindow()
        if(isScreenStared) {
            onScreenStarted()
            if(isScreenResumed) {
                onScreenResumed()
            }
        }
    }

    protected open fun onAttachedToWindow() {}

    internal fun performDetachFromWindow() {
        onScreenStopped()
        onDetachedFromWindow()
    }


    protected open fun onDetachedFromWindow() {}

    fun unBind() {
        unBindView()
        performDetachFromWindow()
        controller = null
        removeLifecycleObserver()
        isScreenResumed = false
        isScreenStared = false
        isBounded = false
    }

    protected abstract fun unBindView()

    private fun observeLifecycle() {
        removeLifecycleObserver()
        lifecycleObserver = object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {}
            override fun onStart(owner: LifecycleOwner) {
                updateScreenStartState(true)
            }
            override fun onResume(owner: LifecycleOwner) {
                updateScreenResumeState(true)
            }

            override fun onPause(owner: LifecycleOwner) {
                updateScreenResumeState(false)
            }

            override fun onStop(owner: LifecycleOwner) {
                updateScreenStartState(false)
            }
            override fun onDestroy(owner: LifecycleOwner) {}
        }
        parentLifecycleOwner!!.lifecycle.addObserver(lifecycleObserver!!)
    }

    private fun updateScreenStartState(isStarted : Boolean) {
        this.isScreenStared = isStarted
        if(isScreenStared) onScreenStarted()
        else onScreenStopped()
    }

    private fun onScreenStarted() {
        when (controller?.state) {
            ItemController.State.CREATE,
            ItemController.State.STOP -> controller!!.performStart(this)
        }
    }

    private fun updateScreenResumeState(isInFocus: Boolean) {
        isScreenResumed = isInFocus
        if (isScreenResumed) {
            onScreenResumed()
        } else {
            onScreenPaused()
        }
    }

    protected open fun onScreenResumed() {
        when (controller?.state) {
            ItemController.State.START,
            ItemController.State.RESUME,
            ItemController.State.PAUSE -> controller!!.performResume()
        }
    }

    private fun onScreenStopped() {
        onScreenPaused()
        when (controller?.state) {
            ItemController.State.STOP,
            ItemController.State.PAUSE -> controller!!.performStop(this)
        }
    }

    protected open fun onScreenPaused() {
        when (controller?.state) {
            ItemController.State.RESUME,
            ItemController.State.PAUSE -> controller!!.performPause()
        }
    }

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