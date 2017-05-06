package com.zacharee1.dpichanger;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DPIActivity extends AppCompatActivity {
    private TextInputEditText dpi_val;
    private DisplayMetrics metrics;

    private SetThings setThings;
    private TextInputEditText res_width;
    private TextInputEditText res_height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dpi);

        setup();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu); //your file name
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setup:
                setThings.editor.putBoolean("isSetup", false);
                setThings.editor.apply();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void setup() {
        setThings = new SetThings(this);

        int[] buttons = new int[]{
                R.id.apply_dpi,
                R.id.apply_res,
                R.id.reset_dpi,
                R.id.reset_res,
        };
        setThings.buttons(buttons);

        metrics = getResources().getDisplayMetrics();
        dpi_val = (TextInputEditText) findViewById(R.id.dpi_value);
        dpi_val.setHint(getResources().getText(R.string.dpi_value_hint) + " " + String.valueOf(metrics.densityDpi));

        TextView noRootInstructions = (TextView) findViewById(R.id.no_root_instructions);
        if (setThings.isRooted) noRootInstructions.setVisibility(View.GONE);
        noRootInstructions = (TextView) findViewById(R.id.no_root_instructions_res);
        if (setThings.isRooted) noRootInstructions.setVisibility(View.GONE);

        res_width = (TextInputEditText) findViewById(R.id.res_width);
        res_height = (TextInputEditText) findViewById(R.id.res_height);
        int[] dims = setThings.getRes();
        res_width.setHint(getResources().getText(R.string.dpi_value_hint) + " " + String.valueOf(dims[0]));
        res_height.setHint(getResources().getText(R.string.dpi_value_hint) + " " + String.valueOf(dims[1]));

        textListeners();
    }

    private void textListeners() {
        dpi_val.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) setThings.editor.putString("new_dpi_value", s.toString());
                else setThings.editor.putString("new_dpi_value", String.valueOf(metrics.densityDpi));
                setThings.editor.apply();
            }
        });

        res_width.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) setThings.editor.putString("new_width", s.toString());
                else setThings.editor.putString("new_width", String.valueOf(setThings.getRes()[0]));
                setThings.editor.apply();
            }
        });

        res_height.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null) setThings.editor.putString("new_height", s.toString());
                else setThings.editor.putString("new_height", String.valueOf(setThings.getRes()[1]));
                setThings.editor.apply();
            }
        });
    }
}