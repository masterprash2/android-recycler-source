package com.clumob.recyclerview.section;

import com.clumob.list.presenter.source.Presenter;
import com.clumob.list.presenter.source.SourceUpdateEvent;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by prashant.rathore on 19/06/18.
 */

public abstract class Section<T extends Presenter> {

    private int itemCount;

    private PublishSubject<SourceUpdateEvent> adapterUpdatePublisher = PublishSubject.create();

    public abstract T getItem(int position);

    public abstract int getItemType(int position);

    public long getItemId(int position) {
        return 0;
    }

    public boolean hasStableIds() {
        return false;
    }

    public final Observable<SourceUpdateEvent> observeAdapterUpdates() {
        return adapterUpdatePublisher;
    }

    public int getItemCount() {
        return itemCount;
    }

    protected abstract int computeItemCount();

    public void onCreate(int index) {

    }

    public void onAttach(int index) {

    }

    public void onDettach(int index) {

    }

    public void onDestroy(int index) {

    }

}
