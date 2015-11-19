package com.matie.redgram.data.managers.presenters;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.matie.redgram.R;
import com.matie.redgram.data.managers.preferences.PreferenceManager;
import com.matie.redgram.data.models.main.home.HomeViewWrapper;
import com.matie.redgram.data.models.main.items.PostItem;
import com.matie.redgram.data.models.main.items.SubredditItem;
import com.matie.redgram.data.models.main.reddit.RedditListing;
import com.matie.redgram.data.network.api.reddit.RedditClient;
import com.matie.redgram.ui.App;
import com.matie.redgram.ui.home.views.HomeView;
import com.matie.redgram.ui.common.views.widgets.postlist.PostRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static rx.android.app.AppObservable.bindFragment;

/**
 * Created by matie on 12/04/15.
 */

public class HomePresenterImpl implements HomePresenter{
    final private App app;
    final private HomeView homeView;
    final private PostRecyclerView homeRecyclerView;
    final private RedditClient redditClient;
    final private PreferenceManager preferenceManager;

    private CompositeSubscription subscriptions;
    
    private String loadMoreId;
    private List<PostItem> items;
    
    private List<SubredditItem> subredditItems;

    private Subscription homeWrapperSubscription;
    private Subscription listingSubscription;


    /**
     * Called onCreate(View) of Activity/Fragment
     * @param homeView
     */
    @Inject
    public HomePresenterImpl(HomeView homeView, App app) {
        this.app = app;
        this.homeView = homeView;
        this.homeRecyclerView = homeView.getRecyclerView();
        this.redditClient = app.getRedditClient();
        this.preferenceManager = app.getPreferenceManager();
        this.items = new ArrayList<PostItem>();
        this.subredditItems = new ArrayList<SubredditItem>();
        this.loadMoreId = "";
    }

    /**
     * Called onResume of Activity/Fragment
     */
    @Override
    public void registerForEvents() {
        if(subscriptions == null)
            subscriptions = new CompositeSubscription();

        if(subscriptions.isUnsubscribed()){
            if(homeWrapperSubscription != null){
                subscriptions.add(homeWrapperSubscription);
            }
            if(listingSubscription != null){
                subscriptions.add(listingSubscription);
            }
        }
    }
    /**
     * Called onPause of Activity/Fragment
     */
    @Override
    public void unregisterForEvents() {
        if(subscriptions.hasSubscriptions() || subscriptions != null){
            subscriptions.unsubscribe();
        }
    }

