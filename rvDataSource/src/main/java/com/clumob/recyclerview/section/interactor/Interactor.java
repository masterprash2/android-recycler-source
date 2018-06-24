package com.clumob.recyclerview.section.interactor;

/**
 * Created by prashant.rathore on 20/06/18.
 */

public interface Interactor<Item> {

    void onCreate(Item item);
    void onAttach(Item item);
    void onDetach(Item item);
    void onDestroy(Item item);

}
