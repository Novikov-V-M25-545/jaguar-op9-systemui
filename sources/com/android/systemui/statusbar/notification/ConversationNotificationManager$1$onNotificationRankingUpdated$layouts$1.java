package com.android.systemui.statusbar.notification;

import android.view.View;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.reflect.KDeclarationContainer;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConversationNotifications.kt */
/* loaded from: classes.dex */
final /* synthetic */ class ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$1 extends FunctionReference implements Function1<NotificationContentView, Sequence<? extends View>> {
    public static final ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$1 INSTANCE = new ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$1();

    ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$1() {
        super(1);
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getName() {
        return "getLayouts";
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final KDeclarationContainer getOwner() {
        return null;
    }

    @Override // kotlin.jvm.internal.CallableReference
    public final String getSignature() {
        return "invoke(Lcom/android/systemui/statusbar/notification/row/NotificationContentView;)Lkotlin/sequences/Sequence;";
    }

    @Override // kotlin.jvm.functions.Function1
    @NotNull
    public final Sequence<View> invoke(@NotNull NotificationContentView p1) {
        Intrinsics.checkParameterIsNotNull(p1, "p1");
        return ConversationNotificationManager$1$onNotificationRankingUpdated$1.INSTANCE.invoke(p1);
    }
}
