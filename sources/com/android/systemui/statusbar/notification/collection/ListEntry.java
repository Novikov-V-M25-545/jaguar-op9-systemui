package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.notification.collection.listbuilder.pluggable.NotifSection;

/* loaded from: classes.dex */
public abstract class ListEntry {
    private final String mKey;
    int mFirstAddedIteration = -1;
    private final ListAttachState mPreviousAttachState = ListAttachState.create();
    private final ListAttachState mAttachState = ListAttachState.create();

    public abstract NotificationEntry getRepresentativeEntry();

    ListEntry(String str) {
        this.mKey = str;
    }

    public String getKey() {
        return this.mKey;
    }

    public GroupEntry getParent() {
        return this.mAttachState.getParent();
    }

    void setParent(GroupEntry groupEntry) {
        this.mAttachState.setParent(groupEntry);
    }

    public int getSection() {
        return this.mAttachState.getSectionIndex();
    }

    public NotifSection getNotifSection() {
        return this.mAttachState.getSection();
    }

    ListAttachState getAttachState() {
        return this.mAttachState;
    }

    ListAttachState getPreviousAttachState() {
        return this.mPreviousAttachState;
    }

    void beginNewAttachState() {
        this.mPreviousAttachState.clone(this.mAttachState);
        this.mAttachState.reset();
    }
}
