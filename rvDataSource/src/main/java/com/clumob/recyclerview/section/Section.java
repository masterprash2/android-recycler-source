package com.clumob.recyclerview.section;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by prashant.rathore on 19/06/18.
 */

public abstract class Section<T extends Section.SectionItem> {

    private int itemCount;

    private PublishSubject<AdapterUpdateEvent> adapterUpdatePublisher = PublishSubject.create();

    public abstract T getItem(int position);

    public abstract int getItemType(int position);

    public long getItemId(int position) {
        return 0;
    }

    public boolean hasStableIds() {
        return false;
    }

    public final Observable<AdapterUpdateEvent> observeAdapterUpdates() {
        return adapterUpdatePublisher;
    }

    public int getItemCount() {
        return itemCount;
    }

    protected abstract int computeItemCount();

    public void onAttach(int index) {

    }

    public void onShow(int index) {

    }

    public void onHide(int index) {

    }

    public void onDetach(int index) {

    }

    public interface SectionItem {
        public int getType();
    }
}
