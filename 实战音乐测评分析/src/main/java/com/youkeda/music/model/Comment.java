package com.youkeda.music.model;

/**
 *
 */
public class Comment {

  private String id;
  private String content;
  private String likedCount;  //表示评论被点赞的次数
  private String time;  //表示评论的发表时间
  private User commentUser;  //发表该评论的用户对象

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getLikedCount() {
    return likedCount;
  }

  public void setLikedCount(String likedCount) {
    this.likedCount = likedCount;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public User getCommentUser() {
    return commentUser;
  }

  public void setCommentUser(User commentUser) {
    this.commentUser = commentUser;
  }
}
