package com.youkeda.music.model;

import java.util.List;

/**
 * 歌曲对象
 */
public class Song {

  private String id;
  private String name;
  private List<User> singers;
  private String sourceUrl;
  private Album album;
  private List<Comment> hotComments;
  private List<Comment> comments;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public Album getAlbum() {
    return album;
  }

  public void setAlbum(Album album) {
    this.album = album;
  }

  public List<Comment> getHotComments() {
    return hotComments;
  }

  public void setHotComments(List<Comment> hotComments) {
    this.hotComments = hotComments;
  }

  public List<Comment> getComments() {
    return comments;
  }

  public void setComments(List<Comment> comments) {
    this.comments = comments;
  }

  public List<User> getSingers() {
    return singers;
  }

  public void setSingers(List<User> singers) {
    this.singers = singers;
  }
}
