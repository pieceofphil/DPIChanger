package com.zacharee1.dpichanger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Zacha on 4/30/2017.
 */

public class SetThings {
    private final Activity currentActivity;
    public final SharedPreferences sharedPreferences;
    public final SharedPreferences.Editor editor;
    public final boolean isRooted;

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
                                    if (Build.VERSION.SDK_INT > 20) {
                                        sudo("wm density " + newDPI);
                                    } else {
                                        sudo("am display-density " + newDPI);
                                    }
                                } else {
                                    Settings.Secure.putString(currentActivity.getContentResolver(), "display_density_forced", newDPI);
                                    if (Build.VERSION.SDK_INT > 16) {
                                        Settings.Global.putString(currentActivity.getContentResolver(), "display_density_forced", newDPI);
                                    }
                                }
                                break;
                            case R.id.apply_res:
                                String newWidth = sharedPreferences.getString("new_width", String.valueOf(currentActivity.getResources().getDisplayMetrics().widthPixels));
                                String newHeight = sharedPreferences.getString("new_height", String.valueOf(currentActivity.getResources().getDisplayMetrics().heightPixels));

                                if (isRooted) {
                                    if (Build.VERSION.SDK_INT > 20) {
                                        sudo("wm size " + newWidth + "x" + newHeight);
                                    } else {
                                        sudo("am display-size " + newWidth + "x" + newHeight);
                                    }
                                } else {
                                    Settings.Secure.putString(currentActivity.getContentResolver(), "display_size_forced", newWidth + "," + newHeight);
                                    if (Build.VERSION.SDK_INT > 16) {
                                        Settings.Global.putString(currentActivity.getContentResolver(), "display_size_forced", newWidth + "," + newHeight);
                                    }
                                }
                        }
                    }
                });
            }
        }
    }

    private void sudo(String... strings) {
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
