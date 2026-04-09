package com.android.systemui.media;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Outline;
import android.util.MathUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import androidx.core.view.GestureDetectorCompat;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import com.android.settingslib.Utils;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.qs.PageIndicator;
import com.android.systemui.util.animation.PhysicsAnimator;
import com.android.systemui.util.concurrency.DelayableExecutor;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaCarouselScrollHandler.kt */
/* loaded from: classes.dex */
public final class MediaCarouselScrollHandler {
    private static final MediaCarouselScrollHandler$Companion$CONTENT_TRANSLATION$1 CONTENT_TRANSLATION;
    public static final Companion Companion = new Companion(null);
    private int activeMediaIndex;
    private int carouselHeight;
    private int carouselWidth;
    private final Function0<Unit> closeGuts;
    private float contentTranslation;
    private int cornerRadius;
    private final Function0<Unit> dismissCallback;
    private final FalsingManager falsingManager;
    private boolean falsingProtectionNeeded;
    private final GestureDetectorCompat gestureDetector;
    private final MediaCarouselScrollHandler$gestureListener$1 gestureListener;
    private final DelayableExecutor mainExecutor;
    private ViewGroup mediaContent;
    private final PageIndicator pageIndicator;
    private int playerWidthPlusPadding;
    private final MediaCarouselScrollHandler$scrollChangedListener$1 scrollChangedListener;
    private int scrollIntoCurrentMedia;
    private final MediaScrollView scrollView;
    private View settingsButton;
    private boolean showsSettingsButton;
    private final MediaCarouselScrollHandler$touchListener$1 touchListener;
    private Function0<Unit> translationChangedListener;

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v1, types: [android.view.GestureDetector$OnGestureListener, com.android.systemui.media.MediaCarouselScrollHandler$gestureListener$1] */
    /* JADX WARN: Type inference failed for: r4v1, types: [com.android.systemui.Gefingerpoken, com.android.systemui.media.MediaCarouselScrollHandler$touchListener$1] */
    /* JADX WARN: Type inference failed for: r5v1, types: [android.view.View$OnScrollChangeListener, com.android.systemui.media.MediaCarouselScrollHandler$scrollChangedListener$1] */
    public MediaCarouselScrollHandler(@NotNull MediaScrollView scrollView, @NotNull PageIndicator pageIndicator, @NotNull DelayableExecutor mainExecutor, @NotNull Function0<Unit> dismissCallback, @NotNull Function0<Unit> translationChangedListener, @NotNull Function0<Unit> closeGuts, @NotNull FalsingManager falsingManager) {
        Intrinsics.checkParameterIsNotNull(scrollView, "scrollView");
        Intrinsics.checkParameterIsNotNull(pageIndicator, "pageIndicator");
        Intrinsics.checkParameterIsNotNull(mainExecutor, "mainExecutor");
        Intrinsics.checkParameterIsNotNull(dismissCallback, "dismissCallback");
        Intrinsics.checkParameterIsNotNull(translationChangedListener, "translationChangedListener");
        Intrinsics.checkParameterIsNotNull(closeGuts, "closeGuts");
        Intrinsics.checkParameterIsNotNull(falsingManager, "falsingManager");
        this.scrollView = scrollView;
        this.pageIndicator = pageIndicator;
        this.mainExecutor = mainExecutor;
        this.dismissCallback = dismissCallback;
        this.translationChangedListener = translationChangedListener;
        this.closeGuts = closeGuts;
        this.falsingManager = falsingManager;
        ?? r3 = new GestureDetector.SimpleOnGestureListener() { // from class: com.android.systemui.media.MediaCarouselScrollHandler$gestureListener$1
            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onFling(@Nullable MotionEvent motionEvent, @Nullable MotionEvent motionEvent2, float f, float f2) {
                return this.this$0.onFling(f, f2);
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onScroll(@Nullable MotionEvent motionEvent, @Nullable MotionEvent motionEvent2, float f, float f2) {
                MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
                if (motionEvent == null) {
                    Intrinsics.throwNpe();
                }
                if (motionEvent2 == null) {
                    Intrinsics.throwNpe();
                }
                return mediaCarouselScrollHandler.onScroll(motionEvent, motionEvent2, f);
            }

            @Override // android.view.GestureDetector.SimpleOnGestureListener, android.view.GestureDetector.OnGestureListener
            public boolean onDown(@Nullable MotionEvent motionEvent) {
                if (!this.this$0.getFalsingProtectionNeeded()) {
                    return false;
                }
                this.this$0.falsingManager.onNotificationStartDismissing();
                return false;
            }
        };
        this.gestureListener = r3;
        ?? r4 = new Gefingerpoken() { // from class: com.android.systemui.media.MediaCarouselScrollHandler$touchListener$1
            @Override // com.android.systemui.Gefingerpoken
            public boolean onTouchEvent(@Nullable MotionEvent motionEvent) {
                MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
                if (motionEvent == null) {
                    Intrinsics.throwNpe();
                }
                return mediaCarouselScrollHandler.onTouch(motionEvent);
            }

            @Override // com.android.systemui.Gefingerpoken
            public boolean onInterceptTouchEvent(@Nullable MotionEvent motionEvent) {
                MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
                if (motionEvent == null) {
                    Intrinsics.throwNpe();
                }
                return mediaCarouselScrollHandler.onInterceptTouch(motionEvent);
            }
        };
        this.touchListener = r4;
        ?? r5 = new View.OnScrollChangeListener() { // from class: com.android.systemui.media.MediaCarouselScrollHandler$scrollChangedListener$1
            @Override // android.view.View.OnScrollChangeListener
            public void onScrollChange(@Nullable View view, int i, int i2, int i3, int i4) {
                if (this.this$0.getPlayerWidthPlusPadding() == 0) {
                    return;
                }
                int relativeScrollX = this.this$0.scrollView.getRelativeScrollX();
                MediaCarouselScrollHandler mediaCarouselScrollHandler = this.this$0;
                mediaCarouselScrollHandler.onMediaScrollingChanged(relativeScrollX / mediaCarouselScrollHandler.getPlayerWidthPlusPadding(), relativeScrollX % this.this$0.getPlayerWidthPlusPadding());
            }
        };
        this.scrollChangedListener = r5;
        this.gestureDetector = new GestureDetectorCompat(scrollView.getContext(), r3);
        scrollView.setTouchListener(r4);
        scrollView.setOverScrollMode(2);
        this.mediaContent = scrollView.getContentContainer();
        scrollView.setOnScrollChangeListener(r5);
        scrollView.setOutlineProvider(new ViewOutlineProvider() { // from class: com.android.systemui.media.MediaCarouselScrollHandler.1
            @Override // android.view.ViewOutlineProvider
            public void getOutline(@Nullable View view, @Nullable Outline outline) {
                if (outline != null) {
                    outline.setRoundRect(0, 0, MediaCarouselScrollHandler.this.carouselWidth, MediaCarouselScrollHandler.this.carouselHeight, MediaCarouselScrollHandler.this.cornerRadius);
                }
            }
        });
    }

    public final boolean isRtl() {
        return this.scrollView.isLayoutRtl();
    }

    public final boolean getFalsingProtectionNeeded() {
        return this.falsingProtectionNeeded;
    }

    public final void setFalsingProtectionNeeded(boolean z) {
        this.falsingProtectionNeeded = z;
    }

    public final float getContentTranslation() {
        return this.contentTranslation;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setContentTranslation(float f) {
        this.contentTranslation = f;
        this.mediaContent.setTranslationX(f);
        updateSettingsPresentation();
        this.translationChangedListener.invoke();
        updateClipToOutline();
    }

    public final int getPlayerWidthPlusPadding() {
        return this.playerWidthPlusPadding;
    }

    public final void setPlayerWidthPlusPadding(int i) {
        this.playerWidthPlusPadding = i;
        int i2 = this.activeMediaIndex * i;
        int i3 = this.scrollIntoCurrentMedia;
        this.scrollView.setRelativeScrollX(i3 > i ? i2 + (i - (i3 - i)) : i2 + i3);
    }

    public final void setShowsSettingsButton(boolean z) {
        this.showsSettingsButton = z;
    }

    public final void onSettingsButtonUpdated(@NotNull View button) {
        Intrinsics.checkParameterIsNotNull(button, "button");
        this.settingsButton = button;
        if (button == null) {
            Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
        }
        Resources resources = button.getResources();
        View view = this.settingsButton;
        if (view == null) {
            Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
        }
        this.cornerRadius = resources.getDimensionPixelSize(Utils.getThemeAttr(view.getContext(), R.attr.dialogCornerRadius));
        updateSettingsPresentation();
        this.scrollView.invalidateOutline();
    }

    private final void updateSettingsPresentation() {
        if (this.showsSettingsButton) {
            float map = MathUtils.map(0.0f, getMaxTranslation(), 0.0f, 1.0f, Math.abs(this.contentTranslation));
            float f = 1.0f - map;
            if (this.settingsButton == null) {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            }
            float width = (-r6.getWidth()) * f * 0.3f;
            if (isRtl()) {
                if (this.contentTranslation > 0) {
                    float width2 = this.scrollView.getWidth() - width;
                    if (this.settingsButton == null) {
                        Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                    }
                    width = -(width2 - r6.getWidth());
                } else {
                    width = -width;
                }
            } else if (this.contentTranslation <= 0) {
                float width3 = this.scrollView.getWidth() - width;
                if (this.settingsButton == null) {
                    Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                }
                width = width3 - r6.getWidth();
            }
            float f2 = f * 50;
            View view = this.settingsButton;
            if (view == null) {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            }
            view.setRotation(f2 * (-Math.signum(this.contentTranslation)));
            float fSaturate = MathUtils.saturate(MathUtils.map(0.5f, 1.0f, 0.0f, 1.0f, map));
            View view2 = this.settingsButton;
            if (view2 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            }
            view2.setAlpha(fSaturate);
            View view3 = this.settingsButton;
            if (view3 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            }
            view3.setVisibility(fSaturate != 0.0f ? 0 : 4);
            View view4 = this.settingsButton;
            if (view4 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            }
            view4.setTranslationX(width);
            View view5 = this.settingsButton;
            if (view5 == null) {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            }
            int height = this.scrollView.getHeight();
            if (this.settingsButton == null) {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            }
            view5.setTranslationY((height - r10.getHeight()) / 2.0f);
            return;
        }
        View view6 = this.settingsButton;
        if (view6 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
        }
        view6.setVisibility(4);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean onTouch(MotionEvent motionEvent) {
        float maxTranslation;
        boolean z = true;
        boolean z2 = motionEvent.getAction() == 1;
        if (z2 && this.falsingProtectionNeeded) {
            this.falsingManager.onNotificationStopDismissing();
        }
        if (this.gestureDetector.onTouchEvent(motionEvent)) {
            if (!z2) {
                return false;
            }
            this.scrollView.cancelCurrentScroll();
            return true;
        }
        if (z2 || motionEvent.getAction() == 3) {
            int relativeScrollX = this.scrollView.getRelativeScrollX();
            int i = this.playerWidthPlusPadding;
            int i2 = relativeScrollX % i;
            final int i3 = i2 > i / 2 ? i - i2 : i2 * (-1);
            if (i3 != 0) {
                this.mainExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaCarouselScrollHandler.onTouch.1
                    @Override // java.lang.Runnable
                    public final void run() {
                        MediaScrollView mediaScrollView = MediaCarouselScrollHandler.this.scrollView;
                        boolean zIsRtl = MediaCarouselScrollHandler.this.isRtl();
                        int i4 = i3;
                        if (zIsRtl) {
                            i4 = -i4;
                        }
                        mediaScrollView.smoothScrollBy(i4, 0);
                    }
                });
            }
            float contentTranslation = this.scrollView.getContentTranslation();
            if (contentTranslation != 0.0f) {
                if (Math.abs(contentTranslation) >= getMaxTranslation() / 2 && !isFalseTouch()) {
                    z = false;
                }
                if (z) {
                    maxTranslation = 0.0f;
                } else {
                    maxTranslation = getMaxTranslation() * Math.signum(contentTranslation);
                    if (!this.showsSettingsButton) {
                        this.mainExecutor.executeDelayed(new Runnable() { // from class: com.android.systemui.media.MediaCarouselScrollHandler.onTouch.2
                            @Override // java.lang.Runnable
                            public final void run() {
                                MediaCarouselScrollHandler.this.dismissCallback.invoke();
                            }
                        }, 100L);
                    }
                }
                PhysicsAnimator.Companion.getInstance(this).spring(CONTENT_TRANSLATION, maxTranslation, 0.0f, MediaCarouselScrollHandlerKt.translationConfig).start();
                this.scrollView.setAnimationTargetX(maxTranslation);
            }
        }
        return false;
    }

    private final boolean isFalseTouch() {
        return this.falsingProtectionNeeded && this.falsingManager.isFalseTouch();
    }

    private final int getMaxTranslation() {
        if (this.showsSettingsButton) {
            View view = this.settingsButton;
            if (view == null) {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            }
            return view.getWidth();
        }
        return this.playerWidthPlusPadding;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean onInterceptTouch(MotionEvent motionEvent) {
        return this.gestureDetector.onTouchEvent(motionEvent);
    }

    public final boolean onScroll(@NotNull MotionEvent down, @NotNull MotionEvent lastMotion, float f) {
        Intrinsics.checkParameterIsNotNull(down, "down");
        Intrinsics.checkParameterIsNotNull(lastMotion, "lastMotion");
        float x = lastMotion.getX() - down.getX();
        float contentTranslation = this.scrollView.getContentTranslation();
        if (contentTranslation == 0.0f && this.scrollView.canScrollHorizontally((int) (-x))) {
            return false;
        }
        float fSignum = contentTranslation - f;
        float fAbs = Math.abs(fSignum);
        if (fAbs > getMaxTranslation() && Math.signum(f) != Math.signum(contentTranslation)) {
            fSignum = Math.abs(contentTranslation) > ((float) getMaxTranslation()) ? contentTranslation - (f * 0.2f) : Math.signum(fSignum) * (getMaxTranslation() + ((fAbs - getMaxTranslation()) * 0.2f));
        }
        if (Math.signum(fSignum) != Math.signum(contentTranslation) && contentTranslation != 0.0f && this.scrollView.canScrollHorizontally(-((int) fSignum))) {
            fSignum = 0.0f;
        }
        PhysicsAnimator companion = PhysicsAnimator.Companion.getInstance(this);
        if (companion.isRunning()) {
            companion.spring(CONTENT_TRANSLATION, fSignum, 0.0f, MediaCarouselScrollHandlerKt.translationConfig).start();
        } else {
            setContentTranslation(fSignum);
        }
        this.scrollView.setAnimationTargetX(fSignum);
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final boolean onFling(float f, float f2) {
        float f3 = f * f;
        double d = f2;
        if (f3 < 0.5d * d * d || f3 < 1000000) {
            return false;
        }
        float contentTranslation = this.scrollView.getContentTranslation();
        float maxTranslation = 0.0f;
        if (contentTranslation != 0.0f) {
            if (Math.signum(f) == Math.signum(contentTranslation) && !isFalseTouch()) {
                maxTranslation = getMaxTranslation() * Math.signum(contentTranslation);
                if (!this.showsSettingsButton) {
                    this.mainExecutor.executeDelayed(new Runnable() { // from class: com.android.systemui.media.MediaCarouselScrollHandler.onFling.1
                        @Override // java.lang.Runnable
                        public final void run() {
                            MediaCarouselScrollHandler.this.dismissCallback.invoke();
                        }
                    }, 100L);
                }
            }
            PhysicsAnimator.Companion.getInstance(this).spring(CONTENT_TRANSLATION, maxTranslation, f, MediaCarouselScrollHandlerKt.translationConfig).start();
            this.scrollView.setAnimationTargetX(maxTranslation);
        } else {
            int relativeScrollX = this.scrollView.getRelativeScrollX();
            int i = this.playerWidthPlusPadding;
            int i2 = i > 0 ? relativeScrollX / i : 0;
            if (!isRtl() ? f >= ((float) 0) : f <= ((float) 0)) {
                i2++;
            }
            final View childAt = this.mediaContent.getChildAt(Math.min(this.mediaContent.getChildCount() - 1, Math.max(0, i2)));
            this.mainExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaCarouselScrollHandler.onFling.2
                @Override // java.lang.Runnable
                public final void run() {
                    MediaScrollView mediaScrollView = MediaCarouselScrollHandler.this.scrollView;
                    View view = childAt;
                    Intrinsics.checkExpressionValueIsNotNull(view, "view");
                    mediaScrollView.smoothScrollTo(view.getLeft(), MediaCarouselScrollHandler.this.scrollView.getScrollY());
                }
            });
        }
        return true;
    }

    public static /* synthetic */ void resetTranslation$default(MediaCarouselScrollHandler mediaCarouselScrollHandler, boolean z, int i, Object obj) {
        if ((i & 1) != 0) {
            z = false;
        }
        mediaCarouselScrollHandler.resetTranslation(z);
    }

    public final void resetTranslation(boolean z) {
        if (this.scrollView.getContentTranslation() != 0.0f) {
            if (z) {
                PhysicsAnimator.Companion.getInstance(this).spring(CONTENT_TRANSLATION, 0.0f, MediaCarouselScrollHandlerKt.translationConfig).start();
                this.scrollView.setAnimationTargetX(0.0f);
            } else {
                PhysicsAnimator.Companion.getInstance(this).cancel();
                setContentTranslation(0.0f);
            }
        }
    }

    private final void updateClipToOutline() {
        this.scrollView.setClipToOutline((this.contentTranslation == 0.0f && this.scrollIntoCurrentMedia == 0) ? false : true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void onMediaScrollingChanged(int i, int i2) {
        boolean z = this.scrollIntoCurrentMedia != 0;
        this.scrollIntoCurrentMedia = i2;
        boolean z2 = i2 != 0;
        if (i != this.activeMediaIndex || z != z2) {
            this.activeMediaIndex = i;
            this.closeGuts.invoke();
            updatePlayerVisibilities();
        }
        float f = this.activeMediaIndex;
        int i3 = this.playerWidthPlusPadding;
        float childCount = f + (i3 > 0 ? i2 / i3 : 0.0f);
        if (isRtl()) {
            childCount = (this.mediaContent.getChildCount() - childCount) - 1;
        }
        this.pageIndicator.setLocation(childCount);
        updateClipToOutline();
    }

    public final void onPlayersChanged() throws Resources.NotFoundException {
        updatePlayerVisibilities();
        updateMediaPaddings();
    }

    private final void updateMediaPaddings() throws Resources.NotFoundException {
        Context context = this.scrollView.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "scrollView.context");
        int dimensionPixelSize = context.getResources().getDimensionPixelSize(com.android.systemui.R.dimen.qs_media_padding);
        int childCount = this.mediaContent.getChildCount();
        int i = 0;
        while (i < childCount) {
            View mediaView = this.mediaContent.getChildAt(i);
            int i2 = i == childCount + (-1) ? 0 : dimensionPixelSize;
            Intrinsics.checkExpressionValueIsNotNull(mediaView, "mediaView");
            ViewGroup.LayoutParams layoutParams = mediaView.getLayoutParams();
            if (layoutParams == null) {
                throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
            }
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            if (marginLayoutParams.getMarginEnd() != i2) {
                marginLayoutParams.setMarginEnd(i2);
                mediaView.setLayoutParams(marginLayoutParams);
            }
            i++;
        }
    }

    private final void updatePlayerVisibilities() {
        boolean z = this.scrollIntoCurrentMedia != 0;
        int childCount = this.mediaContent.getChildCount();
        int i = 0;
        while (i < childCount) {
            View view = this.mediaContent.getChildAt(i);
            int i2 = this.activeMediaIndex;
            boolean z2 = i == i2 || (i == i2 + 1 && z);
            Intrinsics.checkExpressionValueIsNotNull(view, "view");
            view.setVisibility(z2 ? 0 : 4);
            i++;
        }
    }

    public final void onPrePlayerRemoved(@NotNull MediaControlPanel removed) {
        Intrinsics.checkParameterIsNotNull(removed, "removed");
        ViewGroup viewGroup = this.mediaContent;
        PlayerViewHolder view = removed.getView();
        int iIndexOfChild = viewGroup.indexOfChild(view != null ? view.getPlayer() : null);
        int i = this.activeMediaIndex;
        boolean z = true;
        boolean z2 = iIndexOfChild <= i;
        if (z2) {
            this.activeMediaIndex = Math.max(0, i - 1);
        }
        if (!isRtl()) {
            z = z2;
        } else if (z2) {
            z = false;
        }
        if (z) {
            MediaScrollView mediaScrollView = this.scrollView;
            mediaScrollView.setScrollX(Math.max(mediaScrollView.getScrollX() - this.playerWidthPlusPadding, 0));
        }
    }

    public final void setCarouselBounds(int i, int i2) {
        int i3 = this.carouselHeight;
        if (i2 == i3 && i == i3) {
            return;
        }
        this.carouselWidth = i;
        this.carouselHeight = i2;
        this.scrollView.invalidateOutline();
    }

    public final void scrollToStart() {
        this.scrollView.setRelativeScrollX(0);
    }

    /* compiled from: MediaCarouselScrollHandler.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.systemui.media.MediaCarouselScrollHandler$Companion$CONTENT_TRANSLATION$1] */
    static {
        final String str = "contentTranslation";
        CONTENT_TRANSLATION = new FloatPropertyCompat<MediaCarouselScrollHandler>(str) { // from class: com.android.systemui.media.MediaCarouselScrollHandler$Companion$CONTENT_TRANSLATION$1
            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public float getValue(@NotNull MediaCarouselScrollHandler handler) {
                Intrinsics.checkParameterIsNotNull(handler, "handler");
                return handler.getContentTranslation();
            }

            @Override // androidx.dynamicanimation.animation.FloatPropertyCompat
            public void setValue(@NotNull MediaCarouselScrollHandler handler, float f) {
                Intrinsics.checkParameterIsNotNull(handler, "handler");
                handler.setContentTranslation(f);
            }
        };
    }
}
