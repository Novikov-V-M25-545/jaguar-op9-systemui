package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.ReverseLinearLayout;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.tuner.TunerService;
import java.io.PrintWriter;
import java.util.Objects;

/* loaded from: classes.dex */
public class NavigationBarInflaterView extends FrameLayout implements NavigationModeController.ModeChangedListener, TunerService.Tunable {
    private boolean mAlternativeOrder;

    @VisibleForTesting
    SparseArray<ButtonDispatcher> mButtonDispatchers;
    private String mCurrentLayout;
    protected FrameLayout mHorizontal;
    private boolean mInverseLayout;
    private boolean mIsVertical;
    protected LayoutInflater mLandscapeInflater;
    private View mLastLandscape;
    private View mLastPortrait;
    protected LayoutInflater mLayoutInflater;
    private int mNavBarMode;
    private OverviewProxyService mOverviewProxyService;
    private boolean mUsingCustomLayout;
    protected FrameLayout mVertical;

    public NavigationBarInflaterView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mNavBarMode = 0;
        createInflaters();
        this.mOverviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        this.mNavBarMode = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
    }

    @VisibleForTesting
    void createInflaters() {
        this.mLayoutInflater = LayoutInflater.from(((FrameLayout) this).mContext);
        Configuration configuration = new Configuration();
        configuration.setTo(((FrameLayout) this).mContext.getResources().getConfiguration());
        configuration.orientation = 2;
        this.mLandscapeInflater = LayoutInflater.from(((FrameLayout) this).mContext.createConfigurationContext(configuration));
    }

    @Override // android.view.View
    protected void onFinishInflate() throws NumberFormatException {
        super.onFinishInflate();
        inflateChildren();
        clearViews();
        inflateLayout(getDefaultLayout());
    }

    private void inflateChildren() {
        removeAllViews();
        FrameLayout frameLayout = (FrameLayout) this.mLayoutInflater.inflate(R.layout.navigation_layout, (ViewGroup) this, false);
        this.mHorizontal = frameLayout;
        addView(frameLayout);
        FrameLayout frameLayout2 = (FrameLayout) this.mLayoutInflater.inflate(R.layout.navigation_layout_vertical, (ViewGroup) this, false);
        this.mVertical = frameLayout2;
        addView(frameLayout2);
        updateAlternativeOrder();
    }

    protected String getDefaultLayout() {
        int i;
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            i = R.string.config_navBarLayoutHandle;
        } else if (this.mOverviewProxyService.shouldShowSwipeUpUI()) {
            i = R.string.config_navBarLayoutQuickstep;
        } else {
            i = R.string.config_navBarLayout;
        }
        return getContext().getString(i);
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) throws NumberFormatException {
        this.mNavBarMode = i;
        onLikelyDefaultLayoutChange();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "navbar_layout_views");
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "navbar_inverse_layout");
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        ((NavigationModeController) Dependency.get(NavigationModeController.class)).removeListener(this);
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        super.onDetachedFromWindow();
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) throws NumberFormatException {
        str.hashCode();
        if (str.equals("navbar_inverse_layout")) {
            this.mInverseLayout = TunerService.parseIntegerSwitch(str2, false);
            updateLayoutInversion();
        } else if (str.equals("navbar_layout_views") && str2 != null) {
            setNavigationBarLayout(str2);
        }
    }

    @Override // android.view.View
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateLayoutInversion();
    }

    public void setNavigationBarLayout(String str) throws NumberFormatException {
        if (Objects.equals(this.mCurrentLayout, str)) {
            return;
        }
        this.mUsingCustomLayout = str != null;
        clearViews();
        inflateLayout(str);
    }

    public void onLikelyDefaultLayoutChange() throws NumberFormatException {
        if (this.mUsingCustomLayout) {
            return;
        }
        String defaultLayout = getDefaultLayout();
        if (Objects.equals(this.mCurrentLayout, defaultLayout)) {
            return;
        }
        clearViews();
        inflateLayout(defaultLayout);
    }

    public void setButtonDispatchers(SparseArray<ButtonDispatcher> sparseArray) {
        this.mButtonDispatchers = sparseArray;
        for (int i = 0; i < sparseArray.size(); i++) {
            initiallyFill(sparseArray.valueAt(i));
        }
    }

    void updateButtonDispatchersCurrentView() {
        if (this.mButtonDispatchers != null) {
            FrameLayout frameLayout = this.mIsVertical ? this.mVertical : this.mHorizontal;
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                this.mButtonDispatchers.valueAt(i).setCurrentView(frameLayout);
            }
        }
    }

    void setVertical(boolean z) {
        if (z != this.mIsVertical) {
            this.mIsVertical = z;
        }
    }

    void setAlternativeOrder(boolean z) {
        if (z != this.mAlternativeOrder) {
            this.mAlternativeOrder = z;
            updateAlternativeOrder();
        }
    }

    private void updateAlternativeOrder() {
        FrameLayout frameLayout = this.mHorizontal;
        int i = R.id.ends_group;
        updateAlternativeOrder(frameLayout.findViewById(i));
        FrameLayout frameLayout2 = this.mHorizontal;
        int i2 = R.id.center_group;
        updateAlternativeOrder(frameLayout2.findViewById(i2));
        updateAlternativeOrder(this.mVertical.findViewById(i));
        updateAlternativeOrder(this.mVertical.findViewById(i2));
    }

    private void updateAlternativeOrder(View view) {
        if (view instanceof ReverseLinearLayout) {
            ((ReverseLinearLayout) view).setAlternativeOrder(this.mAlternativeOrder);
        }
    }

    private void initiallyFill(ButtonDispatcher buttonDispatcher) {
        FrameLayout frameLayout = this.mHorizontal;
        int i = R.id.ends_group;
        addAll(buttonDispatcher, (ViewGroup) frameLayout.findViewById(i));
        FrameLayout frameLayout2 = this.mHorizontal;
        int i2 = R.id.center_group;
        addAll(buttonDispatcher, (ViewGroup) frameLayout2.findViewById(i2));
        FrameLayout frameLayout3 = this.mHorizontal;
        int i3 = R.id.dpad_group;
        addAll(buttonDispatcher, (ViewGroup) frameLayout3.findViewById(i3));
        addAll(buttonDispatcher, (ViewGroup) this.mVertical.findViewById(i));
        addAll(buttonDispatcher, (ViewGroup) this.mVertical.findViewById(i2));
        addAll(buttonDispatcher, (ViewGroup) this.mVertical.findViewById(i3));
    }

    private void addAll(ButtonDispatcher buttonDispatcher, ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            if (viewGroup.getChildAt(i).getId() == buttonDispatcher.getId()) {
                buttonDispatcher.addView(viewGroup.getChildAt(i));
            }
            if (viewGroup.getChildAt(i) instanceof ViewGroup) {
                addAll(buttonDispatcher, (ViewGroup) viewGroup.getChildAt(i));
            }
        }
    }

    protected void inflateLayout(String str) throws NumberFormatException {
        this.mCurrentLayout = str;
        if (str == null) {
            str = getDefaultLayout();
        }
        String[] strArrSplit = str.split(";", 3);
        if (strArrSplit.length != 3) {
            Log.d("NavBarInflater", "Invalid layout.");
            strArrSplit = getDefaultLayout().split(";", 3);
        }
        String[] strArrSplit2 = strArrSplit[0].split(",");
        String[] strArrSplit3 = strArrSplit[1].split(",");
        String[] strArrSplit4 = strArrSplit[2].split(",");
        FrameLayout frameLayout = this.mHorizontal;
        int i = R.id.ends_group;
        inflateButtons(strArrSplit2, (ViewGroup) frameLayout.findViewById(i), false, true);
        inflateButtons(strArrSplit2, (ViewGroup) this.mVertical.findViewById(i), true, true);
        FrameLayout frameLayout2 = this.mHorizontal;
        int i2 = R.id.center_group;
        inflateButtons(strArrSplit3, (ViewGroup) frameLayout2.findViewById(i2), false, false);
        inflateButtons(strArrSplit3, (ViewGroup) this.mVertical.findViewById(i2), true, false);
        addGravitySpacer((LinearLayout) this.mHorizontal.findViewById(i));
        addGravitySpacer((LinearLayout) this.mVertical.findViewById(i));
        inflateButtons(strArrSplit4, (ViewGroup) this.mHorizontal.findViewById(i), false, false);
        inflateButtons(strArrSplit4, (ViewGroup) this.mVertical.findViewById(i), true, false);
        FrameLayout frameLayout3 = this.mHorizontal;
        int i3 = R.id.dpad_group;
        inflateCursorButtons((ViewGroup) frameLayout3.findViewById(i3), false);
        inflateCursorButtons((ViewGroup) this.mVertical.findViewById(i3), true);
        updateButtonDispatchersCurrentView();
    }

    private void updateLayoutInversion() {
        if (this.mInverseLayout) {
            if (((FrameLayout) this).mContext.getResources().getConfiguration().getLayoutDirection() == 1) {
                setLayoutDirection(0);
                return;
            } else {
                setLayoutDirection(1);
                return;
            }
        }
        setLayoutDirection(2);
    }

    private void addGravitySpacer(LinearLayout linearLayout) {
        linearLayout.addView(new Space(((FrameLayout) this).mContext), new LinearLayout.LayoutParams(0, 0, 1.0f));
    }

    private void inflateButtons(String[] strArr, ViewGroup viewGroup, boolean z, boolean z2) throws NumberFormatException {
        for (String str : strArr) {
            inflateButton(str, viewGroup, z, z2);
        }
    }

    private void inflateCursorButtons(ViewGroup viewGroup, boolean z) {
        (z ? this.mLandscapeInflater : this.mLayoutInflater).inflate(z ? R.layout.nav_buttons_dpad_group_vertical : R.layout.nav_buttons_dpad_group, viewGroup);
        addToDispatchers(viewGroup);
    }

    protected View inflateButton(String str, ViewGroup viewGroup, boolean z, boolean z2) throws NumberFormatException {
        View viewCreateView = createView(str, viewGroup, z ? this.mLandscapeInflater : this.mLayoutInflater);
        if (viewCreateView == null) {
            return null;
        }
        View viewApplySize = applySize(viewCreateView, str, z, z2);
        viewGroup.addView(viewApplySize);
        addToDispatchers(viewApplySize);
        View view = z ? this.mLastLandscape : this.mLastPortrait;
        View childAt = viewApplySize instanceof ReverseLinearLayout.ReverseRelativeLayout ? ((ReverseLinearLayout.ReverseRelativeLayout) viewApplySize).getChildAt(0) : viewApplySize;
        if (view != null) {
            childAt.setAccessibilityTraversalAfter(view.getId());
        }
        if (z) {
            this.mLastLandscape = childAt;
        } else {
            this.mLastPortrait = childAt;
        }
        return viewApplySize;
    }

    private View applySize(View view, String str, boolean z, boolean z2) throws NumberFormatException {
        String strExtractSize = extractSize(str);
        if (strExtractSize == null) {
            return view;
        }
        if (strExtractSize.contains("W") || strExtractSize.contains("A")) {
            ReverseLinearLayout.ReverseRelativeLayout reverseRelativeLayout = new ReverseLinearLayout.ReverseRelativeLayout(((FrameLayout) this).mContext);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(view.getLayoutParams());
            int i = z ? z2 ? 48 : 80 : z2 ? 8388611 : 8388613;
            if (strExtractSize.endsWith("WC")) {
                i = 17;
            } else if (strExtractSize.endsWith("C")) {
                i = 16;
            }
            reverseRelativeLayout.setDefaultGravity(i);
            reverseRelativeLayout.setGravity(i);
            reverseRelativeLayout.addView(view, layoutParams);
            if (strExtractSize.contains("W")) {
                reverseRelativeLayout.setLayoutParams(new LinearLayout.LayoutParams(0, -1, Float.parseFloat(strExtractSize.substring(0, strExtractSize.indexOf("W")))));
            } else {
                reverseRelativeLayout.setLayoutParams(new LinearLayout.LayoutParams((int) convertDpToPx(((FrameLayout) this).mContext, Float.parseFloat(strExtractSize.substring(0, strExtractSize.indexOf("A")))), -1));
            }
            reverseRelativeLayout.setClipChildren(false);
            reverseRelativeLayout.setClipToPadding(false);
            return reverseRelativeLayout;
        }
        float f = Float.parseFloat(strExtractSize);
        view.getLayoutParams().width = (int) (r7.width * f);
        return view;
    }

    private View createView(String str, ViewGroup viewGroup, LayoutInflater layoutInflater) {
        String strExtractButton = extractButton(str);
        if ("left".equals(strExtractButton)) {
            strExtractButton = extractButton("space");
        } else if ("right".equals(strExtractButton)) {
            strExtractButton = extractButton("menu_ime");
        }
        if ("home".equals(strExtractButton)) {
            return layoutInflater.inflate(R.layout.home, viewGroup, false);
        }
        if ("back".equals(strExtractButton)) {
            return layoutInflater.inflate(R.layout.back, viewGroup, false);
        }
        if ("recent".equals(strExtractButton)) {
            return layoutInflater.inflate(R.layout.recent_apps, viewGroup, false);
        }
        if ("menu_ime".equals(strExtractButton)) {
            return layoutInflater.inflate(R.layout.menu_ime, viewGroup, false);
        }
        if ("space".equals(strExtractButton)) {
            return layoutInflater.inflate(R.layout.nav_key_space, viewGroup, false);
        }
        if ("clipboard".equals(strExtractButton)) {
            return layoutInflater.inflate(R.layout.clipboard, viewGroup, false);
        }
        if ("contextual".equals(strExtractButton)) {
            return layoutInflater.inflate(R.layout.contextual, viewGroup, false);
        }
        if ("home_handle".equals(strExtractButton)) {
            return layoutInflater.inflate(R.layout.home_handle, viewGroup, false);
        }
        if ("ime_switcher".equals(strExtractButton)) {
            return layoutInflater.inflate(R.layout.ime_switcher, viewGroup, false);
        }
        if (!strExtractButton.startsWith("key")) {
            return null;
        }
        String strExtractImage = extractImage(strExtractButton);
        int iExtractKeycode = extractKeycode(strExtractButton);
        View viewInflate = layoutInflater.inflate(R.layout.custom_key, viewGroup, false);
        KeyButtonView keyButtonView = (KeyButtonView) viewInflate;
        keyButtonView.setCode(iExtractKeycode);
        if (strExtractImage != null) {
            if (strExtractImage.contains(":")) {
                keyButtonView.loadAsync(Icon.createWithContentUri(strExtractImage));
            } else if (strExtractImage.contains("/")) {
                int iIndexOf = strExtractImage.indexOf(47);
                keyButtonView.loadAsync(Icon.createWithResource(strExtractImage.substring(0, iIndexOf), Integer.parseInt(strExtractImage.substring(iIndexOf + 1))));
            }
        }
        return viewInflate;
    }

    public static String extractImage(String str) {
        if (str.contains(":")) {
            return str.substring(str.indexOf(":") + 1, str.indexOf(")"));
        }
        return null;
    }

    public static int extractKeycode(String str) {
        if (str.contains("(")) {
            return Integer.parseInt(str.substring(str.indexOf("(") + 1, str.indexOf(":")));
        }
        return 1;
    }

    public static String extractSize(String str) {
        if (str.contains("[")) {
            return str.substring(str.indexOf("[") + 1, str.indexOf("]"));
        }
        return null;
    }

    public static String extractButton(String str) {
        return !str.contains("[") ? str : str.substring(0, str.indexOf("["));
    }

    private void addToDispatchers(View view) {
        SparseArray<ButtonDispatcher> sparseArray = this.mButtonDispatchers;
        if (sparseArray != null) {
            int iIndexOfKey = sparseArray.indexOfKey(view.getId());
            if (iIndexOfKey >= 0) {
                this.mButtonDispatchers.valueAt(iIndexOfKey).addView(view);
            }
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                int childCount = viewGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    addToDispatchers(viewGroup.getChildAt(i));
                }
            }
        }
    }

    private void clearViews() {
        if (this.mButtonDispatchers != null) {
            for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
                this.mButtonDispatchers.valueAt(i).clear();
            }
        }
        FrameLayout frameLayout = this.mHorizontal;
        int i2 = R.id.nav_buttons;
        clearAllChildren((ViewGroup) frameLayout.findViewById(i2));
        clearAllChildren((ViewGroup) this.mVertical.findViewById(i2));
    }

    private void clearAllChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            ((ViewGroup) viewGroup.getChildAt(i)).removeAllViews();
        }
    }

    private static float convertDpToPx(Context context, float f) {
        return f * context.getResources().getDisplayMetrics().density;
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("NavigationBarInflaterView {");
        printWriter.println("      mCurrentLayout: " + this.mCurrentLayout);
        printWriter.println("    }");
    }
}
