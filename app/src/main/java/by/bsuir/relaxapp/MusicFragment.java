package by.bsuir.relaxapp;

import com.google.type.DateTime;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Date;

public class MusicFragment extends Fragment {

    private boolean gonna_play = false;
    private static final String CLIENT_ID = "8cd0b669498e49d8b1915d6da500e789";
    private static final String REDIRECT_URI = "https://localhost:8080";
    public static SpotifyAppRemote mSpotifyAppRemote = null;

    public static final String SUPP_PLAYLIST_PLAY_STR = "spotify:playlist:";
    public static final String PLAYLIST_IDS[] = new String[]
            {   "16b887lG3wgyYtXus4PfFT",
                "6UZaOdd7TOy33Xhl3NcJFn",
                "0baxQIQd1HKmmRj4IBAaYf",
                "4jPiN7G9ukHxm9Zzoi4Fqp",
                "7fQVIt29rxa92ZlmNCnlXB",
                "3iGrDRstfZj4uCHdnpWgNp"};

    public static final String PLAYLIST_NAMES[] = new String[]
            {       "Nirvana",
                    "GrOb",
                    "Gorillaz",
                    "Michael Jackson",
                    "Modern Talking",
                    "Avril Lavigne"};

    private TextView
            playlistName,
            albumName,
            artistName,
            songName;

    private ImageView
            coverImage,
            previousSong,
            playStopSong,
            nextSong;

    private void findAllTextViews(View view){
        playlistName = view.findViewById(R.id.playlistName);
        albumName = view.findViewById(R.id.albumName);
        artistName = view.findViewById(R.id.artistName);
        songName = view.findViewById(R.id.songName);
    }

    private void findAllImageViews(View view){
        coverImage = view.findViewById(R.id.coverImage);
        previousSong = view.findViewById(R.id.previousSong);
        playStopSong = view.findViewById(R.id.playStopSong);
        nextSong = view.findViewById(R.id.nextSong);
    }

    private int getPlayListIndexToPlay(){
        int max = MainActivity.APP_MOODS[0].clickedCount, ind = 0;

        for (int i = 1; i < MainActivity.MOODS_COUNT; ++i){
            if (MainActivity.APP_MOODS[i].clickedCount > max){
                max = MainActivity.APP_MOODS[i].clickedCount;
                ind = i;
            }
        }

        Date date = new Date();
        return (date.getHours() + date.getMinutes()  + ind) % MainActivity.MOODS_COUNT;
    }

    private int current_index = 0;

    private void connected(){
        current_index = getPlayListIndexToPlay();
        mSpotifyAppRemote.getPlayerApi().play(SUPP_PLAYLIST_PLAY_STR + PLAYLIST_IDS[current_index]);

        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        artistName.setText(track.artist.name);
                        songName.setText(track.name);
                        albumName.setText(track.album.name);
                        playlistName.setText(PLAYLIST_NAMES[current_index]);
                        mSpotifyAppRemote
                                .getImagesApi()
                                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                                .setResultCallback(
                                        bitmap -> {
                                            coverImage.setImageBitmap(bitmap);
                                        });
                    }
                });
    }

    public static void previousMusic(){
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().skipPrevious();
        }
    }

    public static void stopMusic(){
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().pause();
        }
    }

    public static void playMusic(){
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().resume();
        }
    }

    public static void nextMusic(){
        if (mSpotifyAppRemote != null) {
            mSpotifyAppRemote.getPlayerApi().skipNext();
        }
    }


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public MusicFragment() {   }
    public static MusicFragment newInstance(String param1, String param2) {
        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music, container, false);

        findAllTextViews(view);
        findAllImageViews(view);

        previousSong.setOnClickListener(lambda->{
            previousMusic();
        });

        playStopSong.setOnClickListener(lambda->{
            if (gonna_play){
                playMusic();
                gonna_play = false;
                playStopSong.setImageResource(R.drawable.ic_pause);
            } else{
                stopMusic();
                gonna_play = true;
                playStopSong.setImageResource(R.drawable.ic_play_arrow);
            }
        });

        nextSong.setOnClickListener(lambda->{
            nextMusic();
        });



        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(MainActivity.MAIN_ACTIVITY_CONTEXT, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);
                    }
                });

        return view;
    }
}