package com.clumob.recyclerview.section.interactor;

import com.clumob.recyclerview.section.Section;

/**
 * Created by prashant.rathore on 20/06/18.
 */

public abstract class InteractorSection<VM,IN extends Interactor> extends Section<InteractorSectionItem<VM,IN>> {

    @Override
    public void onAttach(int index) {
        InteractorSectionItem<VM, IN> item = getItem(index);
        item.interactor.onAttach(item);
    }

    @Override
    public void onShow(int index) {
        InteractorSectionItem<VM, IN> item = getItem(index);
//        item.interactor.onShow(item);
    }

    @Override
    public void onHide(int index) {
        InteractorSectionItem<VM, IN> item = getItem(index);
//        item.interactor.onHide();
    }

    @Override
    public void onDetach(int index) {
        InteractorSectionItem<VM, IN> item = getItem(index);
//        item.interactor.onDetach();
    }
}
