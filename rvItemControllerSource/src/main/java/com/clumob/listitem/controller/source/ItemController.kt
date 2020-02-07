package com.clumob.listitem.controller.source

/**
 * Created by prashant.rathore on 20/06/18.
 */
interface ItemController {
    fun onCreate(itemUpdatePublisher: ItemUpdatePublisher)
    fun onAttach(source: Any)
    fun onDetach(source: Any)
    fun onDestroy()
    val type: Int
    val id: Long
}