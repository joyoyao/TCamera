package com.abcew.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Environment;
import android.support.annotation.NonNull;

import com.abcew.camera.ui.utilities.PermissionRequest;

import java.io.File;

/**
 * Created by laputan on 16/11/2.
 */
public class CameraPreviewIntent extends ImgLyIntent {
    private static final Class activityClass = CameraActivity.class;

    public CameraPreviewIntent(android.content.Intent intent, Activity activity) {
        super(intent, activity);
    }

    public CameraPreviewIntent(Activity activity) {
        super(activity, activityClass);
    }

    /**
     * Set the Editor Intent that will be opened after Capture or Image select @see #PhotoEditorIntent
     * @param intent A intent that will be received the captured image.
     * @return this intent.
     */
    @NonNull
    public CameraPreviewIntent setEditorIntent(android.content.Intent intent) {
        putExtra(Extra.EDITOR_INTENT.name(), intent);

        return this;
    }

    /**
     * Set the Export directory save path of the captured image.
     * @param path the absolutely save directory
     * @return this intent.
     */
    @NonNull
    public CameraPreviewIntent setExportDir(String path) {
        putExtra(Extra.EXPORT_PATH.name(), path);
        return this;
    }

    /**
     * Set the Export directory save path of the captured image.
     * @param directory A specific system directory
     * @param folderName A folder name will saved in the system directory.
     * @return this intent.
     */
    @NonNull
    public CameraPreviewIntent setExportDir(@NonNull Directory directory, @NonNull String folderName) {
        File mMediaFolder = new File(Environment.getExternalStoragePublicDirectory(directory.dir), folderName);

        putExtra(Extra.EXPORT_PATH.name(), mMediaFolder.getAbsolutePath());
        return this;
    }

    /**
     * Set the image save name prefix.
     * @param prefix
     * @return this intent.
     */
    @NonNull
    public CameraPreviewIntent setExportPrefix(String prefix) {
        putExtra(Extra.EXPORT_PREFIX.name(), prefix);
        return this;
    }

    /*public CameraPreviewIntent setOpenEditor(boolean openEditor) {
        putExtra(Extra.OPEN_EDITOR.name(), openEditor);
        return this;
    }*/

//    protected android.content.Intent getEditorIntent() {
//        android.content.Intent intent = getParcelableExtra(Extra.EDITOR_INTENT.name());
//        if (intent == null) {
//            intent = new PhotoEditorIntent(activity);
//        }
//        return intent;
//    }

    protected String getExportPath() {
        String string = getStringExtra(Extra.EXPORT_PATH.name());
        if (string == null) {
            string = Environment.getExternalStoragePublicDirectory(Directory.DCIM.dir).getAbsolutePath();
        }
        return string;
    }

    protected String getExportPrefix() {
        String string = getStringExtra(Extra.EXPORT_PREFIX.name());
        if (string == null) {
            string = "image_";
        }
        return string;
    }

    protected boolean getOpenEditor() {
        return getBooleanExtra(Extra.OPEN_EDITOR.name(), true);
    }

    public void startActivityForResult(final int resultId) {
        startActivityForResult(resultId, PermissionRequest.NEEDED_PREVIEW_PERMISSIONS);
    }

}
