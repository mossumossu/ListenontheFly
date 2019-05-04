package com.example.listenonthefly;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PlayerHolder implements PlayerAdapter {

    private static final String TAG = "PlayerHolder";

    private final Context mContext;
    private MediaPlayer mMediaPlayer;
    private Uri songUri;
    private PlaybackInfoListener mPlaybackInfoListener;
    private ScheduledExecutorService mExecutor;
    private Runnable mSeekbarPositionUpdateTask;

    public PlayerHolder(Context context) {
        mContext = context.getApplicationContext();
    }

    private void initMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    nextSong();
                }
            });
        }

    }

    public void setPlaybackInfoListener(PlaybackInfoListener listener) {
        mPlaybackInfoListener = listener;
    }

    @Override
    public void loadMedia(Uri iUri) {
        songUri = iUri;

        initMediaPlayer();

        try {
            mMediaPlayer.setDataSource(mContext,songUri);
        } catch (Exception e){
            Log.d(TAG, "setDataSource" + e.toString());
            Log.d(TAG, songUri.toString());
        }

        try {
            mMediaPlayer.prepare();
        } catch (Exception e){
            Log.d(TAG, "prepare" + e.toString());
        }

        initListener();
    }

    @Override
    public void release() {
        if (mMediaPlayer != null){
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean isPlaying() {
        if (mMediaPlayer != null){
            return mMediaPlayer.isPlaying();
        }
        return false;
    }



    @Override
    public void play() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()){
            mMediaPlayer.start();
            if (mPlaybackInfoListener != null){
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PLAYING);
            }
            startListener();
        }
    }

    @Override
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
            if (mPlaybackInfoListener != null){
                mPlaybackInfoListener.onStateChanged(PlaybackInfoListener.State.PAUSED);
            }
        }
    }

    @Override
    public void seekTo(int position) {
        if (mMediaPlayer != null){
            mMediaPlayer.seekTo(position);
        }
    }

    @Override
    public void initListener() {
        int duration = mMediaPlayer.getDuration();
        if (mPlaybackInfoListener != null){
            mPlaybackInfoListener.onDurationChanged(duration);
            mPlaybackInfoListener.onPositionChanged(0);
        }
    }

    private void startListener() {
        if (mExecutor == null){
            mExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        if (mSeekbarPositionUpdateTask == null){
            mSeekbarPositionUpdateTask = new Runnable() {
                @Override
                public void run() {
                    updateListener();
                }
            };
        }
        mExecutor.scheduleAtFixedRate(
                mSeekbarPositionUpdateTask,
                0,
                500,
                TimeUnit.MILLISECONDS
        );
    }

    private void updateListener() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()){
            int currentPos = mMediaPlayer.getCurrentPosition();
            if (mPlaybackInfoListener != null){
                mPlaybackInfoListener.onPositionChanged(currentPos);
            }
        }
    }

    private void nextSong(){
        if (mMediaPlayer != null){
            mPlaybackInfoListener.onPlaybackCompleted();
        }
    }

    private void stopListener(boolean reset) {
        if (mExecutor != null){
            mExecutor.shutdownNow();
            mExecutor = null;
            mSeekbarPositionUpdateTask = null;
            if (reset && mPlaybackInfoListener != null){
                mPlaybackInfoListener.onPositionChanged(0);
            }
        }
    }
}