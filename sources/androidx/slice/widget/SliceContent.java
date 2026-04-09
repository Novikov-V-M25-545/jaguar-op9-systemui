package androidx.slice.widget;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import androidx.core.graphics.drawable.IconCompat;
import androidx.slice.Slice;
import androidx.slice.SliceItem;
import androidx.slice.SliceUtils;
import androidx.slice.core.SliceAction;
import androidx.slice.core.SliceActionImpl;
import androidx.slice.core.SliceQuery;

/* loaded from: classes.dex */
public class SliceContent {
    protected SliceItem mColorItem;
    protected SliceItem mContentDescr;
    protected SliceItem mLayoutDirItem;
    protected int mRowIndex;
    protected SliceItem mSliceItem;

    public int getHeight(SliceStyle sliceStyle, SliceViewPolicy sliceViewPolicy) {
        return 0;
    }

    public SliceContent(Slice slice) {
        if (slice == null) {
            return;
        }
        init(new SliceItem(slice, "slice", (String) null, slice.getHints()));
        this.mRowIndex = -1;
    }

    public SliceContent(SliceItem sliceItem, int i) {
        if (sliceItem == null) {
            return;
        }
        init(sliceItem);
        this.mRowIndex = i;
    }

    private void init(SliceItem sliceItem) {
        this.mSliceItem = sliceItem;
        if ("slice".equals(sliceItem.getFormat()) || "action".equals(sliceItem.getFormat())) {
            this.mColorItem = SliceQuery.findTopLevelItem(sliceItem.getSlice(), "int", "color", null, null);
            this.mLayoutDirItem = SliceQuery.findTopLevelItem(sliceItem.getSlice(), "int", "layout_direction", null, null);
        }
        this.mContentDescr = SliceQuery.findSubtype(sliceItem, "text", "content_description");
    }

    public SliceItem getSliceItem() {
        return this.mSliceItem;
    }

    public int getLayoutDir() {
        SliceItem sliceItem = this.mLayoutDirItem;
        if (sliceItem != null) {
            return SliceViewUtil.resolveLayoutDirection(sliceItem.getInt());
        }
        return -1;
    }

    public CharSequence getContentDescription() {
        SliceItem sliceItem = this.mContentDescr;
        if (sliceItem != null) {
            return sliceItem.getText();
        }
        return null;
    }

    public int getRowIndex() {
        return this.mRowIndex;
    }

    public boolean isValid() {
        return this.mSliceItem != null;
    }

    public SliceAction getShortcut(Context context) {
        SliceItem sliceItemFind;
        SliceItem sliceItemFind2;
        SliceItem sliceItem = this.mSliceItem;
        if (sliceItem == null) {
            return null;
        }
        SliceItem sliceItemFind3 = SliceQuery.find(sliceItem, "action", new String[]{"title", "shortcut"}, (String[]) null);
        if (sliceItemFind3 != null) {
            sliceItemFind = SliceQuery.find(sliceItemFind3, "image", "title", (String) null);
            sliceItemFind2 = SliceQuery.find(sliceItemFind3, "text", (String) null, (String) null);
        } else {
            sliceItemFind = null;
            sliceItemFind2 = null;
        }
        if (sliceItemFind3 == null) {
            sliceItemFind3 = SliceQuery.find(this.mSliceItem, "action", (String) null, (String) null);
        }
        SliceItem sliceItem2 = sliceItemFind3;
        if (sliceItemFind == null) {
            sliceItemFind = SliceQuery.find(this.mSliceItem, "image", "title", (String) null);
        }
        if (sliceItemFind2 == null) {
            sliceItemFind2 = SliceQuery.find(this.mSliceItem, "text", "title", (String) null);
        }
        if (sliceItemFind == null) {
            sliceItemFind = SliceQuery.find(this.mSliceItem, "image", (String) null, (String) null);
        }
        if (sliceItemFind2 == null) {
            sliceItemFind2 = SliceQuery.find(this.mSliceItem, "text", (String) null, (String) null);
        }
        int imageMode = sliceItemFind != null ? SliceUtils.parseImageMode(sliceItemFind) : 5;
        if (context != null) {
            return fallBackToAppData(context, sliceItemFind2, sliceItemFind, imageMode, sliceItem2);
        }
        if (sliceItemFind == null || sliceItem2 == null || sliceItemFind2 == null) {
            return null;
        }
        return new SliceActionImpl(sliceItem2.getAction(), sliceItemFind.getIcon(), imageMode, sliceItemFind2.getText());
    }

    private SliceAction fallBackToAppData(Context context, SliceItem sliceItem, SliceItem sliceItem2, int i, SliceItem sliceItem3) {
        Intent launchIntentForPackage;
        SliceItem sliceItemFind = SliceQuery.find(this.mSliceItem, "slice", (String) null, (String) null);
        if (sliceItemFind == null) {
            return null;
        }
        Uri uri = sliceItemFind.getSlice().getUri();
        IconCompat icon = sliceItem2 != null ? sliceItem2.getIcon() : null;
        CharSequence text = sliceItem != null ? sliceItem.getText() : null;
        if (context != null) {
            PackageManager packageManager = context.getPackageManager();
            ProviderInfo providerInfoResolveContentProvider = packageManager.resolveContentProvider(uri.getAuthority(), 0);
            ApplicationInfo applicationInfo = providerInfoResolveContentProvider != null ? providerInfoResolveContentProvider.applicationInfo : null;
            if (applicationInfo != null) {
                if (icon == null) {
                    icon = SliceViewUtil.createIconFromDrawable(packageManager.getApplicationIcon(applicationInfo));
                    i = 2;
                }
                if (text == null) {
                    text = packageManager.getApplicationLabel(applicationInfo);
                }
                if (sliceItem3 == null && (launchIntentForPackage = packageManager.getLaunchIntentForPackage(applicationInfo.packageName)) != null) {
                    sliceItem3 = new SliceItem(PendingIntent.getActivity(context, 0, launchIntentForPackage, 0), new Slice.Builder(uri).build(), "action", null, new String[0]);
                }
            }
        }
        if (sliceItem3 == null) {
            sliceItem3 = new SliceItem(PendingIntent.getActivity(context, 0, new Intent(), 0), null, "action", null, null);
        }
        if (text == null || icon == null) {
            return null;
        }
        return new SliceActionImpl(sliceItem3.getAction(), icon, i, text);
    }
}
