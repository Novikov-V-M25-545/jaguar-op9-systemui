package com.android.systemui.statusbar.notification.row;

import android.R;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.ImageResolver;
import com.android.internal.widget.LocalImageResolver;
import com.android.internal.widget.MessagingMessage;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/* loaded from: classes.dex */
public class NotificationInlineImageResolver implements ImageResolver {
    private static final String TAG = NotificationInlineImageResolver.class.getSimpleName();
    private final Context mContext;
    private final ImageCache mImageCache;

    @VisibleForTesting
    protected int mMaxImageHeight;

    @VisibleForTesting
    protected int mMaxImageWidth;
    private Set<Uri> mWantedUriSet;

    interface ImageCache {
        void cancelRunningTasks();

        Drawable get(Uri uri, long j);

        boolean hasEntry(Uri uri);

        void preload(Uri uri);

        void purge();

        void setImageResolver(NotificationInlineImageResolver notificationInlineImageResolver);
    }

    public NotificationInlineImageResolver(Context context, ImageCache imageCache) {
        this.mContext = context.getApplicationContext();
        this.mImageCache = imageCache;
        if (imageCache != null) {
            imageCache.setImageResolver(this);
        }
        updateMaxImageSizes();
    }

    public boolean hasCache() {
        return (this.mImageCache == null || ActivityManager.isLowRamDeviceStatic()) ? false : true;
    }

    private boolean isLowRam() {
        return ActivityManager.isLowRamDeviceStatic();
    }

    public void updateMaxImageSizes() {
        this.mMaxImageWidth = getMaxImageWidth();
        this.mMaxImageHeight = getMaxImageHeight();
    }

    @VisibleForTesting
    protected int getMaxImageWidth() {
        return this.mContext.getResources().getDimensionPixelSize(isLowRam() ? R.dimen.input_method_nav_key_button_ripple_max_width : R.dimen.input_method_nav_content_padding);
    }

    @VisibleForTesting
    protected int getMaxImageHeight() {
        return this.mContext.getResources().getDimensionPixelSize(isLowRam() ? R.dimen.input_method_divider_height : R.dimen.input_extract_action_icon_padding);
    }

    @VisibleForTesting
    protected BitmapDrawable resolveImageInternal(Uri uri) throws IOException {
        return (BitmapDrawable) LocalImageResolver.resolveImage(uri, this.mContext);
    }

    Drawable resolveImage(Uri uri) throws IOException {
        BitmapDrawable bitmapDrawableResolveImageInternal = resolveImageInternal(uri);
        if (bitmapDrawableResolveImageInternal == null || bitmapDrawableResolveImageInternal.getBitmap() == null) {
            throw new IOException("resolveImageInternal returned null for uri: " + uri);
        }
        bitmapDrawableResolveImageInternal.setBitmap(Icon.scaleDownIfNecessary(bitmapDrawableResolveImageInternal.getBitmap(), this.mMaxImageWidth, this.mMaxImageHeight));
        return bitmapDrawableResolveImageInternal;
    }

    public Drawable loadImage(Uri uri) {
        Drawable drawableResolveImage;
        try {
            if (hasCache()) {
                drawableResolveImage = loadImageFromCache(uri, 100L);
            } else {
                drawableResolveImage = resolveImage(uri);
            }
            return drawableResolveImage;
        } catch (IOException | SecurityException e) {
            Log.d(TAG, "loadImage: Can't load image from " + uri, e);
            return null;
        }
    }

    private Drawable loadImageFromCache(Uri uri, long j) {
        if (!this.mImageCache.hasEntry(uri)) {
            this.mImageCache.preload(uri);
        }
        return this.mImageCache.get(uri, j);
    }

    public void preloadImages(Notification notification) {
        if (hasCache()) {
            retrieveWantedUriSet(notification);
            getWantedUriSet().forEach(new Consumer() { // from class: com.android.systemui.statusbar.notification.row.NotificationInlineImageResolver$$ExternalSyntheticLambda0
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.lambda$preloadImages$0((Uri) obj);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$preloadImages$0(Uri uri) {
        if (this.mImageCache.hasEntry(uri)) {
            return;
        }
        this.mImageCache.preload(uri);
    }

    public void purgeCache() {
        if (hasCache()) {
            this.mImageCache.purge();
        }
    }

    private void retrieveWantedUriSet(Notification notification) {
        HashSet hashSet = new HashSet();
        Bundle bundle = notification.extras;
        if (bundle == null) {
            return;
        }
        Parcelable[] parcelableArray = bundle.getParcelableArray("android.messages");
        List<Notification.MessagingStyle.Message> messagesFromBundleArray = parcelableArray == null ? null : Notification.MessagingStyle.Message.getMessagesFromBundleArray(parcelableArray);
        if (messagesFromBundleArray != null) {
            for (Notification.MessagingStyle.Message message : messagesFromBundleArray) {
                if (MessagingMessage.hasImage(message)) {
                    hashSet.add(message.getDataUri());
                }
            }
        }
        Parcelable[] parcelableArray2 = bundle.getParcelableArray("android.messages.historic");
        List<Notification.MessagingStyle.Message> messagesFromBundleArray2 = parcelableArray2 != null ? Notification.MessagingStyle.Message.getMessagesFromBundleArray(parcelableArray2) : null;
        if (messagesFromBundleArray2 != null) {
            for (Notification.MessagingStyle.Message message2 : messagesFromBundleArray2) {
                if (MessagingMessage.hasImage(message2)) {
                    hashSet.add(message2.getDataUri());
                }
            }
        }
        this.mWantedUriSet = hashSet;
    }

    Set<Uri> getWantedUriSet() {
        return this.mWantedUriSet;
    }

    void waitForPreloadedImages(long j) {
        Set<Uri> wantedUriSet;
        if (hasCache() && (wantedUriSet = getWantedUriSet()) != null) {
            final long jElapsedRealtime = SystemClock.elapsedRealtime() + j;
            wantedUriSet.forEach(new Consumer() { // from class: com.android.systemui.statusbar.notification.row.NotificationInlineImageResolver$$ExternalSyntheticLambda1
                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.lambda$waitForPreloadedImages$1(jElapsedRealtime, (Uri) obj);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$waitForPreloadedImages$1(long j, Uri uri) {
        loadImageFromCache(uri, j - SystemClock.elapsedRealtime());
    }

    void cancelRunningTasks() {
        if (hasCache()) {
            this.mImageCache.cancelRunningTasks();
        }
    }
}
