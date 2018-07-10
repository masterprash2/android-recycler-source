package com.clumob.recyclerview.section;

import com.clumob.listitem.controller.source.ItemController;

import java.util.List;

/**
 * Created by prashant.rathore on 19/06/18.
 */

public class ArraySection<T extends ItemController>  extends Section<T> {

    private List<T> items;

    public T getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public int getItemType(int position) {
        return getItem(position).getType();
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public void addItemAtPosition(int position,T item) {
        this.items.add(position, item);
    }


    public void removeItem(int position) {
        this.items.remove(position);
    }

    @Override
    protected int computeItemCount() {
        return items == null ? 0 : items.size();
    }
}
