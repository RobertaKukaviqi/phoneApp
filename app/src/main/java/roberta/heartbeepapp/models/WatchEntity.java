package roberta.heartbeepapp.models;

public class WatchEntity {
    private String watchuUserID;
    private String watchUserName;

    public WatchEntity(){

    }

    public WatchEntity(String watchuUserID, String watchUserName) {
        this.watchuUserID = watchuUserID;
        this.watchUserName = watchUserName;
    }

    public String getWatchuUserID() {
        return watchuUserID;
    }

    public void setWatchuUserID(String watchuUserID) {
        this.watchuUserID = watchuUserID;
    }

    public String getWatchUserName() {
        return watchUserName;
    }

    public void setWatchUserName(String watchUserName) {
        this.watchUserName = watchUserName;
    }
}
