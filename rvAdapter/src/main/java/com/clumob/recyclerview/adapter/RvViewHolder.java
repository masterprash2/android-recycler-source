package com.clumob.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.clumob.recyclerview.section.interactor.Interactor;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public abstract class RvViewHolder<Item,Ir extends Interactor<Item>> extends RecyclerView.ViewHolder {

    private final View itemView;
    private Item item;
    private Ir interactor;

    public RvViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public View getItemView() {
        return itemView;
    }

    void bind(Ir interactor, Item item) {
        this.interactor = interactor;
        this.item = item;
        bindView();
    }

    protected abstract void bindView();

    void onAttach() {
        interactor.onAttach(item);
    }

    void onDetach() {
        interactor.onDetach(item);
    }

    void unBind() {
        unBindView();
        item = null;
        interactor = null;
    }

    protected abstract void unBindView();


}
