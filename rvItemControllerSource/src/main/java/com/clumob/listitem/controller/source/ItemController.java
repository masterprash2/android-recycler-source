package com.clumob.listitem.controller.source;

/**
 * Created by prashant.rathore on 20/06/18.
 */

public interface ItemController {

    void onCreate();
    void onAttach();
    void onDetach();
    void onDestroy();
    int getType();
    long getId();
}
