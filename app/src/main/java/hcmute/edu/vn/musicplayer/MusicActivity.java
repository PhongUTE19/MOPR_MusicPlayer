package hcmute.edu.vn.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MusicActivity extends AppCompatActivity
{
    TextView tvSong;
    TextView tvArtist;
    TextView tvCurrentPosition;
    TextView tvDuration;
    SeekBar sbProgress;
    Button btnBack;
    Button btnPlayPause;
    Button btnNext;
    Button btnPrev;
    Button btnLoop;
    Button btnShuffle;

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (MusicService.UPDATE_MUSIC_CHANGE.equals(intent.getAction()))
            {
                String title = intent.getStringExtra("title");
                String artist = intent.getStringExtra("artist");
                int duration = intent.getIntExtra("duration", 0);

                tvSong.setText(title);
                tvArtist.setText(artist);
                tvDuration.setText(formatTime(duration));
                sbProgress.setMax(duration);
                tvCurrentPosition.setText(formatTime(0));
                sbProgress.setProgress(0);
            }
            else if (MusicService.UPDATE_PROGRESS.equals(intent.getAction()))
            {
                int currentPosition = intent.getIntExtra("currentPosition", 0);

                tvCurrentPosition.setText(formatTime(currentPosition));
                sbProgress.setProgress(currentPosition);
            }
            else if (MusicService.UPDATE_BUTTON_CLICK.equals(intent.getAction()))
            {
                String buttonID = intent.getStringExtra("button");
                boolean enabled = intent.getBooleanExtra("enabled", false);
                String text;
                switch (buttonID)
                {
                    case MusicService.ACTION_PLAY_PAUSE:
                        text = enabled ? "Pause" : "Play";
                        btnPlayPause.setText(text);
                        break;
                    case MusicService.ACTION_LOOP:
                        text = enabled ? "Unloop" : "Loop";
                        btnLoop.setText(text);
                        break;
                    case MusicService.ACTION_SHUFFLE:
                        text = enabled ? "Unshuffle" : "Shuffle";
                        btnShuffle.setText(text);
                        break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        tvSong = findViewById(R.id.tvSong);
        tvArtist = findViewById(R.id.tvArtist);
        sbProgress = findViewById(R.id.sbProgress);
        tvCurrentPosition = findViewById(R.id.tvCurrentTime);
        tvDuration = findViewById(R.id.tvDuration);
        btnBack = findViewById(R.id.btnBack);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnLoop = findViewById(R.id.btnLoop);
        btnShuffle = findViewById(R.id.btnShuffle);

        btnBack.setOnClickListener(v -> finish());
        setButton(btnPlayPause, MusicService.ACTION_PLAY_PAUSE);
        setButton(btnNext, MusicService.ACTION_NEXT);
        setButton(btnPrev, MusicService.ACTION_PREVIOUS);
        setButton(btnLoop, MusicService.ACTION_LOOP);
        setButton(btnShuffle, MusicService.ACTION_SHUFFLE);

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser)
                {
                    Intent intent = new Intent(MusicActivity.this, MusicService.class);
                    intent.setAction(MusicService.ACTION_SEEK);
                    intent.putExtra("seekPosition", progress);
                    startService(intent);
                    tvCurrentPosition.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicService.UPDATE_MUSIC_CHANGE);
        filter.addAction(MusicService.UPDATE_PROGRESS);
        filter.addAction(MusicService.UPDATE_BUTTON_CLICK);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        int index = getIntent().getIntExtra("index", 0);
        int resId = getIntent().getIntExtra("resId", 0);

        Intent serviceIntent = new Intent(this, MusicService.class);
        serviceIntent.setAction(MusicService.ACTION_PLAY_SELECTED);
        serviceIntent.putExtra("index", index);
        serviceIntent.putExtra("resId", resId);
        startService(serviceIntent);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    private void setButton(Button btn, String action)
    {
        btn.setOnClickListener(v ->
        {
            Intent intent = new Intent(MusicActivity.this, MusicService.class);
            intent.setAction(action);
            startService(intent);
        });
    }

    private String formatTime(int milliseconds)
    {
        int seconds = (milliseconds / 1000) % 60;
        int minutes = (milliseconds / 1000) / 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}