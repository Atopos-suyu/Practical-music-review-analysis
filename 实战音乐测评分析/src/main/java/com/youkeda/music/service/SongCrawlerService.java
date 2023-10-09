package com.youkeda.music.service;

import com.youkeda.music.model.Artist;
import com.youkeda.music.model.Song;

/**
 * 音乐抓取服务
 */
public interface SongCrawlerService {  //定义名为songCrawlerService的接口(interface)
   
  public void start(String artistId);  //根据歌单id，抓取歌单数据

  public Artist getArtist(String artistId);  //根据歌单id查询歌单对象

  public Song getSong(String artistId, String songId);  //根据歌曲id查询歌曲对象
}
