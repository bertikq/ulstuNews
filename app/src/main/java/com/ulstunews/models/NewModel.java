package com.ulstunews.models;

import java.io.Serializable;

public class NewModel implements Serializable {
    private String Title;
    private String Body;
    private String Author;

    public NewModel(String title, String body, String author) {
        Title = title;
        Body = body;
        Author = author;
    }


    public String getTitle() {
        return Title;
    }

    public String getBody() {
        return Body;
    }

    public String getAuthor() {
        return Author;
    }
}
