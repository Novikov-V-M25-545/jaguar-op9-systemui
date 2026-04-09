package com.android.systemui.assist;

import android.content.Context;
import android.os.Handler;
import android.view.WindowManager;
import com.android.systemui.assist.AssistDisclosureView;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;

/* compiled from: AssistDisclosure.kt */
/* loaded from: classes.dex */
public final class AssistDisclosure {
    static final /* synthetic */ KProperty[] $$delegatedProperties = {Reflection.property1(new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(AssistDisclosure.class), "windowManager", "getWindowManager()Landroid/view/WindowManager;"))};
    private AssistDisclosureView assistView;
    private boolean assistViewAdded;
    private final Context context;
    private final Handler handler;
    private final Runnable showRunnable;
    private final Lazy windowManager$delegate;

    private final WindowManager getWindowManager() {
        Lazy lazy = this.windowManager$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (WindowManager) lazy.getValue();
    }

    public AssistDisclosure(@NotNull Context context, @NotNull Handler handler) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(handler, "handler");
        this.context = context;
        this.handler = handler;
        this.showRunnable = new Runnable() { // from class: com.android.systemui.assist.AssistDisclosure$showRunnable$1
            @Override // java.lang.Runnable
            public final void run() {
                this.this$0.show();
            }
        };
        this.windowManager$delegate = LazyKt__LazyJVMKt.lazy(new Function0<WindowManager>() { // from class: com.android.systemui.assist.AssistDisclosure$windowManager$2
            {
                super(0);
            }

            /* JADX WARN: Can't rename method to resolve collision */
            @Override // kotlin.jvm.functions.Function0
            public final WindowManager invoke() {
                return (WindowManager) this.this$0.context.getSystemService(WindowManager.class);
            }
        });
    }

    public final void postShow() {
        this.handler.removeCallbacks(this.showRunnable);
        this.handler.post(this.showRunnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void show() {
        AssistDisclosureView assistDisclosureView = this.assistView;
        if (assistDisclosureView == null) {
            assistDisclosureView = new AssistDisclosureView(this.context);
            assistDisclosureView.setOnFinishedListener(new AssistDisclosureView.OnFinishedListener() { // from class: com.android.systemui.assist.AssistDisclosure$show$$inlined$apply$lambda$1
                @Override // com.android.systemui.assist.AssistDisclosureView.OnFinishedListener
                public void onFinished() {
                    this.this$0.hide();
                }
            });
        }
        this.assistView = assistDisclosureView;
        if (this.assistViewAdded) {
            return;
        }
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(2015, 525576, -3);
        layoutParams.setTitle("AssistDisclosure");
        layoutParams.setFitInsetsTypes(0);
        getWindowManager().addView(this.assistView, layoutParams);
        this.assistViewAdded = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void hide() {
        if (this.assistViewAdded) {
            getWindowManager().removeView(this.assistView);
            this.assistViewAdded = false;
        }
    }
}
