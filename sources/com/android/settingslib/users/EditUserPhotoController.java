package com.android.settingslib.users;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.UserHandle;
import android.provider.ContactsContract;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.TextView;
import androidx.core.content.FileProvider;
import com.android.settingslib.R$dimen;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.R$string;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.drawable.CircleFramedDrawable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import libcore.io.Streams;

/* loaded from: classes.dex */
public class EditUserPhotoController {
    private final Activity mActivity;
    private final ActivityStarter mActivityStarter;
    private final Uri mCropPictureUri;
    private final String mFileAuthority;
    private final ImageView mImageView;
    private final File mImagesDir;
    private Bitmap mNewUserPhotoBitmap;
    private Drawable mNewUserPhotoDrawable;
    private final int mPhotoSize;
    private final Uri mTakePictureUri;

    public EditUserPhotoController(Activity activity, ActivityStarter activityStarter, ImageView imageView, Bitmap bitmap, boolean z, String str) {
        this.mActivity = activity;
        this.mActivityStarter = activityStarter;
        this.mImageView = imageView;
        this.mFileAuthority = str;
        File file = new File(activity.getCacheDir(), "multi_user");
        this.mImagesDir = file;
        file.mkdir();
        this.mCropPictureUri = createTempImageUri(activity, "CropEditUserPhoto.jpg", !z);
        this.mTakePictureUri = createTempImageUri(activity, "TakeEditUserPhoto.jpg", !z);
        this.mPhotoSize = getPhotoSize(activity);
        imageView.setOnClickListener(new View.OnClickListener() { // from class: com.android.settingslib.users.EditUserPhotoController$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.lambda$new$0(view);
            }
        });
        this.mNewUserPhotoBitmap = bitmap;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0(View view) {
        showUpdatePhotoPopup();
    }

    public boolean onActivityResult(int i, int i2, Intent intent) {
        if (i2 != -1) {
            return false;
        }
        Uri data = (intent == null || intent.getData() == null) ? this.mTakePictureUri : intent.getData();
        if (!"content".equals(data.getScheme())) {
            Log.e("EditUserPhotoController", "Invalid pictureUri scheme: " + data.getScheme());
            EventLog.writeEvent(1397638484, "172939189", -1, data.getPath());
            return false;
        }
        int iMyUserId = UserHandle.myUserId();
        if (iMyUserId != ContentProvider.getUserIdFromUri(data, iMyUserId)) {
            Log.e("EditUserPhotoController", "Invalid pictureUri: " + data);
            return false;
        }
        switch (i) {
            case 1001:
            case 1002:
                if (this.mTakePictureUri.equals(data)) {
                    if (PhotoCapabilityUtils.canCropPhoto(this.mActivity)) {
                        cropPhoto();
                        break;
                    } else {
                        onPhotoNotCropped(data);
                        break;
                    }
                } else {
                    copyAndCropPhoto(data);
                    break;
                }
            case 1003:
                onPhotoCropped(data);
                break;
        }
        return false;
    }

    public Drawable getNewUserPhotoDrawable() {
        return this.mNewUserPhotoDrawable;
    }

