package com.android.systemui.recents;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Binder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.util.leak.RotationUtils;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Optional;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class ScreenPinningRequest implements View.OnClickListener, NavigationModeController.ModeChangedListener {
    private final AccessibilityManager mAccessibilityService;
    private final Context mContext;
    private RequestWindowView mRequestWindow;
    private final Optional<Lazy<StatusBar>> mStatusBarOptionalLazy;
    private final WindowManager mWindowManager;
    private int taskId;
    private final OverviewProxyService mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
    private int mNavBarMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);

    public ScreenPinningRequest(Context context, Optional<Lazy<StatusBar>> optional) {
        this.mContext = context;
        this.mStatusBarOptionalLazy = optional;
        this.mAccessibilityService = (AccessibilityManager) context.getSystemService("accessibility");
        this.mWindowManager = (WindowManager) context.getSystemService("window");
    }

    public void clearPrompt() {
        RequestWindowView requestWindowView = this.mRequestWindow;
        if (requestWindowView != null) {
            this.mWindowManager.removeView(requestWindowView);
            this.mRequestWindow = null;
        }
    }

    public void showPrompt(int i, boolean z) {
        try {
            clearPrompt();
        } catch (IllegalArgumentException unused) {
        }
        this.taskId = i;
        RequestWindowView requestWindowView = new RequestWindowView(this.mContext, z);
        this.mRequestWindow = requestWindowView;
        requestWindowView.setSystemUiVisibility(LineageHardwareManager.FEATURE_SUNLIGHT_ENHANCEMENT);
        this.mWindowManager.addView(this.mRequestWindow, getWindowLayoutParams());
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
    }

    public void onConfigurationChanged() {
        RequestWindowView requestWindowView = this.mRequestWindow;
        if (requestWindowView != null) {
            requestWindowView.onConfigurationChanged();
        }
    }

    private WindowManager.LayoutParams getWindowLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2024, 264, -3);
        layoutParams.token = new Binder();
        layoutParams.privateFlags |= 16;
        layoutParams.setTitle("ScreenPinningConfirmation");
        layoutParams.gravity = 119;
        layoutParams.setFitInsetsTypes(0);
        return layoutParams;
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (view.getId() == R.id.screen_pinning_ok_button || this.mRequestWindow == view) {
            try {
                ActivityTaskManager.getService().startSystemLockTaskMode(this.taskId);
            } catch (RemoteException unused) {
            }
        }
        clearPrompt();
    }

    public FrameLayout.LayoutParams getRequestLayoutParams(int i) {
        return new FrameLayout.LayoutParams(-2, -2, i == 2 ? 19 : i == 1 ? 21 : 81);
    }

    /* JADX INFO: Access modifiers changed from: private */
    class RequestWindowView extends FrameLayout {
        private final BroadcastDispatcher mBroadcastDispatcher;
        private final ColorDrawable mColor;
        private ValueAnimator mColorAnim;
        private ViewGroup mLayout;
        private final BroadcastReceiver mReceiver;
        private boolean mShowCancel;
        private final Runnable mUpdateLayoutRunnable;

        public RequestWindowView(Context context, boolean z) {
            super(context);
            ColorDrawable colorDrawable = new ColorDrawable(0);
            this.mColor = colorDrawable;
            this.mBroadcastDispatcher = (BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class);
            this.mUpdateLayoutRunnable = new Runnable() { // from class: com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.2
                @Override // java.lang.Runnable
                public void run() {
                    if (RequestWindowView.this.mLayout == null || RequestWindowView.this.mLayout.getParent() == null) {
                        return;
                    }
                    ViewGroup viewGroup = RequestWindowView.this.mLayout;
                    RequestWindowView requestWindowView = RequestWindowView.this;
                    viewGroup.setLayoutParams(ScreenPinningRequest.this.getRequestLayoutParams(RotationUtils.getRotation(((FrameLayout) requestWindowView).mContext)));
                }
            };
            this.mReceiver = new BroadcastReceiver() { // from class: com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.3
                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context2, Intent intent) {
                    if (intent.getAction().equals("android.intent.action.CONFIGURATION_CHANGED")) {
                        RequestWindowView requestWindowView = RequestWindowView.this;
                        requestWindowView.post(requestWindowView.mUpdateLayoutRunnable);
                    } else if (intent.getAction().equals("android.intent.action.USER_SWITCHED") || intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                        ScreenPinningRequest.this.clearPrompt();
                    }
                }
            };
            setClickable(true);
            setOnClickListener(ScreenPinningRequest.this);
            setBackground(colorDrawable);
            this.mShowCancel = z;
        }

        @Override // android.view.ViewGroup, android.view.View
        public void onAttachedToWindow() throws Resources.NotFoundException {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ScreenPinningRequest.this.mWindowManager.getDefaultDisplay().getMetrics(displayMetrics);
            float f = displayMetrics.density;
            int rotation = RotationUtils.getRotation(((FrameLayout) this).mContext);
            inflateView(rotation);
            int color = ((FrameLayout) this).mContext.getColor(R.color.screen_pinning_request_window_bg);
            if (ActivityManager.isHighEndGfx()) {
                this.mLayout.setAlpha(0.0f);
                if (rotation == 2) {
                    this.mLayout.setTranslationX(f * (-96.0f));
                } else if (rotation == 1) {
                    this.mLayout.setTranslationX(f * 96.0f);
                } else {
                    this.mLayout.setTranslationY(f * 96.0f);
                }
                this.mLayout.animate().alpha(1.0f).translationX(0.0f).translationY(0.0f).setDuration(300L).setInterpolator(new DecelerateInterpolator()).start();
                ValueAnimator valueAnimatorOfObject = ValueAnimator.ofObject(new ArgbEvaluator(), 0, Integer.valueOf(color));
                this.mColorAnim = valueAnimatorOfObject;
                valueAnimatorOfObject.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.1
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public void onAnimationUpdate(ValueAnimator valueAnimator) {
                        RequestWindowView.this.mColor.setColor(((Integer) valueAnimator.getAnimatedValue()).intValue());
                    }
                });
                this.mColorAnim.setDuration(1000L);
                this.mColorAnim.start();
            } else {
                this.mColor.setColor(color);
            }
            IntentFilter intentFilter = new IntentFilter("android.intent.action.CONFIGURATION_CHANGED");
            intentFilter.addAction("android.intent.action.USER_SWITCHED");
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            this.mBroadcastDispatcher.registerReceiver(this.mReceiver, intentFilter);
        }

        /* JADX WARN: Removed duplicated region for block: B:13:0x005b  */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        private void inflateView(int r9) throws android.content.res.Resources.NotFoundException {
            /*
                Method dump skipped, instructions count: 491
                To view this dump change 'Code comments level' option to 'DEBUG'
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.ScreenPinningRequest.RequestWindowView.inflateView(int):void");
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ NavigationBarView lambda$inflateView$0(Lazy lazy) {
            return ((StatusBar) lazy.get()).getNavigationBarView();
        }

        private void swapChildrenIfRtlAndVertical(View view) {
            if (((FrameLayout) this).mContext.getResources().getConfiguration().getLayoutDirection() != 1) {
                return;
            }
            LinearLayout linearLayout = (LinearLayout) view;
            if (linearLayout.getOrientation() == 1) {
                int childCount = linearLayout.getChildCount();
                ArrayList arrayList = new ArrayList(childCount);
                for (int i = 0; i < childCount; i++) {
                    arrayList.add(linearLayout.getChildAt(i));
                }
                linearLayout.removeAllViews();
                for (int i2 = childCount - 1; i2 >= 0; i2--) {
                    linearLayout.addView((View) arrayList.get(i2));
                }
            }
        }

        private boolean hasNavigationBar() {
            try {
                return WindowManagerGlobal.getWindowManagerService().hasNavigationBar(((FrameLayout) this).mContext.getDisplayId());
            } catch (RemoteException unused) {
                return false;
            }
        }

        @Override // android.view.ViewGroup, android.view.View
        public void onDetachedFromWindow() {
            this.mBroadcastDispatcher.unregisterReceiver(this.mReceiver);
        }

        protected void onConfigurationChanged() throws Resources.NotFoundException {
            removeAllViews();
            inflateView(RotationUtils.getRotation(((FrameLayout) this).mContext));
        }
    }
}
