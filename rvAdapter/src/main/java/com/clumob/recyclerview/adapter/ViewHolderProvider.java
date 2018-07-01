package com.clumob.recyclerview.adapter;

import android.view.ViewGroup;

import com.clumob.list.presenter.source.Presenter;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public abstract class ViewHolderProvider {
    public abstract RvViewHolder<? extends Presenter> provideViewHolder(ViewGroup parent, int type);
}
