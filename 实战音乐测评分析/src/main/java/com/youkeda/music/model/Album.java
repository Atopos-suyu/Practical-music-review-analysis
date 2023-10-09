package com.youkeda.music.model;

/**
 *
 */
public class Album {  //表示一个音乐专辑对象

  private String id;  //音乐专辑的唯一标识符
  private String name;  //音乐专辑的名称
  private String picUrl;  //音乐专辑的封面URL
  //封装，对Album对象的属性进行设置和获取
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

  public String getPicUrl() {
    return picUrl;
  }

  public void setPicUrl(String picUrl) {
    this.picUrl = picUrl;
  }
}
