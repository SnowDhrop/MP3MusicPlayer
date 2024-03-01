package src;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

public class MusicPlayerGUI extends JFrame {
    // Color configurations
    public static final Color FRAME_COLOR = Color.BLACK;
    public static final Color TEXT_COLOR = Color.WHITE;

    public MusicPlayer musicPlayer;

    // Allows me to use file explorer in my app
    private JFileChooser jFileChooser;
    private JLabel songTitle, songArtist;
    private JPanel playbackBtns;
    private JSlider playbackSlider;

    public MusicPlayerGUI() {
        super("Music Player");
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Launch the app at the center of the screen
        setLocationRelativeTo(null);
        setResizable(false);

        // Set layout to null which allows me to control (x, y, height, width) of my components
        setLayout(null);

        // Change the frame color
        getContentPane().setBackground(FRAME_COLOR);

        musicPlayer = new MusicPlayer(this);
        jFileChooser = new JFileChooser();
        // Set a default path for file explorer
        jFileChooser.setCurrentDirectory(new File("src/assets"));

        // Filter file chooser to only see .mp3 files
        jFileChooser.setFileFilter(new FileNameExtensionFilter("MP3", "mp3"));
        
        addGuiComponents();
    }

    private void addGuiComponents() {
        addToolbar();

        // Icone
        JLabel songImage = new JLabel(loadImage("src/assets/record.png"));
        songImage.setBounds(0, 50, getWidth() - 20, 225);
        add(songImage);

        // Song title
        songTitle = new JLabel("Song Title");
        songTitle.setBounds(0, 285, getWidth() - 10, 30);
        songTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        songTitle.setForeground(TEXT_COLOR);
        songTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(songTitle);

        // Song artist
        songArtist = new JLabel("Artist");
        songArtist.setBounds(0, 315, getWidth() - 10, 30);
        songArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        songArtist.setForeground(TEXT_COLOR);
        songArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(songArtist);

        // Playback slider
        playbackSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth() / 2 - 300/2, 365, 300, 40);
        playbackSlider.setBackground(null);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // When the user is holding the tick we want to pause the song
                musicPlayer.pauseSong();
                disablePauseButtonEnablePlayButton();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                JSlider source = (JSlider) e.getSource();

                // Get the frame value from where the user wants to playback to
                int frame = source.getValue();

                // Update the current frame in the music player to this frame
                musicPlayer.setCurrentFrame(frame);

                //update current time in milli
                musicPlayer.setCurrrentTimeInMilli((int) (frame / (2.08 * musicPlayer.getCurrentSong().getFrameRatePerMilliseconds())));

                musicPlayer.playCurrentSong();

                enablePauseButtonDisablePlayButton();
            }
        });
        add(playbackSlider);

        // Playback buttons
        addPlaybackBtns();
    }

    private void addPlaybackBtns() {
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth() - 10, 80);
        playbackBtns.setBackground(null);

        // Previous button
        JButton prevButton = new JButton(loadImage("src/assets/previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        playbackBtns.add(prevButton);

        // Play Button
        JButton playButton = new JButton(loadImage("src/assets/play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.playCurrentSong();
                // Toggle on play button and toggle off pause button
                enablePauseButtonDisablePlayButton();
            }
        });
        playbackBtns.add(playButton);

        // Pause Button
        JButton pauseButton = new JButton(loadImage("src/assets/pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                musicPlayer.pauseSong();
                disablePauseButtonEnablePlayButton();
            }
        });
        playbackBtns.add(pauseButton);

        // Next Button
        JButton nextButton = new JButton(loadImage("src/assets/next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    private void addToolbar() {
        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);
        // Prevent toolbar from being moved
        toolBar.setFloatable(false);

        // Dropdown Menu
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        // Song menu
        JMenu songMenu = new JMenu("Song");
        menuBar.add(songMenu);

        // Load song item in the song menu
        JMenuItem loadSong = new JMenuItem("Load Song");
        loadSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // An integer is returned to let us know what the user did (open or cancel)
                int result = jFileChooser.showOpenDialog(MusicPlayerGUI.this);
                File selectedFile = jFileChooser.getSelectedFile();

                if (result == JFileChooser.APPROVE_OPTION && selectedFile != null) {
                    // Create a song obj based on selected file
                    Song song = new Song(selectedFile.getPath());

                    // Load song in musicPlayer
                    musicPlayer.loadSong(song);

                    // Update song title and artist
                    updateSongTitleAndArtist(song);

                    updatePlaybackSlider(song);

                    // Toggle on pause button and toggle off play button
                    enablePauseButtonDisablePlayButton();
                }
            }
        });
        songMenu.add(loadSong);

        // Playlist menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        // Create playlist into playlist menu
        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        playlistMenu.add(createPlaylist);

        // Load playlist into playlist menu
        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        playlistMenu.add(loadPlaylist);

        add(toolBar);
    }

    private void enablePauseButtonDisablePlayButton() {
        // Retrieve reference to the buttons from playbackBtns panel
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        // Turn off play button
        playButton.setVisible(false);
        playButton.setEnabled(false);

        // Turn on pause button
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);
    }

    private void disablePauseButtonEnablePlayButton() {
        // Retrieve reference to the buttons from playbackBtns panel
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        playButton.setVisible(true);
        playButton.setEnabled(true);

        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);
    }

    private ImageIcon loadImage(String imagePath) {
        try {
            // Read image file from the give path
            BufferedImage image = ImageIO.read(new File(imagePath));
            return new ImageIcon(image);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void updateSongTitleAndArtist(Song song) {
        songTitle.setText(song.getSongTitle());
        songArtist.setText(song.getSongArtist());
    }

    // Update the playback slider when loading a song
    private void updatePlaybackSlider(Song song) {
        //update max count for slider
        playbackSlider.setMaximum(song.getMp3File().getFrameCount());

        // Create the song length label
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

        JLabel labelBeginning = new JLabel("00:00");
        labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
        labelBeginning.setForeground(TEXT_COLOR);

        JLabel labelEnd = new JLabel(song.getSongLength());
        labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
        labelEnd.setForeground(TEXT_COLOR);

        labelTable.put(0, labelBeginning);
        labelTable.put(song.getMp3File().getFrameCount(), labelEnd);

        playbackSlider.setLabelTable(labelTable);
        playbackSlider.setPaintLabels(true);
    }

    // Update the playback slider from the music player class
    public void setPlaybackSliderValue(int frame) {
        playbackSlider.setValue(frame);
    }
}





























