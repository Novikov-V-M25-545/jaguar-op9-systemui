package com.android.systemui.media;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.util.animation.TransitionLayout;
import java.util.Set;
import kotlin.TypeCastException;
import kotlin.collections.SetsKt__SetsKt;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PlayerViewHolder.kt */
/* loaded from: classes.dex */
public final class PlayerViewHolder {
    public static final Companion Companion = new Companion(null);

    @NotNull
    private static final Set<Integer> controlsIds;

    @NotNull
    private static final Set<Integer> gutsIds;
    private final ImageButton action0;
    private final ImageButton action1;
    private final ImageButton action2;
    private final ImageButton action3;
    private final ImageButton action4;
    private final ImageView albumView;
    private final ImageView appIcon;
    private final TextView appName;
    private final TextView artistText;
    private final View cancel;
    private final ViewGroup dismiss;
    private final View dismissLabel;
    private final TextView elapsedTimeView;

    @NotNull
    private final TransitionLayout player;
    private final ViewGroup progressTimes;
    private final ViewGroup seamless;
    private final ImageView seamlessFallback;
    private final ImageView seamlessIcon;
    private final TextView seamlessText;
    private final SeekBar seekBar;
    private final View settings;
    private final TextView settingsText;
    private final TextView titleText;
    private final TextView totalTimeView;

