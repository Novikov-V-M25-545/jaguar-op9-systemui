package com.android.systemui.charging;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.WindowManager;

/* loaded from: classes.dex */
public class WirelessChargingAnimation {
    private static final boolean DEBUG = Log.isLoggable("WirelessChargingView", 3);
    private static WirelessChargingView mPreviousWirelessChargingView;
    private final WirelessChargingView mCurrentWirelessChargingView;

    public interface Callback {
        void onAnimationEnded();

        void onAnimationStarting();
    }

    public WirelessChargingAnimation(Context context, Looper looper, int i, int i2, Callback callback, boolean z) {
        this.mCurrentWirelessChargingView = new WirelessChargingView(context, looper, i, i2, callback, z);
    }

    public static WirelessChargingAnimation makeWirelessChargingAnimation(Context context, Looper looper, int i, int i2, Callback callback, boolean z) {
        return new WirelessChargingAnimation(context, looper, i, i2, callback, z);
    }

    public void show(long j) {
        WirelessChargingView wirelessChargingView = this.mCurrentWirelessChargingView;
        if (wirelessChargingView == null || wirelessChargingView.mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }
        WirelessChargingView wirelessChargingView2 = mPreviousWirelessChargingView;
        if (wirelessChargingView2 != null) {
            wirelessChargingView2.hide(0L);
        }
        WirelessChargingView wirelessChargingView3 = this.mCurrentWirelessChargingView;
        mPreviousWirelessChargingView = wirelessChargingView3;
        wirelessChargingView3.show(j);
        this.mCurrentWirelessChargingView.hide(j + 1133);
    }

    private static class WirelessChargingView {
        private Callback mCallback;
        private int mGravity;
        private final Handler mHandler;
        private View mNextView;
        private final WindowManager.LayoutParams mParams;
        private View mView;
        private WindowManager mWM;

        public WirelessChargingView(Context context, Looper looper, int i, int i2, Callback callback, boolean z) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            this.mParams = layoutParams;
            this.mCallback = callback;
            this.mNextView = new WirelessChargingLayout(context, i, i2, z);
            this.mGravity = 17;
            layoutParams.height = -2;
            layoutParams.width = -1;
            layoutParams.format = -3;
            layoutParams.type = 2009;
            layoutParams.setTitle("Charging Animation");
            layoutParams.flags = 26;
            layoutParams.dimAmount = 0.3f;
            if (looper == null && (looper = Looper.myLooper()) == null) {
                throw new RuntimeException("Can't display wireless animation on a thread that has not called Looper.prepare()");
            }
            this.mHandler = new Handler(looper, null) { // from class: com.android.systemui.charging.WirelessChargingAnimation.WirelessChargingView.1
                @Override // android.os.Handler
                public void handleMessage(Message message) {
                    int i3 = message.what;
                    if (i3 == 0) {
                        WirelessChargingView.this.handleShow();
                    } else {
                        if (i3 != 1) {
                            return;
                        }
                        WirelessChargingView.this.handleHide();
                        WirelessChargingView.this.mNextView = null;
                    }
                }
            };
        }

        public void show(long j) {
            if (WirelessChargingAnimation.DEBUG) {
                Slog.d("WirelessChargingView", "SHOW: " + this);
            }
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(Message.obtain(handler, 0), j);
        }

        public void hide(long j) {
            this.mHandler.removeMessages(1);
            if (WirelessChargingAnimation.DEBUG) {
                Slog.d("WirelessChargingView", "HIDE: " + this);
            }
            Handler handler = this.mHandler;
            handler.sendMessageDelayed(Message.obtain(handler, 1), j);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handleShow() {
            if (WirelessChargingAnimation.DEBUG) {
                Slog.d("WirelessChargingView", "HANDLE SHOW: " + this + " mView=" + this.mView + " mNextView=" + this.mNextView);
            }
            if (this.mView != this.mNextView) {
                handleHide();
                View view = this.mNextView;
                this.mView = view;
                Context applicationContext = view.getContext().getApplicationContext();
                String opPackageName = this.mView.getContext().getOpPackageName();
                if (applicationContext == null) {
                    applicationContext = this.mView.getContext();
                }
                this.mWM = (WindowManager) applicationContext.getSystemService("window");
                WindowManager.LayoutParams layoutParams = this.mParams;
                layoutParams.packageName = opPackageName;
                layoutParams.hideTimeoutMilliseconds = 1133L;
                if (this.mView.getParent() != null) {
                    if (WirelessChargingAnimation.DEBUG) {
                        Slog.d("WirelessChargingView", "REMOVE! " + this.mView + " in " + this);
                    }
                    this.mWM.removeView(this.mView);
                }
                if (WirelessChargingAnimation.DEBUG) {
                    Slog.d("WirelessChargingView", "ADD! " + this.mView + " in " + this);
                }
                try {
                    Callback callback = this.mCallback;
                    if (callback != null) {
                        callback.onAnimationStarting();
                    }
                    this.mWM.addView(this.mView, this.mParams);
                } catch (WindowManager.BadTokenException e) {
                    Slog.d("WirelessChargingView", "Unable to add wireless charging view. " + e);
                }
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public void handleHide() {
            if (WirelessChargingAnimation.DEBUG) {
                Slog.d("WirelessChargingView", "HANDLE HIDE: " + this + " mView=" + this.mView);
            }
            View view = this.mView;
            if (view != null) {
                if (view.getParent() != null) {
                    if (WirelessChargingAnimation.DEBUG) {
                        Slog.d("WirelessChargingView", "REMOVE! " + this.mView + " in " + this);
                    }
                    Callback callback = this.mCallback;
                    if (callback != null) {
                        callback.onAnimationEnded();
                    }
                    this.mWM.removeViewImmediate(this.mView);
                }
                this.mView = null;
            }
        }
    }
}
