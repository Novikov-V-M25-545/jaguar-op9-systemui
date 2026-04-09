package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import java.util.function.Supplier;

/* loaded from: classes.dex */
public class MultiUserSwitch extends FrameLayout implements View.OnClickListener {
    private boolean mKeyguardMode;
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    protected QSPanel mQsPanel;
    private final int[] mTmpInt2;
    private UserSwitcherController.BaseUserAdapter mUserListener;
    final UserManager mUserManager;
    protected UserSwitcherController mUserSwitcherController;

    @Override // android.view.View
    public boolean hasOverlappingRendering() {
        return false;
    }

    public MultiUserSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mTmpInt2 = new int[2];
        this.mUserManager = UserManager.get(getContext());
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(this);
        refreshContentDescription();
    }

    public void setQsPanel(QSPanel qSPanel) {
        this.mQsPanel = qSPanel;
        setUserSwitcherController((UserSwitcherController) Dependency.get(UserSwitcherController.class));
    }

    public void setUserSwitcherController(UserSwitcherController userSwitcherController) {
        this.mUserSwitcherController = userSwitcherController;
        registerListener();
        refreshContentDescription();
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    public void setKeyguardMode(boolean z) {
        this.mKeyguardMode = z;
        registerListener();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$isMultiUserEnabled$0() {
        return Boolean.valueOf(this.mUserManager.isUserSwitcherEnabled(((FrameLayout) this).mContext.getResources().getBoolean(R.bool.qs_show_user_switcher_for_single_user)));
    }

    public boolean isMultiUserEnabled() {
        return ((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.statusbar.phone.MultiUserSwitch$$ExternalSyntheticLambda0
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$isMultiUserEnabled$0();
            }
        })).booleanValue();
    }

    private void registerListener() {
        UserSwitcherController userSwitcherController;
        if (this.mUserManager.isUserSwitcherEnabled() && this.mUserListener == null && (userSwitcherController = this.mUserSwitcherController) != null) {
            this.mUserListener = new UserSwitcherController.BaseUserAdapter(userSwitcherController) { // from class: com.android.systemui.statusbar.phone.MultiUserSwitch.1
                @Override // android.widget.Adapter
                public View getView(int i, View view, ViewGroup viewGroup) {
                    return null;
                }

                @Override // android.widget.BaseAdapter
                public void notifyDataSetChanged() {
                    MultiUserSwitch.this.refreshContentDescription();
                }
            };
            refreshContentDescription();
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View view) {
        if (this.mKeyguardMode) {
            KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
            if (keyguardUserSwitcher != null) {
                keyguardUserSwitcher.show(true);
                return;
            }
            return;
        }
        if (this.mQsPanel == null || this.mUserSwitcherController == null) {
            return;
        }
        View childAt = getChildCount() > 0 ? getChildAt(0) : this;
        childAt.getLocationInWindow(this.mTmpInt2);
        int[] iArr = this.mTmpInt2;
        iArr[0] = iArr[0] + (childAt.getWidth() / 2);
        int[] iArr2 = this.mTmpInt2;
        iArr2[1] = iArr2[1] + (childAt.getHeight() / 2);
        this.mQsPanel.showDetailAdapter(true, getUserDetailAdapter(), this.mTmpInt2);
    }

    @Override // android.view.View
    public void setClickable(boolean z) {
        super.setClickable(z);
        refreshContentDescription();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Boolean lambda$refreshContentDescription$1() {
        return Boolean.valueOf(this.mUserManager.isUserSwitcherEnabled());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void refreshContentDescription() {
        UserSwitcherController userSwitcherController;
        String currentUserName = (!((Boolean) DejankUtils.whitelistIpcs(new Supplier() { // from class: com.android.systemui.statusbar.phone.MultiUserSwitch$$ExternalSyntheticLambda1
            @Override // java.util.function.Supplier
            public final Object get() {
                return this.f$0.lambda$refreshContentDescription$1();
            }
        })).booleanValue() || (userSwitcherController = this.mUserSwitcherController) == null) ? null : userSwitcherController.getCurrentUserName(((FrameLayout) this).mContext);
        String string = TextUtils.isEmpty(currentUserName) ? null : ((FrameLayout) this).mContext.getString(R.string.accessibility_quick_settings_user, currentUserName);
        if (TextUtils.equals(getContentDescription(), string)) {
            return;
        }
        setContentDescription(string);
    }

    @Override // android.view.View
    public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEvent(accessibilityEvent);
        accessibilityEvent.setClassName(Button.class.getName());
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.setClassName(Button.class.getName());
    }

    protected DetailAdapter getUserDetailAdapter() {
        return this.mUserSwitcherController.userDetailAdapter;
    }
}
