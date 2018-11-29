package monash.sydney.com.mytracker;

public class Song {

    private Long songID;
    private String songTitle;
    private String songArtist;


    public Song(Long songID, String songTitle, String songArtist) {
        this.songID = songID;
        this.songTitle = songTitle;
        this.songArtist = songArtist;
    }

    public Long getSongID() {
        return songID;
    }

    public void setSongID(Long songID) {
        this.songID = songID;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    @Override
    public String toString() {
        return songTitle;
    }
}
