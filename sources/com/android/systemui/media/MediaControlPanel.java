package com.android.systemui.media;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.settingslib.Utils;
import com.android.settingslib.widget.AdaptiveIcon;
import com.android.systemui.R;
import com.android.systemui.media.dialog.MediaOutputDialogFactory;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.util.animation.TransitionLayout;
import dagger.Lazy;
import java.util.List;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public class MediaControlPanel {
    private final ActivityStarter mActivityStarter;
    private int mAlbumArtRadius;
    private int mAlbumArtSize;
    private int mBackgroundColor;
    protected final Executor mBackgroundExecutor;
    private Context mContext;
    private MediaController mController;
    private String mKey;
    private KeyguardDismissUtil mKeyguardDismissUtil;
    private Lazy<MediaDataManager> mMediaDataManagerLazy;
    private final MediaOutputDialogFactory mMediaOutputDialogFactory;
    private MediaViewController mMediaViewController;
    private SeekBarObserver mSeekBarObserver;
    private final SeekBarViewModel mSeekBarViewModel;
    private MediaSession.Token mToken;
    private PlayerViewHolder mViewHolder;
    private final ViewOutlineProvider mViewOutlineProvider;
    private static final Intent SETTINGS_INTENT = new Intent("android.settings.ACTION_MEDIA_CONTROLS_SETTINGS");
    static final int[] ACTION_IDS = {R.id.action0, R.id.action1, R.id.action2, R.id.action3, R.id.action4};

    public MediaControlPanel(Context context, Executor executor, ActivityStarter activityStarter, MediaViewController mediaViewController, SeekBarViewModel seekBarViewModel, Lazy<MediaDataManager> lazy, KeyguardDismissUtil keyguardDismissUtil, MediaOutputDialogFactory mediaOutputDialogFactory) {
        this.mContext = context;
        this.mBackgroundExecutor = executor;
        this.mActivityStarter = activityStarter;
        this.mSeekBarViewModel = seekBarViewModel;
        this.mMediaViewController = mediaViewController;
        this.mMediaDataManagerLazy = lazy;
        this.mKeyguardDismissUtil = keyguardDismissUtil;
        this.mMediaOutputDialogFactory = mediaOutputDialogFactory;
        loadDimens();
        this.mViewOutlineProvider = new ViewOutlineProvider() { // from class: com.android.systemui.media.MediaControlPanel.1
            @Override // android.view.ViewOutlineProvider
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, MediaControlPanel.this.mAlbumArtSize, MediaControlPanel.this.mAlbumArtSize, MediaControlPanel.this.mAlbumArtRadius);
            }
        };
    }

    public void onDestroy() {
        if (this.mSeekBarObserver != null) {
            this.mSeekBarViewModel.getProgress().removeObserver(this.mSeekBarObserver);
        }
        this.mSeekBarViewModel.onDestroy();
        this.mMediaViewController.onDestroy();
    }

    private void loadDimens() {
        this.mAlbumArtRadius = this.mContext.getResources().getDimensionPixelSize(Utils.getThemeAttr(this.mContext, android.R.attr.dialogCornerRadius));
        this.mAlbumArtSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_media_album_size);
    }

    public PlayerViewHolder getView() {
        return this.mViewHolder;
    }

    public MediaViewController getMediaViewController() {
        return this.mMediaViewController;
    }

    public void setListening(boolean z) {
        this.mSeekBarViewModel.setListening(z);
    }

    public void attach(PlayerViewHolder playerViewHolder) {
        this.mViewHolder = playerViewHolder;
        TransitionLayout player = playerViewHolder.getPlayer();
        ImageView albumView = playerViewHolder.getAlbumView();
        albumView.setOutlineProvider(this.mViewOutlineProvider);
        albumView.setClipToOutline(true);
        this.mSeekBarObserver = new SeekBarObserver(playerViewHolder);
        this.mSeekBarViewModel.getProgress().observeForever(this.mSeekBarObserver);
        this.mSeekBarViewModel.attachTouchHandlers(playerViewHolder.getSeekBar());
        this.mMediaViewController.attach(player);
        this.mViewHolder.getPlayer().setOnLongClickListener(new View.OnLongClickListener() { // from class: com.android.systemui.media.MediaControlPanel$$ExternalSyntheticLambda6
            @Override // android.view.View.OnLongClickListener
            public final boolean onLongClick(View view) {
                return this.f$0.lambda$attach$0(view);
            }
        });
        this.mViewHolder.getCancel().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.MediaControlPanel$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$attach$1(view);
            }
        });
        this.mViewHolder.getSettings().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.MediaControlPanel$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$attach$2(view);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$attach$0(View view) {
        if (this.mMediaViewController.isGutsVisible()) {
            return false;
        }
        this.mMediaViewController.openGuts();
        return true;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$attach$1(View view) {
        closeGuts();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$attach$2(View view) {
        this.mActivityStarter.startActivity(SETTINGS_INTENT, true);
    }

    public void bind(final MediaData mediaData, String str) {
        int i;
        if (this.mViewHolder == null) {
            return;
        }
        this.mKey = str;
        MediaSession.Token token = mediaData.getToken();
        this.mBackgroundColor = mediaData.getBackgroundColor();
        MediaSession.Token token2 = this.mToken;
        if (token2 == null || !token2.equals(token)) {
            this.mToken = token;
        }
        if (this.mToken != null) {
            this.mController = new MediaController(this.mContext, this.mToken);
        } else {
            this.mController = null;
        }
        ConstraintSet expandedLayout = this.mMediaViewController.getExpandedLayout();
        ConstraintSet collapsedLayout = this.mMediaViewController.getCollapsedLayout();
        this.mViewHolder.getPlayer().setBackgroundTintList(ColorStateList.valueOf(this.mBackgroundColor));
        final PendingIntent clickIntent = mediaData.getClickIntent();
        if (clickIntent != null) {
            this.mViewHolder.getPlayer().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.MediaControlPanel$$ExternalSyntheticLambda2
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.lambda$bind$3(clickIntent, view);
                }
            });
        }
        ImageView albumView = this.mViewHolder.getAlbumView();
        boolean z = mediaData.getArtwork() != null;
        if (z) {
            albumView.setImageDrawable(scaleDrawable(mediaData.getArtwork()));
        }
        int i2 = R.id.album_art;
        setVisibleAndAlpha(collapsedLayout, i2, z);
        setVisibleAndAlpha(expandedLayout, i2, z);
        ImageView appIcon = this.mViewHolder.getAppIcon();
        if (mediaData.getAppIcon() != null) {
            appIcon.setImageDrawable(mediaData.getAppIcon());
        } else {
            appIcon.setImageDrawable(this.mContext.getDrawable(R.drawable.ic_music_note));
        }
        this.mViewHolder.getTitleText().setText(Notification.safeCharSequence(mediaData.getSong()));
        this.mViewHolder.getAppName().setText(mediaData.getApp());
        this.mViewHolder.getArtistText().setText(Notification.safeCharSequence(mediaData.getArtist()));
        this.mViewHolder.getSeamless().setVisibility(0);
        int i3 = R.id.media_seamless;
        setVisibleAndAlpha(collapsedLayout, i3, true);
        setVisibleAndAlpha(expandedLayout, i3, true);
        this.mViewHolder.getSeamless().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.MediaControlPanel$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$bind$4(mediaData, view);
            }
        });
        ImageView seamlessIcon = this.mViewHolder.getSeamlessIcon();
        TextView seamlessText = this.mViewHolder.getSeamlessText();
        MediaDeviceData device = mediaData.getDevice();
        int id = this.mViewHolder.getSeamless().getId();
        int id2 = this.mViewHolder.getSeamlessFallback().getId();
        boolean z2 = (device == null || device.getEnabled()) ? false : true;
        int i4 = z2 ? 0 : 8;
        this.mViewHolder.getSeamlessFallback().setVisibility(i4);
        expandedLayout.setVisibility(id2, i4);
        collapsedLayout.setVisibility(id2, i4);
        int i5 = z2 ? 8 : 0;
        this.mViewHolder.getSeamless().setVisibility(i5);
        expandedLayout.setVisibility(id, i5);
        collapsedLayout.setVisibility(id, i5);
        float f = mediaData.getResumption() ? 0.38f : 1.0f;
        expandedLayout.setAlpha(id, f);
        collapsedLayout.setAlpha(id, f);
        this.mViewHolder.getSeamless().setEnabled(!mediaData.getResumption());
        if (z2) {
            seamlessIcon.setImageDrawable(null);
            seamlessText.setText((CharSequence) null);
        } else if (device != null) {
            Drawable icon = device.getIcon();
            seamlessIcon.setVisibility(0);
            if (icon instanceof AdaptiveIcon) {
                AdaptiveIcon adaptiveIcon = (AdaptiveIcon) icon;
                adaptiveIcon.setBackgroundColor(this.mBackgroundColor);
                seamlessIcon.setImageDrawable(adaptiveIcon);
            } else {
                seamlessIcon.setImageDrawable(icon);
            }
            seamlessText.setText(device.getName());
        } else {
            Log.w("MediaControlPanel", "device is null. Not binding output chip.");
            seamlessIcon.setVisibility(8);
            seamlessText.setText(android.R.string.date_picker_decrement_month_button);
        }
        List<Integer> actionsToShowInCompact = mediaData.getActionsToShowInCompact();
        List<MediaAction> actions = mediaData.getActions();
        int i6 = 0;
        while (i6 < actions.size()) {
            int[] iArr = ACTION_IDS;
            if (i6 >= iArr.length) {
                break;
            }
            int i7 = iArr[i6];
            ImageButton action = this.mViewHolder.getAction(i7);
            MediaAction mediaAction = actions.get(i6);
            action.setImageDrawable(mediaAction.getDrawable());
            action.setContentDescription(mediaAction.getContentDescription());
            final Runnable action2 = mediaAction.getAction();
            if (action2 == null) {
                action.setEnabled(false);
            } else {
                action.setEnabled(true);
                action.setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.MediaControlPanel$$ExternalSyntheticLambda5
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        action2.run();
                    }
                });
            }
            setVisibleAndAlpha(collapsedLayout, i7, actionsToShowInCompact.contains(Integer.valueOf(i6)));
            setVisibleAndAlpha(expandedLayout, i7, true);
            i6++;
        }
        while (true) {
            int[] iArr2 = ACTION_IDS;
            if (i6 >= iArr2.length) {
                break;
            }
            setVisibleAndAlpha(expandedLayout, iArr2[i6], false);
            setVisibleAndAlpha(collapsedLayout, iArr2[i6], false);
            i6++;
        }
        final MediaController controller = getController();
        this.mBackgroundExecutor.execute(new Runnable() { // from class: com.android.systemui.media.MediaControlPanel$$ExternalSyntheticLambda8
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.lambda$bind$6(controller);
            }
        });
        boolean zIsClearable = mediaData.isClearable();
        TextView settingsText = this.mViewHolder.getSettingsText();
        if (zIsClearable) {
            i = R.string.controls_media_close_session;
        } else {
            i = R.string.controls_media_active_session;
        }
        settingsText.setText(i);
        this.mViewHolder.getDismissLabel().setAlpha(zIsClearable ? 1.0f : 0.38f);
        this.mViewHolder.getDismiss().setEnabled(zIsClearable);
        this.mViewHolder.getDismiss().setOnClickListener(new View.OnClickListener() { // from class: com.android.systemui.media.MediaControlPanel$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$bind$8(mediaData, view);
            }
        });
        this.mMediaViewController.refreshState();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bind$3(PendingIntent pendingIntent, View view) {
        if (this.mMediaViewController.isGutsVisible()) {
            return;
        }
        this.mActivityStarter.postStartActivityDismissingKeyguard(pendingIntent);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bind$4(MediaData mediaData, View view) {
        this.mMediaOutputDialogFactory.create(mediaData.getPackageName(), true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bind$6(MediaController mediaController) {
        this.mSeekBarViewModel.updateController(mediaController);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bind$8(MediaData mediaData, View view) {
        if (this.mKey != null) {
            closeGuts();
            this.mKeyguardDismissUtil.executeWhenUnlocked(new ActivityStarter.OnDismissAction() { // from class: com.android.systemui.media.MediaControlPanel$$ExternalSyntheticLambda7
                @Override // com.android.systemui.plugins.ActivityStarter.OnDismissAction
                public final boolean onDismiss() {
                    return this.f$0.lambda$bind$7();
                }
            }, true);
        } else {
            Log.w("MediaControlPanel", "Dismiss media with null notification. Token uid=" + mediaData.getToken().getUid());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ boolean lambda$bind$7() {
        this.mMediaDataManagerLazy.get().dismissMediaData(this.mKey, 600L);
        return true;
    }

    public void closeGuts(boolean z) {
        this.mMediaViewController.closeGuts(z);
    }

    private void closeGuts() {
        closeGuts(false);
    }

    private Drawable scaleDrawable(Icon icon) {
        Rect rect;
        if (icon == null) {
            return null;
        }
        Drawable drawableLoadDrawable = icon.loadDrawable(this.mContext);
        float intrinsicHeight = drawableLoadDrawable.getIntrinsicHeight() / drawableLoadDrawable.getIntrinsicWidth();
        if (intrinsicHeight > 1.0f) {
            int i = this.mAlbumArtSize;
            rect = new Rect(0, 0, i, (int) (i * intrinsicHeight));
        } else {
            int i2 = this.mAlbumArtSize;
            rect = new Rect(0, 0, (int) (i2 / intrinsicHeight), i2);
        }
        if (rect.width() > this.mAlbumArtSize || rect.height() > this.mAlbumArtSize) {
            rect.offset((int) (-((rect.width() - this.mAlbumArtSize) / 2.0f)), (int) (-((rect.height() - this.mAlbumArtSize) / 2.0f)));
        }
        drawableLoadDrawable.setBounds(rect);
        return drawableLoadDrawable;
    }

    public MediaController getController() {
        return this.mController;
    }

    private void setVisibleAndAlpha(ConstraintSet constraintSet, int i, boolean z) {
        constraintSet.setVisibility(i, z ? 0 : 8);
        constraintSet.setAlpha(i, z ? 1.0f : 0.0f);
    }
}
