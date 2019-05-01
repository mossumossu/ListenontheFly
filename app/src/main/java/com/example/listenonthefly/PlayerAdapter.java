package com.example.listenonthefly;

public interface PlayerAdapter {

    void loadMedia(int resourceId);

    void release();

    boolean isPlaying();

    void play();

    void pause();

    void initListener();

    void seekTo(int position);
}
