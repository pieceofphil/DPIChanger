package com.zacharee1.dpichanger;

import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class DPIActivity extends AppCompatActivity {
    private TextInputEditText dpi_val;
    private DisplayMetrics metrics;
    private int[] buttons;
    private TextView noRootInstructions;

    public SetThings setThings;

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

        buttons = new int[] {
                R.id.apply_dpi
        };
        setThings.buttons(buttons);

        metrics = getResources().getDisplayMetrics();
        dpi_val = (TextInputEditText) findViewById(R.id.dpi_value);
        dpi_val.setHint(getResources().getText(R.string.dpi_value_hint) + " " + String.valueOf(metrics.densityDpi));

        noRootInstructions = (TextView) findViewById(R.id.no_root_instructions);
        if (setThings.isRooted) noRootInstructions.setVisibility(View.GONE);

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
    }
}
