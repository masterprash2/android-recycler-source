package com.clumob.listitem.controller.source;

/**
 * Created by prashant.rathore on 17/12/18.
 */

public class EmptyItemSource extends ItemControllerSource<ItemController> {


    @Override
    public void onAttached() {

    }

    @Override
    public void onItemAttached(int position) {

    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public int getItemPosition(ItemController item) {
        return 0;
    }

    @Override
    public ItemController getItemForPosition(int position) {
        return null;
    }

    @Override
    public void onDetached() {

    }

    @Override
    protected int computeItemCount() {
        return 0;
    }
}
