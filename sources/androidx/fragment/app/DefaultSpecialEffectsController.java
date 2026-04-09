package androidx.fragment.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import androidx.collection.ArrayMap;
import androidx.core.os.CancellationSignal;
import androidx.core.view.OneShotPreDrawListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewGroupCompat;
import androidx.fragment.app.FragmentAnim;
import androidx.fragment.app.SpecialEffectsController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
class DefaultSpecialEffectsController extends SpecialEffectsController {
    private final HashMap<SpecialEffectsController.Operation, HashSet<CancellationSignal>> mRunningOperations;

    DefaultSpecialEffectsController(ViewGroup viewGroup) {
        super(viewGroup);
        this.mRunningOperations = new HashMap<>();
    }

    private void addCancellationSignal(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal) {
        if (this.mRunningOperations.get(operation) == null) {
            this.mRunningOperations.put(operation, new HashSet<>());
        }
        this.mRunningOperations.get(operation).add(cancellationSignal);
    }

    void removeCancellationSignal(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal) {
        HashSet<CancellationSignal> hashSet = this.mRunningOperations.get(operation);
        if (hashSet != null && hashSet.remove(cancellationSignal) && hashSet.isEmpty()) {
            this.mRunningOperations.remove(operation);
            operation.complete();
        }
    }

    void cancelAllSpecialEffects(SpecialEffectsController.Operation operation) {
        HashSet<CancellationSignal> hashSetRemove = this.mRunningOperations.remove(operation);
        if (hashSetRemove != null) {
            Iterator<CancellationSignal> it = hashSetRemove.iterator();
            while (it.hasNext()) {
                it.next().cancel();
            }
        }
    }

