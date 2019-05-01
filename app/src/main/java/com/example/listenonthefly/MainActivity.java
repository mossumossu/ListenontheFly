package com.example.listenonthefly;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int MY_PERMISSION_REQUEST = 1;

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

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        } else
            {
                doStuff();// we may already have this method named something else.
            }
        }

        //ALL these lines are what we need to change
        public void doStuff(){
            listView = (ListView) findViewById(R.id.listView);
            arrayList = new ArrayList<>();
            getMusic();
            adapter = new ArraryAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                    //open music player to play desired song
                }
            });
        }

        //this also came from the video
        public void getMusic(){
            ContentResolver contentResolver = getContentResolver();
            Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

            if(songCursor != null && songCursor.moveToFirst()){
                int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

                do{
                    String currentTitle = songCursor.getString(songTitle);
                    String currentArtist = songCursor.getString(songArtist);
                    String currentLocation = songCursor.getString(songLocation);
                    arrayList.add("Title: " + currentTitle + "\n" + "Artist: " + currentArtist + "\n" + "Location: " + currentLocation);
                }while (songCursor.moveToNext());
            }
        }
//Also came from video
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
            switch (requestCode){
                case MY_PERMISSION_REQUEST: {
                    if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        if(ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();

                            doStuff();
                        }
                    } else {
                        Toast.makeText(this, "No permission granted!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }
            }
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
