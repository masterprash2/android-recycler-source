package com.clumob.recyclerview.adapter;

import android.view.ViewGroup;

import com.clumob.interactor.datasource.Interactor;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public abstract class ViewHolderProvider {
    public abstract RvViewHolder<?, ? extends Interactor<?>> provideViewHolder(ViewGroup parent, int type);
}
