package monash.sydney.com.mytracker;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MusicPlayerActivity extends AppCompatActivity {

    List<Song> songList = new ArrayList<>();
    ListView listView;
    Song currentSong;
    TextView artist;
    TextView title;
    Boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        getAllSongs();
        listView = (ListView) findViewById(R.id.songList);
        artist = (TextView) findViewById(R.id.artistName);
        title = (TextView) findViewById(R.id.title);
        ArrayAdapter<Song> arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, songList);
        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                currentSong = songList.get(position);
                playNewSong();
                updateUI();
            }
        });
        updateUI();

    }


    private void updateUI() {
        if (currentSong != null) {
            title.setText(currentSong.getSongTitle());
            artist.setText(currentSong.getSongArtist());
        }
    }

    public void getAllSongs() {
        ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor songCursor = contentResolver.query(songUri, null, null, null, null);

        if (songCursor != null) {
            int songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);


            while (songCursor.moveToNext()) {
                songList.add(new Song(songCursor.getLong(songId), songCursor.getString(songTitle), songCursor.getString(songArtist)));
            }
            if (!songList.isEmpty()) {
                currentSong = songList.get(0);
            }
        }
    }

    public void playSong(View view) {
        startService(createIntentObject());
        isPlaying = true;
    }

    public void stopSong(View view) {

        stopService(createIntentObject());
        isPlaying = false;
    }

    private Intent createIntentObject() {
        Intent intent = new Intent(this, PlayMusic.class);
        intent.putExtra("ID", currentSong.getSongID());
        return intent;
    }

    public void playPreviousSong(View view) {
        int index = songList.indexOf(currentSong);
        index--;
        if (index == -1) {
            index = songList.size() - 1;
        }

        currentSong = songList.get(index);

        playNewSong();
        updateUI();
    }

    public void playNextSong(View view) {
        int index = songList.indexOf(currentSong);
        index++;
        if (index == songList.size()) {
            index = 0;
        }
        currentSong = songList.get(index);

        playNewSong();
        updateUI();
    }

    private void playNewSong() {
        if (isPlaying) {
            stopService(createIntentObject());
        }
        startService(createIntentObject());
        isPlaying = true;
    }
}
