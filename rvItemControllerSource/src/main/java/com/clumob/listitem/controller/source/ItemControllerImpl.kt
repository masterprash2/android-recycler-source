package com.clumob.listitem.controller.source

/**
 * Created by prashant.rathore on 02/07/18.
 */
abstract class ItemControllerImpl<VD>(val viewData: VD) : ItemController {
    override fun onCreate(publisher: ItemUpdatePublisher) {}
    override fun onAttach(source: Any) {}
    override fun onDetach(source: Any) {}
    override fun onDestroy() {}
    abstract override val type: Int
    abstract override val id: Long

}