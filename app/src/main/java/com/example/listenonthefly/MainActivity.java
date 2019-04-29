package com.example.listenonthefly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";

    // royalty free music from bensound.com, for testing
    public static final int MEDIA_RES_ID = R.raw.bensoundtheelevatorbossanova;

    private PlayerAdapter mPlayerAdapter;
    private SeekBar mSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initRecycler();
        initSeekbar();
        initPlaybackController();

        Log.d(TAG, "onCreate: complete");
    }

    private void initUI(){
        Button btnPlay = (Button) findViewById(R.id.btnPlay);
        Button btnPause = (Button) findViewById(R.id.btnPause);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);

        btnPlay.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.pause();
                    }
                });
        btnPause.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.play();
                    }
                });
    }

    // read user music directory, use adapter to populate recyclerview
    private void initRecycler(){

    }

    private void initSeekbar(){
        mSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                }
        );
    }

    private void initPlaybackController(){
        PlayerHolder mPlayerHolder = new PlayerHolder(this);
        mPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mPlayerHolder;
        Log.d(TAG, "initPlaybackController: complete");
    }

    public class PlaybackListener extends PlaybackInfoListener {
        @Override
        public void onDurationChanged(int duration){

        }
    }
}
