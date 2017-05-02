package com.zacharee1.dpichanger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

//import com.stericson.RootShell.RootShell;

public class MainActivity extends AppCompatActivity {
    public SetThings setThings;

    private boolean isSetup;
    private boolean isRooted;

    public ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setThings = new SetThings(this);
        isSetup = setThings.sharedPreferences.getBoolean("isSetup", false);

        if (isSetup) {
            Intent intent = new Intent(this, DPIActivity.class);
            startActivity(intent);
            finish();
        } else setup();
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(getItem(-1), true);
        } else {
            super.onBackPressed();
        }
    }

    public void setup() {
        viewPager = (ViewPager) findViewById(R.id.setup_pager);
        viewPager.setAdapter(new CustomPagerAdapter(this));
    }

    public void nextPage(View v) {
        viewPager.setCurrentItem(getItem(+1), true);
    }

    public int getItem(int i) {
        return viewPager.getCurrentItem() + i;
    }

    public void getRoot(View v) {
        isRooted = testSudo();

        if (isRooted) {
            viewPager.setCurrentItem(getItem(+2), true);
            Toast.makeText(this, getResources().getText(R.string.rooted), Toast.LENGTH_SHORT).show();
            setThings.editor.putBoolean("isRooted", true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sudo("pm grant com.zacharee1.dpichanger android.permission.WRITE_SECURE_SETTINGS");
                }
            }).start();
        } else {
            viewPager.setCurrentItem(getItem(+1), true);
            Toast.makeText(this, getResources().getText(R.string.not_rooted), Toast.LENGTH_SHORT).show();
            setThings.editor.putBoolean("isRooted", false);
        }

        setThings.editor.apply();
    }

    public void testPerms(View v) {
        try {
            Settings.Secure.putInt(getContentResolver(), "adb_set_up", 1);
        } catch (Exception e) {
            Toast.makeText(this, getResources().getText(R.string.adb_setup_failed), Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this, getResources().getText(R.string.setup_done), Toast.LENGTH_SHORT).show();
        setThings.editor.putBoolean("isSetup", true);
        setThings.editor.apply();
        Intent intent = new Intent(getApplicationContext(), DPIActivity.class);
        startActivity(intent);
    }

    private boolean testSudo() {
        try{
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(su.getOutputStream());
            outputStream.close();

//            outputStream.writeBytes("exit\n");
//            outputStream.flush();

            DataInputStream inputStream = new DataInputStream(su.getInputStream());
            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }

            inputStream = new DataInputStream(su.getErrorStream());
            r = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }

            if (!total.toString().toLowerCase().contains("permission denied") && !total.toString().toLowerCase().contains("not found"))
                return true;

        } catch(IOException e){
            e.printStackTrace();
        }

        return false;
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
