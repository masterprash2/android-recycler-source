package com.clumob.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.clumob.listitem.controller.source.ItemController;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public abstract class RvViewHolder<Controller extends ItemController> extends RecyclerView.ViewHolder {

    private final View itemView;
    private Controller controller;

    public RvViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public Controller getController() {
        return controller;
    }

    public View getItemView() {
        return itemView;
    }

    void bind(Controller controller) {
        this.controller = controller;
        bindView();
    }

    protected abstract void bindView();

    void onAttach() {
        controller.onAttach();
    }

    void onDetach() {
        controller.onDetach();
    }

    void unBind() {
        unBindView();
        controller = null;
    }


    protected abstract void unBindView();
}
