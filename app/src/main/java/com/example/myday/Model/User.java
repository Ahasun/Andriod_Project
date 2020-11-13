package com.example.myday.Model;

public class User
{
    private String id;
    private String userName;
    private String fullName;
    private String imageURL;
    private String bio;



    public User(String id, String userName, String fullName, String imageURL, String bio)
    {
        this.id = id;
        this.userName = userName;
        this.fullName = fullName;
        this.imageURL = imageURL;
        this.bio = bio;
    }



    public User()
    {

    }


    public void setId(String id)
    {
        this.id = id;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public void setImageURL(String imageURL)
    {
        this.imageURL = imageURL;
    }

    public void setBio(String bio)
    {
        this.bio = bio;
    }



    public String getId()
    {
        return this.id;
    }

    public String getUserName()
    {
        return this.userName;
    }

    public String getFullName()
    {
        return this.fullName;
    }

    public String getImageURL()
    {
        return this.imageURL;
    }

    public String getBio()
    {
        return this.bio;
    }
}