    /**
     * Only called once on activity started, unlike getListing. So it is safe to add the subscription here
     */
    @Override
    public void getHomeViewWrapper() {
        // TODO: 2015-10-27 check sharedpreferences and update actual values in home view
        //make sure it is a new set of items
        items.clear();
        homeView.showLoading();

        Map<String,String> params = new HashMap<String, String>();
        String filterChoice = homeView.getContext().getResources().getString(R.string.default_home_filter).toLowerCase();
        String subredditFilterChoice = homeView.getContext().getResources().getString(R.string.default_subreddit_filter).toLowerCase();

        //check if subreddits are cached
        SharedPreferences sharedPreferences = preferenceManager
                        .getSharedPreferences(PreferenceManager.SUBREDDIT_PREF);
        Observable<RedditListing> redditListingObservable = null;
        boolean isSubredditsCached = sharedPreferences.getString(PreferenceManager.SUBREDDIT_LIST, null) != null;
        if(isSubredditsCached){
            String storedListingObject = sharedPreferences.getString(PreferenceManager.SUBREDDIT_LIST, null);
            RedditListing storedListing = new Gson().fromJson(storedListingObject, RedditListing.class);
            redditListingObservable = Observable.just(storedListing);
        }else{
            redditListingObservable = redditClient.getSubreddits(subredditFilterChoice, params);
        }

        homeWrapperSubscription = (Subscription)bindFragment(homeView.getFragment(), Observable
                .zip(redditClient.getListing(filterChoice, params, null),
                        redditListingObservable, Observable.just(isSubredditsCached), new Func3<RedditListing, RedditListing, Boolean, HomeViewWrapper>() {
                            @Override
                            public HomeViewWrapper call(RedditListing listing, RedditListing subredditListing, Boolean inStore) {
                                HomeViewWrapper homeViewWrapper = new HomeViewWrapper();
                                homeViewWrapper.setRedditListing(listing);
                                homeViewWrapper.setSubreddits(subredditListing);
                                homeViewWrapper.setIsSubredditsCached(inStore);
                                return homeViewWrapper;
                            }
                        }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<HomeViewWrapper>() {
                    @Override
                    public void onCompleted() {
                        hideLoadingEvent(REFRESH);
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideLoadingEvent(REFRESH);
                        homeView.showErrorMessage(e.toString());
                    }

                    @Override
                    public void onNext(HomeViewWrapper homeViewWrapper) {
                        //dealing with the posts
                        RedditListing redditListing = homeViewWrapper.getRedditListing();
                        List<PostItem> postItems = (List<PostItem>) (List<?>) redditListing.getItems();
                        items.addAll(postItems);
                        //todo: replaceWith should be in a new view interface method
                        homeRecyclerView.replaceWith(items);
                        // TODO: 29/08/15 send the last item name to fragment to use for loading more.
                        loadMoreId = redditListing.getAfter();

                        //dealing with the subreddits
                        RedditListing subredditListing = homeViewWrapper.getSubreddits();
                        List<SubredditItem> subItems = (List<SubredditItem>) (List<?>) subredditListing.getItems();
                        subredditItems.addAll(subItems);
                        //add to preferences
                        if(!homeViewWrapper.getIsSubredditsCached()){
                            String gson = new Gson().toJson(subredditListing);
                            preferenceManager.getSharedPreferences(PreferenceManager.SUBREDDIT_PREF)
                                    .edit().putString(PreferenceManager.SUBREDDIT_LIST, gson)
                                    .commit();
                        }
                    }
                });
    }

    private void mockData() {
        for(int i = 0; i < 20; i++){
            PostItem item = new PostItem();
            if(i % 2 == 0){
                item.setType(PostItem.Type.SELF);
            }else{
                item.setType(PostItem.Type.IMAGE);
                item.setUrl("http://i.imgur.com/fbbqGVv.jpg");
                item.setThumbnail("http://i.imgur.com/fbbqGVvl.jpg");
            }
//            item.setIsAdult(true);
            item.setAuthor("julien");
            item.setIsSelf(true);
            item.setNumComments(i);
            item.setTitle("This is a title " + i);
            item.setText("this is a rnadom text coming from my cave in 2015, happy halloween " + i);
            item.setScore(i + 100);
            item.setSubreddit("r/androidev");

            items.add(item);

            SubredditItem subredditItem = new SubredditItem();
            subredditItem.setName("subreddit"+(i+1));
            subredditItems.add(subredditItem);
        }

        homeRecyclerView.replaceWith(items);
        hideLoadingEvent(REFRESH);
    }

    /**
     * List populated onCreate(View)
     */
    @Override
    public void getListing(String subreddit, String front, Map<String,String> params) {
        if(params.containsKey("after")){
            params.remove("after");
        }
        items.clear();
        homeView.showLoading();
        listingSubscription = getListingSubscription(subreddit, front, params, REFRESH);
    }

    @Override
    public void getMoreListing(String subreddit, String front, Map<String, String> params) {
        params.put("after", loadMoreId);
        homeView.showLoadMoreIndicator();
        listingSubscription = getListingSubscription(subreddit, front, params, LOAD_MORE);
    }

    @Override
    public List<String> getSubreddits() {
        List<String> subredditNames = new ArrayList<String>();
        for(SubredditItem item : subredditItems){
            subredditNames.add(item.getName());
        }
        return subredditNames;
    }

    private Subscription getListingSubscription(@Nullable String subreddit, @Nullable String filter, Map<String,String> params, int loadingEvent){
        Observable<RedditListing> targetObservable = null;

        if(subreddit != null){
            if(filter != null)
                targetObservable = redditClient.getSubredditListing(subreddit,filter, params, ((loadingEvent == LOAD_MORE)? items : null));
            else
                targetObservable = redditClient.getSubredditListing(subreddit, params, ((loadingEvent == LOAD_MORE)? items : null) );
        }else{
                targetObservable = redditClient.getListing(filter, params, ((loadingEvent == LOAD_MORE)? items : null));
        }

        return buildSubscription(targetObservable, loadingEvent);
    }

    private Subscription buildSubscription(Observable<RedditListing> observable, int loadingEvent){
        return (Subscription)bindFragment(homeView.getFragment(), observable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<RedditListing>() {
                    @Override
                    public void onCompleted() {
                        hideLoadingEvent(loadingEvent);
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideLoadingEvent(loadingEvent);
                        homeView.showErrorMessage(e.toString());
                    }

                    @Override
                    public void onNext(RedditListing wrapper) {
                        // TODO: 2015-10-23 check correctness of this casting
                        List<PostItem> postItems = (List<PostItem>) (List<?>) wrapper.getItems();
                        items.addAll(postItems);
                        //todo: replaceWith should be in a new view interface method
                        homeRecyclerView.replaceWith(items);
                        // TODO: 29/08/15 send the last item name to fragment to use for loading more.
                        loadMoreId = wrapper.getAfter();
                    }
                });
    }

    private void hideLoadingEvent(int loadingEvent){
        if(loadingEvent == REFRESH)
            homeView.hideLoading();
        else if(loadingEvent == LOAD_MORE)
            homeView.hideLoadMoreIndicator();
    }

}
