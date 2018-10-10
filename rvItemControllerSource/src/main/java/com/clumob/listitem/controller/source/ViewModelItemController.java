package com.clumob.listitem.controller.source;

/**
 * Created by prashant.rathore on 02/07/18.
 */

public abstract class ViewModelItemController<VM> implements ItemController {

    public final VM viewModel;

    public ViewModelItemController(VM viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void onCreate(ItemUpdatePublisher publisher) {

    }

    @Override
    public void onAttach(Object source) {

    }

    @Override
    public void onDetach(Object source) {

    }

    @Override
    public void onDestroy() {

    }

    public abstract int getType();

    public abstract long getId();
}
