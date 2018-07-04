package com.clumob.recyclerview.adaptersource;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clumob.list.presenter.source.ArraySource;
import com.clumob.list.presenter.source.MultiplexSource;
import com.clumob.list.presenter.source.PaginatedSource;
import com.clumob.list.presenter.source.Presenter;
import com.clumob.list.presenter.source.PresenterSource;
import com.clumob.recyclerview.adapter.RvAdapter;
import com.clumob.recyclerview.adapter.RvViewHolder;
import com.clumob.recyclerview.adapter.ViewHolderProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(createRecyclerViewAdapter());
    }


    @Override
    protected void onDestroy() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(null);
        super.onDestroy();
    }

    private RecyclerView.Adapter createRecyclerViewAdapter() {
        return new RvAdapter(createViewHolderProvider(), createPaginatedSource());
    }

    private boolean removeAdapter;

    private PaginatedSource paginatedSource;

    private PaginatedSource createPaginatedSource() {
        paginatedSource = new PaginatedSource(new ItemPresenter("loading"), 5, createPaginatedCallBacks());
        return paginatedSource;
    }

    private PaginatedSource.PagenatedCallbacks createPaginatedCallBacks() {
        return new PaginatedSource.PagenatedCallbacks() {

            private boolean loadingNextBottomAdpater;
            private boolean loadingNextTopAdapter;

            private int topPageIndex = 5;
            private int bottomPageIndex = 5;

            @Override
            public boolean hasMoreBottomPage() {
                Log.d("PAGINATED","Has More Bottom Page - " + bottomPageIndex);
                return bottomPageIndex > 0;
            }

            @Override
            public boolean hasMoreTopPage() {
                Log.d("PAGINATED","Has More TOP Page - " + topPageIndex);
                return topPageIndex > 0;
            }

            @Override
            public void loadNextBottomPage() {
                if(loadingNextBottomAdpater) {
                    return;
                }
                loadingNextBottomAdpater = true;
                final Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        bottomPageIndex--;
                        loadingNextBottomAdpater = false;
                        String s = String.valueOf(System.currentTimeMillis());
                        PresenterSource presenterSource = createPresenterAdapter(String.valueOf(s.charAt(s.length() - 1)));
                        int limit = new Random().nextInt(20);
                        Log.d("PAGINATED", "Adding Bottom " +limit);
                        presenterSource.setMaxLimit(limit == 0 ? 1 : limit);
                        paginatedSource.addPageInBottom(presenterSource);
                    }
                });
            }

            @Override
            public void loadNextTopPage() {
                if(loadingNextTopAdapter) {
                    return;
                }
                loadingNextTopAdapter = true;
                final Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        topPageIndex--;
                        loadingNextTopAdapter = false;
                        String s = String.valueOf(System.currentTimeMillis());
                        PresenterSource presenterSource = createPresenterAdapter(String.valueOf(s.charAt(s.length() - 1)));
                        int limit = new Random().nextInt(20);
                        Log.d("PAGINATED", "Adding Top " +limit);
                        presenterSource.setMaxLimit(limit == 0 ? 1 : limit);
                        paginatedSource.addPageOnTop(presenterSource);
                    }
                });
            }

            @Override
            public void unloadingTopPage(PresenterSource<?> source) {
                topPageIndex++;
                Log.d("PAGINATED", "Unloading Top Page " + topPageIndex);
            }

            @Override
            public void unloadingBottomPage(PresenterSource<?> source) {
                bottomPageIndex++;
                Log.d("PAGINATED", "Unloading Bottom Page " +bottomPageIndex);
            }
        };
    }

    private MultiplexSource createMultiplexAdapterSample() {
        final MultiplexSource multiplexSource = new MultiplexSource();
        multiplexSource.addAdapter(createPresenterAdapter("a"));
        multiplexSource.addAdapter(createPresenterAdapter("b"));
        multiplexSource.addAdapter(createPresenterAdapter("c"));
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (removeAdapter) {
                    multiplexSource.removeAdapter(1);
                } else {
                    String s = String.valueOf(System.currentTimeMillis());
                    PresenterSource presenterSource = createPresenterAdapter(String.valueOf(s.charAt(s.length() - 1)));
                    int limit = new Random().nextInt(20);
                    Log.d("LIMIT", s + "-" + limit);
                    presenterSource.setMaxLimit(limit);
                    multiplexSource.addAdapter(presenterSource);
                }

                removeAdapter = !removeAdapter;
                handler.postDelayed(this, 2000);
            }
        });
        return multiplexSource;
    }

    private PresenterSource createPresenterAdapter(String d) {
        ArraySource<ItemPresenter> arraySource = new ArraySource();
        List<ItemPresenter> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            items.add(createPresenter(d + " = Object " + i));
        }
        arraySource.switchItems(items);
        return arraySource;
    }

    private ItemPresenter createPresenter(String data) {
        return new ItemPresenter(data);
    }

    static class ItemPresenter implements Presenter {

        final String data;

        public ItemPresenter(String data) {
            this.data = data;
        }

        public String getData() {
            return data;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onAttach() {

        }

        @Override
        public void onDetach() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public long getId() {
            return 0;
        }
    }


    private ViewHolderProvider createViewHolderProvider() {
        return new ViewHolderProvider() {
            @Override
            public RvViewHolder<ItemPresenter> provideViewHolder(ViewGroup parent, int type) {
                View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new RvViewHolder<ItemPresenter>(inflate) {

                    @Override
                    protected void bindView() {
                        TextView tv = getItemView().findViewById(R.id.text);
                        tv.setText(getPresenter().data);
                    }

                    @Override
                    protected void unBindView() {
                    }
                };
            }
        };
    }


}