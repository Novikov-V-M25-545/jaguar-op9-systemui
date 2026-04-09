package com.android.systemui.qs;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.widget.RemeasuringLinearLayout;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.R;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.media.MediaHost;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.PagedTileLayout;
import com.android.systemui.qs.QSDetail;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QSPanel;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.settings.BrightnessController;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.settings.ToggleSliderView;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Utils;
import com.android.systemui.util.animation.DisappearParameters;
import com.android.systemui.util.animation.UniqueObjectHostView;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/* loaded from: classes.dex */
public class QSPanel extends LinearLayout implements TunerService.Tunable, QSHost.Callback, BrightnessMirrorController.BrightnessMirrorListener, Dumpable {
    protected ImageView mAutoBrightnessView;
    private boolean mBrightnessBottom;
    private BrightnessController mBrightnessController;
    private BrightnessMirrorController mBrightnessMirrorController;
    private boolean mBrightnessSliderEnabled;
    protected View mBrightnessView;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private String mCachedSpecs;
    private QSDetail.Callback mCallback;
    private int mContentMarginEnd;
    private int mContentMarginStart;
    protected final Context mContext;
    private QSCustomizer mCustomizePanel;
    private Record mDetailRecord;
    protected View mDivider;
    private final DumpManager mDumpManager;
    protected boolean mExpanded;
    protected View mFooter;
    private int mFooterMarginStartHorizontal;
    private PageIndicator mFooterPageIndicator;
    private boolean mGridContentVisible;
    private final H mHandler;
    private ViewGroup mHeaderContainer;
    private LinearLayout mHorizontalContentContainer;
    private LinearLayout mHorizontalLinearLayout;
    private QSTileLayout mHorizontalTileLayout;
    protected QSTileHost mHost;
    private boolean mIsAutomaticBrightnessAvailable;
    private int mLastOrientation;
    protected boolean mListening;
    protected final MediaHost mMediaHost;
    private int mMediaTotalBottomMargin;
    private Consumer<Boolean> mMediaVisibilityChangedListener;
    private final MetricsLogger mMetricsLogger;
    private final int mMovableContentStartIndex;
    private final QSLogger mQSLogger;
    private QSTileRevealController mQsTileRevealController;
    protected final ArrayList<TileRecord> mRecords;
    protected QSTileLayout mRegularTileLayout;
    protected QSSecurityFooter mSecurityFooter;
    protected QSTileLayout mTileLayout;
    protected final UiEventLogger mUiEventLogger;
    private boolean mUsingHorizontalLayout;
    protected boolean mUsingMediaPlayer;
    private int mVisualMarginEnd;
    private int mVisualMarginStart;
    private int mVisualTilePadding;

    public interface QSTileLayout {
        void addTile(TileRecord tileRecord);

        int getNumVisibleTiles();

        int getOffsetTop(TileRecord tileRecord);

        void removeTile(TileRecord tileRecord);

        default void restoreInstanceState(Bundle bundle) {
        }

        default void saveInstanceState(Bundle bundle) {
        }

        default void setExpansion(float f) {
        }

        void setListening(boolean z);

        default boolean setMaxColumns(int i) {
            return false;
        }

        default boolean setMinRows(int i) {
            return false;
        }

        boolean updateResources();
    }

    public static final class TileRecord extends Record {
        public QSTile.Callback callback;
        public boolean scanState;
        public QSTile tile;
        public QSTileView tileView;
    }

    protected boolean displayMediaMarginsOnMedia() {
        return true;
    }

    protected String getDumpableTag() {
        return "QSPanel";
    }

    protected boolean needsDynamicRowsAndColumns() {
        return true;
    }

