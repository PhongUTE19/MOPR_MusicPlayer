package hcmute.edu.vn.musicplayer;

public class Song {
    public String title;
    public String artist;
    public String path;
    public int duration;

    public Song(String title, String artist, String path, int duration) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
    }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
