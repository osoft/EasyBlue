package net.zalio.android.easyblue;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

/**
 * Created by Henry on 12/14/13.
 */
public class EditActivity extends Activity {

    public static String EXTRA_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE";
    public static String EXTRA_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";
    public static String KEY_SWITCH = "net.zalio.android.easyblue.switch";
    public static String KEY_BRIGHTNESS = "net.zalio.android.easyblue.brightness";
    public static String KEY_PERSISTENT= "net.zalio.android.easyblue.persistent";

    private Button mBtnSave;
    private Intent resultIntent;
    private int result = RESULT_CANCELED;
    private Switch mSwitch;
    private SeekBar mSbBrightness;
    private Switch mSwitchPersistent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mBtnSave = (Button)findViewById(R.id.btnSave);
        mSwitch = (Switch)findViewById(R.id.switchOnOff);
        mSbBrightness = (SeekBar)findViewById(R.id.sbBrightness);
        mSwitchPersistent = (Switch)findViewById(R.id.switchPersistent);

        Intent intent = getIntent();
        Bundle b = intent.getBundleExtra(EXTRA_BUNDLE);
        if (b != null) {
            boolean defSwitch = b.getBoolean(KEY_SWITCH);
            mSwitch.setChecked(defSwitch);
            int defBrightness = b.getInt(KEY_BRIGHTNESS);
            mSbBrightness.setProgress(defBrightness);
            boolean defPersistent = b.getBoolean(KEY_PERSISTENT);
            mSwitchPersistent.setChecked(defPersistent);
        }

        resultIntent = new Intent();

        mBtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result = RESULT_OK;
                Bundle b = new Bundle();
                b.putBoolean(KEY_SWITCH, mSwitch.isChecked());
                b.putInt(KEY_BRIGHTNESS, mSbBrightness.getProgress());
                resultIntent.putExtra(EXTRA_BUNDLE, b);
                resultIntent.putExtra(EXTRA_BLURB, "Switch it on: " + Boolean.toString(mSwitch.isChecked()) + "\nBrightness: " + mSbBrightness.getProgress());
                setResult(result, resultIntent);
                finish();
            }
        });

        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSbBrightness.setProgress(100);
                } else {
                    mSbBrightness.setProgress(0);
                }
            }
        });

        mSbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!mSwitch.isChecked() && fromUser && progress > 0) {
                    mSwitch.setChecked(true);
                } else if (mSwitch.isChecked() && fromUser && progress == 0) {
                    mSwitch.setChecked(false);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        setResult(result,resultIntent);
    }
}
