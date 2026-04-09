package com.android.systemui.plugins.qs;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(version = 1)
/* loaded from: classes.dex */
public interface DetailAdapter {
    public static final UiEventLogger.UiEventEnum INVALID = new UiEventLogger.UiEventEnum() { // from class: com.android.systemui.plugins.qs.DetailAdapter$$ExternalSyntheticLambda0
        public final int getId() {
            return DetailAdapter.lambda$static$0();
        }
    };
    public static final int VERSION = 1;

    /* JADX INFO: Access modifiers changed from: private */
    static /* synthetic */ int lambda$static$0() {
        return 0;
    }

    View createDetailView(Context context, View view, ViewGroup viewGroup);

    int getMetricsCategory();

    Intent getSettingsIntent();

    CharSequence getTitle();

    default boolean getToggleEnabled() {
        return true;
    }

    Boolean getToggleState();

    default boolean hasHeader() {
        return true;
    }

    void setToggleState(boolean z);

    default UiEventLogger.UiEventEnum openDetailEvent() {
        return INVALID;
    }

    default UiEventLogger.UiEventEnum closeDetailEvent() {
        return INVALID;
    }

    default UiEventLogger.UiEventEnum moreSettingsEvent() {
        return INVALID;
    }
}
