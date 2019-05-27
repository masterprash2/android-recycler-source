package com.clumob.listitem.controller.source;

/**
 * Created by prashant.rathore on 02/07/18.
 */

public abstract class ItemControllerImpl<VD> implements ItemController {

    public final VD viewData;

    public ItemControllerImpl(VD viewData) {
        this.viewData = viewData;
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
