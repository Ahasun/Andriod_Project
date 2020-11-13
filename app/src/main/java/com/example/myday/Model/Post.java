package com.example.myday.Model;

public class Post
{
    private String postId;
    private String postImage;
    private String description;
    private String publisher;



    public Post(String postId, String postImage, String description, String publisher)
    {
        this.postId = postId;
        this.postImage = postImage;
        this.description = description;
        this.publisher = publisher;
    }



    public Post()
    {

    }


    public void setPostId(String postId)
    {
        this.postId = postId;
    }

    public void setPostImage(String postImage)
    {
        this.postImage = postImage;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setPublisher(String publisher)
    {
        this.publisher = publisher;
    }

    public String getPostId()
    {
        return postId;
    }

    public String getPostImage()
    {
        return postImage;
    }

    public String getDescription()
    {
        return description;
    }

    public String getPublisher()
    {
        return publisher;
    }
}
