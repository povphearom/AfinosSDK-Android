package com.afinos.sdk.core.bind.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableList;
import android.databinding.ViewDataBinding;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.afinos.sdk.core.bind.binder.ItemBinder;

import java.util.Collection;

/**
 * Created by Dell on 1/17/2017.
 */

public class BindingBaseAdapter<T> extends BaseAdapter {
    private final ItemBinder<T> itemBinder;
    private ObservableList<T> items;

    public BindingBaseAdapter(ItemBinder<T> itemBinder, Collection<T> items) {
        this.itemBinder = itemBinder;
        setItems(items);
    }

    public void setItems(@Nullable Collection<T> items) {
        if (this.items == items) {
            return;
        }

        if (this.items != null) {
            items.clear();
        }

        if (items instanceof ObservableList) {
            this.items = (ObservableList<T>) items;
            notifyDataSetChanged();
        } else if (items != null) {
            this.items = new ObservableArrayList<>();
            this.items.addAll(items);
        } else {
            this.items = null;
        }
    }

    @Override
    public int getCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public T getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return itemBinder.getLayoutRes(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        T item = getItem(position);
        ViewDataBinding binding;
        if (convertView == null) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), itemBinder.getLayoutRes(item), parent, false);
            convertView = binding.getRoot();
        } else {
            binding = DataBindingUtil.bind(convertView);
        }

        binding.setVariable(itemBinder.getBindingVariable(item), item);
        binding.executePendingBindings();

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return super.getDropDownView(position, convertView, parent);
    }
}
