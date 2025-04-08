package hcmute.edu.vn.musicplayer;

public class Song {
    public String title;
    public String artist;
    public int resId;
    public int index;

    public Song(String title, String artist, int resId, int index) {
        this.title = title;
        this.artist = artist;
        this.resId = resId;
        this.index = index;
    }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
