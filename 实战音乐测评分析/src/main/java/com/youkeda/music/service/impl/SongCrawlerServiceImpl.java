package com.youkeda.music.service.impl;

import com.alibaba.fastjson.JSON;
import com.youkeda.music.model.Album;
import com.youkeda.music.model.Artist;
import com.youkeda.music.model.Comment;
import com.youkeda.music.model.Song;
import com.youkeda.music.model.User;
import com.youkeda.music.service.SongCrawlerService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.youkeda.music.util.WordCloudUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 音乐抓取服务的实现
 */
public class SongCrawlerServiceImpl implements SongCrawlerService {

  // 歌单 API
  private static final String ARTIEST_API_PREFIX = "http://neteaseapi.youkeda.com:3000/artists?id=";
  // 歌曲详情 API
  private static final String S_D_API_PREFIX = "http://neteaseapi.youkeda.com:3000/song/detail?ids=";
  // 歌曲评论 API
  private static final String S_C_API_PREFIX = "http://neteaseapi.youkeda.com:3000/comment/music?id=";
  // 歌曲音乐文件 API
  private static final String S_F_API_PREFIX = "http://neteaseapi.youkeda.com:3000/song/url?id=";

  // okHttpClient 实例
  private OkHttpClient okHttpClient;

  // 歌单数据仓库
  private Map<String, Artist> artists;

  private void init() {
    //1. 构建 okHttpClient 实例
    okHttpClient = new OkHttpClient();
    artists = new HashMap<>();
  }

  @Override
  public void start(String artistId) {
    // 参数判断，未输入参数则直接返回
    if (artistId == null || artistId.equals("")) {
      return;
    }

    // 执行初始化
    init();

    // 初始化歌曲及歌单
    initArtistHotSongs(artistId);
    assembleSongDetail(artistId);
    assembleSongComment(artistId);
    assembleSongUrl(artistId);
    generateWordCloud(artistId);
  }

  @Override
  public Artist getArtist(String artistId) {
    return artists.get(artistId);
  }

