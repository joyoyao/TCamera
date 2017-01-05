package com.abcew.camera;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.abcew.camera.configuration.AbstractConfig;
import com.abcew.camera.configuration.PhotoEditorSdkConfig;
import com.abcew.camera.ui.acs.Cam;
import com.abcew.camera.ui.acs.CamView;
import com.abcew.camera.ui.adapter.DataSourceListAdapter;
import com.abcew.camera.ui.utilities.OrientationSensor;
import com.abcew.camera.ui.utilities.PermissionRequest;
import com.abcew.camera.ui.widgets.ExpandToggleButton;
import com.abcew.camera.ui.widgets.ExpandableView;
import com.abcew.camera.ui.widgets.GalleryButton;
import com.abcew.camera.ui.widgets.GlCameraPreview;
import com.abcew.camera.ui.widgets.HorizontalListView;
import com.abcew.camera.ui.widgets.ShutterButton;
import com.abcew.camera.utils.CameraPreferences;

import java.io.File;

public class CameraActivity extends AppCompatActivity implements DataSourceListAdapter.OnItemClickListener<AbstractConfig.ImageFilterInterface>, CamView.OnSizeChangeListener, CamView.CaptureCallback, Cam.OnStateChangeListener {
    public static final String RESULT_IMAGE_PATH = "RESULT_IMAGE_PATH";

    private static final int RESULT_EDITOR_DONE = 2;
    private static final int RESULT_LOAD_IMAGE = 1;

    private GlCameraPreview preview;

    private CamView cameraView;

    private Button flashButton;

    private ToggleButton hdrToggleButton;

    private HorizontalListView filterListView;

    private ExpandableView expandableView;

    private View actionBar;

    private View filterBar;

    private View rootView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

//        ImgSdk.getAnalyticsPlugin().changeScreen("CameraPreview");


        initViews();

        DataSourceListAdapter toolListAdapter = new DataSourceListAdapter(this);
        toolListAdapter.setData(PhotoEditorSdkConfig.getFilterConfig());
        toolListAdapter.setOnItemClickListener(this);
        filterListView.setAdapter(toolListAdapter);

        preview = new GlCameraPreview(this);

        cameraView.setPreview(preview);
        cameraView.setOnSizeChangeListener(this);
        cameraView.setOnStateChangeListener(this);

