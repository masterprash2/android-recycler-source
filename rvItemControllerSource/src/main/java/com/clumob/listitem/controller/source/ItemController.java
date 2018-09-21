package com.clumob.listitem.controller.source;

import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by prashant.rathore on 20/06/18.
 */

public interface ItemController {
    void onCreate(ItemUpdatePublisher itemUpdatePublisher);
    void onAttach();
    void onDetach();
    void onDestroy();
    int getType();
    long getId();
}
