package com.example.listenonthefly;

import android.Manifest;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
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
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final int MY_PERMISSION_REQUEST = 1;

    public int currentSongId = 0;

    private PlayerAdapter mPlayerAdapter;
    private SeekBar mSeekBar;
    private RecyclerView mRecycler;
    private boolean isUserSeeking = false;
    public ArrayList<songListItem> songs = new ArrayList<songListItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            }
        } else
            {
                getMusic();
            }

        initUI();
        initRecycler();
        initSeekbar();
        initPlaybackController();

        Log.d(TAG, "onCreate: complete");
        }

    public void getMusic(){
        ContentResolver contentResolver = getContentResolver();
        Uri songsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Log.d(TAG, songsUri.getPath());
        Cursor songCursor = contentResolver.query(songsUri, null, null, null, null);

        if (songCursor != null && songCursor.moveToFirst()) {
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int songID = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int songDuration = songCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                String currentTitle = songCursor.getString(songTitle);
                String currentArtist = songCursor.getString(songArtist);
                Uri currentUri = ContentUris.withAppendedId(songsUri, songCursor.getInt(songID));

                long currentMSDuration = songCursor.getInt(songDuration);
                String currentDuration = String.format(Locale.US,"%d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(currentMSDuration),
                        TimeUnit.MILLISECONDS.toSeconds(currentMSDuration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentMSDuration)));

                songListItem cursorItem = new songListItem(currentTitle, currentArtist, currentUri, currentDuration);
                songs.add(cursorItem);
            } while (songCursor.moveToNext());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();

                        getMusic();
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
        //mPlayerAdapter.loadMedia(MEDIA_RES_ID);
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
        ItemAdapter adapter = new ItemAdapter(songs);
        mRecycler.setAdapter(adapter);
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

    public void updateCurrentPlayingUI(int updateTo){
        TextView tvCTitle = findViewById(R.id.tvCurrentTitle);
        tvCTitle.setText(songs.get(updateTo).getTitle());

        TextView tvCDuration = findViewById(R.id.tvCurrentDuration);
        tvCDuration.setText(songs.get(updateTo).getDuration());
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
                //Log.d(TAG, "onPositionChanged: complete");
            }
        }

        @Override
        void onPlaybackCompleted() {
            int nextSongId = currentSongId + 1;
            mPlayerAdapter.release();

            if(songs.get(nextSongId) != null){
                mPlayerAdapter.loadMedia(songs.get(nextSongId).getSongUri());
                mPlayerAdapter.play();
                updateCurrentPlayingUI(nextSongId);
                currentSongId = nextSongId;
            }
            Log.d(TAG, "onPlaybackCompleted: complete");
        }
    }

    public class songListItem{
        private String title;
        private String artist;
        private Uri songUri;
        private String duration;

        public songListItem(String inputTitle, String inputArtist, Uri inputUri, String inputDuration){
            title = inputTitle;
            artist = inputArtist;
            songUri = inputUri;
            duration = inputDuration;
        }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }

        public Uri getSongUri() {
            return songUri;
        }

        public String getDuration() {
            return duration;
        }
    }

    public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder>
    implements ChangeSelect {
        private List<songListItem> songs;
        private int selectedPos = RecyclerView.NO_POSITION;

        public ItemAdapter(List<songListItem> inputSongs) {
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
        public void onBindViewHolder(@NonNull ItemAdapter.ViewHolder viewHolder, int i) {
            songListItem item = songs.get(i);
            viewHolder.itemView.setSelected(selectedPos == i);

            TextView tvTitle = viewHolder.tvTitle;
            tvTitle.setText(item.getTitle());

            TextView tvArtist = viewHolder.tvArtist;
            tvArtist.setText(item.getArtist());

            TextView tvDuration = viewHolder.tvDuration;
            tvDuration.setText(item.getDuration());

            // expand as needed for other properties

        }

        @Override
        public int getItemCount(){
            return songs.size();
        }

        //TODO: properly update recycler on current song
        @Override
        public void change(int position) {
            notifyItemChanged(selectedPos);
            selectedPos = currentSongId;
            notifyItemChanged(selectedPos);
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            public TextView tvTitle;
            public TextView tvArtist;
            public TextView tvDuration;


            public ViewHolder(@NonNull View itemView) {
                super(itemView);

                tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
                tvArtist = (TextView) itemView.findViewById(R.id.tvArtist);
                tvDuration = (TextView) itemView.findViewById(R.id.tvDuration);

                itemView.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        currentSongId = mRecycler.getChildAdapterPosition(v);

                        //notifyItemChanged(selectedPos);
                        //selectedPos = currentSongId;
                        //notifyItemChanged(selectedPos);

                        mPlayerAdapter.release();
                        mPlayerAdapter.loadMedia(songs.get(currentSongId).getSongUri());
                        mPlayerAdapter.play();

                        updateCurrentPlayingUI(currentSongId);

                        Log.d(TAG, "ViewHolder onClick: complete");
                    }
                });
            }
        }
    }

}
