package hcmute.edu.vn.musicplayer;

public class Song {
    public String title;
    public String artist;
    public String path;
    public int resID;
    public int index;

    public Song(String title, String artist, String path, int resID, int index) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.resID = resID;
        this.index = index;
    }

    @Override
    public String toString() {
        return title + " - " + artist;
    }
}
