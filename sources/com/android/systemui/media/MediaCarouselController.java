package com.android.systemui.media;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaHostStatesManager;
import com.android.systemui.media.PlayerViewHolder;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.qs.PageIndicator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.Utils;
import com.android.systemui.util.animation.MeasurementInput;
import com.android.systemui.util.animation.TransitionLayout;
import com.android.systemui.util.animation.UniqueObjectHostView;
import com.android.systemui.util.animation.UniqueObjectHostViewKt;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Provider;
import kotlin.Pair;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.FunctionReference;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaCarouselController.kt */
/* loaded from: classes.dex */
public final class MediaCarouselController {
    private final ActivityStarter activityStarter;
    private int carouselMeasureHeight;
    private int carouselMeasureWidth;
    private final MediaCarouselController$configListener$1 configListener;
    private final Context context;
    private int currentCarouselHeight;
    private int currentCarouselWidth;
    private int currentEndLocation;
    private int currentStartLocation;
    private float currentTransitionProgress;
    private boolean currentlyExpanded;
    private boolean currentlyShowingOnlyActive;
    private MediaHostState desiredHostState;
    private int desiredLocation;
    private boolean isRtl;
    private Set<String> keysNeedRemoval;
    private final MediaScrollView mediaCarousel;
    private final MediaCarouselScrollHandler mediaCarouselScrollHandler;
    private final ViewGroup mediaContent;
    private final Provider<MediaControlPanel> mediaControlPanelFactory;

    @NotNull
    private final ViewGroup mediaFrame;
    private final MediaHostStatesManager mediaHostStatesManager;
    private final MediaDataManager mediaManager;
    private boolean needsReordering;
    private final PageIndicator pageIndicator;
    private boolean playersVisible;
    private View settingsButton;
    private final VisualStabilityManager.Callback visualStabilityCallback;
    private final VisualStabilityManager visualStabilityManager;

