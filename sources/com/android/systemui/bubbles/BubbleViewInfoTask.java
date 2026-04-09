package com.android.systemui.bubbles;

import android.app.Notification;
import android.app.Person;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.PathParser;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.android.internal.graphics.ColorUtils;
import com.android.launcher3.icons.BitmapInfo;
import com.android.systemui.R;
import com.android.systemui.bubbles.Bubble;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.StatusBar;
import java.lang.ref.WeakReference;
import java.util.Objects;

/* loaded from: classes.dex */
public class BubbleViewInfoTask extends AsyncTask<Void, Void, BubbleViewInfo> {
    private Bubble mBubble;
    private Callback mCallback;
    private WeakReference<Context> mContext;
    private BubbleIconFactory mIconFactory;
    private boolean mSkipInflation;
    private WeakReference<BubbleStackView> mStackView;

    public interface Callback {
        void onBubbleViewsReady(Bubble bubble);
    }

    BubbleViewInfoTask(Bubble bubble, Context context, BubbleStackView bubbleStackView, BubbleIconFactory bubbleIconFactory, boolean z, Callback callback) {
        this.mBubble = bubble;
        this.mContext = new WeakReference<>(context);
        this.mStackView = new WeakReference<>(bubbleStackView);
        this.mIconFactory = bubbleIconFactory;
        this.mSkipInflation = z;
        this.mCallback = callback;
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public BubbleViewInfo doInBackground(Void... voidArr) {
        return BubbleViewInfo.populate(this.mContext.get(), this.mStackView.get(), this.mIconFactory, this.mBubble, this.mSkipInflation);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // android.os.AsyncTask
    public void onPostExecute(BubbleViewInfo bubbleViewInfo) {
        if (bubbleViewInfo != null) {
            this.mBubble.setViewInfo(bubbleViewInfo);
            if (this.mCallback == null || isCancelled()) {
                return;
            }
            this.mCallback.onBubbleViewsReady(this.mBubble);
        }
    }

    static class BubbleViewInfo {
        String appName;
        Drawable badgedAppIcon;
        Bitmap badgedBubbleImage;
        int dotColor;
        Path dotPath;
        BubbleExpandedView expandedView;
        Bubble.FlyoutMessage flyoutMessage;
        BadgedImageView imageView;
        ShortcutInfo shortcutInfo;

        BubbleViewInfo() {
        }

        static BubbleViewInfo populate(Context context, BubbleStackView bubbleStackView, BubbleIconFactory bubbleIconFactory, Bubble bubble, boolean z) throws Resources.NotFoundException, PackageManager.NameNotFoundException {
            BubbleViewInfo bubbleViewInfo = new BubbleViewInfo();
            if (!z && !bubble.isInflated()) {
                LayoutInflater layoutInflaterFrom = LayoutInflater.from(context);
                bubbleViewInfo.imageView = (BadgedImageView) layoutInflaterFrom.inflate(R.layout.bubble_view, (ViewGroup) bubbleStackView, false);
                BubbleExpandedView bubbleExpandedView = (BubbleExpandedView) layoutInflaterFrom.inflate(R.layout.bubble_expanded_view, (ViewGroup) bubbleStackView, false);
                bubbleViewInfo.expandedView = bubbleExpandedView;
                bubbleExpandedView.setStackView(bubbleStackView);
            }
            if (bubble.getShortcutInfo() != null) {
                bubbleViewInfo.shortcutInfo = bubble.getShortcutInfo();
            }
            PackageManager packageManagerForUser = StatusBar.getPackageManagerForUser(context, bubble.getUser().getIdentifier());
            try {
                ApplicationInfo applicationInfo = packageManagerForUser.getApplicationInfo(bubble.getPackageName(), 795136);
                if (applicationInfo != null) {
                    bubbleViewInfo.appName = String.valueOf(packageManagerForUser.getApplicationLabel(applicationInfo));
                }
                Drawable applicationIcon = packageManagerForUser.getApplicationIcon(bubble.getPackageName());
                Drawable userBadgedIcon = packageManagerForUser.getUserBadgedIcon(applicationIcon, bubble.getUser());
                Drawable bubbleDrawable = bubbleIconFactory.getBubbleDrawable(context, bubbleViewInfo.shortcutInfo, bubble.getIcon());
                if (bubbleDrawable != null) {
                    applicationIcon = bubbleDrawable;
                }
                BitmapInfo badgeBitmap = bubbleIconFactory.getBadgeBitmap(userBadgedIcon, bubble.isImportantConversation());
                bubbleViewInfo.badgedAppIcon = userBadgedIcon;
                bubbleViewInfo.badgedBubbleImage = bubbleIconFactory.getBubbleBitmap(applicationIcon, badgeBitmap).icon;
                Path pathCreatePathFromPathData = PathParser.createPathFromPathData(context.getResources().getString(android.R.string.config_defaultDndDeniedPackages));
                Matrix matrix = new Matrix();
                float scale = bubbleIconFactory.getNormalizer().getScale(applicationIcon, null, null, null);
                matrix.setScale(scale, scale, 50.0f, 50.0f);
                pathCreatePathFromPathData.transform(matrix);
                bubbleViewInfo.dotPath = pathCreatePathFromPathData;
                bubbleViewInfo.dotColor = ColorUtils.blendARGB(badgeBitmap.color, -1, 0.54f);
                Bubble.FlyoutMessage flyoutMessage = bubble.getFlyoutMessage();
                bubbleViewInfo.flyoutMessage = flyoutMessage;
                if (flyoutMessage != null) {
                    flyoutMessage.senderAvatar = BubbleViewInfoTask.loadSenderAvatar(context, flyoutMessage.senderIcon);
                }
                return bubbleViewInfo;
            } catch (PackageManager.NameNotFoundException unused) {
                Log.w("Bubbles", "Unable to find package: " + bubble.getPackageName());
                return null;
            }
        }
    }

    static Bubble.FlyoutMessage extractFlyoutMessage(NotificationEntry notificationEntry) {
        Objects.requireNonNull(notificationEntry);
        Notification notification = notificationEntry.getSbn().getNotification();
        Class notificationStyle = notification.getNotificationStyle();
        Bubble.FlyoutMessage flyoutMessage = new Bubble.FlyoutMessage();
        flyoutMessage.isGroupChat = notification.extras.getBoolean("android.isGroupConversation");
        try {
        } catch (ArrayIndexOutOfBoundsException | ClassCastException | NullPointerException e) {
            e.printStackTrace();
        }
        if (Notification.BigTextStyle.class.equals(notificationStyle)) {
            CharSequence charSequence = notification.extras.getCharSequence("android.bigText");
            if (TextUtils.isEmpty(charSequence)) {
                charSequence = notification.extras.getCharSequence("android.text");
            }
            flyoutMessage.message = charSequence;
            return flyoutMessage;
        }
        if (Notification.MessagingStyle.class.equals(notificationStyle)) {
            Notification.MessagingStyle.Message messageFindLatestIncomingMessage = Notification.MessagingStyle.findLatestIncomingMessage(Notification.MessagingStyle.Message.getMessagesFromBundleArray((Parcelable[]) notification.extras.get("android.messages")));
            if (messageFindLatestIncomingMessage != null) {
                flyoutMessage.message = messageFindLatestIncomingMessage.getText();
                Person senderPerson = messageFindLatestIncomingMessage.getSenderPerson();
                flyoutMessage.senderName = senderPerson != null ? senderPerson.getName() : null;
                flyoutMessage.senderAvatar = null;
                flyoutMessage.senderIcon = senderPerson != null ? senderPerson.getIcon() : null;
                return flyoutMessage;
            }
        } else if (Notification.InboxStyle.class.equals(notificationStyle)) {
            CharSequence[] charSequenceArray = notification.extras.getCharSequenceArray("android.textLines");
            if (charSequenceArray != null && charSequenceArray.length > 0) {
                flyoutMessage.message = charSequenceArray[charSequenceArray.length - 1];
                return flyoutMessage;
            }
        } else {
            if (Notification.MediaStyle.class.equals(notificationStyle)) {
                return flyoutMessage;
            }
            flyoutMessage.message = notification.extras.getCharSequence("android.text");
            return flyoutMessage;
        }
        return flyoutMessage;
    }

    static Drawable loadSenderAvatar(Context context, Icon icon) {
        Objects.requireNonNull(context);
        if (icon == null) {
            return null;
        }
        if (icon.getType() == 4 || icon.getType() == 6) {
            context.grantUriPermission(context.getPackageName(), icon.getUri(), 1);
        }
        return icon.loadDrawable(context);
    }
}
