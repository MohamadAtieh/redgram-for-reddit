package com.matie.redgram.ui.common.views.widgets.drawer;

import android.support.v7.widget.RecyclerView;

/**
 * Created by matie on 2016-05-04.
 */
public class UserViewHolder extends RecyclerView.ViewHolder {

    private UserItemView userItemView;

    public UserViewHolder(UserItemView itemView) {
        super(itemView);
        this.userItemView = itemView;
    }

    public UserItemView getUserItemView() {
        return userItemView;
    }

    public void setUserItemView(UserItemView userItemView) {
        this.userItemView = userItemView;
    }

}
