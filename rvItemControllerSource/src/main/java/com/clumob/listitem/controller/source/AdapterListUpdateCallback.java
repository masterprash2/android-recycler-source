//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.clumob.listitem.controller.source;


public final class AdapterListUpdateCallback implements ListUpdateCallback {

    private final ItemControllerSource<?> mAdapter;

    public AdapterListUpdateCallback(ItemControllerSource<?> adapter) {
        this.mAdapter = adapter;
    }

    public void onInserted(int position, int count) {
        this.mAdapter.notifyItemsInserted(position, count);
    }

    public void onRemoved(int position, int count) {
        this.mAdapter.notifyItemsRemoved(position, count);
    }

    public void onMoved(int fromPosition, int toPosition) {
        this.mAdapter.notifyItemsMoved(fromPosition, toPosition);
    }

    public void onChanged(int position, int count, Object payload) {
        this.mAdapter.notifyItemsChanged(position, count);
    }
}
