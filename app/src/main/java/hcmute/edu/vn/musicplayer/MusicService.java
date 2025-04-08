package hcmute.edu.vn.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Service;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import java.util.Random;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.session.MediaButtonReceiver;

public class MusicService extends Service
{
    public static final String UPDATE_MUSIC_CHANGE = "UPDATE_MUSIC_CHANGE";
    public static final String UPDATE_PROGRESS = "UPDATE_PROGRESS";
    public static final String UPDATE_BUTTON_CLICK = "UPDATE_BUTTON_CLICK";
    public static final String ACTION_PLAY_SELECTED = "ACTION_PLAY_SELECTED";
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_LOOP = "ACTION_LOOP";
    public static final String ACTION_SHUFFLE = "ACTION_SHUFFLE";
    public static final String ACTION_SEEK = "ACTION_SEEK";
    private static final String CHANNEL_ID = "music_channel";

    private MediaSessionCompat mediaSession;
    private MediaPlayer player;
    private int currentIndex = 0;
    private boolean isPlay = false;
    private boolean isLoop = false;
    private boolean isShuffle = false;

    private final Handler handler = new Handler();
    private final Runnable updateRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if (player != null && isPlay)
            {
                updateProgress();
                handler.postDelayed(this, 1000);
            }
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()) && isPlay)
                playPauseSong();
            else if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction()) && !isPlay)
                playPauseSong();
        }
    };

    @Override
    public void onCreate()
    {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Music Player",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(receiver, filter);

        mediaSession = new MediaSessionCompat(this, "MusicService");
        mediaSession.setCallback(new MediaSessionCompat.Callback()
        {
            @Override
            public void onPlay()
            {
                playPauseSong();
            }

            @Override
            public void onPause()
            {
                playPauseSong();
            }

            @Override
            public void onSkipToNext()
            {
                nextSong();
            }

            @Override
            public void onSkipToPrevious()
            {
                prevSong();
            }
        });
        mediaSession.setActive(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String action = intent.getAction();
        if (action != null)
        {
            switch (action)
            {
                case ACTION_PLAY_SELECTED:
                    currentIndex = intent.getIntExtra("index", 0);
                    playSelectedSong(intent.getIntExtra("resId", 0));
                    break;
                case ACTION_PLAY_PAUSE:
                    playPauseSong();
                    break;
                case ACTION_NEXT:
                    nextSong();
                    break;
                case ACTION_PREVIOUS:
                    prevSong();
                    break;
                case ACTION_LOOP:
                    loopSong();
                    break;
                case ACTION_SHUFFLE:
                    shuffleSong();
                    break;
                case ACTION_SEEK:
                    int seekPosition = intent.getIntExtra("seekPosition", 0);
                    player.seekTo(seekPosition);
                    break;
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (player != null)
        {
            player.stop();
            player.release();
            player = null;
        }
        unregisterReceiver(receiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private void playSelectedSong(int songIndex)
    {
        isPlay = true;
        handler.postDelayed(updateRunnable, 0);

        runSong(songIndex);
        updateButtonClick(ACTION_PLAY_PAUSE, isPlay);
    }

    private void playPauseSong()
    {
        if (isPlay)
        {
            player.pause();
            handler.removeCallbacks(updateRunnable);
        }
        else
        {
            player.start();
            handler.postDelayed(updateRunnable, 0);
        }

        updateNotification(PlaylistActivity.songs.get(currentIndex).title, PlaylistActivity.songs.get(currentIndex).artist);
        isPlay = !isPlay;
        updateButtonClick(ACTION_PLAY_PAUSE, isPlay);
    }

    private void nextSong()
    {
        currentIndex++;
        currentIndex = currentIndex > PlaylistActivity.songs.size() - 1 ? 0 : currentIndex;
        Song song = PlaylistActivity.songs.get(currentIndex);
        runSong(song.resId);
    }

    private void prevSong()
    {
        currentIndex--;
        currentIndex = currentIndex < 0 ? PlaylistActivity.songs.size() - 1 : currentIndex;
        Song song = PlaylistActivity.songs.get(currentIndex);
        runSong(song.resId);
    }

    private void loopSong()
    {
        isLoop = !isLoop;
        updateButtonClick(ACTION_LOOP, isLoop);
    }

    private void shuffleSong()
    {
        isShuffle = !isShuffle;
        updateButtonClick(ACTION_SHUFFLE, isShuffle);
    }

    private void runSong(int songIndex)
    {
        if (player != null)
        {
            player.stop();
            player.release();
        }

        player = MediaPlayer.create(this, songIndex);
        player.setOnCompletionListener(mp ->
        {
            if (isLoop)
            {
                player.seekTo(0);
                player.start();
            }
            else if (isShuffle)
            {
                currentIndex = new Random().nextInt(PlaylistActivity.songs.size());
                Song song = PlaylistActivity.songs.get(currentIndex);
                runSong(song.resId);
            }
            else
                nextSong();
        });

        if (isPlay)
            player.start();

        Song song = PlaylistActivity.songs.get(currentIndex);
        updateNotification(song.title, song.artist);

        Intent intent = new Intent(UPDATE_MUSIC_CHANGE);
        intent.putExtra("title", song.title);
        intent.putExtra("artist", song.artist);
        intent.putExtra("duration", player.getDuration());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void updateNotification(String title, String text)
    {
        if (mediaSession == null)
            return;

        PendingIntent contentIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, PlaylistActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );


        PendingIntent prevIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
                this, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);
        PendingIntent playPauseIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
                this, isPlay ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY);
        PendingIntent nextIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
                this, PlaybackStateCompat.ACTION_SKIP_TO_NEXT);

        // Fallback icons if custom ones not available
        int playPauseIcon = isPlay ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;

        Bitmap albumBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(albumBitmap)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(contentIntent)
                .addAction(android.R.drawable.ic_media_previous, "Previous", prevIntent)
                .addAction(playPauseIcon, isPlay ? "Pause" : "Play", playPauseIntent)
                .addAction(android.R.drawable.ic_media_next, "Next", nextIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1, 2))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        startForeground(1, notification);

        if (player != null)
        {
            PlaybackStateCompat state = new PlaybackStateCompat.Builder()
                    .setActions(
                            PlaybackStateCompat.ACTION_PLAY |
                                    PlaybackStateCompat.ACTION_PAUSE |
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                    PlaybackStateCompat.ACTION_PLAY_PAUSE)
                    .setState(isPlay ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, player.getCurrentPosition(), 1.0f)
                    .build();

            mediaSession.setPlaybackState(state);
        }
    }

    private void updateProgress()
    {
        if (!isPlay)
            return;
        Intent intent = new Intent(UPDATE_PROGRESS);
        intent.putExtra("currentPosition", player.getCurrentPosition());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void updateButtonClick(String text, boolean enabled)
    {
        Intent intent = new Intent(UPDATE_BUTTON_CLICK);
        intent.putExtra("button", text);
        intent.putExtra("enabled", enabled);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}