  @Override
  public Song getSong(String artistId, String songId) {
    Artist artist = artists.get(artistId);
    List<Song> songs = artist.getSongList();

    if (songs == null) {
      return null;
    }

    for (Song song : songs) {
      if (song.getId().equals(songId)) {
        return song;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private Map getSourceDataObj(String prefix, String postfix) {
    // 构建歌单url
    String aUrl = prefix + postfix;
    // 调用 okhttp3 获取返回数据
    String content = getPageContentSync(aUrl);
    // 反序列化成 Map 对象
    Map returnData = JSON.parseObject(content, Map.class);

    return returnData;
  }

  @SuppressWarnings("unchecked")
  private Artist buildArtist(Map returnData) {
    // 从 Map 对象中取得 歌单 数据。歌单也是一个子 Map 对象。
    Map artistData = (Map) returnData.get("artist");
    Artist artist = new Artist();
    artist.setId(artistData.get("id").toString());
    if (artistData.get("picUrl") != null) {
      artist.setPicUrl(artistData.get("picUrl").toString());
    }
    artist.setBriefDesc(artistData.get("briefDesc").toString());
    artist.setImg1v1Url(artistData.get("img1v1Url").toString());
    artist.setName(artistData.get("name").toString());
    artist.setAlias((List) artistData.get("alias"));
    return artist;
  }

  private List<Song> buildSongs(Map returnData) {
    // 从 Map 对象中取得一组 歌曲 数据
    List songsData = (List) returnData.get("hotSongs");
    List<Song> songs = new ArrayList<>();

    for (int i = 0; i < songsData.size(); i++) {
      Map songData = (Map) songsData.get(i);
      Song songObj = new Song();
      songObj.setId(songData.get("id").toString());
      songObj.setName(songData.get("name").toString());

      songs.add(songObj);
    }

    return songs;
  }

  /**
   * 根据输入的url，读取页面内容并返回
   */
  private String getPageContentSync(String url) {
    //2.定义一个request
    Request request = new Request.Builder().url(url).build();
    //3.使用client去请求
    Call call = okHttpClient.newCall(request);
    String result = null;
    try {
      //4.获得返回结果
      result = call.execute().body().string();
      System.out.println("call " + url + " , content's size=" + result.length());
    } catch (IOException e) {
      System.out.println("request " + url + " error . ");
      e.printStackTrace();
    }

    return result;
  }

  private void initArtistHotSongs(String artistId) {
    // 取得整体数据对象。
    Map returnData = getSourceDataObj(ARTIEST_API_PREFIX, artistId);
    // 构建填充了属性的 Artist 实例
    Artist artist = buildArtist(returnData);
    // 构建一组填充了属性的 Song 实例
    List<Song> songs = buildSongs(returnData);
    // 歌曲填入歌单
    artist.setSongList(songs);
    // 存入本地
    artists.put(artist.getId(), artist);
  }

  @SuppressWarnings("unchecked")
  private void assembleSongDetail(String artistId) {
    Artist artist = getArtist(artistId);
    // 取不到歌单说明参数输入错误
    if (artist == null) {
      return;
    }

    // 删除其它语句，保留必要的语句
    List<Song> songs = artist.getSongList();
    String sIdsParam = buildManyIdParam(songs);
    // 抓取结果
    Map songsDetailObj = getSourceDataObj(S_D_API_PREFIX, sIdsParam);
    // 原始数据中的 songs 是歌曲列表
    List<Map> sourceSongs = (List<Map>) songsDetailObj.get("songs");
    // 临时的 Map
    Map<String, Map> sourceSongsMap = new HashMap<>();
    // 遍历歌曲列表
    for (Map songSourceData : sourceSongs) {
      String sId = songSourceData.get("id").toString();
      // 原始歌曲数据对象放入一个临时的 Map 中
      sourceSongsMap.put(sId, songSourceData);
    }

    // 再次遍历歌单中的歌曲，填入详情数据
    for (Song song : songs) {
      String sId = song.getId();
      // 从临时的Map中取得对应的歌曲源数据，使用id直接获取，比较方便
      Map songSourceData = sourceSongsMap.get(sId);
      // 源歌曲数据中，ar 字段是歌手列表
      List<Map> singersData = (List<Map>) songSourceData.get("ar");
      // 歌手集合
      List<User> singers = new ArrayList<>();
      for (Map singerData : singersData) {
        // 歌手对象
        User singer = new User();
        singer.setId(singerData.get("id").toString());
        singer.setNickName(singerData.get("name").toString());
        // 歌手集合放入歌手对象
        singers.add(singer);
      }
      // 歌手集合放入歌曲
      song.setSingers(singers);

      // 专辑
      Map albumData = (Map) songSourceData.get("al");
      Album album = new Album();
      album.setId(albumData.get("id").toString());
      album.setName(albumData.get("name").toString());
      if (albumData.get("picUrl") != null) {
        album.setPicUrl(albumData.get("picUrl").toString());
      }
      // 专辑对象放入歌曲
      song.setAlbum(album);
    }
  }

  @SuppressWarnings("unchecked")
  private void assembleSongComment(String artistId) {
    Artist artist = getArtist(artistId);
    // 取不到歌单说明参数输入错误
    if (artist == null) {
      return;
    }

    List<Song> songs = artist.getSongList();
    for (Song song : songs) {
      String sIdsParam = song.getId() + "&limit=5";
      // 抓取结果
      Map songsCommentObj = getSourceDataObj(S_C_API_PREFIX, sIdsParam);
      // 热门评论列表
      List<Map> hotCommentsObj = (List<Map>) songsCommentObj.get("hotComments");
      // 热门评论列表
      List<Map> commontsObj = (List<Map>) songsCommentObj.get("comments");

      song.setHotComments(buildComments(hotCommentsObj));
      song.setComments(buildComments(commontsObj));
    }
  }

  @SuppressWarnings("unchecked")
  private void assembleSongUrl(String artistId) {
    Artist artist = getArtist(artistId);
    // 取不到歌单说明参数输入错误
    if (artist == null) {
      return;
    }

    // 删除其它语句，保留必要的语句
    List<Song> songs = artist.getSongList();
    String sIdsParam = buildManyIdParam(songs);
    // 抓取结果
    Map songsFileObj = getSourceDataObj(S_F_API_PREFIX, sIdsParam);
    // 原始数据中的 data 是音乐文件列表
    List<Map> datas = (List<Map>) songsFileObj.get("data");
    // 临时的 Map
    Map<String, Map> sourceSongsMap = new HashMap<>();
    // 遍历音乐文件列表
    for (Map songFileData : datas) {
      String sId = songFileData.get("id").toString();
      // 原始音乐文件数据对象放入一个临时的 Map 中
      sourceSongsMap.put(sId, songFileData);
    }

    // 再次遍历歌单中的歌曲，填入音乐文件URL
    for (Song song : songs) {
      String sId = song.getId();
      // 从临时的Map中取得对应的音乐文件源数据，使用id直接获取，比较方便
      Map songFileData = sourceSongsMap.get(sId);
      // 源音乐文件数据中，url 字段就是文件地址
      if (songFileData != null && songFileData.get("url") != null) {
        String songFileUrl = songFileData.get("url").toString();
        song.setSourceUrl(songFileUrl);
      }
    }
  }

  private void generateWordCloud(String artistId) {
    Artist artist = getArtist(artistId);
    // 取不到歌单说明参数输入错误
    if (artist == null) {
      return;
    }

    List<Song> songs = artist.getSongList();
    List<String> contents = new ArrayList<>();
    for (Song song : songs) {
      // 遍历歌曲所有的评论，包括普通评论和热门评论，把评论内容字符串存入集合
      collectContent(song.getComments(), contents);
      collectContent(song.getHotComments(), contents);
    }

    // 制作词云
    WordCloudUtil.generate(artistId, contents);
  }

  private void collectContent(List<Comment> comments, List<String> contents) {
    for (Comment comment : comments) {
      contents.add(comment.getContent());
    }
  }

  private List<Comment> buildComments(List<Map> commontsObj) {
    List<Comment> comments = new ArrayList<>();

    for (Map sourceComment : commontsObj) {
      Comment commont = new Comment();
      commont.setContent(sourceComment.get("content").toString());
      commont.setId(sourceComment.get("commentId").toString());
      commont.setLikedCount(sourceComment.get("likedCount").toString());
      commont.setTime(sourceComment.get("time").toString());

      User user = new User();
      Map sourceUserData = (Map) sourceComment.get("user");
      user.setId(sourceUserData.get("userId").toString());
      user.setNickName(sourceUserData.get("nickname").toString());
      user.setAvatar(sourceUserData.get("avatarUrl").toString());
      commont.setCommentUser(user);

      comments.add(commont);
    }

    return comments;
  }

  private String buildManyIdParam(List<Song> songs) {
    // 收集一个歌单中所有歌曲的id，放入一个list
    List<String> songIds = new ArrayList<>();
    for (Song song : songs) {
      songIds.add(song.getId());
    }

    // 一个歌单中所有歌曲的id，组装成用逗号分割的字符串，形如：347230,347231。记住这个用法，很方便
    String sIdsParam = String.join(",", songIds);

    return sIdsParam;
  }

}