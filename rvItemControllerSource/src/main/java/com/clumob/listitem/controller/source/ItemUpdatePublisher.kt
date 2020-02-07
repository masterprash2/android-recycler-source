package com.clumob.listitem.controller.source

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by prashant.rathore on 20/09/18.
 */
class ItemUpdatePublisher {
    private val updateEventPublisher = BehaviorSubject.create<ItemController>()
    fun observeEvents(): Observable<ItemController> {
        return updateEventPublisher
    }

    fun notifyItemUpdated(itemController: ItemController) {
        updateEventPublisher.onNext(itemController)
    }
}