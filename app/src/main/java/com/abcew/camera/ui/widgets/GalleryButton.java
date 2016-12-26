package com.abcew.camera.ui.widgets;

import android.Manifest;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.abcew.camera.ImgSdk;
import com.abcew.camera.ui.utilities.PermissionRequest;
import com.abcew.camera.utils.BitmapFactoryUtils;
import com.abcew.camera.utils.ExifUtils;

import java.io.File;

/**
 * Created by laputan on 16/11/1.
 */
public class GalleryButton extends ImageButton implements PermissionRequest.Response{
//ImageView editorPreviewView;

    public GalleryButton(Context context) {
        this(context, null);
    }

    public GalleryButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GalleryButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        post(new Runnable() {
            @Override
            public void run() {
                setLatestImagePreview();
            }
        });
    }

    private void setLatestImagePreview() {
        PermissionRequest.getPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE, this);
    }

    @Override
    public void permissionGranted() {
        post(new Runnable() {
            @Override
            public void run() {
                new LoadLastImage().execute();
            }
        });
    }

    @Override
    public void permissionDenied() {

    }

    private class LoadLastImage extends AsyncTask<Void, Void, Bitmap> {

        int previewSize = 1;
        int rotation = 0;
        boolean hasPermission;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            previewSize = getMeasuredWidth();
        }

        @Nullable
        @Override
        protected Bitmap doInBackground(Void... voids) {
            // Find the last picture
            String[] projection = new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.MIME_TYPE
            };

            final Cursor cursor = ImgSdk.getAppContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");

            // Put it in the image view
            if(cursor != null && cursor.getCount() > 0){
                cursor.moveToFirst();
                do {

                    String imageLocation = cursor.getString(1);

                    rotation = ExifUtils.getAngle(imageLocation);

                    if (imageLocation.contains("DCIM")) {
                        File imageFile = new File(imageLocation);
                        if (imageFile.exists()) {
                            return BitmapFactoryUtils.decodeFile(imageLocation, previewSize, true);
                        }
                    }
                } while (cursor.moveToNext());

                cursor.close();
            }

            return null;
        }

        @Override
        protected void onPostExecute(@Nullable Bitmap bitmap) {
            if (bitmap != null) {
                setImageBitmap(bitmap);
            }
        }
    }
}