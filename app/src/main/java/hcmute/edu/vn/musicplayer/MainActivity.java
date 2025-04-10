package hcmute.edu.vn.musicplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;

public class MainActivity extends AppCompatActivity
{
    private final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (hasAllPermissions())
            goToPlaylist();
        else
            requestPermissions();
    }

    private boolean hasAllPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        else
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.POST_NOTIFICATIONS,
                        },
                        REQUEST_CODE);
            }
        }
        else
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },
                    REQUEST_CODE);
        }
    }

    private void goToPlaylist()
    {
        Intent intent = new Intent(this, PlaylistActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (hasAllPermissions())
            goToPlaylist();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE)
        {
            if (hasAllPermissions())
            {
                goToPlaylist();
            }
            else
            {
                boolean shouldShow = true;
                for (String permission : permissions)
                {
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                    {
                        // User selected "Don't ask again"
                        shouldShow = false;
                        break;
                    }
                }

                if (!shouldShow)
                {
                    // Show explanation + send to app settings
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Permission Required")
                            .setMessage("Please grant storage and media permissions from settings to use this app.")
                            .setPositiveButton("Open Settings", (dialog, which) -> {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                                dialog.dismiss();
                            })
                            .setNegativeButton("Exit", (dialog, which) -> {
                                dialog.dismiss();
                                finish();
                            })
                            .show();
                }
                else
                {
                    // Retry permission request
                    requestPermissions();
                }
            }
        }
    }

}