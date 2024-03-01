package src;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class MusicPlayer extends PlaybackListener {
    public static final Object playSignal = new Object(); // Used to update isPaused more synchronously
    private MusicPlayerGUI musicPlayerGUI; // Need reference so that we can update the gui in this class
    private Song currentSong;

    // JLayer Library allows us to create an AdvancedPlayer obj which will handle playing the music
    private AdvancedPlayer advancedPlayer;

    private boolean isPaused;
    private int currentFrame; // The last frame when the playback is finished
    // Track how many milliseconds has passed since playing the song (used for update the slider)
    private int currentTimeInMilli;

    public void setCurrentFrame(int frame) {
        currentFrame = frame;
    };

    public void setCurrrentTimeInMilli(int timeInMilli) {
        currentTimeInMilli = timeInMilli;
    }

    public Song getCurrentSong() {
        return currentSong;
    }

    public MusicPlayer(MusicPlayerGUI musicPlayerGUI) {
        this.musicPlayerGUI = musicPlayerGUI;
    }

    public void loadSong(Song song) {
        currentSong = song;

        if (currentSong != null) {
            playCurrentSong();
        }
    }

    public void pauseSong() {
        if (advancedPlayer != null) {
            stopSong();
            isPaused = true;
        }
    }

    public void stopSong() {
        if (advancedPlayer != null) {
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void playCurrentSong() {
        if (currentSong == null) return;

        try {
            // Read mp3 audio data
            FileInputStream fileInputStream = new FileInputStream(currentSong.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // Create a new advanced player
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            // Start music
            startMusicThread();

            // Start playback slider
            startPlaybackSliderThread();
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
                    if (isPaused) {
                        synchronized(playSignal) {
                            isPaused = false;
                            playSignal.notify();
                        }

                        // Resume from the last frame
                        advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                    } else {
                        // Play music from the beginning
                        advancedPlayer.play();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // Create a thread that will handle updating the slider
    private void startPlaybackSliderThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    try {
                        // Wait till it gets notified by other thread to continue
                        // Make sure that isPaused boolean flag updates to false before continuing
                        synchronized(playSignal) {
                            playSignal.wait();
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                while(!isPaused) {
                    try {
                        currentTimeInMilli++;
                        int calculatedFrame = (int) ((double) currentTimeInMilli * 2.08 * currentSong.getFrameRatePerMilliseconds());

                        // Update gui
                        musicPlayerGUI.setPlaybackSliderValue(calculatedFrame);

                        // Mimic 1 millisecond using thread.sleep
                        Thread.sleep(1);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        // This method gets called in the beginning of the song
        System.out.println("Playback started");
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        // This method gets called when the song finishes or if the player gets close
        System.out.println("Playback finished");

        if (isPaused) {
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        }
    }
}


























