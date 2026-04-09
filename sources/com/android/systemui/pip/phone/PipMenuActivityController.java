package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.RemoteAction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.MotionEvent;
import com.android.systemui.pip.phone.PipMediaController;
import com.android.systemui.pip.phone.PipMenuActivityController;
import com.android.systemui.shared.system.InputConsumerController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class PipMenuActivityController {
    private ParceledListSlice mAppActions;
    private Context mContext;
    private InputConsumerController mInputConsumerController;
    private ParceledListSlice mMediaActions;
    private PipMediaController mMediaController;
    private int mMenuState;
    private Runnable mOnAnimationEndRunnable;
    private boolean mStartActivityRequested;
    private long mStartActivityRequestedTime;
    private Messenger mToActivityMessenger;
    private ArrayList<Listener> mListeners = new ArrayList<>();
    private Bundle mTmpDismissFractionData = new Bundle();
    private Handler mHandler = new Handler(Looper.getMainLooper()) { // from class: com.android.systemui.pip.phone.PipMenuActivityController.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 100) {
                int i2 = message.arg1;
                boolean z = message.arg2 != 0;
                PipMenuActivityController pipMenuActivityController = PipMenuActivityController.this;
                pipMenuActivityController.onMenuStateChanged(i2, z, pipMenuActivityController.getMenuStateChangeFinishedCallback(message.replyTo, (Bundle) message.obj));
                return;
            }
            if (i == 101) {
                PipMenuActivityController.this.mListeners.forEach(new Consumer() { // from class: com.android.systemui.pip.phone.PipMenuActivityController$1$$ExternalSyntheticLambda1
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ((PipMenuActivityController.Listener) obj).onPipExpand();
                    }
                });
                return;
            }
            if (i == 103) {
                PipMenuActivityController.this.mListeners.forEach(new Consumer() { // from class: com.android.systemui.pip.phone.PipMenuActivityController$1$$ExternalSyntheticLambda0
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ((PipMenuActivityController.Listener) obj).onPipDismiss();
                    }
                });
                return;
            }
            if (i != 104) {
                if (i != 107) {
                    return;
                }
                PipMenuActivityController.this.mListeners.forEach(new Consumer() { // from class: com.android.systemui.pip.phone.PipMenuActivityController$1$$ExternalSyntheticLambda2
                    @Override // java.util.function.Consumer
                    public final void accept(Object obj) {
                        ((PipMenuActivityController.Listener) obj).onPipShowMenu();
                    }
                });
                return;
            }
            PipMenuActivityController.this.mToActivityMessenger = message.replyTo;
            PipMenuActivityController.this.setStartActivityRequested(false);
            if (PipMenuActivityController.this.mOnAnimationEndRunnable != null) {
                PipMenuActivityController.this.mOnAnimationEndRunnable.run();
                PipMenuActivityController.this.mOnAnimationEndRunnable = null;
            }
            if (PipMenuActivityController.this.mToActivityMessenger == null) {
                PipMenuActivityController.this.onMenuStateChanged(0, message.arg1 != 0, null);
            }
        }
    };
    private Messenger mMessenger = new Messenger(this.mHandler);
    private Runnable mStartActivityRequestedTimeoutRunnable = new Runnable() { // from class: com.android.systemui.pip.phone.PipMenuActivityController$$ExternalSyntheticLambda1
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.lambda$new$0();
        }
    };
    private PipMediaController.ActionListener mMediaActionListener = new PipMediaController.ActionListener() { // from class: com.android.systemui.pip.phone.PipMenuActivityController.2
        @Override // com.android.systemui.pip.phone.PipMediaController.ActionListener
        public void onMediaActionsChanged(List<RemoteAction> list) throws RemoteException {
            PipMenuActivityController.this.mMediaActions = new ParceledListSlice(list);
            PipMenuActivityController.this.updateMenuActions();
        }
    };

    public interface Listener {
        void onPipDismiss();

        void onPipExpand();

        void onPipMenuStateChanged(int i, boolean z, Runnable runnable);

        void onPipShowMenu();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        setStartActivityRequested(false);
        Runnable runnable = this.mOnAnimationEndRunnable;
        if (runnable != null) {
            runnable.run();
            this.mOnAnimationEndRunnable = null;
        }
        Log.e("PipMenuActController", "Expected start menu activity request timed out");
    }

    public PipMenuActivityController(Context context, PipMediaController pipMediaController, InputConsumerController inputConsumerController) {
        this.mContext = context;
        this.mMediaController = pipMediaController;
        this.mInputConsumerController = inputConsumerController;
    }

    public boolean isMenuActivityVisible() {
        return this.mToActivityMessenger != null;
    }

    public void onActivityPinned() {
        this.mInputConsumerController.registerInputConsumer(true);
    }

    public void onActivityUnpinned() throws RemoteException {
        hideMenu();
        this.mInputConsumerController.unregisterInputConsumer();
        setStartActivityRequested(false);
    }

    public void onPinnedStackAnimationEnded() throws RemoteException {
        if (this.mToActivityMessenger != null) {
            Message messageObtain = Message.obtain();
            messageObtain.what = 6;
            try {
                this.mToActivityMessenger.send(messageObtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu pinned animation ended", e);
            }
        }
    }

    public void addListener(Listener listener) {
        if (this.mListeners.contains(listener)) {
            return;
        }
        this.mListeners.add(listener);
    }

    public void setDismissFraction(float f) throws RemoteException {
        if (this.mToActivityMessenger != null) {
            this.mTmpDismissFractionData.clear();
            this.mTmpDismissFractionData.putFloat("dismiss_fraction", f);
            Message messageObtain = Message.obtain();
            messageObtain.what = 5;
            messageObtain.obj = this.mTmpDismissFractionData;
            try {
                this.mToActivityMessenger.send(messageObtain);
                return;
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu to update dismiss fraction", e);
                return;
            }
        }
        if (!this.mStartActivityRequested || isStartActivityRequestedElapsed()) {
            startMenuActivity(0, null, false, false, false, false);
        }
    }

    public void showMenuWithDelay(int i, Rect rect, boolean z, boolean z2, boolean z3) throws RemoteException {
        fadeOutMenu();
        showMenuInternal(i, rect, z, z2, true, z3);
    }

    public void showMenu(int i, Rect rect, boolean z, boolean z2, boolean z3) throws RemoteException {
        showMenuInternal(i, rect, z, z2, false, z3);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public Runnable getMenuStateChangeFinishedCallback(final Messenger messenger, final Bundle bundle) {
        if (messenger == null || bundle == null) {
            return null;
        }
        return new Runnable() { // from class: com.android.systemui.pip.phone.PipMenuActivityController$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() throws RemoteException {
                PipMenuActivityController.lambda$getMenuStateChangeFinishedCallback$1(bundle, messenger);
            }
        };
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$getMenuStateChangeFinishedCallback$1(Bundle bundle, Messenger messenger) throws RemoteException {
        try {
            Message messageObtain = Message.obtain();
            messageObtain.what = bundle.getInt("message_callback_what");
            messenger.send(messageObtain);
        } catch (RemoteException unused) {
        }
    }

    private void showMenuInternal(int i, Rect rect, boolean z, boolean z2, boolean z3, boolean z4) throws RemoteException {
        if (this.mToActivityMessenger != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("menu_state", i);
            if (rect != null) {
                bundle.putParcelable("stack_bounds", rect);
            }
            bundle.putBoolean("allow_timeout", z);
            bundle.putBoolean("resize_menu_on_show", z2);
            bundle.putBoolean("show_menu_with_delay", z3);
            bundle.putBoolean("show_resize_handle", z4);
            Message messageObtain = Message.obtain();
            messageObtain.what = 1;
            messageObtain.obj = bundle;
            try {
                this.mToActivityMessenger.send(messageObtain);
                return;
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu to show", e);
                return;
            }
        }
        if (!this.mStartActivityRequested || isStartActivityRequestedElapsed()) {
            startMenuActivity(i, rect, z, z2, z3, z4);
        }
    }

    public void pokeMenu() throws RemoteException {
        if (this.mToActivityMessenger != null) {
            Message messageObtain = Message.obtain();
            messageObtain.what = 2;
            try {
                this.mToActivityMessenger.send(messageObtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify poke menu", e);
            }
        }
    }

    private void fadeOutMenu() throws RemoteException {
        if (this.mToActivityMessenger != null) {
            Message messageObtain = Message.obtain();
            messageObtain.what = 9;
            try {
                this.mToActivityMessenger.send(messageObtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu to fade out", e);
            }
        }
    }

    public void hideMenu() throws RemoteException {
        if (this.mToActivityMessenger != null) {
            Message messageObtain = Message.obtain();
            messageObtain.what = 3;
            try {
                this.mToActivityMessenger.send(messageObtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu to hide", e);
            }
        }
    }

    public void hideMenuWithoutResize() {
        onMenuStateChanged(0, false, null);
    }

    public void setAppActions(ParceledListSlice parceledListSlice) throws RemoteException {
        this.mAppActions = parceledListSlice;
        updateMenuActions();
    }

    private ParceledListSlice resolveMenuActions() {
        if (isValidActions(this.mAppActions)) {
            return this.mAppActions;
        }
        return this.mMediaActions;
    }

    private void startMenuActivity(int i, Rect rect, boolean z, boolean z2, boolean z3, boolean z4) {
        int[] iArr;
        try {
            ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(2, 0);
            if (stackInfo != null && (iArr = stackInfo.taskIds) != null && iArr.length > 0) {
                Intent intent = new Intent(this.mContext, (Class<?>) PipMenuActivity.class);
                intent.setFlags(268435456);
                intent.putExtra("messenger", this.mMessenger);
                intent.putExtra("actions", (Parcelable) resolveMenuActions());
                if (rect != null) {
                    intent.putExtra("stack_bounds", rect);
                }
                intent.putExtra("menu_state", i);
                intent.putExtra("allow_timeout", z);
                intent.putExtra("resize_menu_on_show", z2);
                intent.putExtra("show_menu_with_delay", z3);
                intent.putExtra("show_resize_handle", z4);
                ActivityOptions activityOptionsMakeCustomAnimation = ActivityOptions.makeCustomAnimation(this.mContext, 0, 0);
                int[] iArr2 = stackInfo.taskIds;
                activityOptionsMakeCustomAnimation.setLaunchTaskId(iArr2[iArr2.length - 1]);
                activityOptionsMakeCustomAnimation.setTaskOverlay(true, true);
                this.mContext.startActivityAsUser(intent, activityOptionsMakeCustomAnimation.toBundle(), UserHandle.CURRENT);
                setStartActivityRequested(true);
                return;
            }
            Log.e("PipMenuActController", "No PIP tasks found");
        } catch (RemoteException e) {
            setStartActivityRequested(false);
            Log.e("PipMenuActController", "Error showing PIP menu activity", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateMenuActions() throws RemoteException {
        if (this.mToActivityMessenger != null) {
            Rect rect = null;
            try {
                ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(2, 0);
                if (stackInfo != null) {
                    rect = stackInfo.bounds;
                }
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Error showing PIP menu activity", e);
            }
            Bundle bundle = new Bundle();
            bundle.putParcelable("stack_bounds", rect);
            bundle.putParcelable("actions", resolveMenuActions());
            Message messageObtain = Message.obtain();
            messageObtain.what = 4;
            messageObtain.obj = bundle;
            try {
                this.mToActivityMessenger.send(messageObtain);
            } catch (RemoteException e2) {
                Log.e("PipMenuActController", "Could not notify menu activity to update actions", e2);
            }
        }
    }

    private boolean isValidActions(ParceledListSlice parceledListSlice) {
        return parceledListSlice != null && parceledListSlice.getList().size() > 0;
    }

    private boolean isStartActivityRequestedElapsed() {
        return SystemClock.uptimeMillis() - this.mStartActivityRequestedTime >= 300;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onMenuStateChanged(final int i, final boolean z, final Runnable runnable) {
        if (i != this.mMenuState) {
            this.mListeners.forEach(new Consumer() { // from class: com.android.systemui.pip.phone.PipMenuActivityController$$ExternalSyntheticLambda2
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((PipMenuActivityController.Listener) obj).onPipMenuStateChanged(i, z, runnable);
                }
            });
            if (i == 2) {
                this.mMediaController.addListener(this.mMediaActionListener);
            } else {
                this.mMediaController.removeListener(this.mMediaActionListener);
            }
        }
        this.mMenuState = i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setStartActivityRequested(boolean z) {
        this.mHandler.removeCallbacks(this.mStartActivityRequestedTimeoutRunnable);
        this.mStartActivityRequested = z;
        this.mStartActivityRequestedTime = z ? SystemClock.uptimeMillis() : 0L;
    }

    void handlePointerEvent(MotionEvent motionEvent) throws RemoteException {
        if (this.mToActivityMessenger != null) {
            Message messageObtain = Message.obtain();
            messageObtain.what = 7;
            messageObtain.obj = motionEvent;
            try {
                this.mToActivityMessenger.send(messageObtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not dispatch touch event", e);
            }
        }
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipMenuActController");
        printWriter.println(str2 + "mMenuState=" + this.mMenuState);
        printWriter.println(str2 + "mToActivityMessenger=" + this.mToActivityMessenger);
        printWriter.println(str2 + "mListeners=" + this.mListeners.size());
        printWriter.println(str2 + "mStartActivityRequested=" + this.mStartActivityRequested);
        printWriter.println(str2 + "mStartActivityRequestedTime=" + this.mStartActivityRequestedTime);
    }
}
