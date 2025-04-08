package hcmute.edu.vn.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

        loadSongsFromRaw();

        lvSongs.setOnItemClickListener((parent, view, position, id) ->
        {
            Song selected = songs.get(position);
            Intent intent = new Intent(PlaylistActivity.this, MusicPlayerActivity.class);
            intent.putExtra("index", selected.index);
            intent.putExtra("resId", selected.resId);
            intent.putExtra("title", selected.title);
            intent.putExtra("artist", selected.artist);
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

                songs.add(new Song(title, artist, resId, i));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
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