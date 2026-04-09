package com.android.systemui.qs.customize;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toolbar;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.keyguard.ScreenLifecycle;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSDetailClipper;
import com.android.systemui.qs.QSEditEvent;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;
import java.util.Iterator;

/* loaded from: classes.dex */
public class QSCustomizer extends LinearLayout implements Toolbar.OnMenuItemClickListener, TunerService.Tunable {
    private boolean isShown;
    private final QSDetailClipper mClipper;
    private final Animator.AnimatorListener mCollapseAnimationListener;
    private boolean mCustomizing;
    private final Animator.AnimatorListener mExpandAnimationListener;
    private boolean mHeaderImageEnabled;
    private QSTileHost mHost;
    private boolean mIsShowingNavBackdrop;
    private final KeyguardStateController.Callback mKeyguardCallback;
    private KeyguardStateController mKeyguardStateController;
    private final LightBarController mLightBarController;
    private NotificationsQuickSettingsContainer mNotifQsContainer;
    private boolean mOpening;
    private QS mQs;
    private RecyclerView mRecyclerView;
    private final ScreenLifecycle mScreenLifecycle;
    private TileAdapter mTileAdapter;
    private final TileQueryHelper mTileQueryHelper;
    private Toolbar mToolbar;
    private final View mTransparentView;
    private UiEventLogger mUiEventLogger;
    private int mX;
    private int mY;

