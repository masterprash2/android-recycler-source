package com.clumob.recyclerview.adaptersource;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clumob.listitem.controller.source.ArraySource;
import com.clumob.listitem.controller.source.ItemController;
import com.clumob.listitem.controller.source.ItemControllerSource;
import com.clumob.listitem.controller.source.MultiplexSource;
import com.clumob.listitem.controller.source.PaginatedSource;
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
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(createRecyclerViewAdapter());
    }


    @Override
    protected void onDestroy() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(null);
        super.onDestroy();
    }

    private RecyclerView.Adapter createRecyclerViewAdapter() {
        return new RvAdapter(createViewHolderProvider(), createPaginatedSource(), this);
    }

    private boolean removeAdapter;

    private PaginatedSource paginatedSource;

    private PaginatedSource createPaginatedSource() {
        paginatedSource = new PaginatedSource(new ItemItemController("loading"), 5, createPaginatedCallBacks());
        return paginatedSource;
    }

    private PaginatedSource.PagenatedCallbacks createPaginatedCallBacks() {
        return new PaginatedSource.PagenatedCallbacks() {

            private boolean loadingNextBottomAdpater;
            private boolean loadingNextTopAdapter;

            private int topPageIndex = 1;
            private int bottomPageIndex = 5;

            @Override
            public boolean hasMoreBottomPage() {
                Log.d("PAGINATED", "Has More Bottom Page - " + bottomPageIndex);
                return bottomPageIndex > 0;
            }

            @Override
            public boolean hasMoreTopPage() {
                Log.d("PAGINATED", "Has More TOP Page - " + topPageIndex);
                return topPageIndex > 0;
            }

            @Override
            public void loadNextBottomPage() {
                final Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        bottomPageIndex--;
                        loadingNextBottomAdpater = false;
                        String s = String.valueOf(System.currentTimeMillis());
                        ItemControllerSource itemControllerSource = createPresenterAdapter(String.valueOf(s.charAt(s.length() - 1)));
//                        int limit = new Random().nextInt(20);
//                        Log.d("PAGINATED", "Adding Bottom " +limit);
//                        itemControllerSource.setMaxLimit(limit == 0 ? 1 : limit);
                        paginatedSource.addPageInBottom(itemControllerSource);
                    }
                });
            }

            @Override
            public void loadNextTopPage() {
                if (loadingNextTopAdapter) {
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
                        ItemControllerSource itemControllerSource = createPresenterAdapter(String.valueOf(s.charAt(s.length() - 1)));
//                        int limit = new Random().nextInt(20);
//                        Log.d("PAGINATED", "Adding Top " +limit);
//                        itemControllerSource.setMaxLimit(limit == 0 ? 1 : limit);
                        paginatedSource.addPageOnTop(itemControllerSource);
                    }
                });
            }

            @Override
            public void unloadingTopPage(ItemControllerSource<?> source) {
                topPageIndex++;
                Log.d("PAGINATED", "Unloading Top Page " + topPageIndex);
            }

            @Override
            public void unloadingBottomPage(ItemControllerSource<?> source) {
                bottomPageIndex++;
                Log.d("PAGINATED", "Unloading Bottom Page " + bottomPageIndex);
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
                    ItemControllerSource itemControllerSource = createPresenterAdapter(String.valueOf(s.charAt(s.length() - 1)));
                    int limit = new Random().nextInt(20);
                    Log.d("LIMIT", s + "-" + limit);
                    itemControllerSource.setMaxLimit(limit);
                    multiplexSource.addAdapter(itemControllerSource);
                }

                removeAdapter = !removeAdapter;
                handler.postDelayed(this, 2000);
            }
        });
        return multiplexSource;
    }

    private ItemControllerSource createPresenterAdapter(String d) {
        ArraySource<ItemItemController> arraySource = new ArraySource();
        List<ItemItemController> items = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            items.add(createPresenter(d + " = Object " + i));
        }
        arraySource.switchItems(items);
        return arraySource;
    }

    private ItemItemController createPresenter(String data) {
        return new ItemItemController(data);
    }

    static class ItemItemController extends ItemController {

        final String data;

        ItemItemController(String data) {
            this.data = data;
        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        protected void onCreate() {
            super.onCreate();
            Log.d("onCreate", data + " - " + this.toString().split("@")[1]);
        }

        @Override
        protected void onStartAttach() {
            super.onStartAttach();
            Log.d("onStart", data + " - " + this.toString().split("@")[1]);
        }

        @Override
        protected void onResume() {
            super.onResume();
            Log.d("onResume", data + " - " + this.toString().split("@")[1]);
        }

        @Override
        protected void onPause() {
            super.onPause();
            Log.d("onPause", data + " - " + this.toString().split("@")[1]);
        }

        @Override
        protected void onStopDetach() {
            Log.d("onStop", data + " - " + this.toString().split("@")[1]);
            super.onStopDetach();
        }

        @Override
        protected void onDestroy() {
            Log.d("onDestroy", data + " - " + this.toString().split("@")[1]);
            super.onDestroy();
        }
    }


    private ViewHolderProvider createViewHolderProvider() {
        return new ViewHolderProvider() {
            @Override
            public RvViewHolder provideViewHolder(ViewGroup parent, int type) {
                View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new RvViewHolder(inflate) {
                    {
                        itemView.setBackgroundColor(new Random().nextInt(Integer.MAX_VALUE));
                    }

                    @Override
                    protected void bindView() {
                        TextView tv = itemView.findViewById(R.id.text);
                        tv.setText(this.<ItemItemController>getController().data);
                    }

                    @Override
                    protected void unBindView() {
                    }
                };
            }
        };
    }


}