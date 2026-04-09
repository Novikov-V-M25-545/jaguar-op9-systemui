package com.android.systemui.statusbar.policy;

import android.content.res.Resources;
import android.provider.Settings;
import android.util.ArraySet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.NotificationShadeDepthController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.tuner.TunerService;
import java.util.Objects;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class BrightnessMirrorController implements CallbackController<BrightnessMirrorListener> {
    private View mBrightnessMirror;
    private final NotificationShadeDepthController mDepthController;
    private final NotificationPanelViewController mNotificationPanel;
    private boolean mShouldShowAutoBrightness;
    private final NotificationShadeWindowView mStatusBarWindow;
    private final TunerService.Tunable mTunable;
    private final Consumer<Boolean> mVisibilityCallback;
    private final ArraySet<BrightnessMirrorListener> mBrightnessMirrorListeners = new ArraySet<>();
    private final int[] mInt2Cache = new int[2];

    public interface BrightnessMirrorListener {
        void onBrightnessMirrorReinflated(View view);
    }

    public BrightnessMirrorController(NotificationShadeWindowView notificationShadeWindowView, NotificationPanelViewController notificationPanelViewController, NotificationShadeDepthController notificationShadeDepthController, Consumer<Boolean> consumer) {
        TunerService.Tunable tunable = new TunerService.Tunable() { // from class: com.android.systemui.statusbar.policy.BrightnessMirrorController.1
            @Override // com.android.systemui.tuner.TunerService.Tunable
            public void onTuningChanged(String str, String str2) {
                if ("lineagesecure:qs_show_auto_brightness".equals(str)) {
                    BrightnessMirrorController.this.mShouldShowAutoBrightness = TunerService.parseIntegerSwitch(str2, true);
                    BrightnessMirrorController.this.updateIcon();
                }
            }
        };
        this.mTunable = tunable;
        this.mStatusBarWindow = notificationShadeWindowView;
        this.mBrightnessMirror = notificationShadeWindowView.findViewById(R.id.brightness_mirror);
        this.mNotificationPanel = notificationPanelViewController;
        this.mDepthController = notificationShadeDepthController;
        notificationPanelViewController.setPanelAlphaEndAction(new Runnable() { // from class: com.android.systemui.statusbar.policy.BrightnessMirrorController$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$new$0();
            }
        });
        this.mVisibilityCallback = consumer;
        updateResources();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(tunable, "lineagesecure:qs_show_auto_brightness");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        this.mBrightnessMirror.setVisibility(4);
    }

    public void showMirror() {
        this.mBrightnessMirror.setVisibility(0);
        this.mVisibilityCallback.accept(Boolean.TRUE);
        this.mNotificationPanel.setPanelAlpha(0, true);
        this.mDepthController.setBrightnessMirrorVisible(true);
        updateIcon();
    }

    public void hideMirror() {
        this.mVisibilityCallback.accept(Boolean.FALSE);
        this.mNotificationPanel.setPanelAlpha(255, true);
        this.mDepthController.setBrightnessMirrorVisible(false);
    }

    public void setLocation(View view) {
        view.getLocationInWindow(this.mInt2Cache);
        int width = this.mInt2Cache[0] + (view.getWidth() / 2);
        int height = this.mInt2Cache[1] + (view.getHeight() / 2);
        this.mBrightnessMirror.setTranslationX(0.0f);
        this.mBrightnessMirror.setTranslationY(0.0f);
        this.mBrightnessMirror.getLocationInWindow(this.mInt2Cache);
        int width2 = this.mInt2Cache[0] + (this.mBrightnessMirror.getWidth() / 2);
        int height2 = this.mInt2Cache[1] + (this.mBrightnessMirror.getHeight() / 2);
        this.mBrightnessMirror.setTranslationX(width - width2);
        this.mBrightnessMirror.setTranslationY(height - height2);
    }

    public View getMirror() {
        return this.mBrightnessMirror;
    }

    public void updateResources() {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mBrightnessMirror.getLayoutParams();
        Resources resources = this.mBrightnessMirror.getResources();
        layoutParams.width = resources.getDimensionPixelSize(R.dimen.qs_panel_width);
        layoutParams.height = resources.getDimensionPixelSize(R.dimen.brightness_mirror_height);
        layoutParams.gravity = resources.getInteger(R.integer.notification_panel_layout_gravity);
        this.mBrightnessMirror.setLayoutParams(layoutParams);
    }

    public void onOverlayChanged() {
        reinflate();
    }

    public void onDensityOrFontScaleChanged() {
        reinflate();
    }

    private void reinflate() {
        updateResources();
        int iIndexOfChild = this.mStatusBarWindow.indexOfChild(this.mBrightnessMirror);
        this.mStatusBarWindow.removeView(this.mBrightnessMirror);
        View viewInflate = LayoutInflater.from(this.mBrightnessMirror.getContext()).inflate(R.layout.brightness_mirror, (ViewGroup) this.mStatusBarWindow, false);
        this.mBrightnessMirror = viewInflate;
        this.mStatusBarWindow.addView(viewInflate, iIndexOfChild);
        for (int i = 0; i < this.mBrightnessMirrorListeners.size(); i++) {
            this.mBrightnessMirrorListeners.valueAt(i).onBrightnessMirrorReinflated(this.mBrightnessMirror);
        }
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void addCallback(BrightnessMirrorListener brightnessMirrorListener) {
        Objects.requireNonNull(brightnessMirrorListener);
        this.mBrightnessMirrorListeners.add(brightnessMirrorListener);
    }

    @Override // com.android.systemui.statusbar.policy.CallbackController
    public void removeCallback(BrightnessMirrorListener brightnessMirrorListener) {
        this.mBrightnessMirrorListeners.remove(brightnessMirrorListener);
    }

    public void onUiModeChanged() {
        reinflate();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateIcon() {
        int i;
        ImageView imageView = (ImageView) this.mBrightnessMirror.findViewById(R.id.brightness_icon);
        if (this.mBrightnessMirror.getContext().getResources().getBoolean(android.R.bool.config_allow_pin_storage_for_unattended_reboot) && this.mShouldShowAutoBrightness) {
            if (Settings.System.getIntForUser(this.mBrightnessMirror.getContext().getContentResolver(), "screen_brightness_mode", 0, -2) != 0) {
                i = R.drawable.ic_qs_brightness_auto_on;
            } else {
                i = R.drawable.ic_qs_brightness_auto_off;
            }
            imageView.setImageResource(i);
            imageView.setVisibility(0);
            return;
        }
        imageView.setVisibility(8);
    }
}
