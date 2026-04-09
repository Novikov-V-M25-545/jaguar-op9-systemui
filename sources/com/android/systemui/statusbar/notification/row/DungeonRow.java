package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.R;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.icon.IconPack;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DungeonRow.kt */
/* loaded from: classes.dex */
public final class DungeonRow extends LinearLayout {

    @Nullable
    private NotificationEntry entry;

    /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
    public DungeonRow(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attrs, "attrs");
    }

    @Nullable
    public final NotificationEntry getEntry() {
        return this.entry;
    }

    public final void setEntry(@Nullable NotificationEntry notificationEntry) {
        this.entry = notificationEntry;
        update();
    }

    private final void update() throws Resources.NotFoundException {
        IconPack icons;
        StatusBarIconView statusBarIcon;
        ExpandableNotificationRow row;
        View viewFindViewById = findViewById(R.id.app_name);
        if (viewFindViewById == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.widget.TextView");
        }
        TextView textView = (TextView) viewFindViewById;
        NotificationEntry notificationEntry = this.entry;
        StatusBarIcon statusBarIcon2 = null;
        textView.setText((notificationEntry == null || (row = notificationEntry.getRow()) == null) ? null : row.getAppName());
        View viewFindViewById2 = findViewById(R.id.icon);
        if (viewFindViewById2 == null) {
            throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.StatusBarIconView");
        }
        StatusBarIconView statusBarIconView = (StatusBarIconView) viewFindViewById2;
        NotificationEntry notificationEntry2 = this.entry;
        if (notificationEntry2 != null && (icons = notificationEntry2.getIcons()) != null && (statusBarIcon = icons.getStatusBarIcon()) != null) {
            statusBarIcon2 = statusBarIcon.getStatusBarIcon();
        }
        statusBarIconView.set(statusBarIcon2);
    }
}
