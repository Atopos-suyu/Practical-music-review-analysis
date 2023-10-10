package com.youkeda.music.test;

import com.youkeda.music.model.Artist;
import com.youkeda.music.model.Song;
import com.youkeda.music.service.SongCrawlerService;
import com.youkeda.music.service.impl.SongCrawlerServiceImpl;

/**
 * 检查服务是否可以正确返回对象
 */
public class SongCrawlerTest {  //歌曲爬虫的测试类

  private static final String SA_DING_DING = "萨顶顶";
  private static final String A_ID = "9270";
  private static final String ZUO_SHOU_ZHI_YUE = "左手指月";
  private static final String S_ID = "536096151";

  public static void main(String[] args) {  //测试歌曲爬虫能否正确抓取指定歌手的歌曲信息
    SongCrawlerService songService = new SongCrawlerServiceImpl();  //创建实例，将实现类SongCrawlerServiceImpl对象赋值给它
    songService.start(A_ID);  //传入ID号启动歌曲爬虫，开始抓取对应歌手的相关信息

    Artist artist = songService.getArtist(A_ID);  //获取对应歌手的详细信息，并将结果赋值给变量artist
    System.out.println("歌单名称：" + artist.getName());
    if (!SA_DING_DING.equals(artist.getName())) {  //判断是否与常量SA_DING_DING中指定的歌单名称相同
      System.out.println("歌单名称错误，不是本测试用例指定的歌单。");  //不同则输出错误提示信息
      System.exit(1);  //调用方法结束程序执行
    }

    Song song = songService.getSong(A_ID, S_ID);  //获取特定歌手的指定歌曲信息
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