    public QSPanel(Context context, AttributeSet attributeSet, DumpManager dumpManager, BroadcastDispatcher broadcastDispatcher, QSLogger qSLogger, MediaHost mediaHost, UiEventLogger uiEventLogger) {
        super(context, attributeSet);
        this.mRecords = new ArrayList<>();
        this.mCachedSpecs = "";
        this.mHandler = new H();
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mIsAutomaticBrightnessAvailable = false;
        this.mGridContentVisible = true;
        this.mLastOrientation = -1;
        this.mUsingMediaPlayer = Utils.useQsMediaPlayer(context);
        this.mMediaTotalBottomMargin = getResources().getDimensionPixelSize(R.dimen.quick_settings_bottom_margin_media);
        this.mMediaHost = mediaHost;
        mediaHost.addVisibilityChangeListener(new Function1() { // from class: com.android.systemui.qs.QSPanel$$ExternalSyntheticLambda1
            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return this.f$0.lambda$new$0((Boolean) obj);
            }
        });
        this.mContext = context;
        this.mQSLogger = qSLogger;
        this.mDumpManager = dumpManager;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mUiEventLogger = uiEventLogger;
        setOrientation(1);
        addViewsAboveTiles();
        this.mMovableContentStartIndex = getChildCount();
        this.mRegularTileLayout = createRegularTileLayout();
        if (this.mUsingMediaPlayer) {
            RemeasuringLinearLayout remeasuringLinearLayout = new RemeasuringLinearLayout(context);
            this.mHorizontalLinearLayout = remeasuringLinearLayout;
            remeasuringLinearLayout.setOrientation(0);
            this.mHorizontalLinearLayout.setClipChildren(false);
            this.mHorizontalLinearLayout.setClipToPadding(false);
            RemeasuringLinearLayout remeasuringLinearLayout2 = new RemeasuringLinearLayout(context);
            this.mHorizontalContentContainer = remeasuringLinearLayout2;
            remeasuringLinearLayout2.setOrientation(1);
            this.mHorizontalContentContainer.setClipChildren(false);
            this.mHorizontalContentContainer.setClipToPadding(false);
            this.mHorizontalTileLayout = createHorizontalTileLayout();
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -2, 1.0f);
            int dimension = (int) context.getResources().getDimension(R.dimen.qqs_media_spacing);
            layoutParams.setMarginStart(0);
            layoutParams.setMarginEnd(dimension);
            layoutParams.gravity = 16;
            this.mHorizontalLinearLayout.addView(this.mHorizontalContentContainer, layoutParams);
            addView(this.mHorizontalLinearLayout, new LinearLayout.LayoutParams(-1, 0, 1.0f));
            initMediaHostState();
        }
        addSecurityFooter();
        QSTileLayout qSTileLayout = this.mRegularTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            this.mQsTileRevealController = new QSTileRevealController(context, this, (PagedTileLayout) qSTileLayout);
        }
        qSLogger.logAllTilesChangeListening(this.mListening, getDumpableTag(), this.mCachedSpecs);
        updateResources();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ Unit lambda$new$0(Boolean bool) {
        onMediaVisibilityChanged(bool);
        return null;
    }

    protected void onMediaVisibilityChanged(Boolean bool) {
        switchTileLayout();
        Consumer<Boolean> consumer = this.mMediaVisibilityChangedListener;
        if (consumer != null) {
            consumer.accept(bool);
        }
    }

    protected void addSecurityFooter() {
        this.mSecurityFooter = new QSSecurityFooter(this, this.mContext);
    }

    protected void addViewsAboveTiles() {
        View viewInflate = LayoutInflater.from(this.mContext).inflate(R.layout.quick_settings_brightness_dialog, (ViewGroup) this, false);
        this.mBrightnessView = viewInflate;
        addView(viewInflate);
        Context context = getContext();
        int i = R.id.brightness_icon;
        this.mBrightnessController = new BrightnessController(context, (ImageView) findViewById(i), (ToggleSlider) findViewById(R.id.brightness_slider), this.mBroadcastDispatcher);
        this.mAutoBrightnessView = (ImageView) findViewById(i);
        this.mIsAutomaticBrightnessAvailable = getResources().getBoolean(android.R.bool.config_allow_pin_storage_for_unattended_reboot);
    }

    protected QSTileLayout createRegularTileLayout() {
        if (this.mRegularTileLayout == null) {
            this.mRegularTileLayout = (QSTileLayout) LayoutInflater.from(this.mContext).inflate(R.layout.qs_paged_tile_layout, (ViewGroup) this, false);
        }
        return this.mRegularTileLayout;
    }

    protected QSTileLayout createHorizontalTileLayout() {
        return createRegularTileLayout();
    }

    protected void initMediaHostState() {
        this.mMediaHost.setExpansion(1.0f);
        this.mMediaHost.setShowsOnlyActiveMedia(false);
        updateMediaDisappearParameters();
        this.mMediaHost.init(0);
    }

    private void updateMediaDisappearParameters() {
        if (this.mUsingMediaPlayer) {
            DisappearParameters disappearParameters = this.mMediaHost.getDisappearParameters();
            if (this.mUsingHorizontalLayout) {
                disappearParameters.getDisappearSize().set(0.0f, 0.4f);
                disappearParameters.getGonePivot().set(1.0f, 1.0f);
                disappearParameters.getContentTranslationFraction().set(0.25f, 1.0f);
                disappearParameters.setDisappearEnd(0.6f);
            } else {
                disappearParameters.getDisappearSize().set(1.0f, 0.0f);
                disappearParameters.getGonePivot().set(0.0f, 1.0f);
                disappearParameters.getContentTranslationFraction().set(0.0f, 1.05f);
                disappearParameters.setDisappearEnd(0.95f);
            }
            disappearParameters.setFadeStartPosition(0.95f);
            disappearParameters.setDisappearStart(0.0f);
            this.mMediaHost.setDisappearParameters(disappearParameters);
        }
    }

    @Override // android.widget.LinearLayout, android.view.View
    protected void onMeasure(int i, int i2) {
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            PageIndicator pageIndicator = this.mFooterPageIndicator;
            if (pageIndicator != null) {
                pageIndicator.setNumPages(((PagedTileLayout) qSTileLayout).getNumPages());
            }
            int size = 10000 - View.MeasureSpec.getSize(i2);
            int iMakeMeasureSpec = View.MeasureSpec.makeMeasureSpec(10000, 1073741824);
            ((PagedTileLayout) this.mTileLayout).setExcessHeight(size);
            i2 = iMakeMeasureSpec;
        }
        super.onMeasure(i, i2);
        int paddingBottom = getPaddingBottom() + getPaddingTop();
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() != 8) {
                int measuredHeight = paddingBottom + childAt.getMeasuredHeight();
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) childAt.getLayoutParams();
                paddingBottom = measuredHeight + marginLayoutParams.topMargin + marginLayoutParams.bottomMargin;
            }
        }
        setMeasuredDimension(getMeasuredWidth(), paddingBottom);
    }

    public QSTileRevealController getQsTileRevealController() {
        return this.mQsTileRevealController;
    }

    public boolean isShowingCustomize() {
        QSCustomizer qSCustomizer = this.mCustomizePanel;
        return qSCustomizer != null && qSCustomizer.isCustomizing();
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService tunerService = (TunerService) Dependency.get(TunerService.class);
        tunerService.addTunable(this, "lineagesecure:qs_show_auto_brightness");
        tunerService.addTunable(this, "lineagesecure:qs_show_brightness_slider");
        tunerService.addTunable(this, "qs_brightness_position_bottom");
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            setTiles(qSTileHost.getTiles());
        }
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.addCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.mDumpManager.registerDumpable(getDumpableTag(), this);
    }

    @Override // android.view.ViewGroup, android.view.View
    protected void onDetachedFromWindow() {
        ((TunerService) Dependency.get(TunerService.class)).removeTunable(this);
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            qSTileHost.removeCallback(this);
        }
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.setListening(false);
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.removeCallbacks();
        }
        this.mRecords.clear();
        BrightnessMirrorController brightnessMirrorController = this.mBrightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.removeCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.mDumpManager.unregisterDumpable(getDumpableTag());
        super.onDetachedFromWindow();
    }

    @Override // com.android.systemui.qs.QSHost.Callback
    public void onTilesChanged() {
        setTiles(this.mHost.getTiles());
    }

    @Override // com.android.systemui.tuner.TunerService.Tunable
    public void onTuningChanged(String str, String str2) {
        View view;
        if ("lineagesecure:qs_show_auto_brightness".equals(str) && this.mIsAutomaticBrightnessAvailable) {
            updateViewVisibilityForTuningValue(this.mAutoBrightnessView, str2);
            return;
        }
        if ("lineagesecure:qs_show_brightness_slider".equals(str) && (view = this.mBrightnessView) != null) {
            updateViewVisibilityForTuningValue(view, str2);
            return;
        }
        if ("qs_brightness_position_bottom".equals(str)) {
            if (str2 == null || Integer.parseInt(str2) == 0) {
                removeView(this.mBrightnessView);
                addView(this.mBrightnessView, 0);
                this.mBrightnessBottom = false;
            } else {
                removeView(this.mBrightnessView);
                addView(this.mBrightnessView, getBrightnessViewPositionBottom());
                this.mBrightnessBottom = true;
            }
        }
    }

    private int getBrightnessViewPositionBottom() {
        for (int i = 0; i < getChildCount(); i++) {
            if (getChildAt(i) == this.mSecurityFooter.getView()) {
                return i;
            }
        }
        return 0;
    }

    private void updateViewVisibilityForTuningValue(View view, String str) {
        if (view == this.mBrightnessView) {
            this.mBrightnessSliderEnabled = TunerService.parseIntegerSwitch(str, true);
        }
        if (isHorizontalLayout()) {
            view.setVisibility(TunerService.parseIntegerSwitch(str, true) ? 0 : 4);
        } else {
            view.setVisibility(TunerService.parseIntegerSwitch(str, true) ? 0 : 8);
        }
    }

    public void openDetails(String str) {
        QSTile tile = getTile(str);
        if (tile != null) {
            showDetailAdapter(true, tile.getDetailAdapter(), new int[]{getWidth() / 2, 0});
        }
    }

    private QSTile getTile(String str) {
        for (int i = 0; i < this.mRecords.size(); i++) {
            if (str.equals(this.mRecords.get(i).tile.getTileSpec())) {
                return this.mRecords.get(i).tile;
            }
        }
        return this.mHost.createTile(str);
    }

    public void setBrightnessMirror(BrightnessMirrorController brightnessMirrorController) {
        BrightnessMirrorController brightnessMirrorController2 = this.mBrightnessMirrorController;
        if (brightnessMirrorController2 != null) {
            brightnessMirrorController2.removeCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        this.mBrightnessMirrorController = brightnessMirrorController;
        if (brightnessMirrorController != null) {
            brightnessMirrorController.addCallback((BrightnessMirrorController.BrightnessMirrorListener) this);
        }
        updateBrightnessMirror();
    }

    @Override // com.android.systemui.statusbar.policy.BrightnessMirrorController.BrightnessMirrorListener
    public void onBrightnessMirrorReinflated(View view) {
        updateBrightnessMirror();
    }

    View getBrightnessView() {
        return this.mBrightnessView;
    }

    public void setCallback(QSDetail.Callback callback) {
        this.mCallback = callback;
    }

    public void setHost(QSTileHost qSTileHost, QSCustomizer qSCustomizer) {
        this.mHost = qSTileHost;
        qSTileHost.addCallback(this);
        setTiles(this.mHost.getTiles());
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.setHostEnvironment(qSTileHost);
        }
        this.mCustomizePanel = qSCustomizer;
        if (qSCustomizer != null) {
            qSCustomizer.setHost(this.mHost);
        }
    }

    public void setFooterPageIndicator(PageIndicator pageIndicator) {
        if (this.mRegularTileLayout instanceof PagedTileLayout) {
            this.mFooterPageIndicator = pageIndicator;
            updatePageIndicator();
        }
    }

    private void updatePageIndicator() {
        PageIndicator pageIndicator;
        if (!(this.mRegularTileLayout instanceof PagedTileLayout) || (pageIndicator = this.mFooterPageIndicator) == null) {
            return;
        }
        pageIndicator.setVisibility(8);
        ((PagedTileLayout) this.mRegularTileLayout).setPageIndicator(this.mFooterPageIndicator);
    }

    public QSTileHost getHost() {
        return this.mHost;
    }

    public void updateResources() {
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_size);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.qs_tile_background_size);
        this.mFooterMarginStartHorizontal = getResources().getDimensionPixelSize(R.dimen.qs_footer_horizontal_margin);
        this.mVisualTilePadding = (int) ((dimensionPixelSize - dimensionPixelSize2) / 2.0f);
        updatePadding();
        updatePageIndicator();
        if (this.mListening) {
            refreshAllTiles();
        }
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.updateResources();
        }
        if (this.mBrightnessView != null) {
            if (isHorizontalLayout()) {
                this.mBrightnessView.setVisibility(this.mBrightnessSliderEnabled ? 0 : 4);
            } else {
                this.mBrightnessView.setVisibility(this.mBrightnessSliderEnabled ? 0 : 8);
            }
        }
    }

    protected void updatePadding() throws Resources.NotFoundException {
        Resources resources = this.mContext.getResources();
        int dimensionPixelSize = resources.getDimensionPixelSize(R.dimen.qs_panel_padding_top);
        if (this.mUsingHorizontalLayout) {
            dimensionPixelSize = (int) (dimensionPixelSize * 0.6f);
        }
        setPaddingRelative(getPaddingStart(), dimensionPixelSize, getPaddingEnd(), resources.getDimensionPixelSize(R.dimen.qs_panel_padding_bottom));
    }

    @Override // android.view.View
    protected void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.onConfigurationChanged();
        }
        updateResources();
        updateBrightnessMirror();
        int i = configuration.orientation;
        if (i != this.mLastOrientation) {
            this.mLastOrientation = i;
            switchTileLayout();
        }
    }

    @Override // android.view.View
    protected void onFinishInflate() throws Resources.NotFoundException {
        super.onFinishInflate();
        this.mFooter = findViewById(R.id.qs_footer);
        this.mDivider = findViewById(R.id.divider);
        switchTileLayout(true);
    }

    boolean switchTileLayout() {
        return switchTileLayout(false);
    }

    private boolean switchTileLayout(boolean z) throws Resources.NotFoundException {
        QSTileLayout qSTileLayout;
        boolean zShouldUseHorizontalLayout = shouldUseHorizontalLayout();
        if (this.mDivider != null) {
            if (!zShouldUseHorizontalLayout && this.mUsingMediaPlayer && this.mMediaHost.getVisible()) {
                this.mDivider.setVisibility(0);
            } else {
                this.mDivider.setVisibility(8);
            }
        }
        if (zShouldUseHorizontalLayout == this.mUsingHorizontalLayout && !z) {
            return false;
        }
        this.mUsingHorizontalLayout = zShouldUseHorizontalLayout;
        View view = zShouldUseHorizontalLayout ? this.mHorizontalLinearLayout : (View) this.mRegularTileLayout;
        View view2 = zShouldUseHorizontalLayout ? (View) this.mRegularTileLayout : this.mHorizontalLinearLayout;
        LinearLayout linearLayout = zShouldUseHorizontalLayout ? this.mHorizontalContentContainer : this;
        QSTileLayout qSTileLayout2 = zShouldUseHorizontalLayout ? this.mHorizontalTileLayout : this.mRegularTileLayout;
        if (view2 != null && ((qSTileLayout = this.mRegularTileLayout) != this.mHorizontalTileLayout || view2 != qSTileLayout)) {
            view2.setVisibility(8);
        }
        view.setVisibility(0);
        switchAllContentToParent(linearLayout, qSTileLayout2);
        reAttachMediaHost();
        QSTileLayout qSTileLayout3 = this.mTileLayout;
        if (qSTileLayout3 != null) {
            qSTileLayout3.setListening(false);
            Iterator<TileRecord> it = this.mRecords.iterator();
            while (it.hasNext()) {
                TileRecord next = it.next();
                this.mTileLayout.removeTile(next);
                next.tile.removeCallback(next.callback);
            }
        }
        this.mTileLayout = qSTileLayout2;
        QSTileHost qSTileHost = this.mHost;
        if (qSTileHost != null) {
            setTiles(qSTileHost.getTiles());
        }
        qSTileLayout2.setListening(this.mListening);
        if (needsDynamicRowsAndColumns()) {
            qSTileLayout2.setMinRows(zShouldUseHorizontalLayout ? 2 : 1);
            qSTileLayout2.setMaxColumns(zShouldUseHorizontalLayout ? 3 : 100);
        }
        updateTileLayoutMargins();
        updateFooterMargin();
        updateDividerMargin();
        updateMediaDisappearParameters();
        updateMediaHostContentMargins();
        updateHorizontalLinearLayoutMargins();
        updatePadding();
        return true;
    }

    private void updateHorizontalLinearLayoutMargins() {
        if (this.mHorizontalLinearLayout == null || displayMediaMarginsOnMedia()) {
            return;
        }
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) this.mHorizontalLinearLayout.getLayoutParams();
        layoutParams.bottomMargin = this.mMediaTotalBottomMargin - getPaddingBottom();
        this.mHorizontalLinearLayout.setLayoutParams(layoutParams);
    }

    /* JADX WARN: Multi-variable type inference failed */
    private void switchAllContentToParent(ViewGroup viewGroup, QSTileLayout qSTileLayout) {
        ViewGroup viewGroup2;
        int i = viewGroup == this ? this.mMovableContentStartIndex : 0;
        switchToParent((View) qSTileLayout, viewGroup, i);
        int i2 = i + 1;
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            View view = qSSecurityFooter.getView();
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
            if (this.mUsingHorizontalLayout && (viewGroup2 = this.mHeaderContainer) != null) {
                layoutParams.width = 0;
                layoutParams.weight = 1.6f;
                switchToParent(view, viewGroup2, 1);
            } else {
                layoutParams.width = -2;
                layoutParams.weight = 0.0f;
                switchToParent(view, viewGroup, i2);
                i2++;
            }
            view.setLayoutParams(layoutParams);
        }
        View view2 = this.mFooter;
        if (view2 != null) {
            switchToParent(view2, viewGroup, i2);
        }
    }

    private void switchToParent(View view, ViewGroup viewGroup, int i) {
        ViewGroup viewGroup2 = (ViewGroup) view.getParent();
        if (viewGroup2 == viewGroup && viewGroup2.indexOfChild(view) == i) {
            return;
        }
        if (viewGroup2 != null) {
            viewGroup2.removeView(view);
        }
        viewGroup.addView(view, i);
    }

    private boolean shouldUseHorizontalLayout() {
        return this.mUsingMediaPlayer && this.mMediaHost.getVisible() && getResources().getConfiguration().orientation == 2;
    }

    protected boolean isHorizontalLayout() {
        return getResources().getConfiguration().orientation == 2;
    }

    protected void reAttachMediaHost() {
        if (this.mUsingMediaPlayer) {
            boolean zShouldUseHorizontalLayout = shouldUseHorizontalLayout();
            UniqueObjectHostView hostView = this.mMediaHost.getHostView();
            LinearLayout linearLayout = zShouldUseHorizontalLayout ? this.mHorizontalLinearLayout : this;
            ViewGroup viewGroup = (ViewGroup) hostView.getParent();
            if (viewGroup != linearLayout) {
                if (viewGroup != null) {
                    viewGroup.removeView(hostView);
                }
                linearLayout.addView(hostView);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) hostView.getLayoutParams();
                layoutParams.height = -2;
                layoutParams.width = zShouldUseHorizontalLayout ? 0 : -1;
                layoutParams.weight = zShouldUseHorizontalLayout ? 1.2f : 0.0f;
                layoutParams.bottomMargin = (!zShouldUseHorizontalLayout || displayMediaMarginsOnMedia()) ? this.mMediaTotalBottomMargin - getPaddingBottom() : 0;
            }
        }
    }

    public void updateBrightnessMirror() {
        if (this.mBrightnessMirrorController != null) {
            int i = R.id.brightness_slider;
            ToggleSliderView toggleSliderView = (ToggleSliderView) findViewById(i);
            toggleSliderView.setMirror((ToggleSliderView) this.mBrightnessMirrorController.getMirror().findViewById(i));
            toggleSliderView.setMirrorController(this.mBrightnessMirrorController);
        }
    }

    public void setExpanded(boolean z) {
        if (this.mExpanded == z) {
            return;
        }
        this.mQSLogger.logPanelExpanded(z, getDumpableTag());
        this.mExpanded = z;
        if (!z) {
            QSTileLayout qSTileLayout = this.mTileLayout;
            if (qSTileLayout instanceof PagedTileLayout) {
                ((PagedTileLayout) qSTileLayout).setCurrentItem(0, false);
            }
        }
        this.mMetricsLogger.visibility(111, this.mExpanded);
        if (!this.mExpanded) {
            this.mUiEventLogger.log(closePanelEvent());
            closeDetail();
        } else {
            this.mUiEventLogger.log(openPanelEvent());
            logTiles();
        }
    }

    public void setPageListener(PagedTileLayout.PageListener pageListener) {
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout instanceof PagedTileLayout) {
            ((PagedTileLayout) qSTileLayout).setPageListener(pageListener);
        }
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public void setListening(boolean z) {
        if (this.mListening == z) {
            return;
        }
        this.mListening = z;
        if (this.mTileLayout != null) {
            this.mQSLogger.logAllTilesChangeListening(z, getDumpableTag(), this.mCachedSpecs);
            this.mTileLayout.setListening(z);
        }
        if (this.mListening) {
            refreshAllTiles();
        }
    }

    private String getTilesSpecs() {
        return (String) this.mRecords.stream().map(new Function() { // from class: com.android.systemui.qs.QSPanel$$ExternalSyntheticLambda0
            @Override // java.util.function.Function
            public final Object apply(Object obj) {
                return QSPanel.lambda$getTilesSpecs$1((QSPanel.TileRecord) obj);
            }
        }).collect(Collectors.joining(","));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ String lambda$getTilesSpecs$1(TileRecord tileRecord) {
        return tileRecord.tile.getTileSpec();
    }

    public void setListening(boolean z, boolean z2) {
        setListening(z && z2);
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.setListening(z);
        }
        setBrightnessListening(z);
    }

    public void setBrightnessListening(boolean z) {
        BrightnessController brightnessController = this.mBrightnessController;
        if (brightnessController == null) {
            return;
        }
        if (z) {
            brightnessController.registerCallbacks();
        } else {
            brightnessController.unregisterCallbacks();
        }
    }

    public void refreshAllTiles() {
        BrightnessController brightnessController = this.mBrightnessController;
        if (brightnessController != null) {
            brightnessController.checkRestrictionAndSetEnabled();
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            it.next().tile.refreshState();
        }
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.refreshState();
        }
    }

    public void showDetailAdapter(boolean z, DetailAdapter detailAdapter, int[] iArr) {
        int i = iArr[0];
        int i2 = iArr[1];
        ((View) getParent()).getLocationInWindow(iArr);
        Record record = new Record();
        record.detailAdapter = detailAdapter;
        record.x = i - iArr[0];
        record.y = i2 - iArr[1];
        iArr[0] = i;
        iArr[1] = i2;
        showDetail(z, record);
    }

    protected void showDetail(boolean z, Record record) {
        this.mHandler.obtainMessage(1, z ? 1 : 0, 0, record).sendToTarget();
    }

    public void setTiles(Collection<QSTile> collection) {
        setTiles(collection, false);
    }

    public void setTiles(Collection<QSTile> collection, boolean z) {
        if (!z) {
            this.mQsTileRevealController.updateRevealedTiles(collection);
        }
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            this.mTileLayout.removeTile(next);
            next.tile.removeCallback(next.callback);
        }
        this.mRecords.clear();
        this.mCachedSpecs = "";
        Iterator<QSTile> it2 = collection.iterator();
        while (it2.hasNext()) {
            addTile(it2.next(), z);
        }
    }

    protected void drawTile(TileRecord tileRecord, QSTile.State state) {
        tileRecord.tileView.onStateChanged(state);
    }

    protected QSTileView createTileView(QSTile qSTile, boolean z) {
        return this.mHost.createTileView(qSTile, z);
    }

    protected QSEvent openPanelEvent() {
        return QSEvent.QS_PANEL_EXPANDED;
    }

    protected QSEvent closePanelEvent() {
        return QSEvent.QS_PANEL_COLLAPSED;
    }

    protected boolean shouldShowDetail() {
        return this.mExpanded;
    }

    protected TileRecord addTile(QSTile qSTile, boolean z) {
        final TileRecord tileRecord = new TileRecord();
        tileRecord.tile = qSTile;
        tileRecord.tileView = createTileView(qSTile, z);
        QSTile.Callback callback = new QSTile.Callback() { // from class: com.android.systemui.qs.QSPanel.1
            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onStateChanged(QSTile.State state) {
                QSPanel.this.drawTile(tileRecord, state);
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onShowDetail(boolean z2) {
                if (QSPanel.this.shouldShowDetail()) {
                    QSPanel.this.showDetail(z2, tileRecord);
                }
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onToggleStateChanged(boolean z2) {
                if (QSPanel.this.mDetailRecord == tileRecord) {
                    QSPanel.this.fireToggleStateChanged(z2);
                }
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onScanStateChanged(boolean z2) {
                tileRecord.scanState = z2;
                Record record = QSPanel.this.mDetailRecord;
                TileRecord tileRecord2 = tileRecord;
                if (record == tileRecord2) {
                    QSPanel.this.fireScanStateChanged(tileRecord2.scanState);
                }
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onAnnouncementRequested(CharSequence charSequence) {
                if (charSequence != null) {
                    QSPanel.this.mHandler.obtainMessage(3, charSequence).sendToTarget();
                }
            }
        };
        tileRecord.tile.addCallback(callback);
        tileRecord.callback = callback;
        tileRecord.tileView.init(tileRecord.tile);
        tileRecord.tile.refreshState();
        this.mRecords.add(tileRecord);
        this.mCachedSpecs = getTilesSpecs();
        QSTileLayout qSTileLayout = this.mTileLayout;
        if (qSTileLayout != null) {
            qSTileLayout.addTile(tileRecord);
        }
        return tileRecord;
    }

    public void showEdit(final View view) {
        view.post(new Runnable() { // from class: com.android.systemui.qs.QSPanel.2
            @Override // java.lang.Runnable
            public void run() {
                if (QSPanel.this.mCustomizePanel == null || QSPanel.this.mCustomizePanel.isCustomizing()) {
                    return;
                }
                int[] locationOnScreen = view.getLocationOnScreen();
                QSPanel.this.mCustomizePanel.show(locationOnScreen[0] + (view.getWidth() / 2), (locationOnScreen[1] + (view.getHeight() / 2)) - QSPanel.this.getTop());
            }
        });
    }

    public void closeDetail() {
        QSCustomizer qSCustomizer = this.mCustomizePanel;
        if (qSCustomizer != null && qSCustomizer.isShown()) {
            this.mCustomizePanel.hide();
        } else {
            showDetail(false, this.mDetailRecord);
        }
    }

    protected void handleShowDetail(Record record, boolean z) {
        int i;
        if (record instanceof TileRecord) {
            handleShowDetailTile((TileRecord) record, z);
            return;
        }
        int i2 = 0;
        if (record != null) {
            i2 = record.x;
            i = record.y;
        } else {
            i = 0;
        }
        handleShowDetailImpl(record, z, i2, i);
    }

    private void handleShowDetailTile(TileRecord tileRecord, boolean z) {
        Record record = this.mDetailRecord;
        if ((record != null) == z && record == tileRecord) {
            return;
        }
        if (z) {
            DetailAdapter detailAdapter = tileRecord.tile.getDetailAdapter();
            tileRecord.detailAdapter = detailAdapter;
            if (detailAdapter == null) {
                return;
            }
        }
        tileRecord.tile.setDetailListening(z);
        handleShowDetailImpl(tileRecord, z, tileRecord.tileView.getLeft() + (tileRecord.tileView.getWidth() / 2), tileRecord.tileView.getDetailY() + this.mTileLayout.getOffsetTop(tileRecord));
    }

    private void handleShowDetailImpl(Record record, boolean z, int i, int i2) {
        setDetailRecord(z ? record : null);
        fireShowingDetail(z ? record.detailAdapter : null, i, i2);
    }

    protected void setDetailRecord(Record record) {
        if (record == this.mDetailRecord) {
            return;
        }
        this.mDetailRecord = record;
        fireScanStateChanged((record instanceof TileRecord) && ((TileRecord) record).scanState);
    }

    void setGridContentVisibility(boolean z) {
        int i = z ? 0 : 4;
        setVisibility(i);
        if (this.mGridContentVisible != z) {
            this.mMetricsLogger.visibility(111, i);
        }
        this.mGridContentVisible = z;
    }

    private void logTiles() {
        for (int i = 0; i < this.mRecords.size(); i++) {
            QSTile qSTile = this.mRecords.get(i).tile;
            this.mMetricsLogger.write(qSTile.populate(new LogMaker(qSTile.getMetricsCategory()).setType(1)));
        }
    }

    private void fireShowingDetail(DetailAdapter detailAdapter, int i, int i2) {
        QSDetail.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onShowingDetail(detailAdapter, i, i2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireToggleStateChanged(boolean z) {
        QSDetail.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onToggleStateChanged(z);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fireScanStateChanged(boolean z) {
        QSDetail.Callback callback = this.mCallback;
        if (callback != null) {
            callback.onScanStateChanged(z);
        }
    }

    public void clickTile(ComponentName componentName) {
        String spec = CustomTile.toSpec(componentName);
        int size = this.mRecords.size();
        for (int i = 0; i < size; i++) {
            if (this.mRecords.get(i).tile.getTileSpec().equals(spec)) {
                this.mRecords.get(i).tile.click();
                return;
            }
        }
    }

    QSTileLayout getTileLayout() {
        return this.mTileLayout;
    }

    QSTileView getTileView(QSTile qSTile) {
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            if (next.tile == qSTile) {
                return next.tileView;
            }
        }
        return null;
    }

    public QSSecurityFooter getSecurityFooter() {
        return this.mSecurityFooter;
    }

    public View getDivider() {
        return this.mDivider;
    }

    public void showDeviceMonitoringDialog() {
        QSSecurityFooter qSSecurityFooter = this.mSecurityFooter;
        if (qSSecurityFooter != null) {
            qSSecurityFooter.showDeviceMonitoringDialog();
        }
    }

    public void setContentMargins(int i, int i2) {
        this.mContentMarginStart = i;
        this.mContentMarginEnd = i2;
        int i3 = this.mVisualTilePadding;
        updateTileLayoutMargins(i - i3, i2 - i3);
        updateMediaHostContentMargins();
        updateFooterMargin();
        updateDividerMargin();
    }

    private void updateFooterMargin() {
        int i;
        int i2;
        View view = this.mFooter;
        if (view != null) {
            if (this.mUsingHorizontalLayout) {
                i = this.mFooterMarginStartHorizontal;
                i2 = i - this.mVisualMarginEnd;
            } else {
                i = 0;
                i2 = 0;
            }
            updateMargins(view, i, 0);
            PageIndicator pageIndicator = this.mFooterPageIndicator;
            if (pageIndicator != null) {
                updateMargins(pageIndicator, 0, i2);
            }
        }
    }

    private void updateTileLayoutMargins(int i, int i2) {
        this.mVisualMarginStart = i;
        this.mVisualMarginEnd = i2;
        updateTileLayoutMargins();
    }

    private void updateTileLayoutMargins() {
        int i = this.mVisualMarginEnd;
        if (this.mUsingHorizontalLayout) {
            i = 0;
        }
        updateMargins((View) this.mTileLayout, this.mVisualMarginStart, i);
    }

    private void updateDividerMargin() {
        View view = this.mDivider;
        if (view == null) {
            return;
        }
        updateMargins(view, this.mContentMarginStart, this.mContentMarginEnd);
    }

    protected void updateMediaHostContentMargins() {
        if (this.mUsingMediaPlayer) {
            updateMargins(this.mMediaHost.getHostView(), 4, this.mUsingHorizontalLayout ? this.mContentMarginEnd : 4);
        }
    }

    protected void updateMargins(View view, int i, int i2) {
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) view.getLayoutParams();
        layoutParams.setMarginStart(i);
        layoutParams.setMarginEnd(i2);
        view.setLayoutParams(layoutParams);
    }

    public MediaHost getMediaHost() {
        return this.mMediaHost;
    }

    public void setHeaderContainer(ViewGroup viewGroup) {
        this.mHeaderContainer = viewGroup;
    }

    public void setMediaVisibilityChangedListener(Consumer<Boolean> consumer) {
        this.mMediaVisibilityChangedListener = consumer;
    }

    private class H extends Handler {
        private H() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                QSPanel.this.handleShowDetail((Record) message.obj, message.arg1 != 0);
            } else if (i == 3) {
                QSPanel.this.announceForAccessibility((CharSequence) message.obj);
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println(getClass().getSimpleName() + ":");
        printWriter.println("  Tile records:");
        Iterator<TileRecord> it = this.mRecords.iterator();
        while (it.hasNext()) {
            TileRecord next = it.next();
            if (next.tile instanceof Dumpable) {
                printWriter.print("    ");
                ((Dumpable) next.tile).dump(fileDescriptor, printWriter, strArr);
                printWriter.print("    ");
                printWriter.println(next.tileView.toString());
            }
        }
    }

    protected static class Record {
        DetailAdapter detailAdapter;
        int x;
        int y;

        protected Record() {
        }
    }

    public boolean isBrightnessViewBottom() {
        return this.mBrightnessBottom;
    }
}
