package hcmute.edu.vn.musicplayer;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity
{
    ListView lvSongs;
    ArrayList<Song> songList = new ArrayList<>();
    ArrayAdapter<Song> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        lvSongs = findViewById(R.id.lvSongs);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songList);
        lvSongs.setAdapter(adapter);

        loadSongs();
//        loadSongsFromPhone();

        lvSongs.setOnItemClickListener((parent, view, position, id) -> {
            Song selected = songList.get(position);
            Intent intent = new Intent(PlaylistActivity.this, MusicPlayerActivity.class);
            intent.setAction("PLAY_SELECTED");
            intent.putExtra("path", selected.path);
            intent.putExtra("title", selected.title);
            intent.putExtra("artist", selected.artist);
            startActivity(intent);
        });
    }

    private void loadSongs() {
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContentResolver().query(songUri, null, selection, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int titleCol = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCol = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int durationCol = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                String title = cursor.getString(titleCol);
                String artist = cursor.getString(artistCol);
                String path = cursor.getString(dataCol);
                int duration = cursor.getInt(durationCol);

                songList.add(new Song(title, artist, path, duration));
            } while (cursor.moveToNext());

            cursor.close();
        }
    }

    private void loadSongsFromPhone() {
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        Cursor cursor = getContentResolver().query(songUri, null, selection, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

            do {
                String title = cursor.getString(titleColumn);
                String artist = cursor.getString(artistColumn);
                String path = cursor.getString(dataColumn);
                int duration = cursor.getInt(durationColumn);

                songList.add(new Song(title, artist, path, duration));
            } while (cursor.moveToNext());

            cursor.close();
            adapter.notifyDataSetChanged();
        }
    }

}