        cameraView.post(new Runnable() {
            @Override
            public void run() {
                cameraView.setCameraFacing(CameraPreferences.cameraFacing.get());
                hdrToggleButton.setChecked(Cam.SCENE_MODE.HDR == cameraView.setSceneMode(CameraPreferences.isHDR.get() ? Cam.SCENE_MODE.HDR : Cam.SCENE_MODE.AUTO));
                setFlashMode(CameraPreferences.flashMode.get());
            }
        });
    }

    private void initViews() {

        cameraView = (CamView) findViewById(R.id.cameraView);
        ShutterButton shootButton = (ShutterButton) findViewById(R.id.shutterButton);
        GalleryButton galleryButton = (GalleryButton) findViewById(R.id.galleryButton);
        ImageButton cameraSwitchButton = (ImageButton) findViewById(R.id.switchCameraButton);
        flashButton = (Button) findViewById(R.id.flashButton);
        hdrToggleButton = (ToggleButton) findViewById(R.id.hdrButton);
        filterListView = (HorizontalListView) findViewById(R.id.filterList);
        expandableView = (ExpandableView) findViewById(R.id.expandableView);
        actionBar = findViewById(R.id.imglyActionBar);
        filterBar = findViewById(R.id.filterBar);
        rootView = findViewById(R.id.rootView);
        ExpandToggleButton expandToggleButton = (ExpandToggleButton) findViewById(R.id.show_filter_button);

        shootButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTakePicture(view);
            }
        });

        cameraSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSwitchCamera(view);
            }
        });

        flashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onToggleFlashLight((Button) view);
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOpenGallery((GalleryButton) view);
            }
        });

        hdrToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onToggleHdr((ToggleButton) compoundButton, b);
            }
        });

        expandToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                onClickFilterButton((ExpandToggleButton) compoundButton, b);
            }
        });

    }

    @Override
    public void onImageCaptured(String outputPath) {
//        if (intent.getOpenEditor()) {
//            PhotoEditorIntent editorIntent = new PhotoEditorIntent(intent.getEditorIntent(), this);
//            editorIntent.setSourceImagePath(outputPath);
//            editorIntent.setFilter(preview.getFilter());
//            editorIntent.startActivityForResult(RESULT_EDITOR_DONE);
//        } else {
//            setResult(RESULT_OK);
//        }
    }

    @Override
    public void onImageCaptureError(Exception exception) {

    }

    AbstractConfig.ImageFilterInterface anInterface;

    @Override
    public void onItemClick(AbstractConfig.ImageFilterInterface entity) {
        anInterface=entity;
        preview.setFilter(entity);
    }


    public void onTakePicture(View button) {
        final String filePrefix = ImgSdk.getExportPrefix();

        File mMediaFolder = new File(ImgSdk.getExportPath());
        if (!mMediaFolder.exists()) {
            mMediaFolder.mkdirs(); //TODO: SD-Card Error Handling!
        }

        String filePath = mMediaFolder.getAbsolutePath() + "/" + filePrefix + System.currentTimeMillis() + ".jpg";

        Log.i("filePath",filePath);

        cameraView.capture(filePath, this,anInterface);
//        cameraView.onResume();
    }

    public void onSwitchCamera(View switchButton) {
        final Cam.CAMERA_FACING facing;
        switch (cameraView.getCameraFacing()) {
            case FRONT:
                facing = cameraView.setCameraFacing(Cam.CAMERA_FACING.BACK);
                break;

            case BACK:
            default:
                facing = cameraView.setCameraFacing(Cam.CAMERA_FACING.FRONT);
                break;
        }
        CameraPreferences.cameraFacing.set(facing);
    }

    public void onToggleFlashLight(Button flashButton) {
        final Cam.FLASH_MODE mode;
        switch (cameraView.getFlashMode()) {

            case AUTO:
                mode = setFlashMode(Cam.FLASH_MODE.OFF);
                break;

            case ON:
                mode = setFlashMode(Cam.FLASH_MODE.AUTO);
                break;

            case OFF:
            default:
                mode = setFlashMode(Cam.FLASH_MODE.ON);
                break;
        }

        CameraPreferences.flashMode.set(mode);
    }

    public void onToggleHdr(ToggleButton hdrButton, boolean isChecked) {
        cameraView.setSceneMode(isChecked ? Cam.SCENE_MODE.HDR : Cam.SCENE_MODE.AUTO);
    }

    public void onClickFilterButton(ExpandToggleButton showFilterButton, boolean isChecked) {
        if (isChecked) {
            expandableView.expand();
        } else {
            expandableView.collapse();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Cam.FLASH_MODE setFlashMode(final Cam.FLASH_MODE newMode) {
        final Cam.FLASH_MODE mode = cameraView.setFlashMode(newMode);
        return mode;
    }


    @Override
    public void onCamViewResize(final int w, final int h) {

        RectF camRect = new RectF(0, 0, w, h);
        RectF freeRect = new RectF(0, actionBar.getHeight(), rootView.getWidth(), rootView.getHeight() - filterBar.getHeight());

        float translateY = (freeRect.centerY() - camRect.centerY());

        if ((camRect.centerY() + translateY) - (camRect.height() / 2) < 0) {
            translateY -= (camRect.centerY() + translateY) - (camRect.height() / 2);
        }

        cameraView.setY(translateY);

    }

    @Override
    protected void onResume() { //TODO:
        super.onResume();
        if (cameraView != null) {
            cameraView.onResume();
        }

        OrientationSensor.getInstance().start(PhotoEditorSdkConfig.getCameraScreenRotationMode());
    }

    @Override
    protected void onPause() { //TODO:
        if (cameraView != null) {
            cameraView.onPause();
        }

        OrientationSensor.getInstance().stop();

        super.onPause();
    }


    @TargetApi(19)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            cameraView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    public CamView getCameraView() {
        return cameraView;
    }

    public void onOpenGallery(GalleryButton button) {
        android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);

            if (cursor == null || cursor.getCount() < 1) {
                return; // no cursor or no record. DO YOUR ERROR HANDLING
            }

            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);

            if (columnIndex < 0) // no column index
                return; // DO YOUR ERROR HANDLING

            String picturePath = cursor.getString(columnIndex);

            cursor.close();


            Intent result = new Intent();

            result.putExtra(RESULT_IMAGE_PATH, picturePath);

            setResult(RESULT_OK, result);

        } else if (requestCode == RESULT_EDITOR_DONE) {

            setResult(resultCode, data); //to loop through
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    @Override
    public void onCamViewStateChange(@NonNull final Cam.State state) {

        flashButton.post(new Runnable() {
            @Override
            public void run() {
                final Cam.FLASH_MODE mode = cameraView.getFlashMode();
                final String label;
                final Resources resources = getResources();

                switch (mode) {
                    case AUTO:
                        label = resources.getString(R.string.camera_preview_flash_auto);
                        break;

                    case ON:
                        label = resources.getString(R.string.camera_preview_flash_on);
                        break;

                    case OFF:
                    default:
                        label = resources.getString(R.string.camera_preview_flash_off);
                        break;
                }

                flashButton.setText(label);


                boolean isHDR = state.getSceneMode() == Cam.SCENE_MODE.HDR;
                CameraPreferences.isHDR.set(isHDR);
                hdrToggleButton.setChecked(isHDR);

                boolean hasHDRSupport = Build.VERSION.SDK_INT > 17 && cameraView.hasSceneMode(Camera.Parameters.SCENE_MODE_HDR);
                hdrToggleButton.setVisibility(hasHDRSupport ? View.VISIBLE : View.INVISIBLE);
            }
        });

    }
}
