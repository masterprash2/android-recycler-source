package com.clumob.listitem.controller.source;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by prashant.rathore on 21/09/18.
 */

public abstract class StateControlledItemController implements ItemController {

    private ItemUpdatePublisher itemUpdatePublisher;
    enum State {
        IDLE,
        CREATED,
        ATTACHED,
        DETACHED,
        DESTROYED
    }

    private State currentState = State.IDLE;

    private Set<Object> attachedSources = new HashSet<>();

    @Override
    public final void onCreate(ItemUpdatePublisher itemUpdatePublisher) {
        this.itemUpdatePublisher = itemUpdatePublisher;
        switch (currentState) {
            case IDLE:
            case DESTROYED:
                currentState = State.CREATED;
                created();
                break;
        }
    }


    public abstract void created();


    @Override
    public final void onAttach(Object source) {
        if(attachedSources.size() > 0) {
            attachedSources.add(source);
            return;
        }
        attachedSources.add(source);
        switch (currentState) {
            case IDLE:
                currentState = State.ATTACHED;
                created();
            case CREATED:
            case DETACHED:
                currentState = State.ATTACHED;
                attached();
                break;
            case DESTROYED:
                currentState = State.ATTACHED;
                created();
                attached();
                break;
        }
    }

    public abstract void attached();


    @Override
    public final void onDetach(Object source) {
        if(source != null && !attachedSources.contains(source)) {
            return;
        }
        attachedSources.remove(source);
        if(attachedSources.size() > 0) {
            return;
        }
        switch (currentState) {
            case DESTROYED:
            case IDLE:
                currentState = State.DESTROYED;
                created();
                break;
            case CREATED:
                break;
            case ATTACHED:
                currentState = State.DESTROYED;
                detached();
                break;
            case DETACHED:
                break;
        }
    }

    public abstract void detached();

    @Override
    public final void onDestroy() {
        switch (currentState) {
            case DESTROYED:
            case IDLE:
                break;
            case CREATED:
                currentState = State.DESTROYED;
                destroyed();
                break;
            case ATTACHED:
                currentState = State.DESTROYED;
                detached();
            case DETACHED:
                currentState = State.DESTROYED;
                destroyed();
                break;
        }
    }

    public abstract void destroyed();

    public ItemUpdatePublisher getItemUpdatePublisher() {
        return itemUpdatePublisher;
    }
}
