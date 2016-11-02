package com.abcew.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.abcew.camera.ui.utilities.PermissionRequest;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by laputan on 16/11/2.
 */
public abstract class ImgLyIntent extends android.content.Intent{
    protected final Activity activity;

    protected enum Extra {
        EXPORT_PATH,
        EXPORT_PREFIX,
        JPEG_QUALITY,
        SOURCE_FILE,
        DESTROY_SOURCE,
        OPEN_EDITOR,
        EDITOR_INTENT,
        COLOR_FILTER
    }

    public enum Directory {
        DCIM(Environment.DIRECTORY_DCIM),
        DOWNLOADS(Environment.DIRECTORY_DOWNLOADS),
        PICTURES(Environment.DIRECTORY_PICTURES);

        final String dir;

        Directory(String dir) {
            this.dir = dir;
        }
    }

    public ImgLyIntent(android.content.Intent intent, Activity activity) {
        super(intent);
        this.activity = activity;

    }

    public ImgLyIntent(Activity activity, Class activityClass) {
        super(activity, activityClass);
        this.activity = activity;
    }

    /**
     * Start the Activity and return the result at the end.
     * @param resultId the result id to identifier the result.
     */
    public void startActivityForResult(final int resultId) {
        startActivityForResult(resultId, PermissionRequest.NEEDED_EDITOR_PERMISSIONS);
    }

    /**
     * Start the Activity and return the result at the end.
     * @param resultId the result id to identifier the result.
     * @param permissions Permission that muss be accept for Android 6.0 and above
     */
    public void startActivityForResult(final int resultId, @NonNull String[] permissions) {
        if (PermissionRequest.hasAllPermission(activity, permissions)) {
            activity.startActivityForResult(this, resultId);
        } else {
            PermissionRequest.getPermission(activity, permissions, new PermissionRequest.Response() {
                @Nullable
                private final PermissionRequest.Response callback = (activity instanceof PermissionRequest.Response) ? (PermissionRequest.Response) activity : null;
                @Override
                public void permissionGranted() {
                    callback.permissionGranted();
                    activity.startActivityForResult(ImgLyIntent.this, resultId);
                }

                @Override
                public void permissionDenied() {
                    callback.permissionDenied();
                }
            });
        }
    }

    @NonNull
    @Override
    public Intent putExtra(String name, boolean value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, byte value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, char value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, short value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, int value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, long value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, float value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, double value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, String value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, CharSequence value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, Parcelable value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, Parcelable[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, Serializable value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, boolean[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, byte[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, short[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, char[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, int[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, long[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, float[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, double[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, String[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, CharSequence[] value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putExtra(String name, Bundle value) {
        removeExtra(name);
        return super.putExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putIntegerArrayListExtra(String name, ArrayList<Integer> value) {
        removeExtra(name);
        return super.putIntegerArrayListExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putParcelableArrayListExtra(String name, ArrayList<? extends Parcelable> value) {
        removeExtra(name);
        return super.putParcelableArrayListExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putStringArrayListExtra(String name, ArrayList<String> value) {
        removeExtra(name);
        return super.putStringArrayListExtra(name, value);
    }

    @NonNull
    @Override
    public Intent putCharSequenceArrayListExtra(String name, ArrayList<CharSequence> value) {
        removeExtra(name);
        return super.putCharSequenceArrayListExtra(name, value);
    }
}
