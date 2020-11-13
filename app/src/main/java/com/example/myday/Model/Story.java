package com.example.myday.Model;

public class Story
{

    private String imageUrl;
    private String storyId;
    private long timeStart;
    private long timeEnd;
    private String userId;

    public Story(String imageUrl, String storyId, long timeStart, long timeEnd , String userId)
    {
        this.imageUrl = imageUrl;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.storyId = storyId;
        this.userId = userId;
    }

    public Story()
    {

    }


    public void setImageURL(String imageUrl)
    {
        this.imageUrl = imageUrl;
    }

    public void setTimeStart(long timeStart)
    {
        this.timeStart = timeStart;
    }

    public void setTimeEnd(long timeEnd)
    {
        this.timeEnd = timeEnd;
    }

    public void setStoryId(String storyId)
    {
        this.storyId = storyId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getImageURL()
    {
        return imageUrl;
    }

    public long getTimeStart()
    {
        return timeStart;
    }

    public long getTimeEnd()
    {
        return timeEnd;
    }

    public String getStoryId()
    {
        return storyId;
    }

    public String getUserId()
    {
        return userId;
    }
}
