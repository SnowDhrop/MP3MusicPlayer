package src;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

public class MusicPlayer extends PlaybackListener {
    public static final Object playSignal = new Object(); // Used to update isPaused more synchronously
    private MusicPlayerGUI musicPlayerGUI; // Need reference so that we can update the gui in this class
    private Song currentSong;

    // JLayer Library allows us to create an AdvancedPlayer obj which will handle playing the music
    private AdvancedPlayer advancedPlayer;
    private ArrayList<Song> playlist;

    private boolean isPaused;
    private boolean songFinished; // Tell when the song has finished
    private boolean pressedNext, pressedPrev;

    private int currentFrame; // The last frame when the playback is finished
    // Track how many milliseconds has passed since playing the song (used for update the slider)
    private int currentTimeInMilli;
    // Keep track the index we are in the playlist
    private int currentPlaylistIndex;

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

    public void loadPlaylist(File playlistFile) {
        playlist = new ArrayList<>();

        // Store the paths from the text file into the playlist array list
        try {
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Read each line from the text file and store the text into the songpath variable
            String songPath;
            while((songPath = bufferedReader.readLine()) != null) {
                // Create song object based on song path
                Song song = new Song(songPath);

                playlist.add(song);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }

        if (!playlist.isEmpty()){
            musicPlayerGUI.setPlaybackSliderValue(0);
            currentTimeInMilli = 0;

            // Update current song to the first song in the playlist
            currentSong = playlist.get(0);
            currentFrame = 0;

            // Update gui
            musicPlayerGUI.enablePauseButtonDisablePlayButton();
            musicPlayerGUI.updateSongTitleAndArtist(currentSong);
            musicPlayerGUI.updatePlaybackSlider(currentSong);

            playCurrentSong();
        }
    }

    public void pauseSong() {
        if (advancedPlayer != null) {
            isPaused = true;
            stopSong();
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

    public void nextSong() {
        if (playlist == null) return;

        if (currentPlaylistIndex + 1 > playlist.size() - 1) return;

        currentPlaylistIndex++;

        pressedNext = true;

        if (!songFinished) stopSong();

        musicPlayerGUI.disablePauseButtonEnablePlayButton();

        currentSong = playlist.get(currentPlaylistIndex);

        // Update time
        currentFrame = 0;
        currentTimeInMilli = 0;

        // Update gui
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        playCurrentSong();
    }

    public void prevSong() {
        if (playlist == null) return;

        currentPlaylistIndex--;
        if (currentPlaylistIndex < 0) currentPlaylistIndex = 0;

        pressedPrev = true;

        if (!songFinished) stopSong();

        System.out.println(currentPlaylistIndex);

        musicPlayerGUI.disablePauseButtonEnablePlayButton();

        currentSong = playlist.get(currentPlaylistIndex);

        // Update time
        currentFrame = 0;
        currentTimeInMilli = 0;

        // Update gui
        musicPlayerGUI.enablePauseButtonDisablePlayButton();
        musicPlayerGUI.updateSongTitleAndArtist(currentSong);
        musicPlayerGUI.updatePlaybackSlider(currentSong);

        playCurrentSong();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        // This method gets called in the beginning of the song
        System.out.println("Playback started");
        songFinished = false;
        pressedNext = false;
        pressedPrev = false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        // This method gets called when the song finishes or if the player gets close
        System.out.println("Playback finished");

        if (isPaused) {
            currentFrame += (int) ((double) evt.getFrame() * currentSong.getFrameRatePerMilliseconds());
        } else {
            songFinished = true;
            if (playlist == null) {
                musicPlayerGUI.disablePauseButtonEnablePlayButton();
            } else {
                if (pressedPrev || pressedNext) return;
                
                if (currentPlaylistIndex == playlist.size() -1){
                    musicPlayerGUI.disablePauseButtonEnablePlayButton();
                } else {
                    nextSong();
                }
            }
        }
    }
}


























