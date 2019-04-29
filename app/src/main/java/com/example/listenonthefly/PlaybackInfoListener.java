package com.example.listenonthefly;

public abstract class PlaybackInfoListener {


    void onLogUpdated(String message) {
    }

    void onDurationChanged(int duration) {
    }

    void onPositionChanged(int position) {
    }

    void onStateChanged(int state) {
    }

    void onPlaybackCompleted() {
    }
}
