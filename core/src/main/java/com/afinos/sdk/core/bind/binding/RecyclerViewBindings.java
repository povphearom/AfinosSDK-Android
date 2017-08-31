package com.afinos.sdk.core.bind.binding;

import android.databinding.BindingAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.afinos.sdk.core.bind.adapter.BindingRecyclerViewAdapter;
import com.afinos.sdk.core.bind.binder.ItemBinder;
import com.afinos.sdk.core.bind.listener.ClickHandler;
import com.afinos.sdk.core.bind.listener.LongClickHandler;
import com.afinos.sdk.core.bind.listener.RecyclerScrollHandler;
import com.afinos.sdk.core.bind.listener.TopLessHandler;
import com.afinos.sdk.core.utils.DividerItemDecoration;
import com.afinos.sdk.core.utils.SpacesItemDecoration;

import java.util.Collection;

public class RecyclerViewBindings {
    private static final int KEY_ITEMS = -123;
    private static final int KEY_CLICK_HANDLER = -124;
    private static final int KEY_LONG_CLICK_HANDLER = -125;
    private static final int KEY_END_LESS_HANDLER = -126;

    @SuppressWarnings("unchecked")
    @BindingAdapter("items")
    public static <T> void setItems(RecyclerView recyclerView, Collection<T> items) {
        BindingRecyclerViewAdapter<T> adapter = (BindingRecyclerViewAdapter<T>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setItems(items);
        } else {
            recyclerView.setTag(KEY_ITEMS, items);
        }
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter("clickHandler")
    public static <T> void setHandler(RecyclerView recyclerView, ClickHandler<T> handler) {
        BindingRecyclerViewAdapter<T> adapter = (BindingRecyclerViewAdapter<T>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setClickHandler(handler);
        } else {
            recyclerView.setTag(KEY_CLICK_HANDLER, handler);
        }
    }


    @SuppressWarnings("unchecked")
    @BindingAdapter("longClickHandler")
    public static <T> void setHandler(RecyclerView recyclerView, LongClickHandler<T> handler) {
        BindingRecyclerViewAdapter<T> adapter = (BindingRecyclerViewAdapter<T>) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.setLongClickHandler(handler);
        } else {
            recyclerView.setTag(KEY_LONG_CLICK_HANDLER, handler);
        }
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter("android:orientation")
    public static void setOrientation(RecyclerView recyclerView, int orientation) {
        try {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            layoutManager.setOrientation(orientation);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter("endLessHandler")
    public static <T> void setHandler(RecyclerView recyclerView, RecyclerScrollHandler handler) {
        BindingRecyclerViewAdapter<T> adapter = (BindingRecyclerViewAdapter<T>) recyclerView.getAdapter();
        if (adapter != null) {
            if (handler.getLayoutManager() == null)
                handler.setLayoutManager(recyclerView.getLayoutManager());
            recyclerView.addOnScrollListener(handler);
        } else {
            recyclerView.setTag(KEY_END_LESS_HANDLER, handler);
        }
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter("topLessHandler")
    public static <T> void setHandler(RecyclerView recyclerView, TopLessHandler handler) {
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter(value = {"hasFixedSize", "nestedScroll"}, requireAll = true)
    public static <T> void setOption(RecyclerView recyclerView, boolean hasFixed, boolean nested) {
        recyclerView.setHasFixedSize(hasFixed);
        recyclerView.setNestedScrollingEnabled(nested);
    }

    @SuppressWarnings("unchecked")
    @BindingAdapter("itemViewBinder")
    public static <T> void setItemViewBinder(RecyclerView recyclerView, ItemBinder<T> itemViewMapper) {
        try {
            Collection<T> items = (Collection<T>) recyclerView.getTag(KEY_ITEMS);
            ClickHandler<T> clickHandler = (ClickHandler<T>) recyclerView.getTag(KEY_CLICK_HANDLER);
            BindingRecyclerViewAdapter<T> adapter = new BindingRecyclerViewAdapter<>(itemViewMapper, items);
            if (clickHandler != null) {
                adapter.setClickHandler(clickHandler);
            }

            recyclerView.setAdapter(adapter);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @BindingAdapter("itemAnimDuration")
    public static void setAnimItem(RecyclerView recyclerView, int duration) {
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(duration);
        itemAnimator.setRemoveDuration(duration);
        recyclerView.setItemAnimator(itemAnimator);
    }

    @BindingAdapter("itemSpace")
    public static void itemDecoration(RecyclerView recyclerView, int space) {
        RecyclerView.ItemDecoration itemDecoration = new SpacesItemDecoration(space);
        recyclerView.addItemDecoration(itemDecoration);
    }

    @BindingAdapter("itemSpace")
    public static void itemDecoration(RecyclerView recyclerView, float space) {
        RecyclerView.ItemDecoration itemDecoration = new SpacesItemDecoration((int) space);
        recyclerView.addItemDecoration(itemDecoration);
    }

    @BindingAdapter("hasDividerVertical")
    public static void hasDivider(RecyclerView recyclerView, boolean vertical) {
        RecyclerView.ItemDecoration itemDecoration = null;
        if (vertical) {
            itemDecoration = new
                    DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL_LIST);
        } else {
            itemDecoration = new
                    DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.HORIZONTAL_LIST);
        }
        recyclerView.addItemDecoration(itemDecoration);
    }
}