package com.afinos.sdk.core.bind.listener;

import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public abstract class RecyclerScrollHandler extends RecyclerView.OnScrollListener {
    public final static String TAG = RecyclerScrollHandler.class.getSimpleName();
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 2;
    private int firstVisibleItem, visibleItemCount, totalItemCount;

    private LinearLayoutManager mLinearLayoutManager;

    public RecyclerScrollHandler() {
    }

    public RecyclerScrollHandler(LinearLayoutManager linearLayoutManager) {
        this.mLinearLayoutManager = linearLayoutManager;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        if (layoutManager instanceof LinearLayoutManager)
            this.mLinearLayoutManager = (LinearLayoutManager) layoutManager;
    }

    @Override
    public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (null == mLinearLayoutManager) return;
        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = mLinearLayoutManager.getItemCount();
                firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    onLoadMore();
                    loading = true;
                }
            }
        };
        handler.post(r);

    }

    public abstract void onLoadMore();

    public RecyclerView.LayoutManager getLayoutManager() {
        return this.mLinearLayoutManager;
    }

    public void reset() {
        previousTotal = 0;
    }
}