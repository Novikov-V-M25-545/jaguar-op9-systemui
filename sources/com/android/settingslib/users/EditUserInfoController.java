package com.android.settingslib.users;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import com.android.internal.util.UserIcons;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.drawable.CircleFramedDrawable;
import java.io.File;
import java.util.function.BiConsumer;

/* loaded from: classes.dex */
public class EditUserInfoController {
    private Dialog mEditUserInfoDialog;
    private EditUserPhotoController mEditUserPhotoController;
    private final String mFileAuthority;
    private Bitmap mSavedPhoto;
    private boolean mWaitingForActivityResult = false;

    public EditUserInfoController(String str) {
        this.mFileAuthority = str;
    }

    private void clear() {
        EditUserPhotoController editUserPhotoController = this.mEditUserPhotoController;
        if (editUserPhotoController != null) {
            editUserPhotoController.removeNewUserPhotoBitmapFile();
        }
        this.mEditUserInfoDialog = null;
        this.mSavedPhoto = null;
    }

    public void onRestoreInstanceState(Bundle bundle) {
        String string = bundle.getString("pending_photo");
        if (string != null) {
            this.mSavedPhoto = EditUserPhotoController.loadNewUserPhotoBitmap(new File(string));
        }
        this.mWaitingForActivityResult = bundle.getBoolean("awaiting_result", false);
    }

    public void onSaveInstanceState(Bundle bundle) {
        EditUserPhotoController editUserPhotoController;
        File fileSaveNewUserPhotoBitmap;
        if (this.mEditUserInfoDialog != null && (editUserPhotoController = this.mEditUserPhotoController) != null && (fileSaveNewUserPhotoBitmap = editUserPhotoController.saveNewUserPhotoBitmap()) != null) {
            bundle.putString("pending_photo", fileSaveNewUserPhotoBitmap.getPath());
        }
        bundle.putBoolean("awaiting_result", this.mWaitingForActivityResult);
    }

    public void startingActivityForResult() {
        this.mWaitingForActivityResult = true;
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        this.mWaitingForActivityResult = false;
        EditUserPhotoController editUserPhotoController = this.mEditUserPhotoController;
        if (editUserPhotoController == null || this.mEditUserInfoDialog == null) {
            return;
        }
        editUserPhotoController.onActivityResult(i, i2, intent);
    }

    public Dialog createDialog(Activity activity, ActivityStarter activityStarter, Drawable drawable, String str, String str2, BiConsumer<String, Drawable> biConsumer, Runnable runnable) {
        View viewInflate = LayoutInflater.from(activity).inflate(R$layout.edit_user_info_dialog_content, (ViewGroup) null);
        EditText editText = (EditText) viewInflate.findViewById(R$id.user_name);
        editText.setText(str);
        ImageView imageView = (ImageView) viewInflate.findViewById(R$id.user_photo);
        imageView.setImageDrawable(getUserIcon(activity, drawable != null ? drawable : UserIcons.getDefaultUserIcon(activity.getResources(), -10000, false)));
        if (canChangePhoto(activity)) {
            this.mEditUserPhotoController = createEditUserPhotoController(activity, activityStarter, imageView);
        } else {
            imageView.setBackground(null);
        }
        Dialog dialogBuildDialog = buildDialog(activity, viewInflate, editText, drawable, str, str2, biConsumer, runnable);
        this.mEditUserInfoDialog = dialogBuildDialog;
        dialogBuildDialog.getWindow().setSoftInputMode(4);
        return this.mEditUserInfoDialog;
    }

    private Drawable getUserIcon(Activity activity, Drawable drawable) {
        Bitmap bitmap = this.mSavedPhoto;
        return bitmap != null ? CircleFramedDrawable.getInstance(activity, bitmap) : drawable;
    }

    private Dialog buildDialog(Activity activity, View view, final EditText editText, final Drawable drawable, final String str, String str2, final BiConsumer<String, Drawable> biConsumer, final Runnable runnable) {
        return new AlertDialog.Builder(activity).setTitle(str2).setView(view).setCancelable(true).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() { // from class: com.android.settingslib.users.EditUserInfoController$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.lambda$buildDialog$0(drawable, editText, str, biConsumer, dialogInterface, i);
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { // from class: com.android.settingslib.users.EditUserInfoController$$ExternalSyntheticLambda2
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.lambda$buildDialog$1(runnable, dialogInterface, i);
            }
        }).setOnCancelListener(new DialogInterface.OnCancelListener() { // from class: com.android.settingslib.users.EditUserInfoController$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnCancelListener
            public final void onCancel(DialogInterface dialogInterface) {
                this.f$0.lambda$buildDialog$2(runnable, dialogInterface);
            }
        }).create();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildDialog$0(Drawable drawable, EditText editText, String str, BiConsumer biConsumer, DialogInterface dialogInterface, int i) {
        EditUserPhotoController editUserPhotoController = this.mEditUserPhotoController;
        Drawable newUserPhotoDrawable = editUserPhotoController != null ? editUserPhotoController.getNewUserPhotoDrawable() : null;
        if (newUserPhotoDrawable != null) {
            drawable = newUserPhotoDrawable;
        }
        String strTrim = editText.getText().toString().trim();
        if (!strTrim.isEmpty()) {
            str = strTrim;
        }
        clear();
        if (biConsumer != null) {
            biConsumer.accept(str, drawable);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildDialog$1(Runnable runnable, DialogInterface dialogInterface, int i) {
        clear();
        if (runnable != null) {
            runnable.run();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$buildDialog$2(Runnable runnable, DialogInterface dialogInterface) {
        clear();
        if (runnable != null) {
            runnable.run();
        }
    }

    boolean canChangePhoto(Context context) {
        return (PhotoCapabilityUtils.canCropPhoto(context) && PhotoCapabilityUtils.canChoosePhoto(context)) || PhotoCapabilityUtils.canTakePhoto(context);
    }

    EditUserPhotoController createEditUserPhotoController(Activity activity, ActivityStarter activityStarter, ImageView imageView) {
        return new EditUserPhotoController(activity, activityStarter, imageView, this.mSavedPhoto, this.mWaitingForActivityResult, this.mFileAuthority);
    }
}
