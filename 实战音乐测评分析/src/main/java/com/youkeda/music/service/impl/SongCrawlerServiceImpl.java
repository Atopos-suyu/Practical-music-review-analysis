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

 //音乐抓取服务的实现
public class SongCrawlerServiceImpl implements SongCrawlerService {

  // 歌单 API
  private static final String ARTIEST_API_PREFIX = "http://neteaseapi.youkeda.com:3000/artists?id=";
  // 歌曲详情 API
  private static final String S_D_API_PREFIX = "http://neteaseapi.youkeda.com:3000/song/detail?ids=";
  // 歌曲评论 API
  private static final String S_C_API_PREFIX = "http://neteaseapi.youkeda.com:3000/comment/music?id=";
  // 歌曲音乐文件 API
  private static final String S_F_API_PREFIX = "http://neteaseapi.youkeda.com:3000/song/url?id=";

  //声明okHttpClient实例，用于发送HTTP请求
  private OkHttpClient okHttpClient;  //okHttpClient是一个网络请求库，可以用于发送HTTP请求并获取响应

  // 歌单数据仓库
  private Map<String, Artist> artists;  //Map类型的变量artists，用于保存歌单
  //Map 是一种键值映射的数据结构，可以方便地根据键来查找对应的值
  private void init() {
    okHttpClient = new OkHttpClient();  //构建 okHttpClient 实例
    artists = new HashMap<>();  //初始化artists变量，将其赋值为空的HashMap实例，artists就可以用于储存歌单数据了
  }
//当子类继承自父类并且重写了父类中的某个方法时，可以使用@Override注解来显式地告知编译器，这是对父类方法的覆盖
  @Override
  public void start(String artistId) {
    // 空字符串或者内容为空，则表示未输入参数
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
  public Artist getArtist(String artistId) {  //获取指定歌手ID对应的歌手对象
    return artists.get(artistId);
  }

  @Override
  public Song getSong(String artistId, String songId) {
    Artist artist = artists.get(artistId);  //获取指定歌手ID对应的歌手对象，并赋值给artist
    List<Song> songs = artist.getSongList();  //获取歌手对象的歌曲列表，赋值给songs

    if (songs == null) {  //歌手没有歌曲列表
      return null;
    }

    for (Song song : songs) {  //循环遍历songs列表中的每一首歌曲
      if (song.getId().equals(songId)) {  //判断当前的ID与传入的ID相等，则找到对应的歌曲
        return song;
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")  //用于抑制Java编译器产生的“未检查类型转换”警告
  private Map getSourceDataObj(String prefix, String postfix) {
    // 构建歌单url
    String aUrl = prefix + postfix;
    // 调用okhttp3获取返回数据
    String content = getPageContentSync(aUrl);
    // 反序列化成Map对象
    Map returnData = JSON.parseObject(content, Map.class);
//反序列化指的是将对象从持久化的状态（如文件、数据库）转换回内存对象的过程
    return returnData;
  }  //构建歌单的URL,通过发起HTTP请求获取返回的数据，将返回的数据反序列化成Map对象，并将其作为方法的返回值

  @SuppressWarnings("unchecked")
  private Artist buildArtist(Map returnData) {  //根据传入的Map对象构建一个代表歌手信息的Artist对象

    Map artistData = (Map) returnData.get("artist");  //从传入的Map对象中获取名为"artist"的子Map对象，并将其赋值给artistData
    Artist artist = new Artist();
    artist.setId(artistData.get("id").toString());  //获取歌手ID
    if (artistData.get("picUrl") != null) {
      artist.setPicUrl(artistData.get("picUrl").toString());  //获取图片URL
    }//依次获取歌手的简介、头像URL、名字和别名，使用对应的get()方法获取数据，再调用对应的 set() 方法设置到 artist 对象中
    artist.setBriefDesc(artistData.get("briefDesc").toString());
    artist.setImg1v1Url(artistData.get("img1v1Url").toString());
    artist.setName(artistData.get("name").toString());
    artist.setAlias((List) artistData.get("alias"));
    return artist;  //将歌手对象返回
  }

  private List<Song> buildSongs(Map returnData) {  //将传入的Map对象中的歌曲数据提取出来，封装成一个由Song对象组成的List返回

    List songsData = (List) returnData.get("hotSongs");  //获取名为"hotSongs"的键对应的值
    List<Song> songs = new ArrayList<>();  //创建空的List对象，存放提取出来的歌曲数据

    for (int i = 0; i < songsData.size(); i++) {
      Map songData = (Map) songsData.get(i);  //取出一个歌曲数据的Map对象
      Song songObj = new Song();
      songObj.setId(songData.get("id").toString());
      songObj.setName(songData.get("name").toString());

      songs.add(songObj);
    }

    return songs;  //返回组装好的歌曲列表songs
  }

  private String getPageContentSync(String url) {  //根据输入的url，读取页面内容并返回

    Request request = new Request.Builder().url(url).build();  //创建Request对象并使用传入的url构建

    Call call = okHttpClient.newCall(request);  //使用okHttpClient对象发起请求
    String result = null;
    try {
      result = call.execute().body().string();  //调用call.execute()方法发送请求，并获取服务器的响应结果
      System.out.println("call " + url + " , content's size=" + result.length());
    } catch (IOException e) {
      System.out.println("request " + url + " error . ");  //出现异常则将异常信息打印到控制台输出
      e.printStackTrace();
    }

    return result;
  }  //在请求数据时会阻塞程序，直到获取完整的响应数据才会返回结果

  private void initArtistHotSongs(String artistId) {  //用于初始化一个艺术家的热门歌曲信息

    Map returnData = getSourceDataObj(ARTIEST_API_PREFIX, artistId);//传入艺术家API的前缀和ID，获取Map对象returnData

    Artist artist = buildArtist(returnData);  //构建一个填充了属性的Artist实例artist
    List<Song> songs = buildSongs(returnData);

    artist.setSongList(songs);
    artists.put(artist.getId(), artist);//将artist的ID作为键，artist实例作为值，存入名为artist的Map中
  }

  @SuppressWarnings("unchecked")  //忽略类型检查警告
  private void assembleSongDetail(String artistId) {
    Artist artist = getArtist(artistId);
    
    if (artist == null) {  //取不到歌单说明参数输入错误
      return;
    }  //组装艺术家的歌曲详细信息,进一步对该艺术家对象的属性进行操作

    List<Song> songs = artist.getSongList();
    String sIdsParam = buildManyIdParam(songs);  //获取多个歌曲的详细信息
    Map songsDetailObj = getSourceDataObj(S_D_API_PREFIX, sIdsParam);  //获取一个包含歌曲详细信息(API地址前缀、参数)的Map对象

    List<Map> sourceSongs = (List<Map>) songsDetailObj.get("songs");  //获取原始数据中的歌曲列表
    Map<String, Map> sourceSongsMap = new HashMap<>();  //创建临时的Map对象sourceSongsMap

    for (Map songSourceData : sourceSongs) {  //对每首歌的原始数据进行处理
      String sId = songSourceData.get("id").toString();  //获取歌曲ID，转换为字符串类型
      sourceSongsMap.put(sId, songSourceData);  //原始歌曲数据对象放入一个临时的Map中
    }

    for (Song song : songs) {  //再次遍历歌单中的歌曲，填入详情数据
      String sId = song.getId();  //对于每首歌曲，获取它的ID
      Map songSourceData = sourceSongsMap.get(sId);//从临时的Map中取得对应的歌曲源数据，使用id直接获取，比较方便
      List<Map> singersData = (List<Map>) songSourceData.get("ar");//源歌曲数据中，ar字段是歌手列表
     
      List<User> singers = new ArrayList<>();//创建List<User>集合singers,储存歌曲的所有歌手
      for (Map singerData : singersData) {
        User singer = new User();  //创建歌手对象
        singer.setId(singerData.get("id").toString());
        singer.setNickName(singerData.get("name").toString());
        singers.add(singer);  //将singer对象添加到singers集合中
      }
    
      song.setSingers(singers);  //将singers集合赋值给歌曲对象的singers属性

      // 专辑
      Map albumData = (Map) songSourceData.get("al");  //获取专辑数据
      Album album = new Album();
      album.setId(albumData.get("id").toString());
      album.setName(albumData.get("name").toString());
      if (albumData.get("picUrl") != null) {  //如果专辑数据中有PicUrl字段
        album.setPicUrl(albumData.get("picUrl").toString());
      }
      song.setAlbum(album);  //专辑对象放入歌曲
    }
  }

  @SuppressWarnings("unchecked")
  private void assembleSongComment(String artistId) {
    Artist artist = getArtist(artistId);  //根据传入的歌手ID获取对应的艺人对象artist
    
    if (artist == null) {  //取不到歌单说明artistId输入错误
      return;
    }

    List<Song> songs = artist.getSongList();  //从artist中取出该艺人的歌曲列表songs
    for (Song song : songs) {
      String sIdsParam = song.getId() + "&limit=5";  //将歌曲ID和limit=5构成请求参数字符串sIdsParam
     
      Map songsCommentObj = getSourceDataObj(S_C_API_PREFIX, sIdsParam);  //从API接口中抓取该歌曲的热门评论储存
      List<Map> hotCommentsObj = (List<Map>) songsCommentObj.get("hotComments");  //获取热门评论
      List<Map> commontsObj = (List<Map>) songsCommentObj.get("comments");  // 获取最新评论

      song.setHotComments(buildComments(hotCommentsObj));
      song.setComments(buildComments(commontsObj));  //构建评论集合并赋值给对应的对象属性
    }
  }  //通过在线API接口获取歌曲的评论数据并添加到对应歌曲对象的hotComment和comment属性中供后续使用

  @SuppressWarnings("unchecked")  //为每首歌曲添加音乐文件的URL地址
  private void assembleSongUrl(String artistId) {
    Artist artist = getArtist(artistId);
    if (artist == null) {  //取不到歌单说明参数输入错误
      return;
    }

    List<Song> songs = artist.getSongList();  //从artist对象中取出该艺人的歌曲列表songs
    String sIdsParam = buildManyIdParam(songs);  //构建参数字符串sIdsParam,包含所有歌曲id

    Map songsFileObj = getSourceDataObj(S_F_API_PREFIX, sIdsParam);  //从在线API接口中抓取包含所有歌曲音乐文件信息的数据，储存在Map对象中
    List<Map> datas = (List<Map>) songsFileObj.get("data");  //从songsFileObj中取出音乐文件列表datas
    Map<String, Map> sourceSongsMap = new HashMap<>();   //临时的Map
    // 遍历音乐文件列表
    for (Map songFileData : datas) {
      String sId = songFileData.get("id").toString(); 
      sourceSongsMap.put(sId, songFileData);  //原始音乐文件数据对象放入一个临时的Map中
    }

    // 再次遍历歌单中的歌曲，填入音乐文件URL
    for (Song song : songs) {
      String sId = song.getId();
      // 从临时的Map中取得对应的音乐文件源数据，使用id直接获取，比较方便
      Map songFileData = sourceSongsMap.get(sId);
      // 源音乐文件数据中，url字段就是文件地址
      if (songFileData != null && songFileData.get("url") != null) {
        String songFileUrl = songFileData.get("url").toString();  //取出url字段对应的字符串，作为该歌曲的音乐文件URL地址，赋值给sourceUrl属性
        song.setSourceUrl(songFileUrl);
      }
    }
  }
//通过在线API接口获取歌曲的音乐文件信息，以及构建歌曲ID和音乐文件数据对象之间的映射关系，从而便于后续为每首歌曲添加对应的音乐文件URL地址
  private void generateWordCloud(String artistId) {  //生成艺人歌曲风格的词云图
    Artist artist = getArtist(artistId);
    if (artist == null) {  //取不到歌单说明参数输入错误
      return;
    }

    List<Song> songs = artist.getSongList();  //获取艺人对象artist歌曲列表
    List<String> contents = new ArrayList<>();  //存取歌曲评论的额内容
    for (Song song : songs) {
      collectContent(song.getComments(), contents);
      collectContent(song.getHotComments(), contents);
    }//遍历歌曲所有的评论，包括普通评论和热门评论，把评论内容字符串存入集合

    WordCloudUtil.generate(artistId, contents);  //制作词云
  }

  private void collectContent(List<Comment> comments, List<String> contents) {  //参数：评论对象列表，空字符串集合
    for (Comment comment : comments) {
      contents.add(comment.getContent());  //获取评论内容并添加
    }
  }
  //将评论对象数据构建成一个评论对象列表
  private List<Comment> buildComments(List<Map> commontsObj) {  //传入包含评论对象数据的列表
    List<Comment> comments = new ArrayList<>();  //创建空评论对象列表comments

    for (Map sourceComment : commontsObj) {
      Comment commont = new Comment();
      commont.setContent(sourceComment.get("content").toString());
      commont.setId(sourceComment.get("commentId").toString());
      commont.setLikedCount(sourceComment.get("likedCount").toString());
      commont.setTime(sourceComment.get("time").toString());
      //获取用户评论内容、评论ID、点赞数、评论时间
      User user = new User();
      Map sourceUserData = (Map) sourceComment.get("user");
      user.setId(sourceUserData.get("userId").toString());
      user.setNickName(sourceUserData.get("nickname").toString());
      user.setAvatar(sourceUserData.get("avatarUrl").toString());
      commont.setCommentUser(user);
      //获取用户数据，将用户ID、昵称、头像URL设置到user对象的属性中
      comments.add(commont);  //将构建好的comment对象添加到评论对象列表中
    }
    return comments;
  }

  private String buildManyIdParam(List<Song> songs) {
    //收集一个歌单中所有歌曲的id，放入一个list
    List<String> songIds = new ArrayList<>();
    for (Song song : songs) {
      songIds.add(song.getId());
    }

    // 一个歌单中所有歌曲的id，组装成用逗号分割的字符串，形如：347230,347231。记住这个用法，很方便
    String sIdsParam = String.join(",", songIds);

    return sIdsParam;
  }

}
