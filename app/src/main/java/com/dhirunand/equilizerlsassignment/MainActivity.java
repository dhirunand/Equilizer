package com.dhirunand.equilizerlsassignment;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private SeekBar bass_boost_seekbar = null;
    private CheckBox eq_enabled_checkbox = null;
    private Button flat = null;

    public static final int REQUEST_CODE = 1;
    AudioManager manager;

    private TextView open_music_player = null;

    private Equalizer eq = null;
    private BassBoost bb = null;

    private int min_level = 0;
    private int max_level = 100;

    static final int MAX_SLIDERS = 8;
    private final SeekBar[] sliders_seekbar = new SeekBar[MAX_SLIDERS];
    private final TextView[] slider_labels_textview = new TextView[MAX_SLIDERS];

    int num_sliders = 0;

    MediaPlayer mp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        eq_enabled_checkbox = findViewById(R.id.eq_enabled_checkbox);
        eq_enabled_checkbox.setOnCheckedChangeListener(this);

        open_music_player = findViewById(R.id.open_music_player_textView);
        open_music_player.setOnClickListener(this);

        flat = findViewById(R.id.flat);
        flat.setOnClickListener(this);

        bass_boost_seekbar = findViewById(R.id.bass_boost_seekbar);
        bass_boost_seekbar.setOnSeekBarChangeListener(this);

        sliders_seekbar[0] = findViewById(R.id.slider_1);
        slider_labels_textview[0] = findViewById(R.id.slider_label_1);
        sliders_seekbar[1] = findViewById(R.id.slider_2);
        slider_labels_textview[1] = findViewById(R.id.slider_label_2);
        sliders_seekbar[2] = findViewById(R.id.slider_3);
        slider_labels_textview[2] = findViewById(R.id.slider_label_3);
        sliders_seekbar[3] = findViewById(R.id.slider_4);
        slider_labels_textview[3] = findViewById(R.id.slider_label_4);
        sliders_seekbar[4] = findViewById(R.id.slider_5);
        slider_labels_textview[4] = findViewById(R.id.slider_label_5);

        manager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (manager.isMusicActive())
            initialiseEqualizer();

    }

    public String formatBandLabel(int[] band) {
        int avg = (band[0] + band[1]) / 2;
        return (avg < 1000000) ? (avg / 1000 + "HZ") : (avg / 1000000 + "KHZ");
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int level, boolean fromTouch) {
        if (seekBar == bass_boost_seekbar) {
            bb.setEnabled(level > 0);
            bb.setStrength((short) level); // Already in the right range 0-1000
        } else if (eq != null) {
            int new_level = min_level + (max_level - min_level) * level / 100;

            for (int i = 0; i < num_sliders; i++) {
                if (sliders_seekbar[i] == seekBar) {
                    eq.setBandLevel((short) i, (short) new_level);
                    break;
                }
            }
        }
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }


    public void updateSliders() {
        for (int i = 0; i < num_sliders; i++) {
            int level;
            if (eq != null)
                level = eq.getBandLevel((short) i);
            else
                level = 0;
            int pos = 100 * level / (max_level - min_level) + 50;
            sliders_seekbar[i].setProgress(pos);
        }
    }

    public void updateBassBoost() {
        if (bb != null)
            bass_boost_seekbar.setProgress(bb.getRoundedStrength());
        else
            bass_boost_seekbar.setProgress(0);
    }


    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        if (view == eq_enabled_checkbox && eq != null) {
            eq.setEnabled(isChecked);
        }
    }


    @Override
    public void onClick(View view) {
        if (view == flat) {
            setFlat();
        }

        if (view == open_music_player) {
            Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (manager.isMusicActive()) {
            initialiseEqualizer();
        }

    }

    public void initialiseEqualizer() {
        eq = new Equalizer(0, 0);
        eq.setEnabled(true);
        num_sliders = eq.getNumberOfBands();  //Gets the number of frequency bands supported by the Equalizer engine.
        short[] r = eq.getBandLevelRange();  //Gets the level range for use by setBandLevel(short, short). The level is expressed in milliBel.
        min_level = r[0];
        max_level = r[1];

        for (int i = 0; i < num_sliders && i < MAX_SLIDERS; i++) {
            int[] freq_range = eq.getBandFreqRange((short) i);  //Gets the frequency range of the given frequency band.
            sliders_seekbar[i].setOnSeekBarChangeListener(this);
            slider_labels_textview[i].setText(formatBandLabel(freq_range));
        }

        bb = new BassBoost(0, 0);
        updateUserInterface();
    }

    public void setFlat() {
        if (eq != null) {
            for (int i = 0; i < num_sliders; i++)
                eq.setBandLevel((short) i, (short) 0);
        }

        if (bb != null) {
            bb.setEnabled(false);
            bb.setStrength((short) 0);
        }

        updateUserInterface();
    }

    public void updateUserInterface() {
        updateSliders();
        updateBassBoost();
        if (eq != null)
            eq_enabled_checkbox.setChecked(eq.getEnabled());
    }


    @Override
    protected void onPause() {
        //mp.pause();

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //mp.start();
    }

}