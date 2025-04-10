package hcmute.edu.vn.musicplayer;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class PlaylistActivity extends AppCompatActivity
{
    public static ArrayList<Song> songs = new ArrayList<>();
    private ListView lvSongs;
    private ArrayAdapter<Song> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        lvSongs = findViewById(R.id.lvSongs);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, songs);
        lvSongs.setAdapter(adapter);

//        loadSongsFromRaw();
        loadSongsFromStorage();

        lvSongs.setOnItemClickListener((parent, view, position, id) ->
        {
            Song song = songs.get(position);
            Intent intent = new Intent(PlaylistActivity.this, MusicActivity.class);
            intent.putExtra("title", song.title);
            intent.putExtra("artist", song.artist);
            intent.putExtra("path", song.path);
            intent.putExtra("resID", song.resID);
            intent.putExtra("index", song.index);
            startActivity(intent);
        });
    }

    private void loadSongsFromRaw()
    {
        Field[] fields = R.raw.class.getFields();
        for (int i = 0; i < fields.length; i++)
        {
            try
            {
                Field field = fields[i];
                int resId = field.getInt(field);
                String rawName = field.getName();
                String title = capitalize(rawName.replace("_", " "));
                String artist = "Unknown Artist";
                songs.add(new Song(title, artist, "", resId, i));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void loadSongsFromStorage()
    {
        songs.clear();
        MediaScannerConnection.scanFile(this, new String[] { "/storage/emulated/0/Music/" }, null, null);

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        Cursor cursor = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
        );

        int index = 0;

        if (cursor != null)
        {
            while (cursor.moveToNext())
            {
                String title = cursor.getString(0);
                String artist = cursor.getString(1);
                String path = cursor.getString(2);

                songs.add(new Song(title, artist, path, 0, index));
                index++;
            }
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private String capitalize(String input)
    {
        String[] words = input.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words)
        {
            if (word.length() > 0)
            {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

}