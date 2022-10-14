package pe.saydomonkos.lightweightgps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.Arrays;

public class PermissionHandler {
    private String[] permissions;
    private String permission;

    public PermissionHandler() {
        //declare variables to avoid errors from nulldata
        permissions = new String[0];
        permission = "";
    }

    public boolean handle(Context context, Activity activity, final int PERMISSION) {
        if (permissions.length > 0) {
            //we have multiple permissions
            String[] permissionsToRequest = new String[0];
            for (String perm : permissions) {
                if (ActivityCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        permissionsToRequest = Arrays.copyOf(permissionsToRequest, permissionsToRequest.length + 1);
                        permissionsToRequest[permissionsToRequest.length - 1] = perm;
                    }
                }
            }
            if (permissionsToRequest.length > 0) {
                ActivityCompat.requestPermissions(
                        activity,
                        permissionsToRequest,
                        PERMISSION
                );
                return true;
            }
        } else {
            //we only have 1 permission
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{permission},
                        PERMISSION
                );
                return true;
            }
        }
        return false;
    }

    public PermissionHandler permissions(String[] permissions) {
        //multiple permission to check and/or request
        this.permissions = permissions;
        return this;
    }

    public PermissionHandler permission(String permission) {
        //a permission to check and/or request
        this.permission = permission;
        return this;
    }
}
