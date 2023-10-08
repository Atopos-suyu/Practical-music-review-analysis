package com.youkeda.music.model;

import java.util.List;

/**
 * 歌单对象
 */
public class Artist {

  private String id;
  private List<String> alias;
  private String picUrl;
  private String briefDesc;
  private String img1v1Url;
  private String name;
  // 包含一组歌曲
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
