package com.clumob.recyclerview.adapter;

import android.view.View;

import com.clumob.listitem.controller.source.ItemController;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public abstract class RvViewHolder<Controller extends ItemController> extends RecyclerView.ViewHolder {

    private final View itemView;
    private Controller controller;
    private boolean isScreenInFocus;
    private boolean isBounded;
    private LifecycleOwner lifecycle;
    private LifecycleObserver lifecycleObserver;


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
        if (isBounded) {
            unBind();
        }
        this.controller = controller;
        bindView();
        if (lifecycleObserver == null)
            observeLifecycle();
        isBounded = true;
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
        removeLifecycleObserver();
        this.isScreenInFocus = false;
        isBounded = false;
    }


    protected abstract void unBindView();


    private void observeLifecycle() {
        removeLifecycleObserver();
        lifecycleObserver = new DefaultLifecycleObserver() {
            @Override
            public void onCreate(@NonNull LifecycleOwner owner) {

            }

            @Override
            public void onStart(@NonNull LifecycleOwner owner) {

            }

            @Override
            public void onResume(@NonNull LifecycleOwner owner) {
                updateScreenFocus(true);
            }

            @Override
            public void onPause(@NonNull LifecycleOwner owner) {
                updateScreenFocus(false);
            }

            @Override
            public void onStop(@NonNull LifecycleOwner owner) {

            }

            @Override
            public void onDestroy(@NonNull LifecycleOwner owner) {

            }
        };
        lifecycle.getLifecycle().addObserver(lifecycleObserver);
    }

    public LifecycleOwner getParentLifecycleOwner() {
        return lifecycle;
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


    void setLifecycleOwner(LifecycleOwner lifecycle) {
        if (this.lifecycle != lifecycle) {
            removeLifecycleObserver();
            this.lifecycle = lifecycle;
            observeLifecycle();
        }
    }

    private void removeLifecycleObserver() {
        if (this.lifecycle != null && this.lifecycleObserver != null) {
            this.lifecycle.getLifecycle().removeObserver(lifecycleObserver);
            this.lifecycleObserver = null;
        }
    }

}
