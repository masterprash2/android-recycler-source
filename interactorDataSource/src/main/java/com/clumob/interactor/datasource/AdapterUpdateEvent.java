package com.clumob.interactor.datasource;

import com.google.auto.value.AutoValue;

/**
 * Created by prashant.rathore on 28/05/18.
 */

@AutoValue
public abstract class AdapterUpdateEvent {

    public enum Type {
        ITEMS_CHANGED,
        ITEMS_REMOVED,
        ITEMS_ADDED,
        ITEMS_MOVED,
        HAS_STABLE_IDS
    }

    public abstract Type getType();
    public abstract int getPosition();
    public abstract int getItemCount();
    public abstract Builder toBuilder();

    public static Builder builder() {
        return new AutoValue_AdapterUpdateEvent.Builder().setItemCount(-1);
    }

    @AutoValue.Builder
    public static abstract class Builder {
        public abstract Builder setType(Type type);
        public abstract Builder setPosition(int position);
        public abstract Builder setItemCount(int itemCount);
        public abstract AdapterUpdateEvent build();
    }
}
