package com.clumob.recyclerview.section;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prashant.rathore on 19/06/18.
 */

public class MultiplexSection<T extends Section.SectionItem> extends Section<T> {

    private List<Section<T>> sections = new ArrayList<>();

    public void insert(int index, Section section) {
        this.sections.add(index,section);
    }

    public void addToBottom(Section section) {
        this.sections.add(section);
    }

    public void addOnTop(Section section) {
        this.sections.add(0,section);
    }

    @Override
    public T getItem(int position) {
        return sections.get(position).getItem(position);
    }

    @Override
    public int getItemType(int position) {
        return getItem(position).getType();
    }

    @Override
    protected int computeItemCount() {
        int count = 0;
        for(Section section : this.sections) {
            count += section.getItemCount();
        }
        return count;
    }
}
