package com.android.systemui.pip.phone;

import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Pair;

/* loaded from: classes.dex */
public class PipAppOpsListener {
    private IActivityManager mActivityManager;
    private AppOpsManager.OnOpChangedListener mAppOpsChangedListener = new AnonymousClass1();
    private AppOpsManager mAppOpsManager;
    private Callback mCallback;
    private Context mContext;
    private Handler mHandler;

    public interface Callback {
        void dismissPip();
    }

    /* renamed from: com.android.systemui.pip.phone.PipAppOpsListener$1, reason: invalid class name */
    class AnonymousClass1 implements AppOpsManager.OnOpChangedListener {
        AnonymousClass1() {
        }

        @Override // android.app.AppOpsManager.OnOpChangedListener
        public void onOpChanged(String str, String str2) {
            try {
                Pair<ComponentName, Integer> topPipActivity = PipUtils.getTopPipActivity(PipAppOpsListener.this.mContext, PipAppOpsListener.this.mActivityManager);
                if (topPipActivity.first != null) {
                    ApplicationInfo applicationInfoAsUser = PipAppOpsListener.this.mContext.getPackageManager().getApplicationInfoAsUser(str2, 0, ((Integer) topPipActivity.second).intValue());
                    if (!applicationInfoAsUser.packageName.equals(((ComponentName) topPipActivity.first).getPackageName()) || PipAppOpsListener.this.mAppOpsManager.checkOpNoThrow(67, applicationInfoAsUser.uid, str2) == 0) {
                        return;
                    }
                    PipAppOpsListener.this.mHandler.post(new Runnable() { // from class: com.android.systemui.pip.phone.PipAppOpsListener$1$$ExternalSyntheticLambda0
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.lambda$onOpChanged$0();
                        }
                    });
                }
            } catch (PackageManager.NameNotFoundException unused) {
                PipAppOpsListener.this.unregisterAppOpsListener();
            }
        }

        /* JADX INFO: Access modifiers changed from: private */
        public /* synthetic */ void lambda$onOpChanged$0() {
            PipAppOpsListener.this.mCallback.dismissPip();
        }
    }

    public PipAppOpsListener(Context context, IActivityManager iActivityManager, Callback callback) {
        this.mContext = context;
        this.mHandler = new Handler(this.mContext.getMainLooper());
        this.mActivityManager = iActivityManager;
        this.mAppOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mCallback = callback;
    }

    public void onActivityPinned(String str) {
        registerAppOpsListener(str);
    }

    public void onActivityUnpinned() {
        unregisterAppOpsListener();
    }

    private void registerAppOpsListener(String str) {
        this.mAppOpsManager.startWatchingMode(67, str, this.mAppOpsChangedListener);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void unregisterAppOpsListener() {
        this.mAppOpsManager.stopWatchingMode(this.mAppOpsChangedListener);
    }
}
