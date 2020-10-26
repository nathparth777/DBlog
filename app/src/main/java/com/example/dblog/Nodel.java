package com.example.dblog;

public class Nodel {
    private String Title, Image, Blog, Name;

    private Nodel(){}

    private Nodel(String Title, String Image, String Blog, String Name){
        this.Title = Title;
        this.Image = Image;
        this.Blog = Blog;
        this.Name = Name;

    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getBlog() {
        return Blog;
    }

    public void setBlog(String blog) {
        Blog = blog;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
