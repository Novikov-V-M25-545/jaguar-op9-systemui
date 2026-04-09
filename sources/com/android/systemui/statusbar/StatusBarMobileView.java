package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settingslib.graph.SignalDrawable;
import com.android.systemui.DualToneHandler;
import com.android.systemui.R;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.phone.StatusBarSignalPolicy;

/* loaded from: classes.dex */
public class StatusBarMobileView extends FrameLayout implements DarkIconDispatcher.DarkReceiver, StatusIconDisplayable {
    private StatusBarIconView mDotView;
    private DualToneHandler mDualToneHandler;
    private ImageView mIn;
    private View mInoutContainer;
    private ImageView mMobile;
    private SignalDrawable mMobileDrawable;
    private LinearLayout mMobileGroup;
    private ImageView mMobileRoaming;
    private View mMobileRoamingSpace;
    private View mMobileSignalType;
    private ImageView mMobileType;
    private ImageView mMobileTypeSmall;
    private boolean mOldStyleType;
    private ImageView mOut;
    private String mSlot;
    private StatusBarSignalPolicy.MobileIconState mState;
    private int mVisibleState;
    private ImageView mVolte;

    public static StatusBarMobileView fromContext(Context context, String str) throws Resources.NotFoundException {
        StatusBarMobileView statusBarMobileView = (StatusBarMobileView) LayoutInflater.from(context).inflate(R.layout.status_bar_mobile_signal_group, (ViewGroup) null);
        statusBarMobileView.setSlot(str);
        statusBarMobileView.init();
        statusBarMobileView.setVisibleState(0);
        return statusBarMobileView;
    }

    public StatusBarMobileView(Context context) {
        super(context);
        this.mVisibleState = -1;
    }

