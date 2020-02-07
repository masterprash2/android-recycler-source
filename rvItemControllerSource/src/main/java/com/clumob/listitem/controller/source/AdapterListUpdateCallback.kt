//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//
package com.clumob.listitem.controller.source

class AdapterListUpdateCallback(private val mAdapter: ItemControllerSource<*>) : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {
        mAdapter.notifyItemsInserted(position, count)
    }

    override fun onRemoved(position: Int, count: Int) {
        mAdapter.notifyItemsRemoved(position, count)
    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {
        mAdapter.notifyItemsMoved(fromPosition, toPosition)
    }

    override fun onChanged(position: Int, count: Int, payload: Any) {
        mAdapter.notifyItemsChanged(position, count)
    }

}