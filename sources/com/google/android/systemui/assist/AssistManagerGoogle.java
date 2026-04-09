package com.google.android.systemui.assist;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IVoiceInteractionSessionListener;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.assist.AssistHandleBehaviorController;
import com.android.systemui.assist.AssistLogger;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.assist.AssistantSessionEvent;
import com.android.systemui.assist.PhoneStateMonitor;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.model.SysUiState;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.google.android.systemui.assist.uihints.AssistantPresenceHandler;
import com.google.android.systemui.assist.uihints.GoogleDefaultUiController;
import dagger.Lazy;
import lineageos.hardware.LineageHardwareManager;

/* loaded from: classes.dex */
public class AssistManagerGoogle extends AssistManager {
    private final AssistantPresenceHandler mAssistantPresenceHandler;
    private boolean mCheckAssistantStatus;
    private boolean mGoogleIsAssistant;
    private int mNavigationMode;
    private final OpaEnabledReceiver mOpaEnabledReceiver;
    private AssistManager.UiController mUiController;
    private final Handler mUiHandler;

    @Override // com.android.systemui.assist.AssistManager
    public boolean shouldShowOrb() {
        return false;
    }

    public AssistManagerGoogle(DeviceProvisionedController deviceProvisionedController, Context context, AssistUtils assistUtils, AssistHandleBehaviorController assistHandleBehaviorController, CommandQueue commandQueue, PhoneStateMonitor phoneStateMonitor, OverviewProxyService overviewProxyService, ConfigurationController configurationController, Lazy<SysUiState> lazy, GoogleDefaultUiController googleDefaultUiController, AssistLogger assistLogger, BroadcastDispatcher broadcastDispatcher, OpaEnabledDispatcher opaEnabledDispatcher, KeyguardUpdateMonitor keyguardUpdateMonitor, NavigationModeController navigationModeController, AssistantPresenceHandler assistantPresenceHandler, Handler handler) {
        super(deviceProvisionedController, context, assistUtils, assistHandleBehaviorController, commandQueue, phoneStateMonitor, overviewProxyService, configurationController, lazy, googleDefaultUiController, assistLogger);
        this.mCheckAssistantStatus = true;
        this.mUiHandler = handler;
        this.mUiController = googleDefaultUiController;
        this.mOpaEnabledReceiver = new OpaEnabledReceiver(this.mContext, broadcastDispatcher);
        addOpaEnabledListener(opaEnabledDispatcher);
        keyguardUpdateMonitor.registerCallback(new KeyguardUpdateMonitorCallback() { // from class: com.google.android.systemui.assist.AssistManagerGoogle.1
            @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
            public void onUserSwitching(int i) {
                AssistManagerGoogle.this.mOpaEnabledReceiver.onUserSwitching(i);
            }
        });
        this.mNavigationMode = navigationModeController.addListener(new NavigationModeController.ModeChangedListener() { // from class: com.google.android.systemui.assist.AssistManagerGoogle.2
            @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
            public final void onNavigationModeChanged(int i) {
                AssistManagerGoogle.this.mNavigationMode = i;
            }
        });
        this.mAssistantPresenceHandler = assistantPresenceHandler;
        assistantPresenceHandler.registerAssistantPresenceChangeListener(new AssistantPresenceHandler.AssistantPresenceChangeListener() { // from class: com.google.android.systemui.assist.AssistManagerGoogle.3
            @Override // com.google.android.systemui.assist.uihints.AssistantPresenceHandler.AssistantPresenceChangeListener
            public final void onAssistantPresenceChanged(boolean z) {
                if (AssistManagerGoogle.this.mGoogleIsAssistant != z) {
                    AssistManagerGoogle.this.mUiHandler.post(new Runnable() { // from class: com.google.android.systemui.assist.AssistManagerGoogle.3.1
                        @Override // java.lang.Runnable
                        public final void run() {
                            AssistManagerGoogle.this.mUiController.hide();
                        }
                    });
                    ((GoogleDefaultUiController) AssistManagerGoogle.this.mUiController).setGoogleAssistant(z);
                    AssistManagerGoogle.this.mGoogleIsAssistant = z;
                }
                AssistManagerGoogle.this.mCheckAssistantStatus = false;
            }
        });
    }

    @Override // com.android.systemui.assist.AssistManager
    protected void registerVoiceInteractionSessionListener() {
        this.mAssistUtils.registerVoiceInteractionSessionListener(new IVoiceInteractionSessionListener.Stub() { // from class: com.google.android.systemui.assist.AssistManagerGoogle.4
            public void onVoiceSessionShown() throws RemoteException {
                ((AssistManager) AssistManagerGoogle.this).mAssistLogger.reportAssistantSessionEvent(AssistantSessionEvent.ASSISTANT_SESSION_UPDATE);
            }

            public void onVoiceSessionHidden() throws RemoteException {
                ((AssistManager) AssistManagerGoogle.this).mAssistLogger.reportAssistantSessionEvent(AssistantSessionEvent.ASSISTANT_SESSION_CLOSE);
            }

            public void onSetUiHints(Bundle bundle) {
                String string = bundle.getString("action");
                if ("show_assist_handles".equals(string)) {
                    AssistManagerGoogle.this.requestAssistHandles();
                } else if (!"set_assist_gesture_constrained".equals(string)) {
                    AssistManagerGoogle.this.mUiHandler.post(new Runnable() { // from class: com.google.android.systemui.assist.AssistManagerGoogle.4.1
                        @Override // java.lang.Runnable
                        public final void run() {
                            AssistManagerGoogle.this.mAssistantPresenceHandler.requestAssistantPresenceUpdate();
                            AssistManagerGoogle.this.mCheckAssistantStatus = false;
                        }
                    });
                } else {
                    ((SysUiState) ((AssistManager) AssistManagerGoogle.this).mSysUiState.get()).setFlag(LineageHardwareManager.FEATURE_DISPLAY_MODES, bundle.getBoolean("should_constrain", false)).commitUpdate(0);
                }
            }
        });
    }

    @Override // com.android.systemui.assist.AssistManager
    public void onInvocationProgress(int i, float f) {
        if (f == 0.0f || f == 1.0f) {
            this.mCheckAssistantStatus = true;
        }
        if (this.mCheckAssistantStatus) {
            this.mAssistantPresenceHandler.requestAssistantPresenceUpdate();
            this.mCheckAssistantStatus = false;
        }
        if (i != 2) {
            this.mUiController.onInvocationProgress(i, f);
        }
    }

    @Override // com.android.systemui.assist.AssistManager
    public void onGestureCompletion(float f) {
        this.mCheckAssistantStatus = true;
        this.mUiController.onGestureCompletion(f / this.mContext.getResources().getDisplayMetrics().density);
    }

    public void addOpaEnabledListener(OpaEnabledListener opaEnabledListener) {
        this.mOpaEnabledReceiver.addOpaEnabledListener(opaEnabledListener);
    }

    public void dispatchOpaEnabledState() {
        this.mOpaEnabledReceiver.dispatchOpaEnabledState();
    }
}
