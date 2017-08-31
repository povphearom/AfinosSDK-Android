package com.afinos.sdk.core.bind.listener;

public abstract class TopLessHandler {
    private int offset = 2;
    private boolean loading = false;

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return loading;
    }

    public abstract void onLoad(int pos);

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}