    private PlayerViewHolder(View view) {
        if (view != null) {
            TransitionLayout transitionLayout = (TransitionLayout) view;
            this.player = transitionLayout;
            this.appIcon = (ImageView) view.requireViewById(R.id.icon);
            this.appName = (TextView) view.requireViewById(R.id.app_name);
            this.albumView = (ImageView) view.requireViewById(R.id.album_art);
            this.titleText = (TextView) view.requireViewById(R.id.header_title);
            this.artistText = (TextView) view.requireViewById(R.id.header_artist);
            ViewGroup seamless = (ViewGroup) view.requireViewById(R.id.media_seamless);
            this.seamless = seamless;
            this.seamlessIcon = (ImageView) view.requireViewById(R.id.media_seamless_image);
            this.seamlessText = (TextView) view.requireViewById(R.id.media_seamless_text);
            this.seamlessFallback = (ImageView) view.requireViewById(R.id.media_seamless_fallback);
            this.seekBar = (SeekBar) view.requireViewById(R.id.media_progress_bar);
            this.progressTimes = (ViewGroup) view.requireViewById(R.id.notification_media_progress_time);
            this.elapsedTimeView = (TextView) view.requireViewById(R.id.media_elapsed_time);
            this.totalTimeView = (TextView) view.requireViewById(R.id.media_total_time);
            ImageButton action0 = (ImageButton) view.requireViewById(R.id.action0);
            this.action0 = action0;
            ImageButton action1 = (ImageButton) view.requireViewById(R.id.action1);
            this.action1 = action1;
            ImageButton action2 = (ImageButton) view.requireViewById(R.id.action2);
            this.action2 = action2;
            ImageButton action3 = (ImageButton) view.requireViewById(R.id.action3);
            this.action3 = action3;
            ImageButton action4 = (ImageButton) view.requireViewById(R.id.action4);
            this.action4 = action4;
            this.settingsText = (TextView) view.requireViewById(R.id.remove_text);
            View cancel = view.requireViewById(R.id.cancel);
            this.cancel = cancel;
            ViewGroup dismiss = (ViewGroup) view.requireViewById(R.id.dismiss);
            this.dismiss = dismiss;
            this.dismissLabel = dismiss.getChildAt(0);
            View settings = view.requireViewById(R.id.settings);
            this.settings = settings;
            Drawable background = transitionLayout.getBackground();
            if (background == null) {
                throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.media.IlluminationDrawable");
            }
            IlluminationDrawable illuminationDrawable = (IlluminationDrawable) background;
            Intrinsics.checkExpressionValueIsNotNull(seamless, "seamless");
            illuminationDrawable.registerLightSource(seamless);
            Intrinsics.checkExpressionValueIsNotNull(action0, "action0");
            illuminationDrawable.registerLightSource(action0);
            Intrinsics.checkExpressionValueIsNotNull(action1, "action1");
            illuminationDrawable.registerLightSource(action1);
            Intrinsics.checkExpressionValueIsNotNull(action2, "action2");
            illuminationDrawable.registerLightSource(action2);
            Intrinsics.checkExpressionValueIsNotNull(action3, "action3");
            illuminationDrawable.registerLightSource(action3);
            Intrinsics.checkExpressionValueIsNotNull(action4, "action4");
            illuminationDrawable.registerLightSource(action4);
            Intrinsics.checkExpressionValueIsNotNull(cancel, "cancel");
            illuminationDrawable.registerLightSource(cancel);
            Intrinsics.checkExpressionValueIsNotNull(dismiss, "dismiss");
            illuminationDrawable.registerLightSource(dismiss);
            Intrinsics.checkExpressionValueIsNotNull(settings, "settings");
            illuminationDrawable.registerLightSource(settings);
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.util.animation.TransitionLayout");
    }

    public /* synthetic */ PlayerViewHolder(View view, DefaultConstructorMarker defaultConstructorMarker) {
        this(view);
    }

    @NotNull
    public final TransitionLayout getPlayer() {
        return this.player;
    }

    public final ImageView getAppIcon() {
        return this.appIcon;
    }

    public final TextView getAppName() {
        return this.appName;
    }

    public final ImageView getAlbumView() {
        return this.albumView;
    }

    public final TextView getTitleText() {
        return this.titleText;
    }

    public final TextView getArtistText() {
        return this.artistText;
    }

    public final ViewGroup getSeamless() {
        return this.seamless;
    }

    public final ImageView getSeamlessIcon() {
        return this.seamlessIcon;
    }

    public final TextView getSeamlessText() {
        return this.seamlessText;
    }

    public final ImageView getSeamlessFallback() {
        return this.seamlessFallback;
    }

    public final SeekBar getSeekBar() {
        return this.seekBar;
    }

    public final ViewGroup getProgressTimes() {
        return this.progressTimes;
    }

    public final TextView getElapsedTimeView() {
        return this.elapsedTimeView;
    }

    public final TextView getTotalTimeView() {
        return this.totalTimeView;
    }

    public final TextView getSettingsText() {
        return this.settingsText;
    }

    public final View getCancel() {
        return this.cancel;
    }

    public final ViewGroup getDismiss() {
        return this.dismiss;
    }

    public final View getDismissLabel() {
        return this.dismissLabel;
    }

    public final View getSettings() {
        return this.settings;
    }

    @NotNull
    public final ImageButton getAction(int i) {
        if (i == R.id.action0) {
            ImageButton action0 = this.action0;
            Intrinsics.checkExpressionValueIsNotNull(action0, "action0");
            return action0;
        }
        if (i == R.id.action1) {
            ImageButton action1 = this.action1;
            Intrinsics.checkExpressionValueIsNotNull(action1, "action1");
            return action1;
        }
        if (i == R.id.action2) {
            ImageButton action2 = this.action2;
            Intrinsics.checkExpressionValueIsNotNull(action2, "action2");
            return action2;
        }
        if (i == R.id.action3) {
            ImageButton action3 = this.action3;
            Intrinsics.checkExpressionValueIsNotNull(action3, "action3");
            return action3;
        }
        if (i != R.id.action4) {
            throw new IllegalArgumentException();
        }
        ImageButton action4 = this.action4;
        Intrinsics.checkExpressionValueIsNotNull(action4, "action4");
        return action4;
    }

    /* compiled from: PlayerViewHolder.kt */
    public static final class Companion {
        private Companion() {
        }

        public /* synthetic */ Companion(DefaultConstructorMarker defaultConstructorMarker) {
            this();
        }

        @NotNull
        public final PlayerViewHolder create(@NotNull LayoutInflater inflater, @NotNull ViewGroup parent) {
            Intrinsics.checkParameterIsNotNull(inflater, "inflater");
            Intrinsics.checkParameterIsNotNull(parent, "parent");
            View mediaView = inflater.inflate(R.layout.media_view, parent, false);
            Intrinsics.checkExpressionValueIsNotNull(mediaView, "mediaView");
            mediaView.setLayoutDirection(3);
            PlayerViewHolder playerViewHolder = new PlayerViewHolder(mediaView, null);
            SeekBar seekBar = playerViewHolder.getSeekBar();
            Intrinsics.checkExpressionValueIsNotNull(seekBar, "seekBar");
            seekBar.setLayoutDirection(0);
            ViewGroup progressTimes = playerViewHolder.getProgressTimes();
            Intrinsics.checkExpressionValueIsNotNull(progressTimes, "progressTimes");
            progressTimes.setLayoutDirection(0);
            return playerViewHolder;
        }

        @NotNull
        public final Set<Integer> getControlsIds() {
            return PlayerViewHolder.controlsIds;
        }

        @NotNull
        public final Set<Integer> getGutsIds() {
            return PlayerViewHolder.gutsIds;
        }
    }

    static {
        int i = R.id.icon;
        controlsIds = SetsKt__SetsKt.setOf((Object[]) new Integer[]{Integer.valueOf(i), Integer.valueOf(R.id.app_name), Integer.valueOf(R.id.album_art), Integer.valueOf(R.id.header_title), Integer.valueOf(R.id.header_artist), Integer.valueOf(R.id.media_seamless), Integer.valueOf(R.id.notification_media_progress_time), Integer.valueOf(R.id.media_progress_bar), Integer.valueOf(R.id.action0), Integer.valueOf(R.id.action1), Integer.valueOf(R.id.action2), Integer.valueOf(R.id.action3), Integer.valueOf(R.id.action4), Integer.valueOf(i)});
        gutsIds = SetsKt__SetsKt.setOf((Object[]) new Integer[]{Integer.valueOf(R.id.media_text), Integer.valueOf(R.id.remove_text), Integer.valueOf(R.id.cancel), Integer.valueOf(R.id.dismiss), Integer.valueOf(R.id.settings)});
    }
}