    private void showUpdatePhotoPopup() {
        Context context = this.mImageView.getContext();
        boolean zCanTakePhoto = PhotoCapabilityUtils.canTakePhoto(context);
        boolean zCanChoosePhoto = PhotoCapabilityUtils.canChoosePhoto(context);
        if (zCanTakePhoto || zCanChoosePhoto) {
            ArrayList arrayList = new ArrayList();
            if (zCanTakePhoto) {
                arrayList.add(new RestrictedMenuItem(context, context.getString(R$string.user_image_take_photo), "no_set_user_icon", new Runnable() { // from class: com.android.settingslib.users.EditUserPhotoController$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.takePhoto();
                    }
                }));
            }
            if (zCanChoosePhoto) {
                arrayList.add(new RestrictedMenuItem(context, context.getString(R$string.user_image_choose_photo), "no_set_user_icon", new Runnable() { // from class: com.android.settingslib.users.EditUserPhotoController$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.choosePhoto();
                    }
                }));
            }
            final ListPopupWindow listPopupWindow = new ListPopupWindow(context);
            listPopupWindow.setAnchorView(this.mImageView);
            listPopupWindow.setModal(true);
            listPopupWindow.setInputMethodMode(2);
            listPopupWindow.setAdapter(new RestrictedPopupMenuAdapter(context, arrayList));
            listPopupWindow.setWidth(Math.max(this.mImageView.getWidth(), context.getResources().getDimensionPixelSize(R$dimen.update_user_photo_popup_min_width)));
            listPopupWindow.setDropDownGravity(8388611);
            listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.android.settingslib.users.EditUserPhotoController$$ExternalSyntheticLambda1
                @Override // android.widget.AdapterView.OnItemClickListener
                public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                    EditUserPhotoController.lambda$showUpdatePhotoPopup$1(listPopupWindow, adapterView, view, i, j);
                }
            });
            listPopupWindow.show();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$showUpdatePhotoPopup$1(ListPopupWindow listPopupWindow, AdapterView adapterView, View view, int i, long j) {
        listPopupWindow.dismiss();
        ((RestrictedMenuItem) adapterView.getAdapter().getItem(i)).doAction();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE_SECURE");
        appendOutputExtra(intent, this.mTakePictureUri);
        this.mActivityStarter.startActivityForResult(intent, 1002);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void choosePhoto() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT", (Uri) null);
        intent.setType("image/*");
        appendOutputExtra(intent, this.mTakePictureUri);
        this.mActivityStarter.startActivityForResult(intent, 1001);
    }

    private void copyAndCropPhoto(final Uri uri) {
        new AsyncTask<Void, Void, Void>() { // from class: com.android.settingslib.users.EditUserPhotoController.1
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Void doInBackground(Void... voidArr) throws IOException {
                ContentResolver contentResolver = EditUserPhotoController.this.mActivity.getContentResolver();
                try {
                    InputStream inputStreamOpenInputStream = contentResolver.openInputStream(uri);
                    try {
                        OutputStream outputStreamOpenOutputStream = contentResolver.openOutputStream(EditUserPhotoController.this.mTakePictureUri);
                        try {
                            Streams.copy(inputStreamOpenInputStream, outputStreamOpenOutputStream);
                            if (outputStreamOpenOutputStream != null) {
                                outputStreamOpenOutputStream.close();
                            }
                            if (inputStreamOpenInputStream == null) {
                                return null;
                            }
                            inputStreamOpenInputStream.close();
                            return null;
                        } finally {
                        }
                    } finally {
                    }
                } catch (IOException e) {
                    Log.w("EditUserPhotoController", "Failed to copy photo", e);
                    return null;
                }
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Void r1) {
                if (EditUserPhotoController.this.mActivity.isFinishing() || EditUserPhotoController.this.mActivity.isDestroyed()) {
                    return;
                }
                EditUserPhotoController.this.cropPhoto();
            }
        }.execute(new Void[0]);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cropPhoto() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(this.mTakePictureUri, "image/*");
        appendOutputExtra(intent, this.mCropPictureUri);
        appendCropExtras(intent);
        if (intent.resolveActivity(this.mActivity.getPackageManager()) != null) {
            try {
                StrictMode.disableDeathOnFileUriExposure();
                this.mActivityStarter.startActivityForResult(intent, 1003);
                return;
            } finally {
                StrictMode.enableDeathOnFileUriExposure();
            }
        }
        onPhotoNotCropped(this.mTakePictureUri);
    }

    private void appendOutputExtra(Intent intent, Uri uri) {
        intent.putExtra("output", uri);
        intent.addFlags(3);
        intent.setClipData(ClipData.newRawUri("output", uri));
    }

    private void appendCropExtras(Intent intent) {
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", this.mPhotoSize);
        intent.putExtra("outputY", this.mPhotoSize);
    }

    private void onPhotoCropped(final Uri uri) {
        new AsyncTask<Void, Void, Bitmap>() { // from class: com.android.settingslib.users.EditUserPhotoController.2
            /* JADX INFO: Access modifiers changed from: protected */
            /* JADX WARN: Removed duplicated region for block: B:35:0x0040 A[EXC_TOP_SPLITTER, SYNTHETIC] */
            @Override // android.os.AsyncTask
            /*
                Code decompiled incorrectly, please refer to instructions dump.
                To view partially-correct code enable 'Show inconsistent code' option in preferences
            */
            public android.graphics.Bitmap doInBackground(java.lang.Void... r6) throws java.lang.Throwable {
                /*
                    r5 = this;
                    java.lang.String r6 = "Cannot close image stream"
                    java.lang.String r0 = "EditUserPhotoController"
                    r1 = 0
                    com.android.settingslib.users.EditUserPhotoController r2 = com.android.settingslib.users.EditUserPhotoController.this     // Catch: java.lang.Throwable -> L26 java.io.FileNotFoundException -> L2b
                    android.app.Activity r2 = com.android.settingslib.users.EditUserPhotoController.access$000(r2)     // Catch: java.lang.Throwable -> L26 java.io.FileNotFoundException -> L2b
                    android.content.ContentResolver r2 = r2.getContentResolver()     // Catch: java.lang.Throwable -> L26 java.io.FileNotFoundException -> L2b
                    android.net.Uri r5 = r2     // Catch: java.lang.Throwable -> L26 java.io.FileNotFoundException -> L2b
                    java.io.InputStream r5 = r2.openInputStream(r5)     // Catch: java.lang.Throwable -> L26 java.io.FileNotFoundException -> L2b
                    android.graphics.Bitmap r1 = android.graphics.BitmapFactory.decodeStream(r5)     // Catch: java.io.FileNotFoundException -> L24 java.lang.Throwable -> L3d
                    if (r5 == 0) goto L23
                    r5.close()     // Catch: java.io.IOException -> L1f
                    goto L23
                L1f:
                    r5 = move-exception
                    android.util.Log.w(r0, r6, r5)
                L23:
                    return r1
                L24:
                    r2 = move-exception
                    goto L2d
                L26:
                    r5 = move-exception
                    r4 = r1
                    r1 = r5
                    r5 = r4
                    goto L3e
                L2b:
                    r2 = move-exception
                    r5 = r1
                L2d:
                    java.lang.String r3 = "Cannot find image file"
                    android.util.Log.w(r0, r3, r2)     // Catch: java.lang.Throwable -> L3d
                    if (r5 == 0) goto L3c
                    r5.close()     // Catch: java.io.IOException -> L38
                    goto L3c
                L38:
                    r5 = move-exception
                    android.util.Log.w(r0, r6, r5)
                L3c:
                    return r1
                L3d:
                    r1 = move-exception
                L3e:
                    if (r5 == 0) goto L48
                    r5.close()     // Catch: java.io.IOException -> L44
                    goto L48
                L44:
                    r5 = move-exception
                    android.util.Log.w(r0, r6, r5)
                L48:
                    throw r1
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.users.EditUserPhotoController.AnonymousClass2.doInBackground(java.lang.Void[]):android.graphics.Bitmap");
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Bitmap bitmap) {
                EditUserPhotoController.this.onPhotoProcessed(bitmap);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }

    private void onPhotoNotCropped(final Uri uri) {
        new AsyncTask<Void, Void, Bitmap>() { // from class: com.android.settingslib.users.EditUserPhotoController.3
            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public Bitmap doInBackground(Void... voidArr) {
                Bitmap bitmapCreateBitmap = Bitmap.createBitmap(EditUserPhotoController.this.mPhotoSize, EditUserPhotoController.this.mPhotoSize, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmapCreateBitmap);
                try {
                    Bitmap bitmapDecodeStream = BitmapFactory.decodeStream(EditUserPhotoController.this.mActivity.getContentResolver().openInputStream(uri));
                    if (bitmapDecodeStream != null) {
                        EditUserPhotoController editUserPhotoController = EditUserPhotoController.this;
                        int rotation = editUserPhotoController.getRotation(editUserPhotoController.mActivity, uri);
                        int iMin = Math.min(bitmapDecodeStream.getWidth(), bitmapDecodeStream.getHeight());
                        int width = (bitmapDecodeStream.getWidth() - iMin) / 2;
                        int height = (bitmapDecodeStream.getHeight() - iMin) / 2;
                        Matrix matrix = new Matrix();
                        matrix.setRectToRect(new RectF(width, height, width + iMin, height + iMin), new RectF(0.0f, 0.0f, EditUserPhotoController.this.mPhotoSize, EditUserPhotoController.this.mPhotoSize), Matrix.ScaleToFit.CENTER);
                        matrix.postRotate(rotation, EditUserPhotoController.this.mPhotoSize / 2.0f, EditUserPhotoController.this.mPhotoSize / 2.0f);
                        canvas.drawBitmap(bitmapDecodeStream, matrix, new Paint());
                        return bitmapCreateBitmap;
                    }
                } catch (FileNotFoundException unused) {
                }
                return null;
            }

            /* JADX INFO: Access modifiers changed from: protected */
            @Override // android.os.AsyncTask
            public void onPostExecute(Bitmap bitmap) {
                EditUserPhotoController.this.onPhotoProcessed(bitmap);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public int getRotation(Context context, Uri uri) {
        int attributeInt = -1;
        try {
            attributeInt = new ExifInterface(context.getContentResolver().openInputStream(uri)).getAttributeInt("Orientation", -1);
        } catch (IOException e) {
            Log.e("EditUserPhotoController", "Error while getting rotation", e);
        }
        if (attributeInt == 3) {
            return 180;
        }
        if (attributeInt != 6) {
            return attributeInt != 8 ? 0 : 270;
        }
        return 90;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void onPhotoProcessed(Bitmap bitmap) {
        if (bitmap != null) {
            this.mNewUserPhotoBitmap = bitmap;
            CircleFramedDrawable circleFramedDrawable = CircleFramedDrawable.getInstance(this.mImageView.getContext(), this.mNewUserPhotoBitmap);
            this.mNewUserPhotoDrawable = circleFramedDrawable;
            this.mImageView.setImageDrawable(circleFramedDrawable);
        }
        new File(this.mImagesDir, "TakeEditUserPhoto.jpg").delete();
        new File(this.mImagesDir, "CropEditUserPhoto.jpg").delete();
    }

    private static int getPhotoSize(Context context) {
        Cursor cursorQuery = context.getContentResolver().query(ContactsContract.DisplayPhoto.CONTENT_MAX_DIMENSIONS_URI, new String[]{"display_max_dim"}, null, null, null);
        if (cursorQuery == null) {
            if (cursorQuery != null) {
                cursorQuery.close();
            }
            return 500;
        }
        try {
            cursorQuery.moveToFirst();
            int i = cursorQuery.getInt(0);
            cursorQuery.close();
            return i;
        } catch (Throwable th) {
            try {
                cursorQuery.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }

    private Uri createTempImageUri(Context context, String str, boolean z) {
        File file = new File(this.mImagesDir, str);
        if (z) {
            file.delete();
        }
        return FileProvider.getUriForFile(context, this.mFileAuthority, file);
    }

    File saveNewUserPhotoBitmap() throws IOException {
        if (this.mNewUserPhotoBitmap == null) {
            return null;
        }
        try {
            File file = new File(this.mImagesDir, "NewUserPhoto.png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            this.mNewUserPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            return file;
        } catch (IOException e) {
            Log.e("EditUserPhotoController", "Cannot create temp file", e);
            return null;
        }
    }

    static Bitmap loadNewUserPhotoBitmap(File file) {
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }

    void removeNewUserPhotoBitmapFile() {
        new File(this.mImagesDir, "NewUserPhoto.png").delete();
    }

    private static final class RestrictedMenuItem {
        private final Runnable mAction;
        private final RestrictedLockUtils.EnforcedAdmin mAdmin;
        private final Context mContext;
        private final boolean mIsRestrictedByBase;
        private final String mTitle;

        RestrictedMenuItem(Context context, String str, String str2, Runnable runnable) {
            this.mContext = context;
            this.mTitle = str;
            this.mAction = runnable;
            int iMyUserId = UserHandle.myUserId();
            this.mAdmin = RestrictedLockUtilsInternal.checkIfRestrictionEnforced(context, str2, iMyUserId);
            this.mIsRestrictedByBase = RestrictedLockUtilsInternal.hasBaseUserRestriction(context, str2, iMyUserId);
        }

        public String toString() {
            return this.mTitle;
        }

        void doAction() {
            if (isRestrictedByBase()) {
                return;
            }
            if (isRestrictedByAdmin()) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mAdmin);
            } else {
                this.mAction.run();
            }
        }

        boolean isRestrictedByAdmin() {
            return this.mAdmin != null;
        }

        boolean isRestrictedByBase() {
            return this.mIsRestrictedByBase;
        }
    }

    private static final class RestrictedPopupMenuAdapter extends ArrayAdapter<RestrictedMenuItem> {
        RestrictedPopupMenuAdapter(Context context, List<RestrictedMenuItem> list) {
            super(context, R$layout.restricted_popup_menu_item, R$id.text, list);
        }

        @Override // android.widget.ArrayAdapter, android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            View view2 = super.getView(i, view, viewGroup);
            RestrictedMenuItem item = getItem(i);
            TextView textView = (TextView) view2.findViewById(R$id.text);
            ImageView imageView = (ImageView) view2.findViewById(R$id.restricted_icon);
            textView.setEnabled((item.isRestrictedByAdmin() || item.isRestrictedByBase()) ? false : true);
            imageView.setVisibility((!item.isRestrictedByAdmin() || item.isRestrictedByBase()) ? 8 : 0);
            return view2;
        }
    }
}
