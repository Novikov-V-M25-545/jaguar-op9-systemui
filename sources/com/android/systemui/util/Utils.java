package com.android.systemui.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.CommandQueue;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import vendor.lineage.biometrics.fingerprint.inscreen.V1_0.IFingerprintInscreen;

/* loaded from: classes.dex */
public class Utils {
    public static boolean useQsMediaPlayer(Context context) {
        return true;
    }

    public static <T> void safeForeach(List<T> list, Consumer<T> consumer) {
        for (int size = list.size() - 1; size >= 0; size--) {
            T t = list.get(size);
            if (t != null) {
                consumer.accept(t);
            }
        }
    }

    public static class DisableStateTracker implements CommandQueue.Callbacks, View.OnAttachStateChangeListener {
        private final CommandQueue mCommandQueue;
        private boolean mDisabled;
        private final int mMask1;
        private final int mMask2;
        private View mView;

        public DisableStateTracker(int i, int i2, CommandQueue commandQueue) {
            this.mMask1 = i;
            this.mMask2 = i2;
            this.mCommandQueue = commandQueue;
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewAttachedToWindow(View view) {
            this.mView = view;
            this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        }

        @Override // android.view.View.OnAttachStateChangeListener
        public void onViewDetachedFromWindow(View view) {
            this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
            this.mView = null;
        }

        @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
        public void disable(int i, int i2, int i3, boolean z) {
            if (i != this.mView.getDisplay().getDisplayId()) {
                return;
            }
            boolean z2 = ((this.mMask1 & i2) == 0 && (this.mMask2 & i3) == 0) ? false : true;
            if (z2 == this.mDisabled) {
                return;
            }
            this.mDisabled = z2;
            this.mView.setVisibility(z2 ? 8 : 0);
        }
    }

    public static boolean isHeadlessRemoteDisplayProvider(PackageManager packageManager, String str) {
        if (packageManager.checkPermission("android.permission.REMOTE_DISPLAY_PROVIDER", str) != 0) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(str);
        return packageManager.queryIntentActivities(intent, 0).isEmpty();
    }

    public static boolean isGesturalModeOnDefaultDisplay(Context context, int i) {
        return context.getDisplayId() == 0 && QuickStepContract.isGesturalMode(i);
    }

    public static boolean useMediaResumption(Context context) {
        return useQsMediaPlayer(context) && Settings.Secure.getInt(context.getContentResolver(), "qs_media_resumption", 0) > 0;
    }

    public static Set<String> getBlockedMediaApps(Context context) {
        String string = Settings.Secure.getString(context.getContentResolver(), "qs_media_resumption_blocked");
        if (TextUtils.isEmpty(string)) {
            return new HashSet();
        }
        String[] strArrSplit = string.split(":");
        HashSet hashSet = new HashSet(strArrSplit.length);
        for (String str : strArrSplit) {
            hashSet.add(str);
        }
        return hashSet;
    }

    public static int getFODHeight(Context context, boolean z) {
        IFingerprintInscreen fingerprintInScreenDaemon = getFingerprintInScreenDaemon();
        if (fingerprintInScreenDaemon == null) {
            return 0;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (z) {
            ((WindowManager) context.getSystemService(WindowManager.class)).getDefaultDisplay().getMetrics(displayMetrics);
        } else {
            ((WindowManager) context.getSystemService(WindowManager.class)).getDefaultDisplay().getRealMetrics(displayMetrics);
        }
        try {
            return (displayMetrics.heightPixels - fingerprintInScreenDaemon.getPositionY()) + (fingerprintInScreenDaemon.getSize() / 2);
        } catch (RemoteException | NoSuchElementException unused) {
            return 0;
        }
    }

    private static IFingerprintInscreen getFingerprintInScreenDaemon() {
        try {
            return IFingerprintInscreen.getService();
        } catch (RemoteException | NoSuchElementException unused) {
            return null;
        }
    }
}
