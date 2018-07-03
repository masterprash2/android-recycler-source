package com.clumob.recyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.clumob.list.presenter.source.Presenter;
import com.clumob.list.presenter.source.PresenterSource;
import com.clumob.list.presenter.source.SourceUpdateEvent;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public class RvAdapter extends RecyclerView.Adapter<RvViewHolder> {

    private final PresenterSource presenterSource;
    private final ViewHolderProvider viewHolderProvider;
    private OnRecyclerItemClickListener itemClickListener;
    private RecyclerView recyclerView;
    private AdapterUpdateObserver adapterUpdateEventObserver;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (recyclerView != null && itemClickListener != null) {
                RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(view);
                itemClickListener.onRecyclerItemClick(recyclerView, childViewHolder.getAdapterPosition());
            }
        }
    };


    public RvAdapter(ViewHolderProvider viewHolderProvider,
                     PresenterSource presenterSource) {
        this.presenterSource = presenterSource;
        this.viewHolderProvider = viewHolderProvider;
        setHasStableIds(this.presenterSource.hasStableIds());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
        if (this.adapterUpdateEventObserver != null) {
            this.adapterUpdateEventObserver.dispose();
            this.adapterUpdateEventObserver = null;
        }
        this.adapterUpdateEventObserver = new AdapterUpdateObserver();
        presenterSource.observeAdapterUpdates().subscribe(this.adapterUpdateEventObserver);
        this.presenterSource.onAttached();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        this.presenterSource.onDetached();
        this.recyclerView = null;
        if (this.adapterUpdateEventObserver != null) {
            this.adapterUpdateEventObserver.dispose();
            this.adapterUpdateEventObserver = null;
        }
        super.onDetachedFromRecyclerView(recyclerView);

    }

    public void setItemClickListener(OnRecyclerItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RvViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewHolderProvider.provideViewHolder(parent, viewType);
    }


    @Override
    public void onViewAttachedToWindow(@NonNull RvViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttach();
    }

    @Override
    public void onBindViewHolder(@NonNull RvViewHolder holder, int position) {
        Presenter item = presenterSource.getItem(position);
        holder.getItemView().setOnClickListener(onClickListener);
        holder.bind(item);
    }

    @Override
    public long getItemId(int position) {
        return presenterSource.getItemId(position);
    }


    @Override
    public void onViewDetachedFromWindow(@NonNull RvViewHolder holder) {
        holder.onDetach();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(@NonNull RvViewHolder holder) {
        holder.unBind();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemViewType(int position) {
        return presenterSource.getItemType(position);
    }

    @Override
    public int getItemCount() {
        return presenterSource.getItemCount();
    }

    public static interface OnRecyclerItemClickListener {
        public void onRecyclerItemClick(RecyclerView recyclerView, int position);
    }

    private class AdapterUpdateObserver extends DisposableObserver<SourceUpdateEvent> {

        @Override
        public void onNext(SourceUpdateEvent sourceUpdateEvent) {
            if (recyclerView == null) {
                return;
            }
            switch (sourceUpdateEvent.getType()) {
                case UPDATE_BEGINS:
                    break;
                case ITEMS_CHANGED:
                    notifyItemRangeChanged(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case ITEMS_REMOVED:
                    notifyItemRangeRemoved(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case ITEMS_ADDED:
                    notifyItemRangeInserted(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case ITEMS_MOVED:
                    notifyItemMoved(sourceUpdateEvent.getPosition(), sourceUpdateEvent.getItemCount());
                    break;
                case UPDATE_ENDS:
                    break;
                case HAS_STABLE_IDS:
                    setHasStableIds(presenterSource.hasStableIds());
                    break;
            }
        }

        @Override
        public void onError(Throwable e) {
            Log.d("RvAdapter", "Observer Error ");
            e.printStackTrace();
        }

        @Override
        public void onComplete() {
            Log.d("RvAdapter", "Observer OnComplete ");
        }
    }

}
