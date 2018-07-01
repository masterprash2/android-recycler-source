package com.clumob.list.presenter.source;

/**
 * Created by prashant.rathore on 20/06/18.
 */

public interface Presenter {

    void onCreate();
    void onAttach();
    void onDetach();
    void onDestroy();
    int getType();
    long getId();
}