    public QSCustomizer(Context context, AttributeSet attributeSet, LightBarController lightBarController, KeyguardStateController keyguardStateController, ScreenLifecycle screenLifecycle, TileQueryHelper tileQueryHelper, UiEventLogger uiEventLogger) throws Resources.NotFoundException {
        super(new ContextThemeWrapper(context, R.style.edit_theme), attributeSet);
        this.mUiEventLogger = new UiEventLoggerImpl();
        this.mKeyguardCallback = new KeyguardStateController.Callback() { // from class: com.android.systemui.qs.customize.QSCustomizer.4
            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardShowingChanged() {
                if (QSCustomizer.this.isAttachedToWindow() && QSCustomizer.this.mKeyguardStateController.isShowing() && !QSCustomizer.this.mOpening) {
                    QSCustomizer.this.hide();
                }
            }
        };
        this.mExpandAnimationListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.customize.QSCustomizer.5
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (QSCustomizer.this.isShown) {
                    QSCustomizer.this.setCustomizing(true);
                }
                QSCustomizer.this.mOpening = false;
                QSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                QSCustomizer.this.mOpening = false;
                QSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
            }
        };
        this.mCollapseAnimationListener = new AnimatorListenerAdapter() { // from class: com.android.systemui.qs.customize.QSCustomizer.6
            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animator) {
                if (!QSCustomizer.this.isShown) {
                    QSCustomizer.this.setVisibility(8);
                }
                QSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
                QSCustomizer.this.mRecyclerView.setAdapter(QSCustomizer.this.mTileAdapter);
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animator) {
                if (!QSCustomizer.this.isShown) {
                    QSCustomizer.this.setVisibility(8);
                }
                QSCustomizer.this.mNotifQsContainer.setCustomizerAnimating(false);
            }
        };
        LayoutInflater.from(getContext()).inflate(R.layout.qs_customize_panel_content, this);
        this.mClipper = new QSDetailClipper(findViewById(R.id.customize_container));
        this.mToolbar = (Toolbar) findViewById(android.R.id.accessibility_permissionDialog_description);
        TypedValue typedValue = new TypedValue();
        ((LinearLayout) this).mContext.getTheme().resolveAttribute(android.R.attr.homeAsUpIndicator, typedValue, true);
        this.mToolbar.setNavigationIcon(getResources().getDrawable(typedValue.resourceId, ((LinearLayout) this).mContext.getTheme()));
        this.mToolbar.setNavigationOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.qs.customize.QSCustomizer.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                QSCustomizer.this.hide();
            }
        });
        this.mToolbar.setOnMenuItemClickListener(this);
        this.mToolbar.getMenu().add(0, 1, 0, ((LinearLayout) this).mContext.getString(android.R.string.permdesc_readMediaImages));
        this.mToolbar.setTitle(R.string.qs_edit);
        this.mRecyclerView = (RecyclerView) findViewById(android.R.id.list);
        this.mTransparentView = findViewById(R.id.customizer_transparent_view);
        TileAdapter tileAdapter = new TileAdapter(getContext(), uiEventLogger);
        this.mTileAdapter = tileAdapter;
        this.mTileQueryHelper = tileQueryHelper;
        tileQueryHelper.setListener(tileAdapter);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
        this.mTileAdapter.getItemTouchHelper().attachToRecyclerView(this.mRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3) { // from class: com.android.systemui.qs.customize.QSCustomizer.2
            @Override // androidx.recyclerview.widget.GridLayoutManager, androidx.recyclerview.widget.RecyclerView.LayoutManager
            public void onInitializeAccessibilityNodeInfoForItem(RecyclerView.Recycler recycler, RecyclerView.State state, View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            }
        };
        gridLayoutManager.setSpanSizeLookup(this.mTileAdapter.getSizeLookup());
        this.mRecyclerView.setLayoutManager(gridLayoutManager);
        this.mRecyclerView.addItemDecoration(this.mTileAdapter.getItemDecoration());
        DefaultItemAnimator defaultItemAnimator = new DefaultItemAnimator();
        defaultItemAnimator.setMoveDuration(150L);
        this.mRecyclerView.setItemAnimator(defaultItemAnimator);
        this.mLightBarController = lightBarController;
        this.mKeyguardStateController = keyguardStateController;
        this.mScreenLifecycle = screenLifecycle;
        updateNavBackDrop(getResources().getConfiguration());
        ((TunerService) Dependency.get(TunerService.class)).addTunable(this, "system:status_bar_custom_header");
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) throws Resources.NotFoundException {
        str.hashCode();
        if (str.equals("system:status_bar_custom_header")) {
            this.mHeaderImageEnabled = TunerService.parseIntegerSwitch(str2, false);
            updateResources();
        }
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) throws Resources.NotFoundException {
        super.onConfigurationChanged(configuration);
        updateNavBackDrop(configuration);
    }

    private void updateResources() throws Resources.NotFoundException {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mTransparentView.getLayoutParams();
        int dimensionPixelSize = ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(android.R.dimen.message_progress_dialog_end_padding);
        layoutParams.height = dimensionPixelSize;
        if (this.mHeaderImageEnabled) {
            layoutParams.height = dimensionPixelSize + ((LinearLayout) this).mContext.getResources().getDimensionPixelSize(R.dimen.qs_header_image_offset);
        }
        this.mTransparentView.setLayoutParams(layoutParams);
    }

    private void updateNavBackDrop(Configuration configuration) throws Resources.NotFoundException {
        View viewFindViewById = findViewById(R.id.nav_bar_background);
        boolean z = configuration.smallestScreenWidthDp >= 600 || configuration.orientation != 2;
        this.mIsShowingNavBackdrop = z;
        if (viewFindViewById != null) {
            viewFindViewById.setVisibility(z ? 0 : 8);
        }
        updateNavColors();
        updateResources();
    }

    private void updateNavColors() {
        this.mLightBarController.setQsCustomizing(this.mIsShowingNavBackdrop && this.isShown);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        this.mTileAdapter.setHost(qSTileHost);
    }

    public void setContainer(NotificationsQuickSettingsContainer notificationsQuickSettingsContainer) {
        this.mNotifQsContainer = notificationsQuickSettingsContainer;
    }

    public void setQs(QS qs) {
        this.mQs = qs;
    }

    public void show(int i, int i2) {
        if (this.isShown) {
            return;
        }
        int[] locationOnScreen = findViewById(R.id.customize_container).getLocationOnScreen();
        this.mX = i - locationOnScreen[0];
        this.mY = i2 - locationOnScreen[1];
        this.mUiEventLogger.log(QSEditEvent.QS_EDIT_OPEN);
        this.isShown = true;
        this.mOpening = true;
        setTileSpecs();
        setVisibility(0);
        this.mClipper.animateCircularClip(this.mX, this.mY, true, this.mExpandAnimationListener);
        queryTiles();
        this.mNotifQsContainer.setCustomizerAnimating(true);
        this.mNotifQsContainer.setCustomizerShowing(true);
        this.mKeyguardStateController.addCallback(this.mKeyguardCallback);
        updateNavColors();
    }

    public void showImmediately() {
        if (this.isShown) {
            return;
        }
        setVisibility(0);
        this.mClipper.cancelAnimator();
        this.mClipper.showBackground();
        this.isShown = true;
        setTileSpecs();
        setCustomizing(true);
        queryTiles();
        this.mNotifQsContainer.setCustomizerAnimating(false);
        this.mNotifQsContainer.setCustomizerShowing(true);
        this.mKeyguardStateController.addCallback(this.mKeyguardCallback);
        updateNavColors();
    }

    private void queryTiles() {
        this.mTileQueryHelper.queryTiles(this.mHost);
    }

    public void hide() {
        boolean z = this.mScreenLifecycle.getScreenState() != 0;
        if (this.isShown) {
            this.mUiEventLogger.log(QSEditEvent.QS_EDIT_CLOSED);
            this.isShown = false;
            this.mToolbar.dismissPopupMenus();
            this.mClipper.cancelAnimator();
            this.mOpening = false;
            setCustomizing(false);
            save();
            if (z) {
                this.mClipper.animateCircularClip(this.mX, this.mY, false, this.mCollapseAnimationListener);
            } else {
                setVisibility(8);
            }
            this.mNotifQsContainer.setCustomizerAnimating(z);
            this.mNotifQsContainer.setCustomizerShowing(false);
            this.mKeyguardStateController.removeCallback(this.mKeyguardCallback);
            updateNavColors();
        }
    }

    @Override // android.view.View
    public boolean isShown() {
        return this.isShown;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setCustomizing(boolean z) {
        this.mCustomizing = z;
        this.mQs.notifyCustomizeChanged();
    }

    public boolean isCustomizing() {
        return this.mCustomizing || this.mOpening;
    }

    @Override // android.widget.Toolbar.OnMenuItemClickListener
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (menuItem.getItemId() != 1) {
            return false;
        }
        this.mUiEventLogger.log(QSEditEvent.QS_EDIT_RESET);
        reset();
        return false;
    }

    private void reset() {
        this.mTileAdapter.resetTileSpecs(this.mHost, QSTileHost.getDefaultSpecs(((LinearLayout) this).mContext));
    }

    private void setTileSpecs() {
        ArrayList arrayList = new ArrayList();
        Iterator<QSTile> it = this.mHost.getTiles().iterator();
        while (it.hasNext()) {
            arrayList.add(it.next().getTileSpec());
        }
        this.mTileAdapter.setTileSpecs(arrayList);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
    }

    private void save() {
        if (this.mTileQueryHelper.isFinished()) {
            this.mTileAdapter.saveSpecs(this.mHost);
        }
    }

    public void saveInstanceState(Bundle bundle) {
        if (this.isShown) {
            this.mKeyguardStateController.removeCallback(this.mKeyguardCallback);
        }
        bundle.putBoolean("qs_customizing", this.mCustomizing);
    }

    public void restoreInstanceState(Bundle bundle) {
        if (bundle.getBoolean("qs_customizing")) {
            setVisibility(0);
            addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.qs.customize.QSCustomizer.3
                @Override // android.view.View.OnLayoutChangeListener
                public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    QSCustomizer.this.removeOnLayoutChangeListener(this);
                    QSCustomizer.this.showImmediately();
                }
            });
        }
    }

    public void setEditLocation(int i, int i2) {
        int[] locationOnScreen = findViewById(R.id.customize_container).getLocationOnScreen();
        this.mX = i - locationOnScreen[0];
        this.mY = i2 - locationOnScreen[1];
    }
}
