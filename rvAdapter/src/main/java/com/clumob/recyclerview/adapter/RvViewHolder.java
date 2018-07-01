package com.clumob.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.clumob.list.presenter.source.Presenter;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public abstract class RvViewHolder<P extends Presenter> extends RecyclerView.ViewHolder {

    private final View itemView;
    private P presenter;

    public RvViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public P getPresenter() {
        return presenter;
    }

    public View getItemView() {
        return itemView;
    }

    void bind(P presenter) {
        this.presenter = presenter;
        bindView();
    }

    protected abstract void bindView();

    void onAttach() {
        presenter.onAttach();
    }

    void onDetach() {
        presenter.onDetach();
    }

    void unBind() {
        unBindView();
        presenter = null;
    }

    protected abstract void unBindView();


}
