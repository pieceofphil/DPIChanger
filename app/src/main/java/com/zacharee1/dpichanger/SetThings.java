package com.zacharee1.dpichanger;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Zacha on 4/30/2017.
 */

public class SetThings {
    private final Activity currentActivity;
    public final SharedPreferences sharedPreferences;
    public final SharedPreferences.Editor editor;
    public final boolean isRooted;
    public DisplayMetrics metrics;
    public int SDK_INT;

    public int DEFAULT_DPI;
    public int DEFAULT_WIDTH;
    public int DEFAULT_HEIGHT;

    private String DPI_PREF = "display_density_forced";
    private String RES_PREF = "display_size_forced";
    private int DENSITY_DEFAULT = 160;
    private String newDPI;
    private String newWidth;
    private String newHeight;
    private final IWindowManager iWindowManager;
    private final Class<?> iw;


    public SetThings(Activity activity) {
        iWindowManager = IWindowManager.Stub.asInterface(ServiceManager.checkService(Context.WINDOW_SERVICE));
        iw = iWindowManager.getClass();
        currentActivity = activity;
        metrics = currentActivity.getResources().getDisplayMetrics();
        SDK_INT = Build.VERSION.SDK_INT;
        DEFAULT_DPI = SystemPropertiesProxy.getInt(currentActivity, "qemu.sf.lcd_density",
                SystemPropertiesProxy.getInt(currentActivity, "ro.sf.lcd_density", DENSITY_DEFAULT));

        Display display = currentActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        DEFAULT_WIDTH = size.x;
        DEFAULT_HEIGHT = size.y;

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
                        newDPI = sharedPreferences.getString("new_dpi_value", String.valueOf(metrics.densityDpi));
                        newWidth = sharedPreferences.getString("new_width", String.valueOf(getRes()[0]));
                        newHeight = sharedPreferences.getString("new_height", String.valueOf(getRes()[1]));

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                switch (id) {
                                    case R.id.apply_dpi:
                                        if (SDK_INT > 17) {
                                            setDPI();
                                        } else {
                                            if (isRooted && SDK_INT > 16) {
                                                sudo("am display-density " + newDPI);
                                            } else {
                                                Settings.Secure.putString(currentActivity.getContentResolver(), DPI_PREF, newDPI);
                                                if (SDK_INT > 16) {
                                                    Settings.Global.putString(currentActivity.getContentResolver(), DPI_PREF, newDPI);
                                                }
                                            }
                                        }
                                        break;
                                    case R.id.apply_res:
                                        if (SDK_INT > 17) {
                                            setRes();
                                        } else {
                                            if (isRooted) {
                                                sudo("am display-size " + newWidth + "x" + newHeight);
                                            } else {
                                                Settings.Secure.putString(currentActivity.getContentResolver(), RES_PREF, newWidth + "," + newHeight);
                                                if (Build.VERSION.SDK_INT > 16) {
                                                    Settings.Global.putString(currentActivity.getContentResolver(), RES_PREF, newWidth + "," + newHeight);
                                                }
                                            }
                                        }
                                        break;
                                    case R.id.reset_dpi:
                                        if (SDK_INT > 17) {
                                            resetDPI();
                                        } else {
                                            if (isRooted && SDK_INT > 16) {
                                                sudo("am display-density reset");
                                            } else {
                                                Settings.Secure.putString(currentActivity.getContentResolver(), DPI_PREF, "");
                                                if (SDK_INT > 16) {
                                                    Settings.Global.putString(currentActivity.getContentResolver(), DPI_PREF, "");
                                                }
                                            }
                                        }
                                        break;
                                    case R.id.reset_res:
                                        if (SDK_INT > 17) {
                                            resetRes();
                                        } else {
                                            if (isRooted) {
                                                sudo("am display-size reset");
                                            } else {
                                                Settings.Secure.putString(currentActivity.getContentResolver(), RES_PREF, "");
                                                if (SDK_INT > 16) {
                                                    Settings.Global.putString(currentActivity.getContentResolver(), RES_PREF, "");
                                                }
                                            }
                                        }
                                        break;
                                }
                            }
                        }).start();
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

    public int[] getRes() {
        String wmComm;
        int[] ret = new int[] { -1, -1 };
        if (SDK_INT > 20) {
            wmComm = "wm size";
        } else {
            wmComm = "am display-size";
        }
        try {
            Process wm = Runtime.getRuntime().exec(wmComm);

            DataOutputStream outputStream = new DataOutputStream(wm.getOutputStream());
            outputStream.writeBytes("exit\n");
            outputStream.flush();

            DataInputStream inputStream = new DataInputStream(wm.getInputStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }

            inputStream = new DataInputStream(wm.getErrorStream());
            r = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }

            String tot = total.toString();

            if (!tot.contains("Override size:")) {
                int colon = tot.indexOf(":");
                int x = tot.indexOf("x");

                String width = tot.substring(colon + 2, x);
                String height = tot.substring(x + 1, tot.indexOf("\n"));

                ret[0] = Integer.decode(width);
                ret[1] = Integer.decode(height);
            } else {
                String override = tot.substring(tot.indexOf("O"));
                int colon = override.indexOf(":");
                int x = override.indexOf("x");

                String width = override.substring(colon + 2, x);
                String height = override.substring(x + 1, tot.indexOf("\n"));

                ret[0] = Integer.decode(width);
                ret[1] = Integer.decode(height);
            }

            wm.waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    private void setDPI() {
        try {
            if (SDK_INT > 24) {
                iw.getMethod("setForcedDisplayDensityForUser", int.class, int.class, int.class).invoke(iWindowManager, Display.DEFAULT_DISPLAY, Integer.decode(newDPI), UserHandle.USER_CURRENT);
            } else {
                iw.getMethod("setForcedDisplayDensity", int.class, int.class).invoke(iWindowManager, Display.DEFAULT_DISPLAY, Integer.decode(newDPI));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetDPI() {
        try {
            if (SDK_INT > 24) {
                iw.getMethod("clearForcedDisplayDensityForUser", int.class, int.class).invoke(iWindowManager, Display.DEFAULT_DISPLAY, UserHandle.USER_CURRENT);
            } else {
                iw.getMethod("clearForcedDisplayDensity", int.class).invoke(iWindowManager, Display.DEFAULT_DISPLAY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setRes() {
        try {
            iWindowManager.setForcedDisplaySize(Display.DEFAULT_DISPLAY, Integer.decode(newWidth), Integer.decode(newHeight));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetRes() {
        try {
            iWindowManager.clearForcedDisplaySize(Display.DEFAULT_DISPLAY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Point getResTest() {
        Point baseSize = new Point();
        try {
            iWindowManager.getBaseDisplaySize(Display.DEFAULT_DISPLAY, baseSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baseSize;
    }
}
