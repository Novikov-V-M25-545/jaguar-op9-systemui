package com.android.systemui;

import android.app.ActivityTaskManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.hardware.display.DisplayManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.statusbar.CommandQueue;
import java.lang.ref.WeakReference;

/* loaded from: classes.dex */
public class SizeCompatModeActivityController extends SystemUI implements CommandQueue.Callbacks {
    private final SparseArray<RestartActivityButton> mActiveButtons;
    private final CommandQueue mCommandQueue;
    private final SparseArray<WeakReference<Context>> mDisplayContextCache;
    private boolean mHasShownHint;

    @VisibleForTesting
    SizeCompatModeActivityController(Context context, ActivityManagerWrapper activityManagerWrapper, CommandQueue commandQueue) {
        super(context);
        this.mActiveButtons = new SparseArray<>(1);
        this.mDisplayContextCache = new SparseArray<>(0);
        this.mCommandQueue = commandQueue;
        activityManagerWrapper.registerTaskStackListener(new TaskStackChangeListener() { // from class: com.android.systemui.SizeCompatModeActivityController.1
            @Override // com.android.systemui.shared.system.TaskStackChangeListener
            public void onSizeCompatModeActivityChanged(int i, IBinder iBinder) {
                SizeCompatModeActivityController.this.updateRestartButton(i, iBinder);
            }
        });
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z) {
        RestartActivityButton restartActivityButton = this.mActiveButtons.get(i);
        if (restartActivityButton == null) {
            return;
        }
        int i4 = (i2 & 2) != 0 ? 8 : 0;
        if (restartActivityButton.getVisibility() != i4) {
            restartActivityButton.setVisibility(i4);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void onDisplayRemoved(int i) {
        this.mDisplayContextCache.remove(i);
        removeRestartButton(i);
    }

    private void removeRestartButton(int i) {
        RestartActivityButton restartActivityButton = this.mActiveButtons.get(i);
        if (restartActivityButton != null) {
            restartActivityButton.remove();
            this.mActiveButtons.remove(i);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateRestartButton(int i, IBinder iBinder) {
        if (iBinder == null) {
            removeRestartButton(i);
            return;
        }
        RestartActivityButton restartActivityButton = this.mActiveButtons.get(i);
        if (restartActivityButton != null) {
            restartActivityButton.updateLastTargetActivity(iBinder);
            return;
        }
        Context orCreateDisplayContext = getOrCreateDisplayContext(i);
        if (orCreateDisplayContext == null) {
            Log.i("SizeCompatMode", "Cannot get context for display " + i);
            return;
        }
        RestartActivityButton restartActivityButtonCreateRestartButton = createRestartButton(orCreateDisplayContext);
        restartActivityButtonCreateRestartButton.updateLastTargetActivity(iBinder);
        if (restartActivityButtonCreateRestartButton.show()) {
            this.mActiveButtons.append(i, restartActivityButtonCreateRestartButton);
        } else {
            onDisplayRemoved(i);
        }
    }

    @VisibleForTesting
    RestartActivityButton createRestartButton(Context context) {
        RestartActivityButton restartActivityButton = new RestartActivityButton(context, this.mHasShownHint);
        this.mHasShownHint = true;
        return restartActivityButton;
    }

    private Context getOrCreateDisplayContext(int i) {
        Display display;
        if (i == 0) {
            return this.mContext;
        }
        WeakReference<Context> weakReference = this.mDisplayContextCache.get(i);
        Context context = weakReference != null ? weakReference.get() : null;
        if (context != null || (display = ((DisplayManager) this.mContext.getSystemService(DisplayManager.class)).getDisplay(i)) == null) {
            return context;
        }
        Context contextCreateDisplayContext = this.mContext.createDisplayContext(display);
        this.mDisplayContextCache.put(i, new WeakReference<>(contextCreateDisplayContext));
        return contextCreateDisplayContext;
    }

    @VisibleForTesting
    static class RestartActivityButton extends ImageButton implements View.OnClickListener, View.OnLongClickListener {
        IBinder mLastActivityToken;
        final int mPopupOffsetX;
        final int mPopupOffsetY;
        final boolean mShouldShowHint;
        PopupWindow mShowingHint;
        final WindowManager.LayoutParams mWinParams;

        private static int getGravity(int i) {
            return (i == 1 ? 8388611 : 8388613) | 80;
        }

        RestartActivityButton(Context context, boolean z) {
            super(context);
            this.mShouldShowHint = !z;
            Drawable drawable = context.getDrawable(R.drawable.btn_restart);
            setImageDrawable(drawable);
            setContentDescription(context.getString(R.string.restart_button_description));
            int intrinsicWidth = drawable.getIntrinsicWidth();
            int intrinsicHeight = drawable.getIntrinsicHeight();
            this.mPopupOffsetX = intrinsicWidth / 2;
            int i = intrinsicHeight * 2;
            this.mPopupOffsetY = i;
            ColorStateList colorStateListValueOf = ColorStateList.valueOf(-3355444);
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setShape(1);
            gradientDrawable.setColor(colorStateListValueOf);
            setBackground(new RippleDrawable(colorStateListValueOf, null, gradientDrawable));
            setOnClickListener(this);
            setOnLongClickListener(this);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            this.mWinParams = layoutParams;
            layoutParams.gravity = getGravity(getResources().getConfiguration().getLayoutDirection());
            layoutParams.width = intrinsicWidth * 2;
            layoutParams.height = i;
            layoutParams.type = 2038;
            layoutParams.flags = 40;
            layoutParams.format = -3;
            layoutParams.privateFlags |= 16;
            layoutParams.setTitle(SizeCompatModeActivityController.class.getSimpleName() + context.getDisplayId());
        }

        void updateLastTargetActivity(IBinder iBinder) {
            this.mLastActivityToken = iBinder;
        }

        boolean show() {
            try {
                ((WindowManager) getContext().getSystemService(WindowManager.class)).addView(this, this.mWinParams);
                return true;
            } catch (WindowManager.InvalidDisplayException e) {
                Log.w("SizeCompatMode", "Cannot show on display " + getContext().getDisplayId(), e);
                return false;
            }
        }

        void remove() {
            PopupWindow popupWindow = this.mShowingHint;
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
            ((WindowManager) getContext().getSystemService(WindowManager.class)).removeViewImmediate(this);
        }

        @Override // android.view.View.OnClickListener
        public void onClick(View view) {
            try {
                ActivityTaskManager.getService().restartActivityProcessIfVisible(this.mLastActivityToken);
            } catch (RemoteException e) {
                Log.w("SizeCompatMode", "Unable to restart activity", e);
            }
        }

        @Override // android.view.View.OnLongClickListener
        public boolean onLongClick(View view) {
            showHint();
            return true;
        }

        @Override // android.widget.ImageView, android.view.View
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (this.mShouldShowHint) {
                showHint();
            }
        }

        @Override // android.view.View
        public void setLayoutDirection(int i) {
            int gravity = getGravity(i);
            WindowManager.LayoutParams layoutParams = this.mWinParams;
            if (layoutParams.gravity != gravity) {
                layoutParams.gravity = gravity;
                PopupWindow popupWindow = this.mShowingHint;
                if (popupWindow != null) {
                    popupWindow.dismiss();
                    showHint();
                }
                ((WindowManager) getContext().getSystemService(WindowManager.class)).updateViewLayout(this, this.mWinParams);
            }
            super.setLayoutDirection(i);
        }

        void showHint() {
            if (this.mShowingHint != null) {
                return;
            }
            View viewInflate = LayoutInflater.from(getContext()).inflate(R.layout.size_compat_mode_hint, (ViewGroup) null);
            final PopupWindow popupWindow = new PopupWindow(viewInflate, -2, -2);
            popupWindow.setWindowLayoutType(this.mWinParams.type);
            popupWindow.setElevation(getResources().getDimension(R.dimen.bubble_elevation));
            popupWindow.setAnimationStyle(android.R.style.Animation.InputMethod);
            popupWindow.setClippingEnabled(false);
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() { // from class: com.android.systemui.SizeCompatModeActivityController$RestartActivityButton$$ExternalSyntheticLambda1
                @Override // android.widget.PopupWindow.OnDismissListener
                public final void onDismiss() {
                    this.f$0.lambda$showHint$0();
                }
            });
            this.mShowingHint = popupWindow;
            Button button = (Button) viewInflate.findViewById(R.id.got_it);
            button.setBackground(new RippleDrawable(ColorStateList.valueOf(-3355444), null, null));
            button.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.SizeCompatModeActivityController$RestartActivityButton$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    popupWindow.dismiss();
                }
            });
            popupWindow.showAtLocation(this, this.mWinParams.gravity, this.mPopupOffsetX, this.mPopupOffsetY);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$showHint$0() {
            this.mShowingHint = null;
        }
    }
}
