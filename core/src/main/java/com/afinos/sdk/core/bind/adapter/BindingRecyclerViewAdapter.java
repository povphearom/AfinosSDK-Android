package com.afinos.sdk.core.bind.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afinos.sdk.core.bind.binder.ItemBinder;
import com.afinos.sdk.core.bind.listener.ClickHandler;
import com.afinos.sdk.core.bind.listener.LongClickHandler;

import java.lang.ref.WeakReference;
import java.util.Collection;

@SuppressWarnings("unchecked")
public class BindingRecyclerViewAdapter<T> extends RecyclerView.Adapter<BindingRecyclerViewAdapter.ViewHolder> implements View.OnClickListener, View.OnLongClickListener {
    private static final int ITEM_MODEL = -124;
    private final WeakReferenceOnListChangedCallback<T> onListChangedCallback;
    private final ItemBinder<T> itemBinder;
    private ObservableList<T> items;
    private LayoutInflater inflater;
    private ClickHandler<T> clickHandler;
    private LongClickHandler<T> longClickHandler;

    public BindingRecyclerViewAdapter(ItemBinder<T> itemBinder, @Nullable Collection<T> items) {
        this.itemBinder = itemBinder;
        this.onListChangedCallback = new WeakReferenceOnListChangedCallback<>(this);
        setItems(items);
    }

    public ObservableList<T> getItems() {
        return items;
    }

    public void setItems(@Nullable Collection<T> items) {
        if (this.items == items) {
            return;
        }

        if (this.items != null) {
            notifyItemRangeRemoved(0, this.items.size());
            this.items.removeOnListChangedCallback(onListChangedCallback);
        }

        if (items instanceof ObservableList) {
            this.items = (ObservableList<T>) items;
            this.items.addOnListChangedCallback(onListChangedCallback);
            notifyItemRangeInserted(0, this.items.size());
        } else if (items != null) {
            this.items = new ObservableArrayList<>();
            this.items.addOnListChangedCallback(onListChangedCallback);
            this.items.addAll(items);
        } else {
            this.items = null;
        }
        notifyDataSetChanged();
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        if (items != null) {
            items.removeOnListChangedCallback(onListChangedCallback);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int layoutId) {
        if (inflater == null) {
            inflater = LayoutInflater.from(viewGroup.getContext());
        }

        ViewDataBinding binding = DataBindingUtil.inflate(inflater, layoutId, viewGroup, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final T item = items.get(position);

        viewHolder.binding.setVariable(itemBinder.getBindingVariable(item), item);
        viewHolder.binding.getRoot().setTag(ITEM_MODEL, item);
        viewHolder.binding.getRoot().setOnClickListener(this);
        viewHolder.binding.getRoot().setOnLongClickListener(this);
        viewHolder.binding.executePendingBindings();
    }

    @Override
    public int getItemViewType(int position) {
        return itemBinder.getLayoutRes(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    @Override
    public void onClick(View v) {
        if (clickHandler != null) {
            T item = (T) v.getTag(ITEM_MODEL);
            clickHandler.onClick(item, v);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (longClickHandler != null) {
            T item = (T) v.getTag(ITEM_MODEL);
            longClickHandler.onLongClick(item, v);
            return true;
        }
        return false;
    }

    public void setClickHandler(ClickHandler<T> clickHandler) {
        this.clickHandler = clickHandler;
    }

    public void setLongClickHandler(LongClickHandler<T> clickHandler) {
        this.longClickHandler = clickHandler;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ViewDataBinding binding;

        ViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class WeakReferenceOnListChangedCallback<T> extends ObservableList.OnListChangedCallback {

        private final WeakReference<BindingRecyclerViewAdapter<T>> adapterReference;

        public WeakReferenceOnListChangedCallback(BindingRecyclerViewAdapter<T> bindingRecyclerViewAdapter) {
            this.adapterReference = new WeakReference<>(bindingRecyclerViewAdapter);
        }

        @Override
        public void onChanged(ObservableList sender) {
            try {
                RecyclerView.Adapter adapter = adapterReference.get();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onItemRangeChanged(ObservableList sender, int positionStart, int itemCount) {
            try {
                RecyclerView.Adapter adapter = adapterReference.get();
                if (adapter != null) {
                    adapter.notifyItemRangeChanged(positionStart, itemCount);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onItemRangeInserted(ObservableList sender, int positionStart, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyItemRangeInserted(positionStart, itemCount);
            }
        }

        @Override
        public void onItemRangeMoved(ObservableList sender, int fromPosition, int toPosition, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyItemMoved(fromPosition, toPosition);
            }
        }

        @Override
        public void onItemRangeRemoved(ObservableList sender, int positionStart, int itemCount) {
            RecyclerView.Adapter adapter = adapterReference.get();
            if (adapter != null) {
                adapter.notifyItemRangeRemoved(positionStart, itemCount);
            }
        }
    }
}