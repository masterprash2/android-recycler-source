package com.clumob.listitem.controller.source;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by prashant.rathore on 20/09/18.
 */

public class ItemUpdatePublisher {

    private BehaviorSubject<ItemController> updateEventPublisher = BehaviorSubject.create();

    public Observable<ItemController> observeEvents() {
        return updateEventPublisher;
    }

    public void notifyItemUpdated(ItemController itemController) {
        updateEventPublisher.onNext(itemController);
    }

}
