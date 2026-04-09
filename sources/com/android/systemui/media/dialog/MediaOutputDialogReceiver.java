package com.android.systemui.media.dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MediaOutputDialogReceiver.kt */
/* loaded from: classes.dex */
public final class MediaOutputDialogReceiver extends BroadcastReceiver {
    private final MediaOutputDialogFactory mediaOutputDialogFactory;

    public MediaOutputDialogReceiver(@NotNull MediaOutputDialogFactory mediaOutputDialogFactory) {
        Intrinsics.checkParameterIsNotNull(mediaOutputDialogFactory, "mediaOutputDialogFactory");
        this.mediaOutputDialogFactory = mediaOutputDialogFactory;
    }

    @Override // android.content.BroadcastReceiver
    public void onReceive(@NotNull Context context, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (TextUtils.equals("com.android.systemui.action.LAUNCH_MEDIA_OUTPUT_DIALOG", intent.getAction())) {
            if (intent.getStringExtra("com.android.settings.panel.extra.PACKAGE_NAME") == null) {
                this.mediaOutputDialogFactory.create("com.android.systemui", false);
                return;
            }
            MediaOutputDialogFactory mediaOutputDialogFactory = this.mediaOutputDialogFactory;
            String stringExtra = intent.getStringExtra("com.android.settings.panel.extra.PACKAGE_NAME");
            Intrinsics.checkExpressionValueIsNotNull(stringExtra, "intent.getStringExtra(Me…tants.EXTRA_PACKAGE_NAME)");
            mediaOutputDialogFactory.create(stringExtra, false);
        }
    }
}