    /* renamed from: androidx.fragment.app.DefaultSpecialEffectsController$8, reason: invalid class name */
    static /* synthetic */ class AnonymousClass8 {
        static final /* synthetic */ int[] $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type;

        static {
            int[] iArr = new int[SpecialEffectsController.Operation.Type.values().length];
            $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type = iArr;
            try {
                iArr[SpecialEffectsController.Operation.Type.HIDE.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type[SpecialEffectsController.Operation.Type.REMOVE.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type[SpecialEffectsController.Operation.Type.SHOW.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type[SpecialEffectsController.Operation.Type.ADD.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:25:0x0072  */
    @Override // androidx.fragment.app.SpecialEffectsController
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    void executeOperations(java.util.List<androidx.fragment.app.SpecialEffectsController.Operation> r11, boolean r12) {
        /*
            r10 = this;
            java.util.Iterator r0 = r11.iterator()
            r1 = 0
            r2 = r1
        L6:
            boolean r3 = r0.hasNext()
            r4 = 1
            if (r3 == 0) goto L31
            java.lang.Object r3 = r0.next()
            androidx.fragment.app.SpecialEffectsController$Operation r3 = (androidx.fragment.app.SpecialEffectsController.Operation) r3
            int[] r5 = androidx.fragment.app.DefaultSpecialEffectsController.AnonymousClass8.$SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type
            androidx.fragment.app.SpecialEffectsController$Operation$Type r6 = r3.getType()
            int r6 = r6.ordinal()
            r5 = r5[r6]
            if (r5 == r4) goto L2d
            r4 = 2
            if (r5 == r4) goto L2d
            r4 = 3
            if (r5 == r4) goto L2b
            r4 = 4
            if (r5 == r4) goto L2b
            goto L6
        L2b:
            r2 = r3
            goto L6
        L2d:
            if (r1 != 0) goto L6
            r1 = r3
            goto L6
        L31:
            java.util.ArrayList r0 = new java.util.ArrayList
            r0.<init>()
            java.util.ArrayList r3 = new java.util.ArrayList
            r3.<init>()
            java.util.ArrayList r5 = new java.util.ArrayList
            r5.<init>(r11)
            java.util.Iterator r11 = r11.iterator()
        L44:
            boolean r6 = r11.hasNext()
            if (r6 == 0) goto L8e
            java.lang.Object r6 = r11.next()
            androidx.fragment.app.SpecialEffectsController$Operation r6 = (androidx.fragment.app.SpecialEffectsController.Operation) r6
            androidx.core.os.CancellationSignal r7 = new androidx.core.os.CancellationSignal
            r7.<init>()
            r10.addCancellationSignal(r6, r7)
            androidx.fragment.app.DefaultSpecialEffectsController$AnimationInfo r8 = new androidx.fragment.app.DefaultSpecialEffectsController$AnimationInfo
            r8.<init>(r6, r7)
            r0.add(r8)
            androidx.core.os.CancellationSignal r7 = new androidx.core.os.CancellationSignal
            r7.<init>()
            r10.addCancellationSignal(r6, r7)
            androidx.fragment.app.DefaultSpecialEffectsController$TransitionInfo r8 = new androidx.fragment.app.DefaultSpecialEffectsController$TransitionInfo
            r9 = 0
            if (r12 == 0) goto L70
            if (r6 != r1) goto L73
            goto L72
        L70:
            if (r6 != r2) goto L73
        L72:
            r9 = r4
        L73:
            r8.<init>(r6, r7, r12, r9)
            r3.add(r8)
            androidx.fragment.app.DefaultSpecialEffectsController$1 r7 = new androidx.fragment.app.DefaultSpecialEffectsController$1
            r7.<init>()
            r6.addCompletionListener(r7)
            androidx.core.os.CancellationSignal r7 = r6.getCancellationSignal()
            androidx.fragment.app.DefaultSpecialEffectsController$2 r8 = new androidx.fragment.app.DefaultSpecialEffectsController$2
            r8.<init>()
            r7.setOnCancelListener(r8)
            goto L44
        L8e:
            r10.startTransitions(r3, r12, r1, r2)
            java.util.Iterator r11 = r0.iterator()
        L95:
            boolean r12 = r11.hasNext()
            if (r12 == 0) goto Lad
            java.lang.Object r12 = r11.next()
            androidx.fragment.app.DefaultSpecialEffectsController$AnimationInfo r12 = (androidx.fragment.app.DefaultSpecialEffectsController.AnimationInfo) r12
            androidx.fragment.app.SpecialEffectsController$Operation r0 = r12.getOperation()
            androidx.core.os.CancellationSignal r12 = r12.getSignal()
            r10.startAnimation(r0, r12)
            goto L95
        Lad:
            java.util.Iterator r11 = r5.iterator()
        Lb1:
            boolean r12 = r11.hasNext()
            if (r12 == 0) goto Lc1
            java.lang.Object r12 = r11.next()
            androidx.fragment.app.SpecialEffectsController$Operation r12 = (androidx.fragment.app.SpecialEffectsController.Operation) r12
            r10.applyContainerChanges(r12)
            goto Lb1
        Lc1:
            r5.clear()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.fragment.app.DefaultSpecialEffectsController.executeOperations(java.util.List, boolean):void");
    }

    private void startAnimation(final SpecialEffectsController.Operation operation, final CancellationSignal cancellationSignal) {
        Animation enterViewTransitionAnimation;
        final ViewGroup container = getContainer();
        Context context = container.getContext();
        Fragment fragment = operation.getFragment();
        final View view = fragment.mView;
        SpecialEffectsController.Operation.Type type = operation.getType();
        SpecialEffectsController.Operation.Type type2 = SpecialEffectsController.Operation.Type.ADD;
        FragmentAnim.AnimationOrAnimator animationOrAnimatorLoadAnimation = FragmentAnim.loadAnimation(context, fragment, type == type2 || operation.getType() == SpecialEffectsController.Operation.Type.SHOW);
        if (animationOrAnimatorLoadAnimation == null) {
            removeCancellationSignal(operation, cancellationSignal);
            return;
        }
        container.startViewTransition(view);
        if (animationOrAnimatorLoadAnimation.animation != null) {
            if (operation.getType() == type2 || operation.getType() == SpecialEffectsController.Operation.Type.SHOW) {
                enterViewTransitionAnimation = new FragmentAnim.EnterViewTransitionAnimation(animationOrAnimatorLoadAnimation.animation);
            } else {
                enterViewTransitionAnimation = new FragmentAnim.EndViewTransitionAnimation(animationOrAnimatorLoadAnimation.animation, container, view);
            }
            Animation animation = enterViewTransitionAnimation;
            animation.setAnimationListener(new Animation.AnimationListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.3
                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationRepeat(Animation animation2) {
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationStart(Animation animation2) {
                }

                @Override // android.view.animation.Animation.AnimationListener
                public void onAnimationEnd(Animation animation2) {
                    container.post(new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.3.1
                        @Override // java.lang.Runnable
                        public void run() {
                            AnonymousClass3 anonymousClass3 = AnonymousClass3.this;
                            container.endViewTransition(view);
                            AnonymousClass3 anonymousClass32 = AnonymousClass3.this;
                            DefaultSpecialEffectsController.this.removeCancellationSignal(operation, cancellationSignal);
                        }
                    });
                }
            });
            view.startAnimation(animation);
        } else {
            animationOrAnimatorLoadAnimation.animator.addListener(new AnimatorListenerAdapter() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.4
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animator) {
                    container.endViewTransition(view);
                    DefaultSpecialEffectsController.this.removeCancellationSignal(operation, cancellationSignal);
                }
            });
            animationOrAnimatorLoadAnimation.animator.setTarget(view);
            animationOrAnimatorLoadAnimation.animator.start();
        }
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.5
            @Override // androidx.core.os.CancellationSignal.OnCancelListener
            public void onCancel() {
                view.clearAnimation();
            }
        });
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void startTransitions(List<TransitionInfo> list, boolean z, SpecialEffectsController.Operation operation, SpecialEffectsController.Operation operation2) {
        Object obj;
        View view;
        Iterator<TransitionInfo> it;
        ArrayList<String> sharedElementTargetNames;
        ArrayList<String> sharedElementSourceNames;
        View view2;
        final View view3;
        SpecialEffectsController.Operation operation3 = operation2;
        final FragmentTransitionImpl fragmentTransitionImpl = null;
        for (TransitionInfo transitionInfo : list) {
            FragmentTransitionImpl handlingImpl = transitionInfo.getHandlingImpl();
            if (fragmentTransitionImpl == null) {
                fragmentTransitionImpl = handlingImpl;
            } else if (handlingImpl != null && fragmentTransitionImpl != handlingImpl) {
                throw new IllegalArgumentException("Mixing framework transitions and AndroidX transitions is not allowed. Fragment " + transitionInfo.getOperation().getFragment() + " returned Transition " + transitionInfo.getTransition() + " which uses a different Transition  type than other Fragments.");
            }
        }
        if (fragmentTransitionImpl == null) {
            for (TransitionInfo transitionInfo2 : list) {
                removeCancellationSignal(transitionInfo2.getOperation(), transitionInfo2.getSignal());
            }
            return;
        }
        View view4 = new View(getContainer().getContext());
        final Rect rect = new Rect();
        ArrayList<View> arrayList = new ArrayList<>();
        ArrayList<View> arrayList2 = new ArrayList<>();
        ArrayMap arrayMap = new ArrayMap();
        Iterator<TransitionInfo> it2 = list.iterator();
        View view5 = null;
        boolean z2 = false;
        Object objWrapTransitionInSet = null;
        while (it2.hasNext()) {
            TransitionInfo next = it2.next();
            if (!next.hasSharedElementTransition() || operation == null || operation3 == null) {
                it = it2;
                view5 = view5;
            } else {
                objWrapTransitionInSet = fragmentTransitionImpl.wrapTransitionInSet(fragmentTransitionImpl.cloneTransition(next.getSharedElementTransition()));
                Fragment fragment = next.getOperation().getFragment();
                if (!z) {
                    sharedElementTargetNames = fragment.getSharedElementSourceNames();
                    sharedElementSourceNames = fragment.getSharedElementTargetNames();
                } else {
                    sharedElementTargetNames = fragment.getSharedElementTargetNames();
                    sharedElementSourceNames = fragment.getSharedElementSourceNames();
                }
                int size = sharedElementTargetNames.size();
                int i = 0;
                while (i < size) {
                    int i2 = size;
                    ArrayList<String> arrayList3 = sharedElementTargetNames;
                    arrayMap.put(arrayList3.get(i), sharedElementSourceNames.get(i));
                    i++;
                    sharedElementTargetNames = arrayList3;
                    size = i2;
                    it2 = it2;
                    view5 = view5;
                }
                it = it2;
                View view6 = view5;
                ArrayList<String> arrayList4 = sharedElementTargetNames;
                ArrayMap arrayMap2 = new ArrayMap();
                findNamedViews(arrayMap2, operation.getFragment().mView);
                arrayMap2.retainAll(arrayList4);
                arrayMap.retainAll(arrayMap2.keySet());
                ArrayMap arrayMap3 = new ArrayMap();
                findNamedViews(arrayMap3, operation2.getFragment().mView);
                arrayMap3.retainAll(sharedElementSourceNames);
                FragmentTransition.retainValues(arrayMap, arrayMap3);
                arrayMap2.retainAll(arrayMap.keySet());
                arrayMap3.retainAll(arrayMap.values());
                if (arrayMap.isEmpty()) {
                    arrayList.clear();
                    arrayList2.clear();
                    view5 = view6;
                    objWrapTransitionInSet = null;
                } else {
                    for (Iterator it3 = arrayMap2.values().iterator(); it3.hasNext(); it3 = it3) {
                        captureTransitioningViews(arrayList, (View) it3.next());
                    }
                    if (arrayList4.isEmpty()) {
                        view2 = view6;
                    } else {
                        view2 = (View) arrayMap2.get(arrayList4.get(0));
                        fragmentTransitionImpl.setEpicenter(objWrapTransitionInSet, view2);
                    }
                    Iterator it4 = arrayMap3.values().iterator();
                    while (it4.hasNext()) {
                        captureTransitioningViews(arrayList2, (View) it4.next());
                    }
                    if (!sharedElementSourceNames.isEmpty() && (view3 = (View) arrayMap3.get(sharedElementSourceNames.get(0))) != null) {
                        OneShotPreDrawListener.add(getContainer(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.6
                            @Override // java.lang.Runnable
                            public void run() {
                                fragmentTransitionImpl.getBoundsOnScreen(view3, rect);
                            }
                        });
                        z2 = true;
                    }
                    fragmentTransitionImpl.addTargets(objWrapTransitionInSet, arrayList);
                    view5 = view2;
                }
            }
            it2 = it;
        }
        View view7 = view5;
        ArrayList arrayList5 = new ArrayList();
        Iterator<TransitionInfo> it5 = list.iterator();
        Object objMergeTransitionsTogether = null;
        Object objMergeTransitionsTogether2 = null;
        while (it5.hasNext()) {
            TransitionInfo next2 = it5.next();
            Iterator<TransitionInfo> it6 = it5;
            Object objCloneTransition = fragmentTransitionImpl.cloneTransition(next2.getTransition());
            ArrayMap arrayMap4 = arrayMap;
            SpecialEffectsController.Operation operation4 = next2.getOperation();
            boolean z3 = objWrapTransitionInSet != null && (operation4 == operation || operation4 == operation3);
            if (objCloneTransition == null) {
                if (!z3) {
                    removeCancellationSignal(next2.getOperation(), next2.getSignal());
                }
                obj = objWrapTransitionInSet;
                view = view7;
            } else {
                ArrayList<View> arrayList6 = new ArrayList<>();
                obj = objWrapTransitionInSet;
                captureTransitioningViews(arrayList6, next2.getOperation().getFragment().mView);
                if (z3) {
                    if (operation4 == operation) {
                        arrayList6.removeAll(arrayList);
                    } else {
                        arrayList6.removeAll(arrayList2);
                    }
                }
                if (arrayList6.isEmpty()) {
                    fragmentTransitionImpl.addTarget(objCloneTransition, view4);
                } else {
                    fragmentTransitionImpl.addTargets(objCloneTransition, arrayList6);
                }
                if (next2.getOperation().getType().equals(SpecialEffectsController.Operation.Type.ADD)) {
                    arrayList5.addAll(arrayList6);
                    if (z2) {
                        fragmentTransitionImpl.setEpicenter(objCloneTransition, rect);
                    }
                    view = view7;
                } else {
                    view = view7;
                    fragmentTransitionImpl.setEpicenter(objCloneTransition, view);
                }
                if (next2.isOverlapAllowed()) {
                    objMergeTransitionsTogether = fragmentTransitionImpl.mergeTransitionsTogether(objMergeTransitionsTogether, objCloneTransition, null);
                } else {
                    objMergeTransitionsTogether2 = fragmentTransitionImpl.mergeTransitionsTogether(objMergeTransitionsTogether2, objCloneTransition, null);
                }
            }
            it5 = it6;
            view7 = view;
            arrayMap = arrayMap4;
            objWrapTransitionInSet = obj;
            operation3 = operation2;
        }
        ArrayMap arrayMap5 = arrayMap;
        Object objMergeTransitionsInSequence = fragmentTransitionImpl.mergeTransitionsInSequence(objMergeTransitionsTogether, objMergeTransitionsTogether2, objWrapTransitionInSet);
        for (final TransitionInfo transitionInfo3 : list) {
            if (transitionInfo3.getTransition() != null) {
                fragmentTransitionImpl.setListenerForTransitionEnd(transitionInfo3.getOperation().getFragment(), objMergeTransitionsInSequence, transitionInfo3.getSignal(), new Runnable() { // from class: androidx.fragment.app.DefaultSpecialEffectsController.7
                    @Override // java.lang.Runnable
                    public void run() {
                        DefaultSpecialEffectsController.this.removeCancellationSignal(transitionInfo3.getOperation(), transitionInfo3.getSignal());
                    }
                });
            }
        }
        FragmentTransition.setViewVisibility(arrayList5, 4);
        ArrayList<String> arrayListPrepareSetNameOverridesReordered = fragmentTransitionImpl.prepareSetNameOverridesReordered(arrayList2);
        fragmentTransitionImpl.beginDelayedTransition(getContainer(), objMergeTransitionsInSequence);
        fragmentTransitionImpl.setNameOverridesReordered(getContainer(), arrayList, arrayList2, arrayListPrepareSetNameOverridesReordered, arrayMap5);
        FragmentTransition.setViewVisibility(arrayList5, 0);
        fragmentTransitionImpl.swapSharedElementTargets(objWrapTransitionInSet, arrayList, arrayList2);
    }

    void captureTransitioningViews(ArrayList<View> arrayList, View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            if (ViewGroupCompat.isTransitionGroup(viewGroup)) {
                arrayList.add(viewGroup);
                return;
            }
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt.getVisibility() == 0) {
                    captureTransitioningViews(arrayList, childAt);
                }
            }
            return;
        }
        arrayList.add(view);
    }

    void findNamedViews(Map<String, View> map, View view) {
        String transitionName = ViewCompat.getTransitionName(view);
        if (transitionName != null) {
            map.put(transitionName, view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt.getVisibility() == 0) {
                    findNamedViews(map, childAt);
                }
            }
        }
    }

    void applyContainerChanges(SpecialEffectsController.Operation operation) {
        View view = operation.getFragment().mView;
        int i = AnonymousClass8.$SwitchMap$androidx$fragment$app$SpecialEffectsController$Operation$Type[operation.getType().ordinal()];
        if (i == 1) {
            view.setVisibility(8);
            return;
        }
        if (i == 2) {
            getContainer().removeView(view);
        } else if (i == 3 || i == 4) {
            view.setVisibility(0);
        }
    }

    private static class AnimationInfo {
        private final SpecialEffectsController.Operation mOperation;
        private final CancellationSignal mSignal;

        AnimationInfo(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal) {
            this.mOperation = operation;
            this.mSignal = cancellationSignal;
        }

        SpecialEffectsController.Operation getOperation() {
            return this.mOperation;
        }

        CancellationSignal getSignal() {
            return this.mSignal;
        }
    }

    private static class TransitionInfo {
        private final SpecialEffectsController.Operation mOperation;
        private final boolean mOverlapAllowed;
        private final Object mSharedElementTransition;
        private final CancellationSignal mSignal;
        private final Object mTransition;

        TransitionInfo(SpecialEffectsController.Operation operation, CancellationSignal cancellationSignal, boolean z, boolean z2) {
            Object enterTransition;
            boolean allowReturnTransitionOverlap;
            Object exitTransition;
            this.mOperation = operation;
            this.mSignal = cancellationSignal;
            if (operation.getType() == SpecialEffectsController.Operation.Type.ADD || operation.getType() == SpecialEffectsController.Operation.Type.SHOW) {
                if (z) {
                    enterTransition = operation.getFragment().getReenterTransition();
                } else {
                    enterTransition = operation.getFragment().getEnterTransition();
                }
                this.mTransition = enterTransition;
                if (z) {
                    allowReturnTransitionOverlap = operation.getFragment().getAllowEnterTransitionOverlap();
                } else {
                    allowReturnTransitionOverlap = operation.getFragment().getAllowReturnTransitionOverlap();
                }
                this.mOverlapAllowed = allowReturnTransitionOverlap;
            } else {
                if (z) {
                    exitTransition = operation.getFragment().getReturnTransition();
                } else {
                    exitTransition = operation.getFragment().getExitTransition();
                }
                this.mTransition = exitTransition;
                this.mOverlapAllowed = true;
            }
            if (!z2) {
                this.mSharedElementTransition = null;
            } else if (z) {
                this.mSharedElementTransition = operation.getFragment().getSharedElementReturnTransition();
            } else {
                this.mSharedElementTransition = operation.getFragment().getSharedElementEnterTransition();
            }
        }

        SpecialEffectsController.Operation getOperation() {
            return this.mOperation;
        }

        CancellationSignal getSignal() {
            return this.mSignal;
        }

        Object getTransition() {
            return this.mTransition;
        }

        boolean isOverlapAllowed() {
            return this.mOverlapAllowed;
        }

        public boolean hasSharedElementTransition() {
            return this.mSharedElementTransition != null;
        }

        public Object getSharedElementTransition() {
            return this.mSharedElementTransition;
        }

        FragmentTransitionImpl getHandlingImpl() {
            FragmentTransitionImpl handlingImpl = getHandlingImpl(this.mTransition);
            FragmentTransitionImpl handlingImpl2 = getHandlingImpl(this.mSharedElementTransition);
            if (handlingImpl == null || handlingImpl2 == null || handlingImpl == handlingImpl2) {
                return handlingImpl != null ? handlingImpl : handlingImpl2;
            }
            throw new IllegalArgumentException("Mixing framework transitions and AndroidX transitions is not allowed. Fragment " + this.mOperation.getFragment() + " returned Transition " + this.mTransition + " which uses a different Transition  type than its shared element transition " + this.mSharedElementTransition);
        }

        private FragmentTransitionImpl getHandlingImpl(Object obj) {
            if (obj == null) {
                return null;
            }
            FragmentTransitionImpl fragmentTransitionImpl = FragmentTransition.PLATFORM_IMPL;
            if (fragmentTransitionImpl != null && fragmentTransitionImpl.canHandle(obj)) {
                return fragmentTransitionImpl;
            }
            FragmentTransitionImpl fragmentTransitionImpl2 = FragmentTransition.SUPPORT_IMPL;
            if (fragmentTransitionImpl2 != null && fragmentTransitionImpl2.canHandle(obj)) {
                return fragmentTransitionImpl2;
            }
            throw new IllegalArgumentException("Transition " + obj + " for fragment " + this.mOperation.getFragment() + " is not a valid framework Transition or AndroidX Transition");
        }
    }
}
