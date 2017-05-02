package com.zacharee1.dpichanger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.support.v4.os.UserManagerCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Zacha on 4/30/2017.
 */

public class SetThings {
    private Activity currentActivity;
    public SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public boolean isRooted;

    public SetThings(Activity activity) {
        currentActivity = activity;
        sharedPreferences = activity.getSharedPreferences("com.zacharee1.dpichanger", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();

        isRooted = sharedPreferences.getBoolean("isRooted", false);
    }

    public void buttons(int[] buttons) {
        for (final int id : buttons) {
            Button button = (Button) currentActivity.findViewById(id);

            if (button != null) {
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        switch (id) {
                            case R.id.apply_dpi:
                                String newDPI = sharedPreferences.getString("new_dpi_value", String.valueOf(currentActivity.getResources().getDisplayMetrics().densityDpi));
                                if (isRooted) {
                                    sudo("wm density " + newDPI);
                                } else {
                                    Settings.Secure.putString(currentActivity.getContentResolver(), "display_density_forced", newDPI);
                                }
                        }
                    }
                });
            }
        }
    }

    public void sudo(String...strings) {
        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());

            for (String s : strings) {
                outputStream.writeBytes(s+"\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            try {
                su.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e("No Root?", e.getMessage());
            }
            outputStream.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
