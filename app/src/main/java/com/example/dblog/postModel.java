package com.example.dblog;

public class postModel {
    private String Title, Post, Blog;

    private postModel(){}

    private postModel(String Title, String Post, String Blog){
        this.Title = Title;
        this.Post = Post;
        this.Blog = Blog;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getPost() {
        return Post;
    }

    public void setPost(String post) {
        Post = post;
    }

    public String getBlog() {
        return Blog;
    }

    public void setBlog(String blog) {
        Blog = blog;
    }
}
