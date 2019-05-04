package com.example.listenonthefly;

import android.net.Uri;

public interface PlayerAdapter {

    void loadMedia(Uri iUri);

    void release();

    boolean isPlaying();

    void play();

    void pause();

    void initListener();

    void seekTo(int position);
}
