package com.android.systemui.screenshot;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.util.ScreenshotHelper;
import com.android.systemui.screenshot.TakeScreenshotService;
import com.android.systemui.shared.recents.utilities.BitmapUtil;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class TakeScreenshotService extends Service {
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { // from class: com.android.systemui.screenshot.TakeScreenshotService.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (!"android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction()) || TakeScreenshotService.this.mScreenshot == null) {
                return;
            }
            TakeScreenshotService.this.mScreenshot.dismissScreenshot("close system dialogs", false);
        }
    };
    private Handler mHandler = new AnonymousClass2(Looper.myLooper());
    private final GlobalScreenshot mScreenshot;
    private final UiEventLogger mUiEventLogger;
    private final UserManager mUserManager;

    /* renamed from: com.android.systemui.screenshot.TakeScreenshotService$2, reason: invalid class name */
    class AnonymousClass2 extends Handler {
        AnonymousClass2(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) throws Resources.NotFoundException {
            final Messenger messenger = message.replyTo;
            final Consumer<Uri> consumer = new Consumer() { // from class: com.android.systemui.screenshot.TakeScreenshotService$2$$ExternalSyntheticLambda2
                @Override // java.util.function.Consumer
                public final void accept(Object obj) throws RemoteException {
                    TakeScreenshotService.AnonymousClass2.lambda$handleMessage$0(messenger, (Uri) obj);
                }
            };
            Runnable runnable = new Runnable() { // from class: com.android.systemui.screenshot.TakeScreenshotService$2$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() throws RemoteException {
                    TakeScreenshotService.AnonymousClass2.lambda$handleMessage$1(messenger);
                }
            };
            if (!TakeScreenshotService.this.mUserManager.isUserUnlocked()) {
                Log.w("TakeScreenshotService", "Skipping screenshot because storage is locked!");
                post(new Runnable() { // from class: com.android.systemui.screenshot.TakeScreenshotService$2$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        consumer.accept(null);
                    }
                });
                post(runnable);
                return;
            }
            ScreenshotHelper.ScreenshotRequest screenshotRequest = (ScreenshotHelper.ScreenshotRequest) message.obj;
            TakeScreenshotService.this.mUiEventLogger.log(ScreenshotEvent.getScreenshotSource(screenshotRequest.getSource()));
            int i = message.what;
            if (i == 1) {
                TakeScreenshotService.this.mScreenshot.takeScreenshotFullscreen(consumer, runnable);
                return;
            }
            if (i == 2) {
                TakeScreenshotService.this.mScreenshot.takeScreenshotPartial(consumer, runnable);
                return;
            }
            if (i == 3) {
                TakeScreenshotService.this.mScreenshot.handleImageAsScreenshot(BitmapUtil.bundleToHardwareBitmap(screenshotRequest.getBitmapBundle()), screenshotRequest.getBoundsInScreen(), screenshotRequest.getInsets(), screenshotRequest.getTaskId(), screenshotRequest.getUserId(), screenshotRequest.getTopComponent(), consumer, runnable);
                return;
            }
            Log.d("TakeScreenshotService", "Invalid screenshot option: " + message.what);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$handleMessage$0(Messenger messenger, Uri uri) throws RemoteException {
            try {
                messenger.send(Message.obtain(null, 1, uri));
            } catch (RemoteException unused) {
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public static /* synthetic */ void lambda$handleMessage$1(Messenger messenger) throws RemoteException {
            try {
                messenger.send(Message.obtain((Handler) null, 2));
            } catch (RemoteException unused) {
            }
        }
    }

    public TakeScreenshotService(GlobalScreenshot globalScreenshot, UserManager userManager, UiEventLogger uiEventLogger) {
        this.mScreenshot = globalScreenshot;
        this.mUserManager = userManager;
        this.mUiEventLogger = uiEventLogger;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"));
        return new Messenger(this.mHandler).getBinder();
    }

    @Override // android.app.Service
    public boolean onUnbind(Intent intent) {
        GlobalScreenshot globalScreenshot = this.mScreenshot;
        if (globalScreenshot != null) {
            globalScreenshot.stopScreenshot();
        }
        unregisterReceiver(this.mBroadcastReceiver);
        return true;
    }
}
