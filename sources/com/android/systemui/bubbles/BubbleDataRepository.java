package com.android.systemui.bubbles;

import android.annotation.SuppressLint;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.os.UserHandle;
import com.android.systemui.bubbles.storage.BubbleEntity;
import com.android.systemui.bubbles.storage.BubblePersistentRepository;
import com.android.systemui.bubbles.storage.BubbleVolatileRepository;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.collections.CollectionsKt__CollectionsJVMKt;
import kotlin.collections.CollectionsKt__CollectionsKt;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt__IntrinsicsKt;
import kotlin.coroutines.jvm.internal.Boxing;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt__Builders_commonKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.JobKt;
import kotlinx.coroutines.YieldKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BubbleDataRepository.kt */
/* loaded from: classes.dex */
public final class BubbleDataRepository {
    private final CoroutineScope ioScope;
    private Job job;
    private final LauncherApps launcherApps;
    private final BubblePersistentRepository persistentRepository;
    private final CoroutineScope uiScope;
    private final BubbleVolatileRepository volatileRepository;

    public BubbleDataRepository(@NotNull BubbleVolatileRepository volatileRepository, @NotNull BubblePersistentRepository persistentRepository, @NotNull LauncherApps launcherApps) {
        Intrinsics.checkParameterIsNotNull(volatileRepository, "volatileRepository");
        Intrinsics.checkParameterIsNotNull(persistentRepository, "persistentRepository");
        Intrinsics.checkParameterIsNotNull(launcherApps, "launcherApps");
        this.volatileRepository = volatileRepository;
        this.persistentRepository = persistentRepository;
        this.launcherApps = launcherApps;
        this.ioScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getIO());
        this.uiScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getMain());
    }

    public final void addBubble(@NotNull Bubble bubble) {
        Intrinsics.checkParameterIsNotNull(bubble, "bubble");
        addBubbles(CollectionsKt__CollectionsJVMKt.listOf(bubble));
    }

    public final void addBubbles(@NotNull List<? extends Bubble> bubbles) {
        Intrinsics.checkParameterIsNotNull(bubbles, "bubbles");
        this.volatileRepository.addBubbles(transform(bubbles));
        if (!r2.isEmpty()) {
            persistToDisk();
        }
    }

    public final void removeBubbles(@NotNull List<? extends Bubble> bubbles) {
        Intrinsics.checkParameterIsNotNull(bubbles, "bubbles");
        this.volatileRepository.removeBubbles(transform(bubbles));
        if (!r2.isEmpty()) {
            persistToDisk();
        }
    }

    /* compiled from: BubbleDataRepository.kt */
    @DebugMetadata(c = "com.android.systemui.bubbles.BubbleDataRepository$persistToDisk$1", f = "BubbleDataRepository.kt", l = {106, 108}, m = "invokeSuspend")
    /* renamed from: com.android.systemui.bubbles.BubbleDataRepository$persistToDisk$1, reason: invalid class name and case insensitive filesystem */
    static final class C00211 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Job $prev;
        Object L$0;
        int label;
        private CoroutineScope p$;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        C00211(Job job, Continuation continuation) {
            super(2, continuation);
            this.$prev = job;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        @NotNull
        public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> completion) {
            Intrinsics.checkParameterIsNotNull(completion, "completion");
            C00211 c00211 = BubbleDataRepository.this.new C00211(this.$prev, completion);
            c00211.p$ = (CoroutineScope) obj;
            return c00211;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((C00211) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) throws Throwable {
            CoroutineScope coroutineScope;
            Object coroutine_suspended = IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED();
            int i = this.label;
            if (i == 0) {
                ResultKt.throwOnFailure(obj);
                coroutineScope = this.p$;
                Job job = this.$prev;
                if (job != null) {
                    this.L$0 = coroutineScope;
                    this.label = 1;
                    if (JobKt.cancelAndJoin(job, this) == coroutine_suspended) {
                        return coroutine_suspended;
                    }
                }
            } else {
                if (i != 1) {
                    if (i == 2) {
                        ResultKt.throwOnFailure(obj);
                        BubbleDataRepository.this.persistentRepository.persistsToDisk(BubbleDataRepository.this.volatileRepository.getBubbles());
                        return Unit.INSTANCE;
                    }
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                coroutineScope = (CoroutineScope) this.L$0;
                ResultKt.throwOnFailure(obj);
            }
            this.L$0 = coroutineScope;
            this.label = 2;
            if (YieldKt.yield(this) == coroutine_suspended) {
                return coroutine_suspended;
            }
            BubbleDataRepository.this.persistentRepository.persistsToDisk(BubbleDataRepository.this.volatileRepository.getBubbles());
            return Unit.INSTANCE;
        }
    }

    private final void persistToDisk() {
        this.job = BuildersKt__Builders_commonKt.launch$default(this.ioScope, null, null, new C00211(this.job, null), 3, null);
    }

    /* compiled from: BubbleDataRepository.kt */
    @DebugMetadata(c = "com.android.systemui.bubbles.BubbleDataRepository$loadBubbles$1", f = "BubbleDataRepository.kt", l = {}, m = "invokeSuspend")
    /* renamed from: com.android.systemui.bubbles.BubbleDataRepository$loadBubbles$1, reason: invalid class name */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        final /* synthetic */ Function1 $cb;
        int label;
        private CoroutineScope p$;

        /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
        AnonymousClass1(Function1 function1, Continuation continuation) {
            super(2, continuation);
            this.$cb = function1;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        @NotNull
        public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> completion) {
            Intrinsics.checkParameterIsNotNull(completion, "completion");
            AnonymousClass1 anonymousClass1 = BubbleDataRepository.this.new AnonymousClass1(this.$cb, completion);
            anonymousClass1.p$ = (CoroutineScope) obj;
            return anonymousClass1;
        }

        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) throws Throwable {
            Object next;
            IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label != 0) {
                throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
            }
            ResultKt.throwOnFailure(obj);
            List<BubbleEntity> fromDisk = BubbleDataRepository.this.persistentRepository.readFromDisk();
            BubbleDataRepository.this.volatileRepository.addBubbles(fromDisk);
            ArrayList arrayList = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(fromDisk, 10));
            for (BubbleEntity bubbleEntity : fromDisk) {
                arrayList.add(new ShortcutKey(bubbleEntity.getUserId(), bubbleEntity.getPackageName()));
            }
            Set<ShortcutKey> set = CollectionsKt___CollectionsKt.toSet(arrayList);
            ArrayList arrayList2 = new ArrayList();
            for (ShortcutKey shortcutKey : set) {
                List<ShortcutInfo> shortcuts = BubbleDataRepository.this.launcherApps.getShortcuts(new LauncherApps.ShortcutQuery().setPackage(shortcutKey.getPkg()).setQueryFlags(1041), UserHandle.of(shortcutKey.getUserId()));
                if (shortcuts == null) {
                    shortcuts = CollectionsKt__CollectionsKt.emptyList();
                }
                CollectionsKt__MutableCollectionsKt.addAll(arrayList2, shortcuts);
            }
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (Object obj2 : arrayList2) {
                ShortcutInfo it = (ShortcutInfo) obj2;
                Intrinsics.checkExpressionValueIsNotNull(it, "it");
                int userId = it.getUserId();
                String str = it.getPackage();
                Intrinsics.checkExpressionValueIsNotNull(str, "it.`package`");
                ShortcutKey shortcutKey2 = new ShortcutKey(userId, str);
                Object arrayList3 = linkedHashMap.get(shortcutKey2);
                if (arrayList3 == null) {
                    arrayList3 = new ArrayList();
                    linkedHashMap.put(shortcutKey2, arrayList3);
                }
                ((List) arrayList3).add(obj2);
            }
            ArrayList arrayList4 = new ArrayList();
            Iterator<T> it2 = fromDisk.iterator();
            while (true) {
                Bubble bubble = null;
                if (!it2.hasNext()) {
                    BuildersKt__Builders_commonKt.launch$default(BubbleDataRepository.this.uiScope, null, null, new C00011(arrayList4, null), 3, null);
                    return Unit.INSTANCE;
                }
                BubbleEntity bubbleEntity2 = (BubbleEntity) it2.next();
                List list = (List) linkedHashMap.get(new ShortcutKey(bubbleEntity2.getUserId(), bubbleEntity2.getPackageName()));
                if (list != null) {
                    Iterator it3 = list.iterator();
                    while (true) {
                        if (!it3.hasNext()) {
                            next = null;
                            break;
                        }
                        next = it3.next();
                        ShortcutInfo shortcutInfo = (ShortcutInfo) next;
                        String shortcutId = bubbleEntity2.getShortcutId();
                        Intrinsics.checkExpressionValueIsNotNull(shortcutInfo, "shortcutInfo");
                        if (Boxing.boxBoolean(Intrinsics.areEqual(shortcutId, shortcutInfo.getId())).booleanValue()) {
                            break;
                        }
                    }
                    ShortcutInfo shortcutInfo2 = (ShortcutInfo) next;
                    if (shortcutInfo2 != null) {
                        bubble = new Bubble(bubbleEntity2.getKey(), shortcutInfo2, bubbleEntity2.getDesiredHeight(), bubbleEntity2.getDesiredHeightResId(), bubbleEntity2.getTitle());
                    }
                }
                if (bubble != null) {
                    arrayList4.add(bubble);
                }
            }
        }

        /* compiled from: BubbleDataRepository.kt */
        @DebugMetadata(c = "com.android.systemui.bubbles.BubbleDataRepository$loadBubbles$1$1", f = "BubbleDataRepository.kt", l = {}, m = "invokeSuspend")
        /* renamed from: com.android.systemui.bubbles.BubbleDataRepository$loadBubbles$1$1, reason: invalid class name and collision with other inner class name */
        static final class C00011 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
            final /* synthetic */ List $bubbles;
            int label;
            private CoroutineScope p$;

            /* JADX WARN: 'super' call moved to the top of the method (can break code semantics) */
            C00011(List list, Continuation continuation) {
                super(2, continuation);
                this.$bubbles = list;
            }

            @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
            @NotNull
            public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> completion) {
                Intrinsics.checkParameterIsNotNull(completion, "completion");
                C00011 c00011 = AnonymousClass1.this.new C00011(this.$bubbles, completion);
                c00011.p$ = (CoroutineScope) obj;
                return c00011;
            }

            @Override // kotlin.jvm.functions.Function2
            public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
                return ((C00011) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
            }

            @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
            @Nullable
            public final Object invokeSuspend(@NotNull Object obj) throws Throwable {
                IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED();
                if (this.label != 0) {
                    throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
                }
                ResultKt.throwOnFailure(obj);
                AnonymousClass1.this.$cb.invoke(this.$bubbles);
                return Unit.INSTANCE;
            }
        }
    }

    @SuppressLint({"WrongConstant"})
    @NotNull
    public final Job loadBubbles(@NotNull Function1<? super List<? extends Bubble>, Unit> cb) {
        Intrinsics.checkParameterIsNotNull(cb, "cb");
        return BuildersKt__Builders_commonKt.launch$default(this.ioScope, null, null, new AnonymousClass1(cb, null), 3, null);
    }

    private final List<BubbleEntity> transform(List<? extends Bubble> list) {
        BubbleEntity bubbleEntity;
        ArrayList arrayList = new ArrayList();
        for (Bubble bubble : list) {
            UserHandle user = bubble.getUser();
            Intrinsics.checkExpressionValueIsNotNull(user, "b.user");
            int identifier = user.getIdentifier();
            String packageName = bubble.getPackageName();
            Intrinsics.checkExpressionValueIsNotNull(packageName, "b.packageName");
            String metadataShortcutId = bubble.getMetadataShortcutId();
            if (metadataShortcutId != null) {
                String key = bubble.getKey();
                Intrinsics.checkExpressionValueIsNotNull(key, "b.key");
                bubbleEntity = new BubbleEntity(identifier, packageName, metadataShortcutId, key, bubble.getRawDesiredHeight(), bubble.getRawDesiredHeightResId(), bubble.getTitle());
            } else {
                bubbleEntity = null;
            }
            if (bubbleEntity != null) {
                arrayList.add(bubbleEntity);
            }
        }
        return arrayList;
    }
}
