package com.example.shivam.youtubeproject;

/**
 * Created by Casino on 7/4/17.
 */

public class CommentsVoiceNote {

    private String comment, youtubeID;
    private long duration;

    public CommentsVoiceNote(String comment, String youtubeID, long duration)
    {
        this.comment = comment;
        this.youtubeID = youtubeID;
        this.duration = duration;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getYoutubeID() {
        return youtubeID;
    }

    public void setYoutubeID(String youtubeID) {
        this.youtubeID = youtubeID;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
