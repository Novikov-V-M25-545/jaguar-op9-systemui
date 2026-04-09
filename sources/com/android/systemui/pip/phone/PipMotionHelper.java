package com.android.systemui.pip.phone;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.ArrayMap;
import android.view.Choreographer;
import androidx.dynamicanimation.animation.AnimationHandler;
import com.android.systemui.pip.PipSnapAlgorithm;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.phone.PipAppOpsListener;
import com.android.systemui.util.FloatingContentCoordinator;
import com.android.systemui.util.animation.FloatProperties;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.magnetictarget.MagnetizedObject;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/* loaded from: classes.dex */
public class PipMotionHelper implements PipAppOpsListener.Callback, FloatingContentCoordinator.FloatingContent {
    private final Rect mAnimatingToBounds;
    private final Rect mBounds;
    private final PhysicsAnimator.SpringConfig mConflictResolutionSpringConfig;
    private final Context mContext;
    private boolean mDismissalPending;
    private PhysicsAnimator.FlingConfig mFlingConfigX;
    private PhysicsAnimator.FlingConfig mFlingConfigY;
    private final Rect mFloatingAllowedArea;
    private FloatingContentCoordinator mFloatingContentCoordinator;
    private MagnetizedObject<Rect> mMagnetizedPip;
    private PipMenuActivityController mMenuController;
    private final Rect mMovementBounds;
    private final PipTaskOrganizer mPipTaskOrganizer;
    private final PipTaskOrganizer.PipTransitionCallback mPipTransitionCallback;
    private Runnable mPostPipTransitionCallback;
    private final PhysicsAnimator.UpdateListener<Rect> mResizePipUpdateListener;
    private ThreadLocal<AnimationHandler> mSfAnimationHandlerThreadLocal;
    private PipSnapAlgorithm mSnapAlgorithm;
    private final PhysicsAnimator.SpringConfig mSpringConfig;
    private boolean mSpringingToTouch;
    private final Rect mTemporaryBounds;
    private PhysicsAnimator<Rect> mTemporaryBoundsPhysicsAnimator;
    private final Consumer<Rect> mUpdateBoundsCallback;

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ AnimationHandler lambda$new$2() {
        return new AnimationHandler(new AnimationHandler.FrameCallbackScheduler() { // from class: com.android.systemui.pip.phone.PipMotionHelper$$ExternalSyntheticLambda1
            @Override // androidx.dynamicanimation.animation.AnimationHandler.FrameCallbackScheduler
            public final void postFrameCallback(Runnable runnable) {
                PipMotionHelper.lambda$new$1(runnable);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$new$1(final Runnable runnable) {
        Choreographer.getSfInstance().postFrameCallback(new Choreographer.FrameCallback() { // from class: com.android.systemui.pip.phone.PipMotionHelper$$ExternalSyntheticLambda0
            @Override // android.view.Choreographer.FrameCallback
            public final void doFrame(long j) {
                runnable.run();
            }
        });
    }

    public PipMotionHelper(Context context, PipTaskOrganizer pipTaskOrganizer, PipMenuActivityController pipMenuActivityController, PipSnapAlgorithm pipSnapAlgorithm, FloatingContentCoordinator floatingContentCoordinator) {
        final Rect rect = new Rect();
        this.mBounds = rect;
        this.mMovementBounds = new Rect();
        this.mFloatingAllowedArea = new Rect();
        Rect rect2 = new Rect();
        this.mTemporaryBounds = rect2;
        this.mAnimatingToBounds = new Rect();
        this.mSfAnimationHandlerThreadLocal = ThreadLocal.withInitial(new Supplier() { // from class: com.android.systemui.pip.phone.PipMotionHelper$$ExternalSyntheticLambda8
            @Override // java.util.function.Supplier
            public final Object get() {
                return PipMotionHelper.lambda$new$2();
            }
        });
        this.mTemporaryBoundsPhysicsAnimator = PhysicsAnimator.getInstance(rect2);
        this.mSpringConfig = new PhysicsAnimator.SpringConfig(1500.0f, 0.75f);
        this.mConflictResolutionSpringConfig = new PhysicsAnimator.SpringConfig(200.0f, 0.75f);
        Objects.requireNonNull(rect);
        this.mUpdateBoundsCallback = new Consumer() { // from class: com.android.systemui.pip.phone.PipMotionHelper$$ExternalSyntheticLambda7
            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                rect.set((Rect) obj);
            }
        };
        this.mSpringingToTouch = false;
        this.mDismissalPending = false;
        PipTaskOrganizer.PipTransitionCallback pipTransitionCallback = new PipTaskOrganizer.PipTransitionCallback() { // from class: com.android.systemui.pip.phone.PipMotionHelper.1
            @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
            public void onPipTransitionCanceled(ComponentName componentName, int i) {
            }

            @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
            public void onPipTransitionStarted(ComponentName componentName, int i) {
            }

            @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
            public void onPipTransitionFinished(ComponentName componentName, int i) {
                if (PipMotionHelper.this.mPostPipTransitionCallback != null) {
                    PipMotionHelper.this.mPostPipTransitionCallback.run();
                    PipMotionHelper.this.mPostPipTransitionCallback = null;
                }
            }
        };
        this.mPipTransitionCallback = pipTransitionCallback;
        this.mContext = context;
        this.mPipTaskOrganizer = pipTaskOrganizer;
        this.mMenuController = pipMenuActivityController;
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mFloatingContentCoordinator = floatingContentCoordinator;
        pipTaskOrganizer.registerPipTransitionCallback(pipTransitionCallback);
        this.mTemporaryBoundsPhysicsAnimator.setCustomAnimationHandler(this.mSfAnimationHandlerThreadLocal.get());
        this.mResizePipUpdateListener = new PhysicsAnimator.UpdateListener() { // from class: com.android.systemui.pip.phone.PipMotionHelper$$ExternalSyntheticLambda2
            @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
            public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                this.f$0.lambda$new$3((Rect) obj, arrayMap);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$3(Rect rect, ArrayMap arrayMap) {
        if (this.mTemporaryBounds.isEmpty()) {
            return;
        }
        this.mPipTaskOrganizer.scheduleUserResizePip(this.mBounds, this.mTemporaryBounds, null);
    }

    @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
    public Rect getFloatingBoundsOnScreen() {
        return !this.mAnimatingToBounds.isEmpty() ? this.mAnimatingToBounds : this.mBounds;
    }

    @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
    public Rect getAllowedFloatingBoundsRegion() {
        return this.mFloatingAllowedArea;
    }

    @Override // com.android.systemui.util.FloatingContentCoordinator.FloatingContent
    public void moveToBounds(Rect rect) {
        animateToBounds(rect, this.mConflictResolutionSpringConfig);
    }

    void synchronizePinnedStackBounds() {
        cancelAnimations();
        this.mBounds.set(this.mPipTaskOrganizer.getLastReportedBounds());
        this.mTemporaryBounds.setEmpty();
        if (this.mPipTaskOrganizer.isInPip()) {
            this.mFloatingContentCoordinator.onContentMoved(this);
        }
    }

    void movePip(Rect rect) {
        movePip(rect, false);
    }

    void movePip(Rect rect, boolean z) {
        if (!z) {
            this.mFloatingContentCoordinator.onContentMoved(this);
        }
        if (!this.mSpringingToTouch) {
            cancelAnimations();
            if (!z) {
                resizePipUnchecked(rect);
                this.mBounds.set(rect);
                return;
            } else {
                this.mTemporaryBounds.set(rect);
                this.mPipTaskOrganizer.scheduleUserResizePip(this.mBounds, this.mTemporaryBounds, null);
                return;
            }
        }
        this.mTemporaryBoundsPhysicsAnimator.spring(FloatProperties.RECT_WIDTH, this.mBounds.width(), this.mSpringConfig).spring(FloatProperties.RECT_HEIGHT, this.mBounds.height(), this.mSpringConfig).spring(FloatProperties.RECT_X, rect.left, this.mSpringConfig).spring(FloatProperties.RECT_Y, rect.top, this.mSpringConfig);
        startBoundsAnimator(rect.left, rect.top, false);
    }

    void animateIntoDismissTarget(MagnetizedObject.MagneticTarget magneticTarget, float f, float f2, boolean z, Function0<Unit> function0) {
        PointF centerOnScreen = magneticTarget.getCenterOnScreen();
        float fWidth = this.mBounds.width() / 2;
        float fHeight = this.mBounds.height() / 2;
        float f3 = centerOnScreen.x - (fWidth / 2.0f);
        float f4 = centerOnScreen.y - (fHeight / 2.0f);
        if (this.mTemporaryBounds.isEmpty()) {
            this.mTemporaryBounds.set(this.mBounds);
        }
        this.mTemporaryBoundsPhysicsAnimator.spring(FloatProperties.RECT_X, f3, f, this.mSpringConfig).spring(FloatProperties.RECT_Y, f4, f2, this.mSpringConfig).spring(FloatProperties.RECT_WIDTH, fWidth, this.mSpringConfig).spring(FloatProperties.RECT_HEIGHT, fHeight, this.mSpringConfig).withEndActions(function0);
        startBoundsAnimator(f3, f4, false);
    }

    void setSpringingToTouch(boolean z) {
        this.mSpringingToTouch = z;
    }

    void expandPipToFullscreen() {
        expandPipToFullscreen(false);
    }

    void expandPipToFullscreen(final boolean z) {
        cancelAnimations();
        this.mMenuController.hideMenuWithoutResize();
        this.mPipTaskOrganizer.getUpdateHandler().post(new Runnable() { // from class: com.android.systemui.pip.phone.PipMotionHelper$$ExternalSyntheticLambda6
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$expandPipToFullscreen$4(z);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$expandPipToFullscreen$4(boolean z) {
        this.mPipTaskOrganizer.exitPip(z ? 0 : 300);
    }

    @Override // com.android.systemui.pip.phone.PipAppOpsListener.Callback
    public void dismissPip() {
        cancelAnimations();
        this.mMenuController.hideMenuWithoutResize();
        this.mPipTaskOrganizer.removePip();
    }

    void setCurrentMovementBounds(Rect rect) {
        this.mMovementBounds.set(rect);
        rebuildFlingConfigs();
        this.mFloatingAllowedArea.set(this.mMovementBounds);
        this.mFloatingAllowedArea.right += this.mBounds.width();
        this.mFloatingAllowedArea.bottom += this.mBounds.height();
    }

    Rect getBounds() {
        return this.mBounds;
    }

    Rect getPossiblyAnimatingBounds() {
        return this.mTemporaryBounds.isEmpty() ? this.mBounds : this.mTemporaryBounds;
    }

    void flingToSnapTarget(float f, float f2, final Runnable runnable, Runnable runnable2) {
        this.mSpringingToTouch = false;
        this.mTemporaryBoundsPhysicsAnimator.spring(FloatProperties.RECT_WIDTH, this.mBounds.width(), this.mSpringConfig).spring(FloatProperties.RECT_HEIGHT, this.mBounds.height(), this.mSpringConfig).flingThenSpring(FloatProperties.RECT_X, f, this.mFlingConfigX, this.mSpringConfig, true).flingThenSpring(FloatProperties.RECT_Y, f2, this.mFlingConfigY, this.mSpringConfig).withEndActions(runnable2);
        if (runnable != null) {
            this.mTemporaryBoundsPhysicsAnimator.addUpdateListener(new PhysicsAnimator.UpdateListener() { // from class: com.android.systemui.pip.phone.PipMotionHelper$$ExternalSyntheticLambda3
                @Override // com.android.systemui.util.animation.PhysicsAnimator.UpdateListener
                public final void onAnimationUpdateForProperty(Object obj, ArrayMap arrayMap) {
                    runnable.run();
                }
            });
        }
        startBoundsAnimator(f < 0.0f ? this.mMovementBounds.left : this.mMovementBounds.right, PhysicsAnimator.estimateFlingEndValue(this.mTemporaryBounds.top, f2, this.mFlingConfigY), false);
    }

    void animateToBounds(Rect rect, PhysicsAnimator.SpringConfig springConfig) {
        if (!this.mTemporaryBoundsPhysicsAnimator.isRunning()) {
            this.mTemporaryBounds.set(this.mBounds);
        }
        this.mTemporaryBoundsPhysicsAnimator.spring(FloatProperties.RECT_X, rect.left, springConfig).spring(FloatProperties.RECT_Y, rect.top, springConfig);
        startBoundsAnimator(rect.left, rect.top, false);
    }

    void animateDismiss() {
        this.mTemporaryBoundsPhysicsAnimator.spring(FloatProperties.RECT_Y, this.mMovementBounds.bottom + (this.mBounds.height() * 2), 0.0f, this.mSpringConfig).withEndActions(new Runnable() { // from class: com.android.systemui.pip.phone.PipMotionHelper$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.dismissPip();
            }
        });
        Rect rect = this.mBounds;
        startBoundsAnimator(rect.left, rect.bottom + rect.height(), true);
        this.mDismissalPending = false;
    }

    float animateToExpandedState(Rect rect, Rect rect2, Rect rect3, Runnable runnable) {
        float snapFraction = this.mSnapAlgorithm.getSnapFraction(new Rect(this.mBounds), rect2);
        this.mSnapAlgorithm.applySnapFraction(rect, rect3, snapFraction);
        this.mPostPipTransitionCallback = runnable;
        resizeAndAnimatePipUnchecked(rect, 250);
        return snapFraction;
    }

    void animateToUnexpandedState(Rect rect, float f, Rect rect2, Rect rect3, boolean z) {
        if (f < 0.0f) {
            f = this.mSnapAlgorithm.getSnapFraction(new Rect(this.mBounds), rect3);
        }
        this.mSnapAlgorithm.applySnapFraction(rect, rect2, f);
        if (z) {
            movePip(rect);
        } else {
            resizeAndAnimatePipUnchecked(rect, 250);
        }
    }

    void animateToOffset(Rect rect, int i) {
        cancelAnimations();
        this.mPipTaskOrganizer.scheduleOffsetPip(rect, i, 300, this.mUpdateBoundsCallback);
    }

    private void cancelAnimations() {
        this.mTemporaryBoundsPhysicsAnimator.cancel();
        this.mAnimatingToBounds.setEmpty();
        this.mSpringingToTouch = false;
    }

    private void rebuildFlingConfigs() {
        Rect rect = this.mMovementBounds;
        this.mFlingConfigX = new PhysicsAnimator.FlingConfig(2.0f, rect.left, rect.right);
        Rect rect2 = this.mMovementBounds;
        this.mFlingConfigY = new PhysicsAnimator.FlingConfig(2.0f, rect2.top, rect2.bottom);
    }

    private void startBoundsAnimator(float f, float f2, boolean z) {
        if (!this.mSpringingToTouch) {
            cancelAnimations();
        }
        int i = (int) f;
        int i2 = (int) f2;
        this.mAnimatingToBounds.set(i, i2, this.mBounds.width() + i, this.mBounds.height() + i2);
        setAnimatingToBounds(this.mAnimatingToBounds);
        if (!this.mTemporaryBoundsPhysicsAnimator.isRunning()) {
            this.mTemporaryBoundsPhysicsAnimator.addUpdateListener(this.mResizePipUpdateListener).withEndActions(new Runnable() { // from class: com.android.systemui.pip.phone.PipMotionHelper$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.onBoundsAnimationEnd();
                }
            });
        }
        this.mTemporaryBoundsPhysicsAnimator.start();
    }

    void notifyDismissalPending() {
        this.mDismissalPending = true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onBoundsAnimationEnd() {
        if (!this.mDismissalPending && !this.mSpringingToTouch && !this.mMagnetizedPip.getObjectStuckToTarget()) {
            this.mBounds.set(this.mTemporaryBounds);
            if (!this.mDismissalPending) {
                this.mPipTaskOrganizer.scheduleFinishResizePip(this.mBounds);
            }
            this.mTemporaryBounds.setEmpty();
        }
        this.mAnimatingToBounds.setEmpty();
        this.mSpringingToTouch = false;
        this.mDismissalPending = false;
    }

    private void setAnimatingToBounds(Rect rect) {
        this.mAnimatingToBounds.set(rect);
        this.mFloatingContentCoordinator.onContentMoved(this);
    }

    private void resizePipUnchecked(Rect rect) {
        if (rect.equals(this.mBounds)) {
            return;
        }
        this.mPipTaskOrganizer.scheduleResizePip(rect, this.mUpdateBoundsCallback);
    }

    private void resizeAndAnimatePipUnchecked(Rect rect, int i) {
        this.mPipTaskOrganizer.scheduleAnimateResizePip(rect, i, this.mUpdateBoundsCallback);
        setAnimatingToBounds(rect);
    }

    MagnetizedObject<Rect> getMagnetizedPip() {
        if (this.mMagnetizedPip == null) {
            this.mMagnetizedPip = new MagnetizedObject<Rect>(this.mContext, this.mTemporaryBounds, FloatProperties.RECT_X, FloatProperties.RECT_Y) { // from class: com.android.systemui.pip.phone.PipMotionHelper.2
                @Override // com.android.systemui.util.magnetictarget.MagnetizedObject
                public float getWidth(Rect rect) {
                    return rect.width();
                }

                @Override // com.android.systemui.util.magnetictarget.MagnetizedObject
                public float getHeight(Rect rect) {
                    return rect.height();
                }

                @Override // com.android.systemui.util.magnetictarget.MagnetizedObject
                public void getLocationOnScreen(Rect rect, int[] iArr) {
                    iArr[0] = rect.left;
                    iArr[1] = rect.top;
                }
            };
        }
        return this.mMagnetizedPip;
    }

    public void dump(PrintWriter printWriter, String str) {
        printWriter.println(str + "PipMotionHelper");
        printWriter.println((str + "  ") + "mBounds=" + this.mBounds);
    }
}
