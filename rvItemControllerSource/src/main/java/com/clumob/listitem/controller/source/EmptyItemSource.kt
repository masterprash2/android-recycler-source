package com.clumob.listitem.controller.source

/**
 * Created by prashant.rathore on 17/12/18.
 */
class EmptyItemSource : ItemControllerSource<ItemController>() {
    override fun onAttachToView() {}
    override fun onItemAttached(position: Int) {}
    override fun hasStableIds(): Boolean {
        return false
    }

    override fun getItemPosition(item: ItemController): Int {
        return 0
    }

    override fun getItemForPosition(position: Int): ItemController {
        throw Exception("Its an EmptySource. Should not come here $position")
    }

    override fun onDetachFromView() {}
    override fun computeItemCount(): Int {
        return 0
    }
}