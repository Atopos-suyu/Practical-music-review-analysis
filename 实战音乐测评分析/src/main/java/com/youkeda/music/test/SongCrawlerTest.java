package com.youkeda.music.test;

import com.youkeda.music.model.Artist;
import com.youkeda.music.model.Song;
import com.youkeda.music.service.SongCrawlerService;
import com.youkeda.music.service.impl.SongCrawlerServiceImpl;

/**
 * 检查服务是否可以正确返回对象
 */
public class SongCrawlerTest {

  private static final String SA_DING_DING = "萨顶顶";
  private static final String A_ID = "9270";
  private static final String ZUO_SHOU_ZHI_YUE = "左手指月";
  private static final String S_ID = "536096151";

  public static void main(String[] args) {
    SongCrawlerService songService = new SongCrawlerServiceImpl();
    songService.start(A_ID);

    Artist artist = songService.getArtist(A_ID);
    System.out.println("歌单名称：" + artist.getName());
    if (!SA_DING_DING.equals(artist.getName())) {
      System.out.println("歌单名称错误，不是本测试用例指定的歌单。");
      System.exit(1);
    }

    Song song = songService.getSong(A_ID, S_ID);
    System.out.println("歌曲名称：" + song.getName());
    if (!ZUO_SHOU_ZHI_YUE.equals(song.getName())) {
      System.out.println("歌曲名称错误，不是本测试用例指定的歌曲。");
      System.exit(1);
    }

    if (!SA_DING_DING.equals(song.getSingers().get(0).getNickName())) {
      System.out.println("歌曲名称错误，不是本测试用例指定的歌曲。");
      System.exit(1);
    }

    if (!"香蜜沉沉烬如霜 电视原声音乐专辑".equals(song.getAlbum().getName())) {
      System.out.println("专辑名称错误，不是本测试用例指定的歌曲的专辑。");
      System.exit(1);
    }

    if (song.getSourceUrl() == null) {
      System.out.println("歌曲名称错误，不是本测试用例指定的歌曲。");
      System.exit(1);
    }

    if (song.getHotComments() == null || song.getHotComments().isEmpty()) {
      System.out.println("歌曲热门评论错误，没有正确抓取评论数据。");
      System.exit(1);
    }

    System.out.println("歌曲所属专辑名称：" + song.getAlbum().getName());
    System.out.println("歌曲的歌手名称：" + song.getSingers().get(0).getNickName());
    System.out.println("歌曲音乐为文件地址：" + song.getSourceUrl());
    System.out.println("歌曲热门评论：" + song.getHotComments().get(0).getContent());


    System.out.println("Mission Complete");
    System.out.println("歌曲服务运行成功。非常棒！");
  }
}
