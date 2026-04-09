package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.ArraySet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.DemoMode;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.StatusBarMobileView;
import com.android.systemui.statusbar.StatusBarWifiView;
import com.android.systemui.statusbar.StatusIconDisplayable;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;
import com.android.systemui.statusbar.policy.StatusBarNetworkTraffic;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Utils;
import java.util.List;

/* loaded from: classes.dex */
public interface StatusBarIconController {
    void addIconGroup(IconManager iconManager);

    void removeAllIconsForSlot(String str);

    void removeIconGroup(IconManager iconManager);

    void setExternalIcon(String str);

    void setIcon(String str, int i, CharSequence charSequence);

    void setIcon(String str, StatusBarIcon statusBarIcon);

    void setIconAccessibilityLiveRegion(String str, int i);

    void setIconVisibility(String str, boolean z);

    void setMobileIcons(String str, List<StatusBarSignalPolicy.MobileIconState> list);

    void setSignalIcon(String str, StatusBarSignalPolicy.WifiIconState wifiIconState);

    static ArraySet<String> getIconBlacklist(Context context, String str) {
        String[] strArrSplit;
        ArraySet<String> arraySet = new ArraySet<>();
        if (str == null) {
            strArrSplit = context.getResources().getStringArray(R.array.config_statusBarIconBlackList);
        } else {
            strArrSplit = str.split(",");
        }
        for (String str2 : strArrSplit) {
            if (!TextUtils.isEmpty(str2)) {
                arraySet.add(str2);
            }
        }
        return arraySet;
    }

    public static class DarkIconManager extends IconManager {
        private final DarkIconDispatcher mDarkIconDispatcher;
        private int mIconHPadding;

