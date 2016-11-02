package com.abcew.camera.ui.utilities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by laputan on 16/11/1.
 */
public class PermissionRequest {

    /**
     * Permissions needed for the Preview Activity
     */
    public static final String[] NEEDED_PREVIEW_PERMISSIONS = new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    /**
     * Permissions needed for the Editor Activity
     */
    public static final String[] NEEDED_EDITOR_PERMISSIONS = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    /**
     * Callback Interface
     */
    public interface Response {
        /**
         * Would be called if the permissions are granted
         * {@inheritDoc}
         */
        void permissionGranted();
        /**
         * Would be called if any permission are denied
         * {@inheritDoc}
         */
        void permissionDenied();
    }

    private static class ResponseWrapper {
        final Response response;
        final String[] permission;

        public ResponseWrapper(Response response, String permission) {
            this.response = response;
            this.permission = new String[]{ permission };
        }

        public ResponseWrapper(Response response, String[] permission) {
            this.response = response;
            this.permission = permission;
        }
    }

    private static final HashMap<Integer, ResponseWrapper> map = new HashMap<>();

    /**
     * Request Android Permissions.
     * @param context The Context of the Activity or the Fragment.
     * @param permissions The Permission you are need.
     * @param response Result callback.
     */
    public static void getPermission(@NonNull Context context, String permissions, @NonNull Response response) {
        getPermission(context, new String[]{permissions}, response);
    }

    /**
     * Check if the user has granted the permissions.
     * @param context a Android context
     * @param permissions The Permission you are need to check.
     * @return true if all permissions are granted or false if any permission is missing
     */
    public static boolean hasAllPermission(@NonNull Context context, @NonNull String[] permissions) {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        } else {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Request Android Permissions.
     * @param context The Context of the Activity or the Fragment.
     * @param permissions The Permissions you are need.
     * @param response Result callback.
     */
    public static void getPermission(@NonNull Context context, @NonNull String[] permissions, @NonNull Response response) {
        if (Build.VERSION.SDK_INT < 23) {
            response.permissionGranted();
        } else {

            HashSet<String> permissionSet = new HashSet<>();

            for (String permission : permissions) if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionSet.add(permission);
            }

            if (permissionSet.size() > 0) {

                Activity activity = (context instanceof Activity) ? (Activity) context : null;

                if (activity == null) {
                    response.permissionDenied();
                    return;
                }

                int id = 42167;
                while(map.containsKey(id)) id = (int) Math.round(Math.random() * Integer.MAX_VALUE);

                map.put(id, new ResponseWrapper(response, permissions));

                activity.requestPermissions(permissionSet.toArray(new String[permissionSet.size()]), id);
            } else {
                response.permissionGranted();
            }
        }
    }

    /**
     * Must bee called by Activity.onRequestPermissionsResult
     * @param requestCode The automatically generated request code
     * @param permissions The requested permissions
     * @param grantResults The result codes of the request
     * @see Activity
     */
    public static void onRequestPermissionsResult(int requestCode, String permissions[], @NonNull int[] grantResults) {
        ResponseWrapper responseWrapper = map.get(requestCode);
        if (responseWrapper == null) return;

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted, yay! Do the

            responseWrapper.response.permissionGranted();

        } else {

            // permission denied, boo! Disable the
            // functionality that depends on this permission.

            responseWrapper.response.permissionDenied();
        }
    }
}
