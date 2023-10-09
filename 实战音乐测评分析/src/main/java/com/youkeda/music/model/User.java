package com.youkeda.music.model;

/**
 *
 */  //储存一个用户的相关信息
public class User {  //表示一个用户对象

  private String id;
  private String nickName;  //用户昵称
  private String avatar;  //用户头像

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }
}
