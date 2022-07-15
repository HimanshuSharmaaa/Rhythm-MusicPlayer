package com.example.rhythmmusicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    public static final int REQUEST_CODE = 1;
    static ArrayList<MusicFiles> musicFiles;
    static  boolean shuffleBool = false, repeatBool = false;
    static ArrayList<MusicFiles> albums = new ArrayList<>();
    private String MY_SORT_PREF = "SortOrder";
    public static final String MUSIC_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static boolean SHOW_MINI_PLAYER = false;
    public static String PATH_TO_FRAG = null;
    public static String SONG_NAME_TO_FRAG = null;
    public static String ARTIST_NAME_TO_FRAG = null;
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)));
        permission();

    }

    private void permission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} ,
                    REQUEST_CODE);
        }
        else{
//            Toast.makeText(this, "Permission Granted !", Toast.LENGTH_SHORT).show();
            musicFiles = getAllAudio(this);
            initViewPager();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(this, "Successfully Permission Granted!", Toast.LENGTH_SHORT).show();
                musicFiles = getAllAudio(this);
                initViewPager();
            }
            else{
                ActivityCompat.requestPermissions(MainActivity.this , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE} ,
                        REQUEST_CODE);
            }
        }
    }

    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(new SongsFragment(),"Songs");
        viewPagerAdapter.addFragments(new AlbumFragment(),"Albums");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);


    //    int[] imageList = {R.drawable.ic_music,R.drawable.ic_library_music};
//
//        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_library_music);
//        drawable.setBounds(0,0,drawable.getIntrinsicWidth(),drawable.getIntrinsicHeight());

    }




    public static class ViewPagerAdapter extends FragmentPagerAdapter{

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;
        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        void addFragments(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    public ArrayList<MusicFiles> getAllAudio(Context context){

        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREF,MODE_PRIVATE);
        String sortOrder = preferences.getString("sorting","sortByName");

        ArrayList<String> duplicate = new ArrayList<>();
        albums.clear();

         ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
         String order = null;
         Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

         switch (sortOrder){
             case "sortByName":
                 order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                 break;
             case "sortByDate":
                 order = MediaStore.MediaColumns.DATE_ADDED + " ASC";
                 break;
             case "sortBySize":
                 order = MediaStore.MediaColumns.SIZE + " DESC";
                 break;
         }

         String[] projection = {
                 MediaStore.Audio.Media.ALBUM,
                 MediaStore.Audio.Media.TITLE,
                 MediaStore.Audio.Media.DURATION,
                 MediaStore.Audio.Media.DATA, // FOR PATH
                 MediaStore.Audio.Media.ARTIST,
                 MediaStore.Audio.Media._ID

         };
        Cursor cursor = context.getContentResolver().query(uri,projection,null,null,order);
        if(cursor != null){
            while(cursor.moveToNext()){
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);

                MusicFiles musicFiles = new MusicFiles(path,title,artist,album,duration,id);
                // take log.e for check
                Log.e("Path : " + path,"album : "+album);
                tempAudioList.add(musicFiles);

                if(!duplicate.contains(album)){
                    albums.add(musicFiles);
                    duplicate.add(album);
                }
            }
            cursor.close();
        }
        return tempAudioList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search,menu);
        MenuItem menuItem = menu.findItem(R.id.search_opt);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String userInput = newText.toLowerCase();
        ArrayList<MusicFiles> myFiles = new ArrayList<>();
        for(MusicFiles song : musicFiles){
            if(song.getTitle().toLowerCase().contains(userInput)){
                myFiles.add(song);
            }
        }
        SongsFragment.musicAdapter.updateList(myFiles);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF,MODE_PRIVATE).edit();
        switch (item.getItemId()){
            case R.id.by_name:
                editor.putString("sorting","sortByName");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_date:
                editor.putString("sorting","sortByDate");
                editor.apply();
                this.recreate();
                break;
            case R.id.by_size:
                editor.putString("sorting","sortBySize");
                editor.apply();
                this.recreate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(MUSIC_LAST_PLAYED,MODE_PRIVATE);
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
    }
}