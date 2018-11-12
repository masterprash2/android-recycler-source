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
        if (attachedSources.size() > 0) {
            attachedSources.add(source);
            return;
        }
        attachedSources.add(source);
        switch (currentState) {
            case IDLE:
                onCreate(itemUpdatePublisher);
                currentState = State.CREATED;
            case CREATED:
                currentState = State.ATTACHED;
                attached();
                break;
            case DESTROYED:
                onCreate(itemUpdatePublisher);
                attached();
                currentState = State.ATTACHED;
                break;
        }
    }

    public abstract void attached();


    @Override
    public final void onDetach(Object source) {
        if (source != null && !attachedSources.contains(source)) {
            return;
        }
        attachedSources.remove(source);
        if (attachedSources.size() > 0) {
            return;
        }
        switch (currentState) {
            case DESTROYED:
            case IDLE:
                onCreate(itemUpdatePublisher);
                currentState = State.CREATED;
                break;
            case CREATED:
                break;
            case ATTACHED:
                currentState = State.CREATED;
                detached();
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
        }
    }

    public abstract void destroyed();

    public ItemUpdatePublisher getItemUpdatePublisher() {
        return itemUpdatePublisher;
    }
}
