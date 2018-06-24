package com.clumob.recyclerview.section.interactor;

import com.clumob.recyclerview.section.Section;

/**
 * Created by prashant.rathore on 20/06/18.
 */

public class InteractorSectionItem<VM,IN extends Interactor> implements Section.SectionItem {

    public final VM viewModel;
    public final IN interactor;
    public final int type;

    public InteractorSectionItem(VM viewModel, IN interactor, int type) {
        this.viewModel = viewModel;
        this.interactor = interactor;
        this.type = type;
    }

    @Override
    public int getType() {
        return type;
    }
}
