package com.clumob.recyclerview.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.clumob.interactor.datasource.AdapterUpdateEvent;
import com.clumob.interactor.datasource.InteractorAdapter;
import com.clumob.interactor.datasource.InteractorItem;

import io.reactivex.observers.DisposableObserver;

/**
 * Created by prashant.rathore on 28/05/18.
 */

public class RvAdapter extends RecyclerView.Adapter<RvViewHolder> {

    private final InteractorAdapter interactorAdapter;
    private final ViewHolderProvider viewHolderProvider;
    private OnRecyclerItemClickListener itemClickListener;
    private RecyclerView recyclerView;
    private AdapterUpdateObserver adapterUpdateEventObserver;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (recyclerView != null && itemClickListener != null) {
                RecyclerView.ViewHolder childViewHolder = recyclerView.getChildViewHolder(recyclerView);
                itemClickListener.onRecyclerItemClick(recyclerView, childViewHolder.getAdapterPosition());
            }
        }
    };


    public RvAdapter(ViewHolderProvider viewHolderProvider,
                     InteractorAdapter interactorAdapter) {
        this.interactorAdapter = interactorAdapter;
        this.viewHolderProvider = viewHolderProvider;
        setHasStableIds(this.interactorAdapter.hasStableIds());
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
        interactorAdapter.observeAdapterUpdates().subscribe(this.adapterUpdateEventObserver);
        this.interactorAdapter.onAttached();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        this.interactorAdapter.onDetached();
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
        InteractorItem item = interactorAdapter.getItem(position);
        holder.getItemView().setOnClickListener(onClickListener);
        holder.bind(item.getInteractor(), item.getItem());
    }

    @Override
    public long getItemId(int position) {
        return interactorAdapter.getItemId(position);
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
        return interactorAdapter.getItemType(position);
    }

    @Override
    public int getItemCount() {
        return interactorAdapter.getItemCount();
    }

    public static interface OnRecyclerItemClickListener {
        public void onRecyclerItemClick(RecyclerView recyclerView, int position);
    }

    private class AdapterUpdateObserver extends DisposableObserver<AdapterUpdateEvent> {

        @Override
        public void onNext(AdapterUpdateEvent adapterUpdateEvent) {
            if (recyclerView == null) {
                return;
            }
            switch (adapterUpdateEvent.getType()) {

                case ITEMS_CHANGED:
                    notifyItemRangeChanged(adapterUpdateEvent.getPosition(), adapterUpdateEvent.getItemCount());
                    break;
                case ITEMS_REMOVED:
                    notifyItemRangeRemoved(adapterUpdateEvent.getPosition(), adapterUpdateEvent.getItemCount());
                    break;
                case ITEMS_ADDED:
                    notifyItemRangeInserted(adapterUpdateEvent.getPosition(), adapterUpdateEvent.getItemCount());
                    break;
                case ITEMS_MOVED:
                    notifyItemMoved(adapterUpdateEvent.getPosition(), adapterUpdateEvent.getItemCount());
                    break;
                case HAS_STABLE_IDS:
                    setHasStableIds(interactorAdapter.hasStableIds());
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
