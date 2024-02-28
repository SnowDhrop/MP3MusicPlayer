package src;

import javazoom.jl.player.advanced.AdvancedPlayer;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class MusicPlayer {
    private Song currentSong;

    // JLayer Library allows us to create an AdvancedPlayer obj which will handle playing the music
    private AdvancedPlayer advancedPlayer;

    public MusicPlayer() {

    }

    public void loadSong(Song song) {
        currentSong = song;

        if (currentSong != null) {
            playCurrentSong();
        }
    }

    private void playCurrentSong() {
        try {
            // Read mp3 audio data
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // Create a new advanced player
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);

            // Start music
            startMusicThread();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Create a thread that will handle playing the music
    private void startMusicThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Play music
                    advancedPlayer.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}


























