package com.youkeda.music.model;

import java.util.List;

/**
 * 歌单对象
 */
public class Artist {  //表示一个歌手对象

  private String id;  //歌手的唯一标识符
  private List<String> alias;  //歌手的别名列表
  private String picUrl;  //歌手的图片URL
  private String briefDesc;  //歌手的简介
  private String img1v1Url;  //歌手的小头像URL
  private String name;  //歌手的名字
  // 储存歌手的歌曲列表，包含一组歌曲
  private List<Song> songList;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getAlias() {
    return alias;
  }

  public void setAlias(List<String> alias) {
    this.alias = alias;
  }

  public String getPicUrl() {
    return picUrl;
  }

  public void setPicUrl(String picUrl) {
    this.picUrl = picUrl;
  }

  public String getBriefDesc() {
    return briefDesc;
  }

  public void setBriefDesc(String briefDesc) {
    this.briefDesc = briefDesc;
  }

  public String getImg1v1Url() {
    return img1v1Url;
  }

  public void setImg1v1Url(String img1v1Url) {
    this.img1v1Url = img1v1Url;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Song> getSongList() {
    return songList;
  }

  public void setSongList(List<Song> songList) {
    this.songList = songList;
  }
}
