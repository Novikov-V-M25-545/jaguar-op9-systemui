package com.android.systemui.controls.management;

import android.content.res.Configuration;
import com.android.systemui.controls.ControlsServiceInfo;
import com.android.systemui.controls.management.ControlsListingController;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import kotlin.Unit;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;

/* compiled from: AppAdapter.kt */
/* loaded from: classes.dex */
public final class AppAdapter$callback$1 implements ControlsListingController.ControlsListingCallback {
    final /* synthetic */ Executor $backgroundExecutor;
    final /* synthetic */ Executor $uiExecutor;
    final /* synthetic */ AppAdapter this$0;

    AppAdapter$callback$1(AppAdapter appAdapter, Executor executor, Executor executor2) {
        this.this$0 = appAdapter;
        this.$backgroundExecutor = executor;
        this.$uiExecutor = executor2;
    }

    @Override // com.android.systemui.controls.management.ControlsListingController.ControlsListingCallback
    public void onServicesUpdated(@NotNull final List<ControlsServiceInfo> serviceInfos) {
        Intrinsics.checkParameterIsNotNull(serviceInfos, "serviceInfos");
        this.$backgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1

            /* compiled from: AppAdapter.kt */
            /* renamed from: com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1$1, reason: invalid class name */
            static final /* synthetic */ class AnonymousClass1 extends FunctionReference implements Function0<Unit> {
                AnonymousClass1(AppAdapter appAdapter) {
                    super(0, appAdapter);
                }

                @Override // kotlin.jvm.internal.CallableReference
                public final String getName() {
                    return "notifyDataSetChanged";
                }

                @Override // kotlin.jvm.internal.CallableReference
                public final KDeclarationContainer getOwner() {
                    return Reflection.getOrCreateKotlinClass(AppAdapter.class);
                }

                @Override // kotlin.jvm.internal.CallableReference
                public final String getSignature() {
                    return "notifyDataSetChanged()V";
                }

                @Override // kotlin.jvm.functions.Function0
                public /* bridge */ /* synthetic */ Unit invoke() {
                    invoke2();
                    return Unit.INSTANCE;
                }

                /* renamed from: invoke, reason: avoid collision after fix types in other method */
                public final void invoke2() {
                    ((AppAdapter) this.receiver).notifyDataSetChanged();
                }
            }

            @Override // java.lang.Runnable
            public final void run() {
                Configuration configuration = this.this$0.this$0.resources.getConfiguration();
                Intrinsics.checkExpressionValueIsNotNull(configuration, "resources.configuration");
                final Collator collator = Collator.getInstance(configuration.getLocales().get(0));
                Intrinsics.checkExpressionValueIsNotNull(collator, "collator");
                Comparator<T> comparator = new Comparator<T>() { // from class: com.android.systemui.controls.management.AppAdapter$callback$1$onServicesUpdated$1$$special$$inlined$compareBy$1
                    /* JADX WARN: Multi-variable type inference failed */
                    @Override // java.util.Comparator
                    public final int compare(T t, T t2) {
                        Comparator comparator2 = collator;
                        CharSequence charSequenceLoadLabel = ((ControlsServiceInfo) t).loadLabel();
                        Intrinsics.checkExpressionValueIsNotNull(charSequenceLoadLabel, "it.loadLabel()");
                        CharSequence charSequenceLoadLabel2 = ((ControlsServiceInfo) t2).loadLabel();
                        Intrinsics.checkExpressionValueIsNotNull(charSequenceLoadLabel2, "it.loadLabel()");
                        return comparator2.compare(charSequenceLoadLabel, charSequenceLoadLabel2);
                    }
                };
                this.this$0.this$0.listOfServices = CollectionsKt___CollectionsKt.sortedWith(serviceInfos, comparator);
                Executor executor = this.this$0.$uiExecutor;
                final AnonymousClass1 anonymousClass1 = new AnonymousClass1(this.this$0.this$0);
                executor.execute(new Runnable() { // from class: com.android.systemui.controls.management.AppAdapter$sam$java_lang_Runnable$0
                    @Override // java.lang.Runnable
                    public final /* synthetic */ void run() {
                        Intrinsics.checkExpressionValueIsNotNull(anonymousClass1.invoke(), "invoke(...)");
                    }
                });
            }
        });
    }
}
