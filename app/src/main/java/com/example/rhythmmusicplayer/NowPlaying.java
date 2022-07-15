package com.example.rhythmmusicplayer;

import static android.content.Context.MODE_PRIVATE;
import static com.example.rhythmmusicplayer.MainActivity.ARTIST_NAME_TO_FRAG;
import static com.example.rhythmmusicplayer.MainActivity.PATH_TO_FRAG;
import static com.example.rhythmmusicplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.example.rhythmmusicplayer.MainActivity.SONG_NAME_TO_FRAG;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NowPlaying extends Fragment implements ServiceConnection {

    ImageView  albumArt,nextBtn;
    TextView artistName, songName;
    FloatingActionButton playPauseBtn;
    View view;
    MusicService musicService;
    RelativeLayout cardBottomPlayer;
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

    public NowPlaying() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_now_playing, container, false);

        artistName = view.findViewById(R.id.song_artist_miniplayer);
        songName = view.findViewById(R.id.song_name_miniplayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        nextBtn = view.findViewById(R.id.skip_next_bottom);
        playPauseBtn = view.findViewById(R.id.play_pause_miniplayer);
        cardBottomPlayer = view.findViewById(R.id.card_bottom_player);

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
          //      Toast.makeText(getContext(), "Next", Toast.LENGTH_SHORT).show();
                if(musicService != null){
                    musicService.nextBtnClicked();
                    if(getActivity() != null) {
                        SharedPreferences.Editor editor = getActivity().
                                getSharedPreferences(MUSIC_LAST_PLAYED, MODE_PRIVATE).edit();
                        editor.putString(MUSIC_FILE, musicService.musicFiles.
                                get(musicService.position).getPath());
                        editor.putString(ARTIST_NAME, musicService.musicFiles.
                                get(musicService.position).getArtist());
                        editor.putString(SONG_NAME, musicService.musicFiles.
                                get(musicService.position).getTitle());
                        editor.apply();
                        SharedPreferences preferences = getActivity().getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
                        String path = preferences.getString(MUSIC_FILE,null);
                        String artist = preferences.getString(ARTIST_NAME,null);
                        String song_name = preferences.getString(SONG_NAME,null);
                        if(path != null){
                            SHOW_MINI_PLAYER = true;
                            PATH_TO_FRAG = path;
                            ARTIST_NAME_TO_FRAG = artist;
                            SONG_NAME_TO_FRAG = song_name;
                        }
                        else {
                            SHOW_MINI_PLAYER = false;
                            PATH_TO_FRAG = null;
                            ARTIST_NAME_TO_FRAG = null;
                            SONG_NAME_TO_FRAG = null;
                        }

                        if(SHOW_MINI_PLAYER){
                            if(PATH_TO_FRAG != null){
                                byte[] art = getAlbumArt(PATH_TO_FRAG);
                                if(art != null){
                                    Glide.with(getContext()).load(art)
                                            .into(albumArt);
                                }
                                else {
                                    Glide.with(getContext()).load(R.drawable.defaultalbumart)
                                            .into(albumArt);
                                }
                                songName.setText(SONG_NAME_TO_FRAG);
                                artistName.setText(ARTIST_NAME_TO_FRAG);

                            }
                        }
                    }
                }
            }
        });

        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(musicService != null){
                    musicService.playPauseBtnClicked();
                    if(musicService.isPlaying()){
                        playPauseBtn.setImageResource(R.drawable.ic_pause);
                        Toast.makeText(getContext(), "Playing", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        playPauseBtn.setImageResource(R.drawable.ic_play_arrow);
                        Toast.makeText(getContext(), "Pause", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

//        cardBottomPlayer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(requireContext(),PlayerActivity.class);
//                intent.putExtra("index",PlayerActivity.class);
//                ContextCompat.startActivity(requireContext(),intent,null);
//            }
//        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_MINI_PLAYER){
            if(PATH_TO_FRAG != null){
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if(art != null){
                    Glide.with(getContext()).load(art)
                            .into(albumArt);
                }
                else {
                    Glide.with(getContext()).load(R.drawable.defaultalbumart)
                            .into(albumArt);
                }
                songName.setText(SONG_NAME_TO_FRAG);
                artistName.setText(ARTIST_NAME_TO_FRAG);
                Intent intent = new Intent(getContext(),MusicService.class);
                if(getContext() != null){
                    getContext().bindService(intent,this, Context.BIND_AUTO_CREATE);
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(getContext() != null){
            getContext().unbindService(this);
        }
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return  art;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder binder = (MusicService.MyBinder)service;
        musicService = binder.getService();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }
}