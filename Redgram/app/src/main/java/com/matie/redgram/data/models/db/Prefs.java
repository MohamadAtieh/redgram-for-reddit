package com.matie.redgram.data.models.db;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Reddit preferences - bound to the user
 * Created by matie on 2016-05-07.
 */
public class Prefs extends RealmObject {
    @PrimaryKey
    private String id;
    private String defaultCommentSort;
    private boolean hideUps;
    private boolean hideDowns;
    private boolean labelNsfw;
    private boolean over18;
    private boolean showTrending;
    private boolean showFlair;
    private boolean showLinkFlair;
    private boolean storeVisits;
    private int minCommentsScore;
    private int minLinkScore;
    private int numComments;
    private int numSites;
    private boolean highlightControversial;
    private boolean ignoreSuggestedSort;
    private String media;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDefaultCommentSort() {
        return defaultCommentSort;
    }

    public void setDefaultCommentSort(String defaultCommentSort) {
        this.defaultCommentSort = defaultCommentSort;
    }

    public boolean isHideDowns() {
        return hideDowns;
    }

    public void setHideDowns(boolean hideDowns) {
        this.hideDowns = hideDowns;
    }

    public boolean isHideUps() {
        return hideUps;
    }

    public void setHideUps(boolean hideUps) {
        this.hideUps = hideUps;
    }

    public boolean isLabelNsfw() {
        return labelNsfw;
    }

    public void setLabelNsfw(boolean labelNsfw) {
        this.labelNsfw = labelNsfw;
    }

    public boolean isOver18() {
        return over18;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public boolean isShowTrending() {
        return showTrending;
    }

    public void setShowTrending(boolean showTrending) {
        this.showTrending = showTrending;
    }

    public boolean isShowFlair() {
        return showFlair;
    }

    public void setShowFlair(boolean showFlair) {
        this.showFlair = showFlair;
    }

    public boolean isShowLinkFlair() {
        return showLinkFlair;
    }

    public void setShowLinkFlair(boolean showLinkFlair) {
        this.showLinkFlair = showLinkFlair;
    }

    public boolean isStoreVisits() {
        return storeVisits;
    }

    public void setStoreVisits(boolean storeVisits) {
        this.storeVisits = storeVisits;
    }

    public int getMinCommentsScore() {
        return minCommentsScore;
    }

    public void setMinCommentsScore(int minCommentsScore) {
        this.minCommentsScore = minCommentsScore;
    }

    public int getMinLinkScore() {
        return minLinkScore;
    }

    public void setMinLinkScore(int minLinkScore) {
        this.minLinkScore = minLinkScore;
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public void setNumSites(int numSites) {
        this.numSites = numSites;
    }

    public int getNumSites() {
        return numSites;
    }


    public boolean isHighlightControversial() {
        return highlightControversial;
    }

    public void setHighlightControversial(boolean highlightControversial) {
        this.highlightControversial = highlightControversial;
    }

    public boolean isIgnoreSuggestedSort() {
        return ignoreSuggestedSort;
    }

    public void setIgnoreSuggestedSort(boolean ignoreSuggestedSort) {
        this.ignoreSuggestedSort = ignoreSuggestedSort;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }
}
