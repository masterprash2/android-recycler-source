package com.clumob.recyclerview.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.clumob.listitem.controller.source.ItemController;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public abstract class RvViewHolder<Controller extends ItemController> extends RecyclerView.ViewHolder {

    private final View itemView;
    private Controller controller;
    private boolean isScreenInFocus;
    private Observable<Boolean> screenVisibilityObservable;
    private Disposable screenVisibilityObserver;

    public RvViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
    }

    public Controller getController() {
        return controller;
    }

    public View getItemView() {
        return itemView;
    }

    void bind(Controller controller) {
        this.controller = controller;
        bindView();
        if(this.screenVisibilityObserver == null || this.screenVisibilityObserver.isDisposed()) {
            observeScreenVisibility(this.screenVisibilityObservable);
        }
    }

    protected abstract void bindView();

    void onAttach() {
        onAttached();
        controller.onAttach(this);
    }

    protected void onAttached() {

    }

    void onDetach() {
        controller.onDetach(this);
        onDetached();
    }

    public void onDetached() {

    }

    void unBind() {
        unBindView();
        controller = null;
        if (this.screenVisibilityObserver != null) {
            this.screenVisibilityObserver.dispose();
        }
        this.screenVisibilityObserver = null;
        this.isScreenInFocus = false;
    }


    protected abstract void unBindView();

    void setScreenVisibilityObserver(Observable<Boolean> screenVisibilityObserver) {
        if (this.screenVisibilityObservable != screenVisibilityObserver) {
            this.screenVisibilityObservable = screenVisibilityObserver;
            observeScreenVisibility(screenVisibilityObservable);
        }
    }

    private void observeScreenVisibility(Observable<Boolean> observable) {
        if (this.screenVisibilityObserver != null) {
            this.screenVisibilityObserver.dispose();
        }
        this.screenVisibilityObserver = observable.subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                updateScreenFocus(aBoolean);
            }
        });

    }

    private void updateScreenFocus(boolean isInFocus) {
        isScreenInFocus = isInFocus;
        if (isScreenInFocus) {
            onScreenIsInFocus();
        } else {
            onScreenIsOutOfFocus();
        }
    }

    protected void onScreenIsInFocus() {

    }

    protected void onScreenIsOutOfFocus() {

    }
}
