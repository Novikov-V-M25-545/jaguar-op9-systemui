package com.android.systemui;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.lifecycle.LifecycleOwner;
import com.android.settingslib.graph.BatteryMeterDrawableBase;
import com.android.settingslib.graph.ThemedBatteryDrawable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.SysuiLifecycle;
import com.android.systemui.util.Utils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class BatteryMeterView extends LinearLayout implements BatteryController.BatteryStateChangeCallback, TunerService.Tunable, DarkIconDispatcher.DarkReceiver, ConfigurationController.ConfigurationListener {
    private BatteryController mBatteryController;
    private final ImageView mBatteryIconView;
    private boolean mBatteryPercentCharging;
    private TextView mBatteryPercentView;
    public int mBatteryStyle;
    private final ArrayList<BatteryMeterViewCallbacks> mCallbacks;
    private boolean mCharging;
    private final ThemedBatteryDrawable mDrawable;
    private DualToneHandler mDualToneHandler;
    private boolean mIgnoreTunerUpdates;
    private boolean mIsSubscribedForTunerUpdates;
    private int mLevel;
    private int mNonAdaptedBackgroundColor;
    private int mNonAdaptedForegroundColor;
    private int mNonAdaptedSingleToneColor;
    private final int mPercentageStyleId;
    private SettingObserver mSettingObserver;
    public int mShowBatteryEstimate;
    public int mShowBatteryPercent;
    private int mShowPercentMode;
    private final String mSlotBattery;
    private int mTextColor;
    private boolean mUseWallpaperTextColors;
    private int mUser;
    private final CurrentUserTracker mUserTracker;
    private final BatteryMeterDrawableBase mXDrawable;

    public interface BatteryMeterViewCallbacks {
        default void onHiddenBattery(boolean z) {
        }
    }

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public BatteryMeterView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public BatteryMeterView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mShowPercentMode = 0;
        this.mBatteryStyle = 0;
        this.mShowBatteryEstimate = 0;
        this.mCallbacks = new ArrayList<>();
        BroadcastDispatcher broadcastDispatcher = (BroadcastDispatcher) Dependency.get(BroadcastDispatcher.class);
        setOrientation(0);
        setGravity(8388627);
        TypedArray typedArrayObtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.BatteryMeterView, i, 0);
        int color = typedArrayObtainStyledAttributes.getColor(R.styleable.BatteryMeterView_frameColor, context.getColor(R.color.meter_background_color));
        this.mPercentageStyleId = typedArrayObtainStyledAttributes.getResourceId(R.styleable.BatteryMeterView_textAppearance, 0);
        ThemedBatteryDrawable themedBatteryDrawable = new ThemedBatteryDrawable(context, color);
        this.mDrawable = themedBatteryDrawable;
        this.mXDrawable = new BatteryMeterDrawableBase(context, color);
        typedArrayObtainStyledAttributes.recycle();
        this.mSettingObserver = new SettingObserver(new Handler(context.getMainLooper()));
        addOnAttachStateChangeListener(new Utils.DisableStateTracker(0, 2, (CommandQueue) Dependency.get(CommandQueue.class)));
        setupLayoutTransition();
        this.mSlotBattery = context.getString(android.R.string.permlab_foregroundServiceMediaProjection);
        ImageView imageView = new ImageView(context);
        this.mBatteryIconView = imageView;
        imageView.setImageDrawable(themedBatteryDrawable);
        ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(getResources().getDimensionPixelSize(R.dimen.status_bar_battery_icon_width), getResources().getDimensionPixelSize(R.dimen.status_bar_battery_icon_height));
        marginLayoutParams.setMargins(0, 0, 0, getResources().getDimensionPixelOffset(R.dimen.battery_margin_bottom));
        addView(imageView, marginLayoutParams);
        this.mBatteryStyle = Settings.System.getIntForUser(((LinearLayout) this).mContext.getContentResolver(), "status_bar_battery_style", 0, -2);
        updateBatteryStyle();
        updatePercentView();
        updateVisibility();
        this.mDualToneHandler = new DualToneHandler(context);
        onDarkChanged(new Rect(), 0.0f, -1);
        this.mUserTracker = new CurrentUserTracker(broadcastDispatcher) { // from class: com.android.systemui.BatteryMeterView.1
            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i2) {
                BatteryMeterView.this.mUser = i2;
                BatteryMeterView.this.getContext().getContentResolver().unregisterContentObserver(BatteryMeterView.this.mSettingObserver);
                BatteryMeterView.this.updateShowPercent();
            }
        };
        setClipChildren(false);
        setClipToPadding(false);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).observe(SysuiLifecycle.viewAttachLifecycle(this), (LifecycleOwner) this);
    }

    private void setupLayoutTransition() {
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(200L);
        layoutTransition.setAnimator(2, ObjectAnimator.ofFloat((Object) null, "alpha", 0.0f, 1.0f));
        layoutTransition.setInterpolator(2, Interpolators.ALPHA_IN);
        ObjectAnimator objectAnimatorOfFloat = ObjectAnimator.ofFloat((Object) null, "alpha", 1.0f, 0.0f);
        layoutTransition.setInterpolator(3, Interpolators.ALPHA_OUT);
        layoutTransition.setAnimator(3, objectAnimatorOfFloat);
        setLayoutTransition(layoutTransition);
    }

    public void setIgnoreTunerUpdates(boolean z) {
        this.mIgnoreTunerUpdates = z;
        updateTunerSubscription();
    }

    private void updateTunerSubscription() {
        if (this.mIgnoreTunerUpdates) {
            unsubscribeFromTunerUpdates();
        } else {
            subscribeForTunerUpdates();
        }
    }

    private void subscribeForTunerUpdates() {
        if (this.mIsSubscribedForTunerUpdates || this.mIgnoreTunerUpdates) {
            return;
        }
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:status_bar_battery_style", "system:status_bar_show_battery_percent", "system:status_bar_battery_text_charging");
        this.mIsSubscribedForTunerUpdates = true;
    }

    private void unsubscribeFromTunerUpdates() {
        if (this.mIsSubscribedForTunerUpdates) {
            ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
            this.mIsSubscribedForTunerUpdates = false;
        }
    }

    public void setColorsFromContext(Context context) {
        if (context == null) {
            return;
        }
        this.mDualToneHandler.setColorsFromContext(context);
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        str.hashCode();
        switch (str) {
            case "system:status_bar_show_battery_percent":
                this.mShowBatteryPercent = TunerService.parseInteger(str2, 0);
                updatePercentView();
                break;
            case "system:status_bar_battery_style":
                this.mBatteryStyle = TunerService.parseInteger(str2, 0);
                updateBatteryStyle();
                updatePercentView();
                updateVisibility();
                break;
            case "system:status_bar_battery_text_charging":
                this.mBatteryPercentCharging = TunerService.parseIntegerSwitch(str2, true);
                updatePercentView();
                break;
        }
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        BatteryController batteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mBatteryController = batteryController;
        batteryController.addCallback(this);
        this.mUser = ActivityManager.getCurrentUser();
        getContext().getContentResolver().registerContentObserver(Settings.Global.getUriFor("battery_estimates_last_update_time"), false, this.mSettingObserver);
        updateShowPercent();
        subscribeForTunerUpdates();
        this.mUserTracker.startTracking();
    }

    @Override // android.view.ViewGroup, android.view.View
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mUserTracker.stopTracking();
        this.mBatteryController.removeCallback(this);
        getContext().getContentResolver().unregisterContentObserver(this.mSettingObserver);
        unsubscribeFromTunerUpdates();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
        if (this.mLevel != i) {
            this.mLevel = i;
            this.mDrawable.setBatteryLevel(i);
            this.mXDrawable.setBatteryLevel(i);
        }
        if (this.mCharging != z) {
            this.mCharging = z;
            this.mDrawable.setCharging(z);
            this.mXDrawable.setCharging(this.mCharging);
            updateShowPercent();
            return;
        }
        updatePercentText();
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        this.mDrawable.setPowerSaveEnabled(z);
        this.mXDrawable.setPowerSave(z);
        updateShowPercent();
    }

    private TextView loadPercentView() {
        return (TextView) LayoutInflater.from(getContext()).inflate(R.layout.battery_percentage_view, (ViewGroup) null);
    }

    public void updatePercentView() {
        removeBatteryPercentView();
        updateShowPercent();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updatePercentText() {
        if (this.mBatteryController == null) {
            return;
        }
        if (this.mBatteryPercentView != null) {
            setPercentTextAtCurrentLevel();
        } else {
            setContentDescription(getContext().getString(this.mCharging ? R.string.accessibility_battery_level_charging : R.string.accessibility_battery_level, Integer.valueOf(this.mLevel)));
        }
    }

    private void setPercentTextAtCurrentLevel() {
        String str;
        final String str2 = NumberFormat.getPercentInstance().format(this.mLevel / 100.0f);
        if (this.mShowBatteryEstimate != 0 && !this.mCharging) {
            this.mBatteryController.getEstimatedTimeRemainingString(new BatteryController.EstimateFetchCompletion() { // from class: com.android.systemui.BatteryMeterView$$ExternalSyntheticLambda0
                @Override // com.android.systemui.statusbar.policy.BatteryController.EstimateFetchCompletion
                public final void onBatteryRemainingEstimateRetrieved(String str3) {
                    this.f$0.lambda$setPercentTextAtCurrentLevel$0(str2, str3);
                }
            });
            return;
        }
        if (this.mCharging && this.mBatteryStyle == 5) {
            str = "⚡︎ ";
        } else {
            str = "";
        }
        this.mBatteryPercentView.setText(((Object) str) + str2);
        setContentDescription(getContext().getString(this.mCharging ? R.string.accessibility_battery_level_charging : R.string.accessibility_battery_level, Integer.valueOf(this.mLevel)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$setPercentTextAtCurrentLevel$0(String str, String str2) {
        if (str2 != null) {
            if (this.mShowPercentMode == 1 || this.mShowBatteryPercent == 2) {
                this.mBatteryPercentView.setText(str + " · " + str2);
            } else {
                this.mBatteryPercentView.setText(str2);
            }
        } else if (this.mShowPercentMode == 1 || this.mShowBatteryPercent == 2) {
            this.mBatteryPercentView.setText(str);
        } else {
            this.mBatteryPercentView.setText("");
        }
        setContentDescription(getContext().getString(R.string.accessibility_battery_level_with_estimate, Integer.valueOf(this.mLevel), str2));
    }

    private void removeBatteryPercentView() {
        TextView textView = this.mBatteryPercentView;
        if (textView != null) {
            removeView(textView);
            this.mBatteryPercentView = null;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateShowPercent() {
        boolean z = this.mBatteryPercentView != null;
        int i = this.mShowBatteryPercent;
        boolean z2 = i == 1 && !this.mCharging;
        boolean z3 = i >= 2 || (this.mBatteryPercentCharging && this.mCharging) || this.mShowPercentMode == 1 || this.mShowBatteryEstimate != 0;
        this.mDrawable.setShowPercent(z2);
        this.mXDrawable.setShowPercent(z2);
        if (z3) {
            if (!z) {
                TextView textViewLoadPercentView = loadPercentView();
                this.mBatteryPercentView = textViewLoadPercentView;
                int i2 = this.mPercentageStyleId;
                if (i2 != 0) {
                    textViewLoadPercentView.setTextAppearance(i2);
                }
                int i3 = this.mTextColor;
                if (i3 != 0) {
                    this.mBatteryPercentView.setTextColor(i3);
                }
                updatePercentText();
                addView(this.mBatteryPercentView, new ViewGroup.LayoutParams(-2, -1));
            }
            if (this.mBatteryStyle == 5) {
                this.mBatteryPercentView.setPaddingRelative(0, 0, 0, 0);
                return;
            } else {
                this.mBatteryPercentView.setPaddingRelative(getContext().getResources().getDimensionPixelSize(R.dimen.battery_level_padding_start), 0, 0, 0);
                setLayoutDirection(this.mShowBatteryPercent <= 2 ? 0 : 1);
                return;
            }
        }
        removeBatteryPercentView();
    }

    public void updateVisibility() {
        if (this.mBatteryStyle == 5) {
            this.mBatteryIconView.setVisibility(8);
            this.mBatteryIconView.setImageDrawable(null);
        } else {
            this.mBatteryIconView.setVisibility(0);
            scaleBatteryMeterViews();
        }
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).onHiddenBattery(this.mBatteryStyle == 5);
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() throws Resources.NotFoundException {
        scaleBatteryMeterViews();
    }

    private void scaleBatteryMeterViews() throws Resources.NotFoundException {
        int dimensionPixelSize;
        Resources resources = getContext().getResources();
        TypedValue typedValue = new TypedValue();
        resources.getValue(R.dimen.status_bar_icon_scale_factor, typedValue, true);
        float f = typedValue.getFloat();
        int dimensionPixelSize2 = resources.getDimensionPixelSize(R.dimen.status_bar_battery_icon_height);
        int i = this.mBatteryStyle;
        if (i == 1 || i == 2 || i == 6) {
            dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.status_bar_battery_icon_circle_width);
        } else {
            dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.status_bar_battery_icon_width);
        }
        int dimensionPixelSize3 = resources.getDimensionPixelSize(R.dimen.battery_margin_bottom);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) (dimensionPixelSize * f), (int) (dimensionPixelSize2 * f));
        layoutParams.setMargins(0, 0, 0, dimensionPixelSize3);
        this.mBatteryIconView.setLayoutParams(layoutParams);
    }

    public void updateBatteryStyle() {
        int i = this.mBatteryStyle;
        if (i == 5) {
            return;
        }
        if (i == 0) {
            this.mBatteryIconView.setImageDrawable(this.mDrawable);
        } else {
            this.mXDrawable.setMeterStyle(i);
            this.mBatteryIconView.setImageDrawable(this.mXDrawable);
        }
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        if (!DarkIconDispatcher.isInArea(rect, this)) {
            f = 0.0f;
        }
        this.mNonAdaptedSingleToneColor = this.mDualToneHandler.getSingleColor(f);
        this.mNonAdaptedForegroundColor = this.mDualToneHandler.getFillColor(f);
        int backgroundColor = this.mDualToneHandler.getBackgroundColor(f);
        this.mNonAdaptedBackgroundColor = backgroundColor;
        if (this.mUseWallpaperTextColors) {
            return;
        }
        updateColors(this.mNonAdaptedForegroundColor, backgroundColor, this.mNonAdaptedSingleToneColor);
    }

    private void updateColors(int i, int i2, int i3) {
        this.mDrawable.setColors(i, i2, i3);
        this.mXDrawable.setColors(i, i2);
        this.mTextColor = i3;
        TextView textView = this.mBatteryPercentView;
        if (textView != null) {
            textView.setTextColor(i3);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        if (this.mDrawable == null) {
            str = null;
        } else {
            str = this.mDrawable.getPowerSaveEnabled() + "";
        }
        TextView textView = this.mBatteryPercentView;
        CharSequence text = textView != null ? textView.getText() : null;
        printWriter.println("  BatteryMeterView:");
        printWriter.println("    mDrawable.getPowerSave: " + str);
        printWriter.println("    mBatteryPercentView.getText(): " + ((Object) text));
        printWriter.println("    mTextColor: #" + Integer.toHexString(this.mTextColor));
        printWriter.println("    mLevel: " + this.mLevel);
    }

    private final class SettingObserver extends ContentObserver {
        public SettingObserver(Handler handler) {
            super(handler);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean z, Uri uri) {
            super.onChange(z, uri);
            BatteryMeterView.this.updateShowPercent();
            if (TextUtils.equals(uri.getLastPathSegment(), "battery_estimates_last_update_time")) {
                BatteryMeterView.this.updatePercentText();
            }
        }
    }

    public void addCallback(BatteryMeterViewCallbacks batteryMeterViewCallbacks) {
        this.mCallbacks.add(batteryMeterViewCallbacks);
    }

    public void removeCallback(BatteryMeterViewCallbacks batteryMeterViewCallbacks) {
        this.mCallbacks.remove(batteryMeterViewCallbacks);
    }
}