    /* compiled from: MediaCarouselController.kt */
    /* renamed from: com.android.systemui.media.MediaCarouselController$1, reason: invalid class name */
    static final /* synthetic */ class AnonymousClass1 extends FunctionReference implements Function0<Unit> {
        AnonymousClass1(MediaDataManager mediaDataManager) {
            super(0, mediaDataManager);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "onSwipeToDismiss";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(MediaDataManager.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "onSwipeToDismiss()V";
        }

        @Override // kotlin.jvm.functions.Function0
        public /* bridge */ /* synthetic */ Unit invoke() {
            invoke2();
            return Unit.INSTANCE;
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final void invoke2() {
            ((MediaDataManager) this.receiver).onSwipeToDismiss();
        }
    }

    /* compiled from: MediaCarouselController.kt */
    /* renamed from: com.android.systemui.media.MediaCarouselController$2, reason: invalid class name */
    static final /* synthetic */ class AnonymousClass2 extends FunctionReference implements Function0<Unit> {
        AnonymousClass2(MediaCarouselController mediaCarouselController) {
            super(0, mediaCarouselController);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "updatePageIndicatorLocation";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(MediaCarouselController.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "updatePageIndicatorLocation()V";
        }

        @Override // kotlin.jvm.functions.Function0
        public /* bridge */ /* synthetic */ Unit invoke() {
            invoke2();
            return Unit.INSTANCE;
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final void invoke2() {
            ((MediaCarouselController) this.receiver).updatePageIndicatorLocation();
        }
    }

    /* compiled from: MediaCarouselController.kt */
    /* renamed from: com.android.systemui.media.MediaCarouselController$3, reason: invalid class name */
    static final /* synthetic */ class AnonymousClass3 extends FunctionReference implements Function0<Unit> {
        AnonymousClass3(MediaCarouselController mediaCarouselController) {
            super(0, mediaCarouselController);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "closeGuts";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(MediaCarouselController.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "closeGuts()V";
        }

        @Override // kotlin.jvm.functions.Function0
        public /* bridge */ /* synthetic */ Unit invoke() {
            invoke2();
            return Unit.INSTANCE;
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final void invoke2() {
            ((MediaCarouselController) this.receiver).closeGuts();
        }
    }

    /* compiled from: MediaCarouselController.kt */
    /* renamed from: com.android.systemui.media.MediaCarouselController$addOrUpdatePlayer$1, reason: invalid class name and case insensitive filesystem */
    static final /* synthetic */ class C00801 extends FunctionReference implements Function0<Unit> {
        C00801(MediaCarouselController mediaCarouselController) {
            super(0, mediaCarouselController);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getName() {
            return "updateCarouselDimensions";
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final KDeclarationContainer getOwner() {
            return Reflection.getOrCreateKotlinClass(MediaCarouselController.class);
        }

        @Override // kotlin.jvm.internal.CallableReference
        public final String getSignature() {
            return "updateCarouselDimensions()V";
        }

        @Override // kotlin.jvm.functions.Function0
        public /* bridge */ /* synthetic */ Unit invoke() {
            invoke2();
            return Unit.INSTANCE;
        }

        /* renamed from: invoke, reason: avoid collision after fix types in other method */
        public final void invoke2() {
            ((MediaCarouselController) this.receiver).updateCarouselDimensions();
        }
    }

    /* JADX WARN: Type inference failed for: r5v1, types: [com.android.systemui.media.MediaCarouselController$configListener$1, java.lang.Object] */
    public MediaCarouselController(@NotNull Context context, @NotNull Provider<MediaControlPanel> mediaControlPanelFactory, @NotNull VisualStabilityManager visualStabilityManager, @NotNull MediaHostStatesManager mediaHostStatesManager, @NotNull ActivityStarter activityStarter, @NotNull DelayableExecutor executor, @NotNull MediaDataManager mediaManager, @NotNull ConfigurationController configurationController, @NotNull FalsingManager falsingManager) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(mediaControlPanelFactory, "mediaControlPanelFactory");
        Intrinsics.checkParameterIsNotNull(visualStabilityManager, "visualStabilityManager");
        Intrinsics.checkParameterIsNotNull(mediaHostStatesManager, "mediaHostStatesManager");
        Intrinsics.checkParameterIsNotNull(activityStarter, "activityStarter");
        Intrinsics.checkParameterIsNotNull(executor, "executor");
        Intrinsics.checkParameterIsNotNull(mediaManager, "mediaManager");
        Intrinsics.checkParameterIsNotNull(configurationController, "configurationController");
        Intrinsics.checkParameterIsNotNull(falsingManager, "falsingManager");
        this.context = context;
        this.mediaControlPanelFactory = mediaControlPanelFactory;
        this.visualStabilityManager = visualStabilityManager;
        this.mediaHostStatesManager = mediaHostStatesManager;
        this.activityStarter = activityStarter;
        this.mediaManager = mediaManager;
        this.desiredLocation = -1;
        this.currentEndLocation = -1;
        this.currentStartLocation = -1;
        this.currentTransitionProgress = 1.0f;
        this.keysNeedRemoval = new LinkedHashSet();
        this.currentlyExpanded = true;
        ?? r5 = new ConfigurationController.ConfigurationListener() { // from class: com.android.systemui.media.MediaCarouselController$configListener$1
            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onDensityOrFontScaleChanged() throws Resources.NotFoundException {
                this.this$0.recreatePlayers();
                this.this$0.inflateSettingsButton();
            }

            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onOverlayChanged() throws Resources.NotFoundException {
                this.this$0.recreatePlayers();
                this.this$0.inflateSettingsButton();
            }

            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onConfigChanged(@Nullable Configuration configuration) {
                if (configuration == null) {
                    return;
                }
                this.this$0.setRtl(configuration.getLayoutDirection() == 1);
            }
        };
        this.configListener = r5;
        ViewGroup viewGroupInflateMediaCarousel = inflateMediaCarousel();
        this.mediaFrame = viewGroupInflateMediaCarousel;
        View viewRequireViewById = viewGroupInflateMediaCarousel.requireViewById(R.id.media_carousel_scroller);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById, "mediaFrame.requireViewBy….media_carousel_scroller)");
        MediaScrollView mediaScrollView = (MediaScrollView) viewRequireViewById;
        this.mediaCarousel = mediaScrollView;
        View viewRequireViewById2 = viewGroupInflateMediaCarousel.requireViewById(R.id.media_page_indicator);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById2, "mediaFrame.requireViewBy….id.media_page_indicator)");
        PageIndicator pageIndicator = (PageIndicator) viewRequireViewById2;
        this.pageIndicator = pageIndicator;
        this.mediaCarouselScrollHandler = new MediaCarouselScrollHandler(mediaScrollView, pageIndicator, executor, new AnonymousClass1(mediaManager), new AnonymousClass2(this), new AnonymousClass3(this), falsingManager);
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration configuration = resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "context.resources.configuration");
        setRtl(configuration.getLayoutDirection() == 1);
        inflateSettingsButton();
        View viewRequireViewById3 = mediaScrollView.requireViewById(R.id.media_carousel);
        Intrinsics.checkExpressionValueIsNotNull(viewRequireViewById3, "mediaCarousel.requireViewById(R.id.media_carousel)");
        this.mediaContent = (ViewGroup) viewRequireViewById3;
        configurationController.addCallback(r5);
        VisualStabilityManager.Callback callback = new VisualStabilityManager.Callback() { // from class: com.android.systemui.media.MediaCarouselController.4
            @Override // com.android.systemui.statusbar.notification.VisualStabilityManager.Callback
            public final void onChangeAllowed() throws Resources.NotFoundException {
                if (MediaCarouselController.this.needsReordering) {
                    MediaCarouselController.this.needsReordering = false;
                    MediaCarouselController.this.reorderAllPlayers();
                }
                Iterator it = MediaCarouselController.this.keysNeedRemoval.iterator();
                while (it.hasNext()) {
                    MediaCarouselController.removePlayer$default(MediaCarouselController.this, (String) it.next(), false, 2, null);
                }
                MediaCarouselController.this.keysNeedRemoval.clear();
                MediaCarouselController.this.mediaCarouselScrollHandler.scrollToStart();
            }
        };
        this.visualStabilityCallback = callback;
        visualStabilityManager.addReorderingAllowedCallback(callback, true);
        mediaManager.addListener(new MediaDataManager.Listener() { // from class: com.android.systemui.media.MediaCarouselController.5
            @Override // com.android.systemui.media.MediaDataManager.Listener
            public void onMediaDataLoaded(@NotNull String key, @Nullable String str, @NotNull MediaData data) throws Resources.NotFoundException {
                Intrinsics.checkParameterIsNotNull(key, "key");
                Intrinsics.checkParameterIsNotNull(data, "data");
                MediaCarouselController.this.addOrUpdatePlayer(key, str, data);
                Boolean boolIsPlaying = data.isPlaying();
                if (!((boolIsPlaying != null ? boolIsPlaying.booleanValue() ^ true : data.isClearable()) && !data.getActive()) || Utils.useMediaResumption(MediaCarouselController.this.context)) {
                    MediaCarouselController.this.keysNeedRemoval.remove(key);
                } else if (!MediaCarouselController.this.visualStabilityManager.isReorderingAllowed()) {
                    MediaCarouselController.this.keysNeedRemoval.add(key);
                } else {
                    onMediaDataRemoved(key);
                }
            }

            @Override // com.android.systemui.media.MediaDataManager.Listener
            public void onMediaDataRemoved(@NotNull String key) throws Resources.NotFoundException {
                Intrinsics.checkParameterIsNotNull(key, "key");
                MediaCarouselController.removePlayer$default(MediaCarouselController.this, key, false, 2, null);
            }
        });
        viewGroupInflateMediaCarousel.addOnLayoutChangeListener(new View.OnLayoutChangeListener() { // from class: com.android.systemui.media.MediaCarouselController.6
            @Override // android.view.View.OnLayoutChangeListener
            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                MediaCarouselController.this.updatePageIndicatorLocation();
            }
        });
        mediaHostStatesManager.addCallback(new MediaHostStatesManager.Callback() { // from class: com.android.systemui.media.MediaCarouselController.7
            @Override // com.android.systemui.media.MediaHostStatesManager.Callback
            public void onHostStateChanged(int i, @NotNull MediaHostState mediaHostState) {
                Intrinsics.checkParameterIsNotNull(mediaHostState, "mediaHostState");
                if (i == MediaCarouselController.this.desiredLocation) {
                    MediaCarouselController mediaCarouselController = MediaCarouselController.this;
                    MediaCarouselController.onDesiredLocationChanged$default(mediaCarouselController, mediaCarouselController.desiredLocation, mediaHostState, false, 0L, 0L, 24, null);
                }
            }
        });
    }

    @NotNull
    public final ViewGroup getMediaFrame() {
        return this.mediaFrame;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void setRtl(boolean z) {
        if (z != this.isRtl) {
            this.isRtl = z;
            this.mediaFrame.setLayoutDirection(z ? 1 : 0);
            this.mediaCarouselScrollHandler.scrollToStart();
        }
    }

    private final void setCurrentlyExpanded(boolean z) {
        if (this.currentlyExpanded != z) {
            this.currentlyExpanded = z;
            Iterator<MediaControlPanel> it = MediaPlayerData.INSTANCE.players().iterator();
            while (it.hasNext()) {
                it.next().setListening(this.currentlyExpanded);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void inflateSettingsButton() {
        View viewInflate = LayoutInflater.from(this.context).inflate(R.layout.media_carousel_settings_button, this.mediaFrame, false);
        if (viewInflate == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.View");
        }
        View view = this.settingsButton;
        if (view != null) {
            ViewGroup viewGroup = this.mediaFrame;
            if (view == null) {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
            }
            viewGroup.removeView(view);
        }
        this.settingsButton = viewInflate;
        this.mediaFrame.addView(viewInflate);
        this.mediaCarouselScrollHandler.onSettingsButtonUpdated(viewInflate);
        View view2 = this.settingsButton;
        if (view2 == null) {
            Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
        }
        view2.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.MediaCarouselController.inflateSettingsButton.2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view3) {
                MediaCarouselController.this.activityStarter.startActivity(MediaCarouselControllerKt.settingsIntent, true);
            }
        });
    }

    private final ViewGroup inflateMediaCarousel() {
        View viewInflate = LayoutInflater.from(this.context).inflate(R.layout.media_carousel, (ViewGroup) new UniqueObjectHostView(this.context), false);
        if (viewInflate == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
        }
        ViewGroup viewGroup = (ViewGroup) viewInflate;
        viewGroup.setLayoutDirection(3);
        return viewGroup;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void reorderAllPlayers() throws Resources.NotFoundException {
        this.mediaContent.removeAllViews();
        for (MediaControlPanel mediaPlayer : MediaPlayerData.INSTANCE.players()) {
            Intrinsics.checkExpressionValueIsNotNull(mediaPlayer, "mediaPlayer");
            PlayerViewHolder view = mediaPlayer.getView();
            if (view != null) {
                this.mediaContent.addView(view.getPlayer());
            }
        }
        this.mediaCarouselScrollHandler.onPlayersChanged();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void addOrUpdatePlayer(String str, String str2, MediaData mediaData) throws Resources.NotFoundException {
        TransitionLayout player;
        MediaPlayerData mediaPlayerData = MediaPlayerData.INSTANCE;
        MediaControlPanel mediaPlayer = mediaPlayerData.getMediaPlayer(str, str2);
        if (mediaPlayer == null) {
            MediaControlPanel newPlayer = this.mediaControlPanelFactory.get();
            PlayerViewHolder.Companion companion = PlayerViewHolder.Companion;
            LayoutInflater layoutInflaterFrom = LayoutInflater.from(this.context);
            Intrinsics.checkExpressionValueIsNotNull(layoutInflaterFrom, "LayoutInflater.from(context)");
            newPlayer.attach(companion.create(layoutInflaterFrom, this.mediaContent));
            Intrinsics.checkExpressionValueIsNotNull(newPlayer, "newPlayer");
            newPlayer.getMediaViewController().setSizeChangedListener(new C00801(this));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            PlayerViewHolder view = newPlayer.getView();
            if (view != null && (player = view.getPlayer()) != null) {
                player.setLayoutParams(layoutParams);
            }
            newPlayer.bind(mediaData, str);
            newPlayer.setListening(this.currentlyExpanded);
            mediaPlayerData.addMediaPlayer(str, mediaData, newPlayer);
            updatePlayerToState(newPlayer, true);
            reorderAllPlayers();
        } else {
            mediaPlayer.bind(mediaData, str);
            mediaPlayerData.addMediaPlayer(str, mediaData, mediaPlayer);
            if (this.visualStabilityManager.isReorderingAllowed()) {
                reorderAllPlayers();
            } else {
                this.needsReordering = true;
            }
        }
        updatePageIndicator();
        this.mediaCarouselScrollHandler.onPlayersChanged();
        UniqueObjectHostViewKt.setRequiresRemeasuring(this.mediaCarousel, true);
        if (mediaPlayerData.players().size() != this.mediaContent.getChildCount()) {
            Log.wtf("MediaCarouselController", "Size of players list and number of views in carousel are out of sync");
        }
    }

    static /* synthetic */ void removePlayer$default(MediaCarouselController mediaCarouselController, String str, boolean z, int i, Object obj) throws Resources.NotFoundException {
        if ((i & 2) != 0) {
            z = true;
        }
        mediaCarouselController.removePlayer(str, z);
    }

    private final void removePlayer(String str, boolean z) throws Resources.NotFoundException {
        MediaControlPanel mediaControlPanelRemoveMediaPlayer = MediaPlayerData.INSTANCE.removeMediaPlayer(str);
        if (mediaControlPanelRemoveMediaPlayer != null) {
            this.mediaCarouselScrollHandler.onPrePlayerRemoved(mediaControlPanelRemoveMediaPlayer);
            ViewGroup viewGroup = this.mediaContent;
            PlayerViewHolder view = mediaControlPanelRemoveMediaPlayer.getView();
            viewGroup.removeView(view != null ? view.getPlayer() : null);
            mediaControlPanelRemoveMediaPlayer.onDestroy();
            this.mediaCarouselScrollHandler.onPlayersChanged();
            updatePageIndicator();
            if (z) {
                this.mediaManager.dismissMediaData(str, 0L);
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void recreatePlayers() throws Resources.NotFoundException {
        Iterator<T> it = MediaPlayerData.INSTANCE.mediaData().iterator();
        while (it.hasNext()) {
            Pair pair = (Pair) it.next();
            String str = (String) pair.component1();
            MediaData mediaData = (MediaData) pair.component2();
            removePlayer(str, false);
            addOrUpdatePlayer(str, null, mediaData);
        }
    }

    private final void updatePageIndicator() {
        int childCount = this.mediaContent.getChildCount();
        this.pageIndicator.setNumPages(childCount, -1);
        if (childCount == 1) {
            this.pageIndicator.setLocation(0.0f);
        }
        updatePageIndicatorAlpha();
    }

    public final void setCurrentState(int i, int i2, float f, boolean z) {
        if (i == this.currentStartLocation && i2 == this.currentEndLocation && f == this.currentTransitionProgress && !z) {
            return;
        }
        this.currentStartLocation = i;
        this.currentEndLocation = i2;
        this.currentTransitionProgress = f;
        for (MediaControlPanel mediaPlayer : MediaPlayerData.INSTANCE.players()) {
            Intrinsics.checkExpressionValueIsNotNull(mediaPlayer, "mediaPlayer");
            updatePlayerToState(mediaPlayer, z);
        }
        maybeResetSettingsCog();
        updatePageIndicatorAlpha();
    }

    private final void updatePageIndicatorAlpha() {
        Map<Integer, MediaHostState> mediaHostStates = this.mediaHostStatesManager.getMediaHostStates();
        MediaHostState mediaHostState = mediaHostStates.get(Integer.valueOf(this.currentEndLocation));
        boolean visible = mediaHostState != null ? mediaHostState.getVisible() : false;
        MediaHostState mediaHostState2 = mediaHostStates.get(Integer.valueOf(this.currentStartLocation));
        boolean visible2 = mediaHostState2 != null ? mediaHostState2.getVisible() : false;
        float fLerp = 1.0f;
        float f = visible2 ? 1.0f : 0.0f;
        float f2 = visible ? 1.0f : 0.0f;
        if (!visible || !visible2) {
            float f3 = this.currentTransitionProgress;
            if (!visible) {
                f3 = 1.0f - f3;
            }
            fLerp = MathUtils.lerp(f, f2, MathUtils.constrain(MathUtils.map(0.95f, 1.0f, 0.0f, 1.0f, f3), 0.0f, 1.0f));
        }
        this.pageIndicator.setAlpha(fLerp);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updatePageIndicatorLocation() {
        int width;
        int width2;
        if (this.isRtl) {
            width = this.pageIndicator.getWidth();
            width2 = this.currentCarouselWidth;
        } else {
            width = this.currentCarouselWidth;
            width2 = this.pageIndicator.getWidth();
        }
        this.pageIndicator.setTranslationX(((width - width2) / 2.0f) + this.mediaCarouselScrollHandler.getContentTranslation());
        if (this.pageIndicator.getLayoutParams() == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
        }
        this.pageIndicator.setTranslationY((this.currentCarouselHeight - r1.getHeight()) - ((ViewGroup.MarginLayoutParams) r0).bottomMargin);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public final void updateCarouselDimensions() {
        int iMax = 0;
        int iMax2 = 0;
        for (MediaControlPanel mediaPlayer : MediaPlayerData.INSTANCE.players()) {
            Intrinsics.checkExpressionValueIsNotNull(mediaPlayer, "mediaPlayer");
            MediaViewController mediaViewController = mediaPlayer.getMediaViewController();
            Intrinsics.checkExpressionValueIsNotNull(mediaViewController, "mediaPlayer.mediaViewController");
            iMax = Math.max(iMax, mediaViewController.getCurrentWidth() + ((int) mediaViewController.getTranslationX()));
            iMax2 = Math.max(iMax2, mediaViewController.getCurrentHeight() + ((int) mediaViewController.getTranslationY()));
        }
        if (iMax == this.currentCarouselWidth && iMax2 == this.currentCarouselHeight) {
            return;
        }
        this.currentCarouselWidth = iMax;
        this.currentCarouselHeight = iMax2;
        this.mediaCarouselScrollHandler.setCarouselBounds(iMax, iMax2);
        updatePageIndicatorLocation();
    }

    private final void maybeResetSettingsCog() {
        Map<Integer, MediaHostState> mediaHostStates = this.mediaHostStatesManager.getMediaHostStates();
        MediaHostState mediaHostState = mediaHostStates.get(Integer.valueOf(this.currentEndLocation));
        boolean showsOnlyActiveMedia = mediaHostState != null ? mediaHostState.getShowsOnlyActiveMedia() : true;
        MediaHostState mediaHostState2 = mediaHostStates.get(Integer.valueOf(this.currentStartLocation));
        boolean showsOnlyActiveMedia2 = mediaHostState2 != null ? mediaHostState2.getShowsOnlyActiveMedia() : showsOnlyActiveMedia;
        if (this.currentlyShowingOnlyActive == showsOnlyActiveMedia) {
            float f = this.currentTransitionProgress;
            if (f == 1.0f || f == 0.0f || showsOnlyActiveMedia2 == showsOnlyActiveMedia) {
                return;
            }
        }
        this.currentlyShowingOnlyActive = showsOnlyActiveMedia;
        this.mediaCarouselScrollHandler.resetTranslation(true);
    }

    private final void updatePlayerToState(MediaControlPanel mediaControlPanel, boolean z) {
        mediaControlPanel.getMediaViewController().setCurrentState(this.currentStartLocation, this.currentEndLocation, this.currentTransitionProgress, z);
    }

    public static /* synthetic */ void onDesiredLocationChanged$default(MediaCarouselController mediaCarouselController, int i, MediaHostState mediaHostState, boolean z, long j, long j2, int i2, Object obj) {
        mediaCarouselController.onDesiredLocationChanged(i, mediaHostState, z, (i2 & 8) != 0 ? 200L : j, (i2 & 16) != 0 ? 0L : j2);
    }

    public final void onDesiredLocationChanged(int i, @Nullable MediaHostState mediaHostState, boolean z, long j, long j2) {
        if (mediaHostState != null) {
            this.desiredLocation = i;
            this.desiredHostState = mediaHostState;
            setCurrentlyExpanded(mediaHostState.getExpansion() > ((float) 0));
            for (MediaControlPanel mediaPlayer : MediaPlayerData.INSTANCE.players()) {
                if (z) {
                    Intrinsics.checkExpressionValueIsNotNull(mediaPlayer, "mediaPlayer");
                    mediaPlayer.getMediaViewController().animatePendingStateChange(j, j2);
                }
                Intrinsics.checkExpressionValueIsNotNull(mediaPlayer, "mediaPlayer");
                mediaPlayer.getMediaViewController().onLocationPreChange(i);
            }
            this.mediaCarouselScrollHandler.setShowsSettingsButton(!mediaHostState.getShowsOnlyActiveMedia());
            this.mediaCarouselScrollHandler.setFalsingProtectionNeeded(mediaHostState.getFalsingProtectionNeeded());
            boolean visible = mediaHostState.getVisible();
            if (visible != this.playersVisible) {
                this.playersVisible = visible;
                if (visible) {
                    MediaCarouselScrollHandler.resetTranslation$default(this.mediaCarouselScrollHandler, false, 1, null);
                }
            }
            updateCarouselSize();
        }
    }

    public final void closeGuts() {
        Iterator<T> it = MediaPlayerData.INSTANCE.players().iterator();
        while (it.hasNext()) {
            ((MediaControlPanel) it.next()).closeGuts(true);
        }
    }

    private final void updateCarouselSize() {
        MeasurementInput measurementInput;
        MeasurementInput measurementInput2;
        MeasurementInput measurementInput3;
        MeasurementInput measurementInput4;
        MediaHostState mediaHostState = this.desiredHostState;
        int width = (mediaHostState == null || (measurementInput4 = mediaHostState.getMeasurementInput()) == null) ? 0 : measurementInput4.getWidth();
        MediaHostState mediaHostState2 = this.desiredHostState;
        int height = (mediaHostState2 == null || (measurementInput3 = mediaHostState2.getMeasurementInput()) == null) ? 0 : measurementInput3.getHeight();
        if ((width == this.carouselMeasureWidth || width == 0) && (height == this.carouselMeasureHeight || height == 0)) {
            return;
        }
        this.carouselMeasureWidth = width;
        this.carouselMeasureHeight = height;
        int dimensionPixelSize = this.context.getResources().getDimensionPixelSize(R.dimen.qs_media_padding) + width;
        MediaHostState mediaHostState3 = this.desiredHostState;
        int widthMeasureSpec = (mediaHostState3 == null || (measurementInput2 = mediaHostState3.getMeasurementInput()) == null) ? 0 : measurementInput2.getWidthMeasureSpec();
        MediaHostState mediaHostState4 = this.desiredHostState;
        this.mediaCarousel.measure(widthMeasureSpec, (mediaHostState4 == null || (measurementInput = mediaHostState4.getMeasurementInput()) == null) ? 0 : measurementInput.getHeightMeasureSpec());
        MediaScrollView mediaScrollView = this.mediaCarousel;
        mediaScrollView.layout(0, 0, width, mediaScrollView.getMeasuredHeight());
        this.mediaCarouselScrollHandler.setPlayerWidthPlusPadding(dimensionPixelSize);
    }
}