        public DarkIconManager(LinearLayout linearLayout, CommandQueue commandQueue) {
            super(linearLayout, commandQueue);
            this.mIconHPadding = this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_padding);
            this.mDarkIconDispatcher = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        protected void onIconAdded(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
            this.mDarkIconDispatcher.addDarkReceiver(addHolder(i, str, z, statusBarIconHolder));
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        protected LinearLayout.LayoutParams onCreateLayoutParams() {
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, this.mIconSize);
            int i = this.mIconHPadding;
            layoutParams.setMargins(i, 0, i, 0);
            return layoutParams;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        protected void destroy() {
            for (int i = 0; i < this.mGroup.getChildCount(); i++) {
                this.mDarkIconDispatcher.removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mGroup.getChildAt(i));
            }
            this.mGroup.removeAllViews();
            ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        protected void onRemoveIcon(int i) {
            this.mDarkIconDispatcher.removeDarkReceiver((DarkIconDispatcher.DarkReceiver) this.mGroup.getChildAt(i));
            super.onRemoveIcon(i);
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        public void onSetIcon(int i, StatusBarIcon statusBarIcon) throws Resources.NotFoundException {
            super.onSetIcon(i, statusBarIcon);
            this.mDarkIconDispatcher.applyDark((DarkIconDispatcher.DarkReceiver) this.mGroup.getChildAt(i));
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        protected DemoStatusIcons createDemoStatusIcons() {
            DemoStatusIcons demoStatusIconsCreateDemoStatusIcons = super.createDemoStatusIcons();
            this.mDarkIconDispatcher.addDarkReceiver(demoStatusIconsCreateDemoStatusIcons);
            return demoStatusIconsCreateDemoStatusIcons;
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        protected void exitDemoMode() {
            this.mDarkIconDispatcher.removeDarkReceiver(this.mDemoStatusIcons);
            super.exitDemoMode();
        }
    }

    public static class TintedIconManager extends IconManager {
        private int mColor;

        public TintedIconManager(ViewGroup viewGroup, CommandQueue commandQueue) {
            super(viewGroup, commandQueue);
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        protected void onIconAdded(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
            StatusIconDisplayable statusIconDisplayableAddHolder = addHolder(i, str, z, statusBarIconHolder);
            statusIconDisplayableAddHolder.setStaticDrawableColor(this.mColor);
            statusIconDisplayableAddHolder.setDecorColor(this.mColor);
        }

        public void setTint(int i) {
            this.mColor = i;
            for (int i2 = 0; i2 < this.mGroup.getChildCount(); i2++) {
                KeyEvent.Callback childAt = this.mGroup.getChildAt(i2);
                if (childAt instanceof StatusIconDisplayable) {
                    StatusIconDisplayable statusIconDisplayable = (StatusIconDisplayable) childAt;
                    statusIconDisplayable.setStaticDrawableColor(this.mColor);
                    statusIconDisplayable.setDecorColor(this.mColor);
                }
            }
        }

        @Override // com.android.systemui.statusbar.phone.StatusBarIconController.IconManager
        protected DemoStatusIcons createDemoStatusIcons() {
            DemoStatusIcons demoStatusIconsCreateDemoStatusIcons = super.createDemoStatusIcons();
            demoStatusIconsCreateDemoStatusIcons.setColor(this.mColor);
            return demoStatusIconsCreateDemoStatusIcons;
        }
    }

    public static class IconManager implements DemoMode, TunerService.Tunable {
        private boolean mConfigUseOldMobileType;
        protected final Context mContext;
        protected DemoStatusIcons mDemoStatusIcons;
        protected final ViewGroup mGroup;
        protected final int mIconSize;
        private boolean mIsInDemoMode;
        private boolean mOldStyleType;
        protected boolean mShouldLog = false;
        protected boolean mDemoable = true;

        public IconManager(ViewGroup viewGroup, CommandQueue commandQueue) {
            this.mGroup = viewGroup;
            Context context = viewGroup.getContext();
            this.mContext = context;
            this.mIconSize = context.getResources().getDimensionPixelSize(android.R.dimen.notification_custom_view_max_image_width_low_ram);
            this.mConfigUseOldMobileType = context.getResources().getBoolean(android.R.bool.config_cecSoundbarModeEnabled_default);
            Utils.DisableStateTracker disableStateTracker = new Utils.DisableStateTracker(0, 2, commandQueue);
            viewGroup.addOnAttachStateChangeListener(disableStateTracker);
            if (viewGroup.isAttachedToWindow()) {
                disableStateTracker.onViewAttachedToWindow(viewGroup);
            }
        }

        public boolean isDemoable() {
            return this.mDemoable;
        }

        public void setShouldLog(boolean z) {
            this.mShouldLog = z;
        }

        public boolean shouldLog() {
            return this.mShouldLog;
        }

        protected void onIconAdded(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
            addHolder(i, str, z, statusBarIconHolder);
        }

        protected StatusIconDisplayable addHolder(int i, String str, boolean z, StatusBarIconHolder statusBarIconHolder) {
            int type = statusBarIconHolder.getType();
            if (type == 0) {
                return addIcon(i, str, z, statusBarIconHolder.getIcon());
            }
            if (type == 1) {
                return addSignalIcon(i, str, statusBarIconHolder.getWifiState());
            }
            if (type == 2) {
                return addMobileIcon(i, str, statusBarIconHolder.getMobileState());
            }
            if (type != 42) {
                return null;
            }
            return addNetworkTraffic(i, str);
        }

        protected StatusBarIconView addIcon(int i, String str, boolean z, StatusBarIcon statusBarIcon) throws Resources.NotFoundException {
            StatusBarIconView statusBarIconViewOnCreateStatusBarIconView = onCreateStatusBarIconView(str, z);
            statusBarIconViewOnCreateStatusBarIconView.set(statusBarIcon);
            this.mGroup.addView(statusBarIconViewOnCreateStatusBarIconView, i, onCreateLayoutParams());
            return statusBarIconViewOnCreateStatusBarIconView;
        }

        protected StatusBarWifiView addSignalIcon(int i, String str, StatusBarSignalPolicy.WifiIconState wifiIconState) throws Resources.NotFoundException {
            StatusBarWifiView statusBarWifiViewOnCreateStatusBarWifiView = onCreateStatusBarWifiView(str);
            statusBarWifiViewOnCreateStatusBarWifiView.applyWifiState(wifiIconState);
            this.mGroup.addView(statusBarWifiViewOnCreateStatusBarWifiView, i, onCreateLayoutParams());
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.addDemoWifiView(wifiIconState);
            }
            return statusBarWifiViewOnCreateStatusBarWifiView;
        }

        protected StatusBarNetworkTraffic addNetworkTraffic(int i, String str) {
            StatusBarNetworkTraffic statusBarNetworkTrafficOnCreateNetworkTraffic = onCreateNetworkTraffic(str);
            this.mGroup.addView(statusBarNetworkTrafficOnCreateNetworkTraffic, i, onCreateLayoutParams());
            return statusBarNetworkTrafficOnCreateNetworkTraffic;
        }

        protected StatusBarMobileView addMobileIcon(int i, String str, StatusBarSignalPolicy.MobileIconState mobileIconState) throws Resources.NotFoundException {
            StatusBarMobileView statusBarMobileViewOnCreateStatusBarMobileView = onCreateStatusBarMobileView(str);
            statusBarMobileViewOnCreateStatusBarMobileView.applyMobileState(mobileIconState);
            this.mGroup.addView(statusBarMobileViewOnCreateStatusBarMobileView, i, onCreateLayoutParams());
            ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:use_old_mobiletype");
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.addMobileView(mobileIconState);
            }
            return statusBarMobileViewOnCreateStatusBarMobileView;
        }

        private StatusBarIconView onCreateStatusBarIconView(String str, boolean z) {
            return new StatusBarIconView(this.mContext, str, null, z);
        }

        private StatusBarWifiView onCreateStatusBarWifiView(String str) {
            return StatusBarWifiView.fromContext(this.mContext, str);
        }

        private StatusBarMobileView onCreateStatusBarMobileView(String str) {
            return StatusBarMobileView.fromContext(this.mContext, str);
        }

        private StatusBarNetworkTraffic onCreateNetworkTraffic(String str) {
            return new StatusBarNetworkTraffic(this.mContext);
        }

        protected LinearLayout.LayoutParams onCreateLayoutParams() {
            return new LinearLayout.LayoutParams(-2, this.mIconSize);
        }

        protected void destroy() {
            this.mGroup.removeAllViews();
            ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        }

        protected void onIconExternal(int i, int i2) {
            ImageView imageView = (ImageView) this.mGroup.getChildAt(i);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setAdjustViewBounds(true);
            setHeightAndCenter(imageView, i2);
        }

        private void setHeightAndCenter(ImageView imageView, int i) {
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            layoutParams.height = i;
            if (layoutParams instanceof LinearLayout.LayoutParams) {
                ((LinearLayout.LayoutParams) layoutParams).gravity = 16;
            }
            imageView.setLayoutParams(layoutParams);
        }

        protected void onRemoveIcon(int i) {
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.onRemoveIcon((StatusIconDisplayable) this.mGroup.getChildAt(i));
            }
            this.mGroup.removeViewAt(i);
        }

        public void onSetIcon(int i, StatusBarIcon statusBarIcon) throws Resources.NotFoundException {
            View childAt = this.mGroup.getChildAt(i);
            if (childAt instanceof StatusBarIconView) {
                ((StatusBarIconView) childAt).set(statusBarIcon);
            }
        }

        public void onSetIconHolder(int i, StatusBarIconHolder statusBarIconHolder) throws Resources.NotFoundException {
            int type = statusBarIconHolder.getType();
            if (type == 0) {
                onSetIcon(i, statusBarIconHolder.getIcon());
            } else if (type == 1) {
                onSetSignalIcon(i, statusBarIconHolder.getWifiState());
            } else {
                if (type != 2) {
                    return;
                }
                onSetMobileIcon(i, statusBarIconHolder.getMobileState());
            }
        }

        public void onSetSignalIcon(int i, StatusBarSignalPolicy.WifiIconState wifiIconState) throws Resources.NotFoundException {
            StatusBarWifiView statusBarWifiView = (StatusBarWifiView) this.mGroup.getChildAt(i);
            if (statusBarWifiView != null) {
                statusBarWifiView.applyWifiState(wifiIconState);
            }
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.updateWifiState(wifiIconState);
            }
        }

        public void onSetMobileIcon(int i, StatusBarSignalPolicy.MobileIconState mobileIconState) throws Resources.NotFoundException {
            StatusBarMobileView statusBarMobileView = (StatusBarMobileView) this.mGroup.getChildAt(i);
            if (statusBarMobileView != null) {
                statusBarMobileView.applyMobileState(mobileIconState);
            }
            if (this.mIsInDemoMode) {
                this.mDemoStatusIcons.updateMobileState(mobileIconState);
            }
        }

        @Override // com.android.systemui.DemoMode
        public void dispatchDemoCommand(String str, Bundle bundle) throws Resources.NotFoundException {
            if (this.mDemoable) {
                if (str.equals("exit")) {
                    DemoStatusIcons demoStatusIcons = this.mDemoStatusIcons;
                    if (demoStatusIcons != null) {
                        demoStatusIcons.dispatchDemoCommand(str, bundle);
                        exitDemoMode();
                    }
                    this.mIsInDemoMode = false;
                    return;
                }
                if (this.mDemoStatusIcons == null) {
                    this.mIsInDemoMode = true;
                    this.mDemoStatusIcons = createDemoStatusIcons();
                }
                this.mDemoStatusIcons.dispatchDemoCommand(str, bundle);
            }
        }

        protected void exitDemoMode() {
            this.mDemoStatusIcons.remove();
            this.mDemoStatusIcons = null;
        }

        protected DemoStatusIcons createDemoStatusIcons() {
            return new DemoStatusIcons((LinearLayout) this.mGroup, this.mIconSize);
        }

        @Override // com.android.systemui.tuner.TunerService.Tunable
        public void onTuningChanged(String str, String str2) {
            str.hashCode();
            if (str.equals("system:use_old_mobiletype")) {
                this.mOldStyleType = TunerService.parseIntegerSwitch(str2, this.mConfigUseOldMobileType);
                updateOldStyleMobileDataIcons();
            }
        }

        private void updateOldStyleMobileDataIcons() {
            for (int i = 0; i < this.mGroup.getChildCount(); i++) {
                View childAt = this.mGroup.getChildAt(i);
                if (childAt instanceof StatusBarMobileView) {
                    ((StatusBarMobileView) childAt).updateDisplayType(this.mOldStyleType);
                }
            }
        }
    }
}