    public StatusBarMobileView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mVisibleState = -1;
    }

    public StatusBarMobileView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mVisibleState = -1;
    }

    public StatusBarMobileView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mVisibleState = -1;
    }

    @Override // android.view.View
    public void getDrawingRect(Rect rect) {
        super.getDrawingRect(rect);
        float translationX = getTranslationX();
        float translationY = getTranslationY();
        rect.left = (int) (rect.left + translationX);
        rect.right = (int) (rect.right + translationX);
        rect.top = (int) (rect.top + translationY);
        rect.bottom = (int) (rect.bottom + translationY);
    }

    private void init() throws Resources.NotFoundException {
        this.mDualToneHandler = new DualToneHandler(getContext());
        this.mMobileGroup = (LinearLayout) findViewById(R.id.mobile_group);
        this.mMobile = (ImageView) findViewById(R.id.mobile_signal);
        this.mMobileType = (ImageView) findViewById(R.id.mobile_type);
        this.mMobileRoaming = (ImageView) findViewById(R.id.mobile_roaming);
        this.mMobileRoamingSpace = findViewById(R.id.mobile_roaming_space);
        this.mIn = (ImageView) findViewById(R.id.mobile_in);
        this.mOut = (ImageView) findViewById(R.id.mobile_out);
        this.mInoutContainer = findViewById(R.id.inout_container);
        this.mVolte = (ImageView) findViewById(R.id.mobile_volte);
        this.mMobileSignalType = findViewById(R.id.mobile_signal_type);
        this.mMobileTypeSmall = (ImageView) findViewById(R.id.mobile_type_small);
        SignalDrawable signalDrawable = new SignalDrawable(getContext());
        this.mMobileDrawable = signalDrawable;
        this.mMobile.setImageDrawable(signalDrawable);
        initDotView();
    }

    private void initDotView() throws Resources.NotFoundException {
        StatusBarIconView statusBarIconView = new StatusBarIconView(((FrameLayout) this).mContext, this.mSlot, null);
        this.mDotView = statusBarIconView;
        statusBarIconView.setVisibleState(1);
        int dimensionPixelSize = ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_icon_size);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize);
        layoutParams.gravity = 8388627;
        addView(this.mDotView, layoutParams);
    }

    public void applyMobileState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        if (mobileIconState == null) {
            zUpdateState = getVisibility() != 8;
            setVisibility(8);
            this.mState = null;
        } else {
            StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
            if (mobileIconState2 == null) {
                this.mState = mobileIconState.copy();
                initViewState();
            } else {
                zUpdateState = !mobileIconState2.equals(mobileIconState) ? updateState(mobileIconState.copy()) : false;
            }
        }
        if (zUpdateState) {
            requestLayout();
        }
    }

    /* JADX WARN: Removed duplicated region for block: B:20:0x0053  */
    /* JADX WARN: Removed duplicated region for block: B:23:0x0063  */
    /* JADX WARN: Removed duplicated region for block: B:24:0x0065  */
    /* JADX WARN: Removed duplicated region for block: B:30:0x0076  */
    /* JADX WARN: Removed duplicated region for block: B:33:0x0082  */
    /* JADX WARN: Removed duplicated region for block: B:34:0x0084  */
    /* JADX WARN: Removed duplicated region for block: B:37:0x0090  */
    /* JADX WARN: Removed duplicated region for block: B:38:0x0092  */
    /* JADX WARN: Removed duplicated region for block: B:45:0x00a5  */
    /* JADX WARN: Removed duplicated region for block: B:48:0x00af  */
    /* JADX WARN: Removed duplicated region for block: B:49:0x00ba  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    private void initViewState() {
        /*
            r5 = this;
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r0 = r5.mState
            java.lang.String r0 = r0.contentDescription
            r5.setContentDescription(r0)
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r0 = r5.mState
            boolean r0 = r0.visible
            r1 = 8
            r2 = 0
            if (r0 != 0) goto L16
            android.widget.LinearLayout r0 = r5.mMobileGroup
            r0.setVisibility(r1)
            goto L1b
        L16:
            android.widget.LinearLayout r0 = r5.mMobileGroup
            r0.setVisibility(r2)
        L1b:
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r0 = r5.mState
            int r0 = r0.strengthId
            if (r0 <= 0) goto L30
            android.widget.ImageView r0 = r5.mMobile
            r0.setVisibility(r2)
            com.android.settingslib.graph.SignalDrawable r0 = r5.mMobileDrawable
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r3 = r5.mState
            int r3 = r3.strengthId
            r0.setLevel(r3)
            goto L35
        L30:
            android.widget.ImageView r0 = r5.mMobile
            r0.setVisibility(r1)
        L35:
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r0 = r5.mState
            int r3 = r0.typeId
            r4 = 1
            if (r3 <= 0) goto L49
            boolean r3 = r5.mOldStyleType
            if (r3 == 0) goto L45
            r5.showOldStyle(r0)
            r0 = r4
            goto L4d
        L45:
            r5.showNewStyle(r0)
            goto L4c
        L49:
            r5.hideIndicators()
        L4c:
            r0 = r2
        L4d:
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r3 = r5.mState
            boolean r3 = r3.roaming
            if (r3 == 0) goto L5b
            android.widget.ImageView r3 = r5.mMobileTypeSmall
            r3.setVisibility(r1)
            r5.setMobileSignalWidth(r4)
        L5b:
            android.widget.ImageView r3 = r5.mMobileRoaming
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r4 = r5.mState
            boolean r4 = r4.roaming
            if (r4 == 0) goto L65
            r4 = r2
            goto L66
        L65:
            r4 = r1
        L66:
            r3.setVisibility(r4)
            android.view.View r3 = r5.mMobileRoamingSpace
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r4 = r5.mState
            boolean r4 = r4.roaming
            if (r4 != 0) goto L76
            if (r0 == 0) goto L74
            goto L76
        L74:
            r0 = r1
            goto L77
        L76:
            r0 = r2
        L77:
            r3.setVisibility(r0)
            android.widget.ImageView r0 = r5.mIn
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r3 = r5.mState
            boolean r3 = r3.activityIn
            if (r3 == 0) goto L84
            r3 = r2
            goto L85
        L84:
            r3 = r1
        L85:
            r0.setVisibility(r3)
            android.widget.ImageView r0 = r5.mOut
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r3 = r5.mState
            boolean r3 = r3.activityOut
            if (r3 == 0) goto L92
            r3 = r2
            goto L93
        L92:
            r3 = r1
        L93:
            r0.setVisibility(r3)
            android.view.View r0 = r5.mInoutContainer
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r3 = r5.mState
            boolean r4 = r3.activityIn
            if (r4 != 0) goto La5
            boolean r3 = r3.activityOut
            if (r3 == 0) goto La3
            goto La5
        La3:
            r3 = r1
            goto La6
        La5:
            r3 = r2
        La6:
            r0.setVisibility(r3)
            com.android.systemui.statusbar.phone.StatusBarSignalPolicy$MobileIconState r0 = r5.mState
            int r0 = r0.volteId
            if (r0 <= 0) goto Lba
            android.widget.ImageView r1 = r5.mVolte
            r1.setImageResource(r0)
            android.widget.ImageView r5 = r5.mVolte
            r5.setVisibility(r2)
            goto Lbf
        Lba:
            android.widget.ImageView r5 = r5.mVolte
            r5.setVisibility(r1)
        Lbf:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.StatusBarMobileView.initViewState():void");
    }

    private void setMobileSignalWidth(boolean z) {
        ViewGroup.LayoutParams layoutParams = this.mMobileSignalType.getLayoutParams();
        if (z) {
            layoutParams.width = ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_mobile_signal_width);
        } else {
            layoutParams.width = ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_mobile_signal_with_type_width);
            this.mMobileTypeSmall.setPadding(this.mMobileTypeSmall.getWidth() < ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_mobile_type_padding_limit) ? ((FrameLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_mobile_type_padding) : 0, 0, 0, 0);
        }
        this.mMobileSignalType.setLayoutParams(layoutParams);
    }

    private boolean updateState(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        boolean z;
        boolean z2;
        setContentDescription(mobileIconState.contentDescription);
        boolean z3 = this.mState.visible;
        boolean z4 = mobileIconState.visible;
        boolean z5 = true;
        if (z3 != z4) {
            this.mMobileGroup.setVisibility(z4 ? 0 : 8);
            z = true;
        } else {
            z = false;
        }
        int i = mobileIconState.strengthId;
        if (i > 0) {
            this.mMobileDrawable.setLevel(i);
            this.mMobile.setVisibility(0);
        } else {
            this.mMobile.setVisibility(8);
        }
        int i2 = this.mState.typeId;
        int i3 = mobileIconState.typeId;
        if (i2 == i3) {
            z2 = false;
        } else {
            z |= i3 == 0 || i2 == 0;
            if (i3 != 0) {
                if (this.mOldStyleType) {
                    showOldStyle(mobileIconState);
                    z2 = true;
                } else {
                    showNewStyle(mobileIconState);
                }
            } else {
                hideIndicators();
            }
            z2 = false;
        }
        if (mobileIconState.roaming) {
            this.mMobileTypeSmall.setVisibility(8);
            setMobileSignalWidth(true);
        }
        this.mMobileRoaming.setVisibility(mobileIconState.roaming ? 0 : 8);
        this.mMobileRoamingSpace.setVisibility((z2 || mobileIconState.roaming) ? 0 : 8);
        this.mIn.setVisibility(mobileIconState.activityIn ? 0 : 8);
        this.mOut.setVisibility(mobileIconState.activityOut ? 0 : 8);
        this.mInoutContainer.setVisibility((mobileIconState.activityIn || mobileIconState.activityOut) ? 0 : 8);
        int i4 = this.mState.volteId;
        int i5 = mobileIconState.volteId;
        if (i4 != i5) {
            if (i5 != 0) {
                this.mVolte.setImageResource(i5);
                this.mVolte.setVisibility(0);
            } else {
                this.mVolte.setVisibility(8);
            }
        }
        boolean z6 = mobileIconState.roaming;
        StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
        if (z6 == mobileIconState2.roaming && mobileIconState.activityIn == mobileIconState2.activityIn && mobileIconState.activityOut == mobileIconState2.activityOut) {
            z5 = false;
        }
        boolean z7 = z | z5;
        this.mState = mobileIconState;
        return z7;
    }

    @Override // com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver
    public void onDarkChanged(Rect rect, float f, int i) {
        if (!DarkIconDispatcher.isInArea(rect, this)) {
            f = 0.0f;
        }
        this.mMobileDrawable.setTintList(ColorStateList.valueOf(this.mDualToneHandler.getSingleColor(f)));
        ColorStateList colorStateListValueOf = ColorStateList.valueOf(DarkIconDispatcher.getTint(rect, this, i));
        this.mIn.setImageTintList(colorStateListValueOf);
        this.mOut.setImageTintList(colorStateListValueOf);
        this.mMobileType.setImageTintList(colorStateListValueOf);
        this.mMobileTypeSmall.setImageTintList(colorStateListValueOf);
        this.mMobileRoaming.setImageTintList(colorStateListValueOf);
        this.mVolte.setImageTintList(colorStateListValueOf);
        this.mDotView.setDecorColor(i);
        this.mDotView.setIconColor(i, false);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public String getSlot() {
        return this.mSlot;
    }

    public void setSlot(String str) {
        this.mSlot = str;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setStaticDrawableColor(int i) {
        ColorStateList colorStateListValueOf = ColorStateList.valueOf(i);
        this.mMobileDrawable.setTintList(ColorStateList.valueOf(this.mDualToneHandler.getSingleColor(i == -1 ? 0.0f : 1.0f)));
        this.mIn.setImageTintList(colorStateListValueOf);
        this.mOut.setImageTintList(colorStateListValueOf);
        this.mMobileType.setImageTintList(colorStateListValueOf);
        this.mMobileTypeSmall.setImageTintList(colorStateListValueOf);
        this.mMobileRoaming.setImageTintList(colorStateListValueOf);
        this.mVolte.setImageTintList(colorStateListValueOf);
        this.mDotView.setDecorColor(i);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setDecorColor(int i) {
        this.mDotView.setDecorColor(i);
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public boolean isIconVisible() {
        return this.mState.visible;
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public void setVisibleState(int i, boolean z) {
        if (i == this.mVisibleState) {
            return;
        }
        this.mVisibleState = i;
        if (i == 0) {
            this.mMobileGroup.setVisibility(0);
            this.mDotView.setVisibility(8);
        } else if (i == 1) {
            this.mMobileGroup.setVisibility(4);
            this.mDotView.setVisibility(0);
        } else {
            this.mMobileGroup.setVisibility(4);
            this.mDotView.setVisibility(4);
        }
    }

    @Override // com.android.systemui.statusbar.StatusIconDisplayable
    public int getVisibleState() {
        return this.mVisibleState;
    }

    @VisibleForTesting
    public StatusBarSignalPolicy.MobileIconState getState() {
        return this.mState;
    }

    @Override // android.view.View
    public String toString() {
        return "StatusBarMobileView(slot=" + this.mSlot + " state=" + this.mState + ")";
    }

    public void updateDisplayType(boolean z) {
        boolean z2;
        if (this.mOldStyleType != z) {
            StatusBarSignalPolicy.MobileIconState mobileIconState = this.mState;
            if (mobileIconState.typeId == 0) {
                hideIndicators();
            } else if (z) {
                showOldStyle(mobileIconState);
                z2 = true;
            } else {
                showNewStyle(mobileIconState);
            }
            z2 = false;
        } else {
            z2 = false;
        }
        if (this.mState.roaming) {
            this.mMobileTypeSmall.setVisibility(8);
            setMobileSignalWidth(true);
        }
        this.mMobileRoaming.setVisibility(this.mState.roaming ? 0 : 8);
        this.mMobileRoamingSpace.setVisibility((z2 || this.mState.roaming) ? 0 : 8);
        this.mIn.setVisibility(this.mState.activityIn ? 0 : 8);
        this.mOut.setVisibility(this.mState.activityOut ? 0 : 8);
        View view = this.mInoutContainer;
        StatusBarSignalPolicy.MobileIconState mobileIconState2 = this.mState;
        view.setVisibility((mobileIconState2.activityIn || mobileIconState2.activityOut) ? 0 : 8);
        boolean z3 = this.mOldStyleType != z;
        this.mOldStyleType = z;
        if (z3) {
            requestLayout();
        }
    }

    private void showOldStyle(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        this.mMobileType.setVisibility(8);
        this.mMobileTypeSmall.setContentDescription(mobileIconState.typeContentDescription);
        this.mMobileTypeSmall.setImageResource(mobileIconState.typeId);
        this.mMobileTypeSmall.setVisibility(0);
        setMobileSignalWidth(false);
    }

    private void showNewStyle(StatusBarSignalPolicy.MobileIconState mobileIconState) {
        this.mMobileType.setVisibility(0);
        this.mMobileType.setContentDescription(mobileIconState.typeContentDescription);
        this.mMobileType.setImageResource(mobileIconState.typeId);
        this.mMobileTypeSmall.setVisibility(8);
        setMobileSignalWidth(true);
    }

    private void hideIndicators() {
        this.mMobileType.setVisibility(8);
        this.mMobileTypeSmall.setVisibility(8);
        setMobileSignalWidth(true);
    }
}
