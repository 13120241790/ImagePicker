package com.github.picker.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.github.picker.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Created by jiangecho on 2016/10/25.
 */

public class PermissionCheckUtil {
    private static final String TAG = PermissionCheckUtil.class.getSimpleName();

    public static boolean requestPermissions(final Fragment fragment, @NonNull String[] permissions) {
        return requestPermissions(fragment, permissions, 0);
    }

    /**
     * <pre>
     *  how to use
     *  1. call this method
     *  2. if return true, all permissions are granted, just continue your logic
     *  3. override onRequestPermissionsResult to check result
     *  4. optional override onActivityResult to check result
     *
     * </pre>
     *
     * @param fragment
     * @param permissions permissions to check
     * @param requestCode 0-255
     * @return true, when all permissions are granted;
     */
    @TargetApi(23)
    public static boolean requestPermissions(final Fragment fragment, @NonNull final String[] permissions, final int requestCode) {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return true;
        }

        if (permissions == null || permissions.length == 0) {
            return true;
        }

        final Activity context = fragment.getActivity();
        final List<String> permissionsNotGranted = new ArrayList<>();
        final int[] requestResults = new int[permissions.length];
        boolean shouldShowRequestPermissionRationale = false;
        boolean result = false;


        for (int i = 0; i < permissions.length; i++) {
            requestResults[i] = context.checkCallingOrSelfPermission(permissions[i]);
            if (requestResults[i] != PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.add(permissions[i]);

                //shouldShowRequestPermissionRationale(permission) return false when user denied the permission and checked don't ask again
                if (!shouldShowRequestPermissionRationale && !context.shouldShowRequestPermissionRationale(permissions[i])) {
                    shouldShowRequestPermissionRationale = true;
                }
            }
        }

        if (shouldShowRequestPermissionRationale) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                            intent.setData(uri);
                            context.startActivityForResult(intent, requestCode > 0 ? requestCode : -1);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            context.onRequestPermissionsResult(requestCode, permissions, requestResults);
                            break;
                        default:
                            break;
                    }

                }
            };
            showPermissionAlert(context, context.getResources().getString(R.string.rc_permission_grant_needed) + getNotGrantedPermissionMsg(context, permissionsNotGranted), listener);
        } else if (permissionsNotGranted.size() > 0) {
            context.requestPermissions(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]), requestCode);
        } else {
            result = true;
        }

        return result;
    }

    public static boolean requestPermissions(final Activity activity, @NonNull String[] permissions) {
        return requestPermissions(activity, permissions, 0);
    }

    @TargetApi(23)
    public static boolean requestPermissions(final Activity activity, @NonNull final String[] permissions, final int requestCode) {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return true;
        }

        if (permissions == null || permissions.length == 0) {
            return true;
        }

        final List<String> permissionsNotGranted = new ArrayList<>();
        final int[] requests = new int[permissions.length];
        boolean shouldShowRequestPermissionRationale = false;
        boolean result = false;

        for (int i = 0; i < permissions.length; i++) {
            requests[i] = activity.checkCallingOrSelfPermission(permissions[i]);
            if (requests[i] != PackageManager.PERMISSION_GRANTED) {
                permissionsNotGranted.add(permissions[i]);
                //shouldShowRequestPermissionRationale(permission) return false when user denied the permission and checked don't ask again
                if (!shouldShowRequestPermissionRationale && !activity.shouldShowRequestPermissionRationale(permissions[i])) {
                    shouldShowRequestPermissionRationale = true;
                }
            }

        }

        if (shouldShowRequestPermissionRationale) {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                            intent.setData(uri);
                            activity.startActivityForResult(intent, requestCode > 0 ? requestCode : -1);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            activity.onRequestPermissionsResult(requestCode, permissions, requests);
                            break;
                        default:
                            break;
                    }

                }
            };
            showPermissionAlert(activity, activity.getResources().getString(R.string.rc_permission_grant_needed) + getNotGrantedPermissionMsg(activity, permissionsNotGranted), listener);
        } else if (permissionsNotGranted.size() > 0) {
            activity.requestPermissions(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]), requestCode);
        } else {
            result = true;
        }
        return result;
    }

    public static boolean checkPermissions(Context context, @NonNull String[] permissions) {
        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
            return true;
        }

        if (permissions == null || permissions.length == 0) {
            return true;
        }
        for (String permission : permissions) {
            if (context.checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static String getNotGrantedPermissionMsg(Context context, List<String> permissions) {
        Set<String> permissionsValue = new HashSet<>();
        String permissionValue;
        for (String permission : permissions) {
            permissionValue = context.getString(context.getResources().getIdentifier("rc_" + permission, "string", context.getPackageName()), 0);
            permissionsValue.add(permissionValue);
        }

        String result = "(";
        for (String value : permissionsValue) {
            result += (value + " ");
        }
        result = result.trim() + ")";
        return result;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private static void showPermissionAlert(Context context, String content, DialogInterface.OnClickListener listener) {
        new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert)
                .setMessage(content)
                .setPositiveButton(R.string.rc_confirm, listener)
                .setNegativeButton(R.string.rc_cancel, listener)
                .setCancelable(false)
                .create()
                .show();
    }

    /**
     * 检查是否有悬浮窗权限
     * @param context
     * @return
     */
    @TargetApi(19)
    public static boolean canDrawOverlays(Context context) {
        boolean result = true;
        boolean booleanValue;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                booleanValue = (Boolean) Settings.class.getDeclaredMethod("canDrawOverlays", new Class[]{Context.class}).invoke(null, new Object[]{context});
                Log.i(TAG, "isFloatWindowOpAllowed allowed: " + booleanValue);
                return booleanValue;
            } catch (Exception e) {
                Log.e(TAG, String.format("getDeclaredMethod:canDrawOverlays! Error:%s, etype:%s", e.getMessage(), e.getClass().getCanonicalName()));
                return true;
            }
        } else if (Build.VERSION.SDK_INT < 19) {
            return true;
        } else {
            Method method;
            Object systemService = context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                method = Class.forName("android.app.AppOpsManager").getMethod("checkOp", new Class[]{Integer.TYPE, Integer.TYPE, String.class});
            } catch (NoSuchMethodException e) {
                Log.e(TAG, String.format("NoSuchMethodException method:checkOp! Error:%s", e.getMessage()));
                method = null;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                method = null;
            }
            if (method != null) {
                try {
                    Integer tmp = (Integer) method.invoke(systemService, new Object[]{Integer.valueOf(24), Integer.valueOf(context.getApplicationInfo().uid), context.getPackageName()});
                    result = tmp == 0;
                } catch (Exception e) {
                    Log.e(TAG, String.format("call checkOp failed: %s etype:%s", e.getMessage(), e.getClass().getCanonicalName()));
                }
            }
            Log.i(TAG, "isFloatWindowOpAllowed allowed: " + result);
            return result;
        }
    }
}
