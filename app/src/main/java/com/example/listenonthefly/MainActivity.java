package com.example.listenonthefly;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity"; 

    // royalty free music from bensound.com, for testing
    public static final int MEDIA_RES_ID = R.raw.bensoundtheelevatorbossanova;

    private PlayerAdapter mPlayerAdapter;
    private SeekBar mSeekBar;
    private RecyclerView mRecycler;
    private boolean isUserSeeking = false;
    public ArrayList<songListItem> songs = new ArrayList<songListItem>();

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

    @Override
    protected void onStart(){
        super.onStart();
        mPlayerAdapter.loadMedia(MEDIA_RES_ID);
    }

    @Override
    protected void onStop(){
        super.onStop();
        mPlayerAdapter.release();
    }

    private void initUI(){
        Button btnPlay = (Button) findViewById(R.id.btnPlay);
        Button btnPause = (Button) findViewById(R.id.btnPause);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mRecycler = (RecyclerView) findViewById(R.id.recyclerView);

        btnPlay.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.play();
                    }
                });
        btnPause.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPlayerAdapter.pause();
                    }
                });

        Log.d(TAG, "initUI: complete");
    }

    // read user music directory, use adapter to populate recyclerview
    private void initRecycler(){

        songListItem test1 = new songListItem("Test this");
        songListItem test2 = new songListItem("Test are cool");
        songListItem test3 = new songListItem("Test bananana");
        songListItem test4 = new songListItem("Test balls");

        ItemAdapater adpater = new ItemAdapater(songs);
        mRecycler.setAdapter(adpater);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

    }

    private void initSeekbar(){
        mSeekBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userPosition = 0;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser){
                            userPosition = progress;
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        isUserSeeking = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        isUserSeeking = false;
                        mPlayerAdapter.seekTo(userPosition);
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
            mSeekBar.setMax(duration);
            Log.d(TAG, "onDurationChanged: complete");
        }

        @Override
        public void onPositionChanged(int position){
            if (!isUserSeeking){
                mSeekBar.setProgress(position, true);
                Log.d(TAG, "onPositionChanged: complete");
            }
        }
    }

    public class songListItem{
        private String title;

        public songListItem(String inputTitle){
            title = inputTitle;

        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }

    public class ItemAdapater extends RecyclerView.Adapter<ItemAdapater.ViewHolder>{

        private List<songListItem> songs;

        public ItemAdapater(List<songListItem> inputSongs) {
            this.songs = inputSongs;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View itemView = inflater.inflate(R.layout.songlistitem, parent, false);

            ViewHolder viewHolder = new ViewHolder(itemView);
            return viewHolder;

        }

        @Override
        public void onBindViewHolder(@NonNull ItemAdapater.ViewHolder viewHolder, int i) {

        }

        @Override
        public int getItemCount(){
            return songs.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{


            public ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }

}
