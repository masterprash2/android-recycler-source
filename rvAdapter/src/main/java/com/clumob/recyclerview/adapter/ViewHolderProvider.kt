package com.clumob.recyclerview.adapter

import android.view.ViewGroup
import com.clumob.listitem.controller.source.ItemController

/**
 * Created by prashant.rathore on 28/05/18.
 */
abstract class ViewHolderProvider {
    abstract fun provideViewHolder(parent: ViewGroup?, type: Int): RvViewHolder<out ItemController>
}