package com.clumob.recyclerview.section;

import com.clumob.listitem.controller.source.ItemController;

/**
 * Created by prashant.rathore on 19/06/18.
 */

public class ItemSection<T extends ItemController> extends Section<T> {

    private T item;

    public T getItem() {
        return item;
    }

    public void setItem(T item) {
        this.item = item;
    }

    @Override
    public T getItem(int position) {
        return item;
    }

    @Override
    public int getItemType(int position) {
        return getItem().getType();
    }

    @Override
    protected int computeItemCount() {
        return item == null ? 0 : 1;
    }
}
