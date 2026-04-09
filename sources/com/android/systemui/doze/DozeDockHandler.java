package com.android.systemui.doze;

import android.hardware.display.AmbientDisplayConfiguration;
import com.android.systemui.dock.DockManager;
import com.android.systemui.doze.DozeMachine;
import java.io.PrintWriter;

/* loaded from: classes.dex */
public class DozeDockHandler implements DozeMachine.Part {
    private static final boolean DEBUG = DozeService.DEBUG;
    private final AmbientDisplayConfiguration mConfig;
    private final DockManager mDockManager;
    private final DozeMachine mMachine;
    private int mDockState = 0;
    private final DockEventListener mDockEventListener = new DockEventListener(this, null);

    DozeDockHandler(AmbientDisplayConfiguration ambientDisplayConfiguration, DozeMachine dozeMachine, DockManager dockManager) {
        this.mMachine = dozeMachine;
        this.mConfig = ambientDisplayConfiguration;
        this.mDockManager = dockManager;
    }

    /* renamed from: com.android.systemui.doze.DozeDockHandler$1, reason: invalid class name */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$systemui$doze$DozeMachine$State;

        static {
            int[] iArr = new int[DozeMachine.State.values().length];
            $SwitchMap$com$android$systemui$doze$DozeMachine$State = iArr;
            try {
                iArr[DozeMachine.State.INITIALIZED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$com$android$systemui$doze$DozeMachine$State[DozeMachine.State.FINISH.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void transitionTo(DozeMachine.State state, DozeMachine.State state2) {
        int i = AnonymousClass1.$SwitchMap$com$android$systemui$doze$DozeMachine$State[state2.ordinal()];
        if (i == 1) {
            this.mDockEventListener.register();
        } else {
            if (i != 2) {
                return;
            }
            this.mDockEventListener.unregister();
        }
    }

    @Override // com.android.systemui.doze.DozeMachine.Part
    public void dump(PrintWriter printWriter) {
        printWriter.println("DozeDockHandler:");
        printWriter.println(" dockState=" + this.mDockState);
    }

    private class DockEventListener implements DockManager.DockEventListener {
        private boolean mRegistered;

        private DockEventListener() {
        }

        /* synthetic */ DockEventListener(DozeDockHandler dozeDockHandler, AnonymousClass1 anonymousClass1) {
            this();
        }

        void register() {
            if (this.mRegistered) {
                return;
            }
            if (DozeDockHandler.this.mDockManager != null) {
                DozeDockHandler.this.mDockManager.addListener(this);
            }
            this.mRegistered = true;
        }

        void unregister() {
            if (this.mRegistered) {
                if (DozeDockHandler.this.mDockManager != null) {
                    DozeDockHandler.this.mDockManager.removeListener(this);
                }
                this.mRegistered = false;
            }
        }
    }
}
