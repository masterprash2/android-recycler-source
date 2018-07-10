package com.clumob.recyclerview.adapter;

import android.view.ViewGroup;

import com.clumob.listitem.controller.source.ItemController;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public abstract class ViewHolderProvider {
    public abstract RvViewHolder<? extends ItemController> provideViewHolder(ViewGroup parent, int type);
}
