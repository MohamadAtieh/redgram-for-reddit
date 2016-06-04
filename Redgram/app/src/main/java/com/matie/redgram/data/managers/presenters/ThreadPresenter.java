package com.matie.redgram.data.managers.presenters;

import com.matie.redgram.data.models.main.items.PostItem;

/**
 * Thread Presenter Interface
 */
public interface ThreadPresenter {
    void registerForEvents();
    void unregisterForEvents();

    void getThread(String id );
    void vote(PostItem item, int dir);
    void save(PostItem postItem, boolean save);
    void hide(PostItem postItem, boolean hide);
}
