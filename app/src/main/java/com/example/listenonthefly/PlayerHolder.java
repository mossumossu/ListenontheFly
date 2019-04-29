package com.example.listenonthefly;

import android.content.Context;
import android.media.MediaPlayer;

public class PlayerHolder implements PlayerAdapter {

    private final Context mContext;
    private MediaPlayer mMediaPlayer;
    private PlaybackInfoListener mPlaybackInfoListener;

    public PlayerHolder(Context context) {
        mContext = context.getApplicationContext();
    }

    private void initMediaPlayer(){

    }

    public void setPlaybackInfoListener(PlaybackInfoListener listener) {
        mPlaybackInfoListener = listener;
    }

    @Override
    public void loadMedia(int resourceId) {

    }

    @Override
    public void release() {

    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void play() {

    }

    @Override
    public void reset() {

    }

    @Override
    public void pause() {

    }

    @Override
    public void initializeProgressCallback() {

    }

    @Override
    public void seekTo(int position) {

    }
}
