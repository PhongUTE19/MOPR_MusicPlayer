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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Build;

import java.util.Random;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MusicService extends Service
{
    public static final String UPDATE_MUSIC_CHANGE = "UPDATE_MUSIC_CHANGE";
    public static final String UPDATE_PROGRESS = "UPDATE_PROGRESS";
    public static final String UPDATE_BUTTON_CLICK = "UPDATE_BUTTON_CLICK";
    public static final String ACTION_PLAY_PAUSE = "ACTION_PLAY_PAUSE";
    public static final String ACTION_NEXT = "ACTION_NEXT";
    public static final String ACTION_PREVIOUS = "ACTION_PREVIOUS";
    public static final String ACTION_LOOP = "ACTION_LOOP";
    public static final String ACTION_SHUFFLE = "ACTION_SHUFFLE";
    public static final String ACTION_SEEK = "ACTION_SEEK";
    private static final String CHANNEL_ID = "music_channel";

    private final int[] playlist = {R.raw.littleroot_town, R.raw.oldale_town, R.raw.petalburg_city};
    private final String[] songTitles = {"Littleroot Town", "Oldale Town", "Petalburg City"};
    private final String[] artistNames = {"Game Freak", "Game Freak 2", "Game Freak 3"};

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
            if (player != null && player.isPlaying())
            {
                sendProgressUpdate();
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

        runSong(currentIndex);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        String action = intent.getAction();
        if (action != null)
        {
            switch (action)
            {
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

    private void playPauseSong()
    {
        if (isPlay)
        {
            player.pause();
            handler.removeCallbacks(updateRunnable);
            updateNotification("Ahihi", "Ahihi");
        }
        else
        {
            player.start();
            handler.postDelayed(updateRunnable, 0);
            updateNotification(songTitles[currentIndex], artistNames[currentIndex]);
        }

        isPlay = !isPlay;
        sendButtonUpdate(ACTION_PLAY_PAUSE, isPlay);
    }

    private void nextSong()
    {
        currentIndex++;
        currentIndex = currentIndex > playlist.length - 1 ? 0 : currentIndex;
        runSong(currentIndex);
    }

    private void prevSong()
    {
        currentIndex--;
        currentIndex = currentIndex < 0 ? playlist.length - 1 : currentIndex;
        runSong(currentIndex);
    }

    private void loopSong()
    {
        isLoop = !isLoop;
        sendButtonUpdate(ACTION_LOOP, isLoop);
    }

    private void shuffleSong()
    {
        isShuffle = !isShuffle;
        sendButtonUpdate(ACTION_SHUFFLE, isShuffle);
    }

    private void runSong(int index)
    {
        if (player != null)
        {
            player.stop();
            player.release();
        }

        player = MediaPlayer.create(this, playlist[index]);
        player.setOnCompletionListener(mp ->
        {
            if (isLoop)
            {
                player.seekTo(0);
                player.start();
            }
            else if (isShuffle)
            {
                currentIndex = new Random().nextInt(playlist.length);
                runSong(currentIndex);
            }
            else
                nextSong();
        });

        if (isPlay)
            player.start();

        updateNotification(songTitles[index], artistNames[index]);

        Intent intent = new Intent(UPDATE_MUSIC_CHANGE);
        intent.putExtra("title", songTitles[index]);
        intent.putExtra("artist", artistNames[index]);
        intent.putExtra("duration", player.getDuration());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void updateNotification(String title, String text)
    {
        Intent playPauseIntent = new Intent(this, MusicService.class);
        playPauseIntent.setAction(ACTION_PLAY_PAUSE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, playPauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .addAction(R.drawable.ic_notification, isPlay ? "Pause" : "Play", pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);

//        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle(title)
//                .setContentText(text)
//                .setSmallIcon(R.drawable.ic_notification)
//                .build();
//        startForeground(1, notification);
    }

    private void sendProgressUpdate()
    {
        if (player != null && player.isPlaying())
        {
            Intent intent = new Intent(UPDATE_PROGRESS);
            intent.putExtra("currentPosition", player.getCurrentPosition());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    private void sendButtonUpdate(String text, boolean enabled)
    {
        Intent intent = new Intent(UPDATE_BUTTON_CLICK);
        intent.putExtra("button", text);
        intent.putExtra("enabled", enabled);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
