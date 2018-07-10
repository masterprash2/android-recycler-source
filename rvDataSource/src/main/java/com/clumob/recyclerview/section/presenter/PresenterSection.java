package com.clumob.recyclerview.section.presenter;

import com.clumob.listitem.controller.source.ItemController;
import com.clumob.recyclerview.section.Section;

/**
 * Created by prashant.rathore on 20/06/18.
 */

public abstract class PresenterSection<P extends ItemController> extends Section<P> {

    @Override
    public void onCreate(int index) {
        super.onCreate(index);
        getItem(index).onCreate();
    }

    @Override
    public void onAttach(int index) {
        super.onAttach(index);
        getItem(index).onAttach();
    }

    @Override
    public void onDettach(int index) {
        super.onDettach(index);
        getItem(index).onDetach();
    }

    @Override
    public void onDestroy(int index) {
        super.onDestroy(index);
        getItem(index).onDestroy();
    }
}
