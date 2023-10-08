package com.youkeda.music.util;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.FontWeight;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer;
import com.kennycason.kumo.palette.ColorPalette;
import java.awt.Color;
import java.awt.Dimension;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 生成图云的工具类
 */
public class WordCloudUtil {

  /**
   * 生成词云
   *
   * @param artistId 歌单id
   * @param texts 文本
   */
  public static void generate(String artistId, List<String> texts) {

    FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
    //设置返回的词数
    frequencyAnalyzer.setWordFrequenciesToReturn(500);
    //设置返回的词语最小出现频次
    frequencyAnalyzer.setMinWordLength(4);

    //引入中文解析器
    frequencyAnalyzer.setWordTokenizer(new ChineseWordTokenizer());
    //输入文章数据，进行分词
    final List<WordFrequency> wordFrequencyList = frequencyAnalyzer.load(texts);
    //设置图片分辨率大小
    Dimension dimension = new Dimension(600, 600);
    //此处的设置采用内置常量即可，生成词云对象
    WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
    //设置边界及字体
    wordCloud.setPadding(2);
    // 设置字体，字体必须支持中文，不能随便改
    wordCloud.setKumoFont(new KumoFont("阿里巴巴普惠体 Light", FontWeight.PLAIN));
    //ColorPalette是调色板，用于设置词云显示的多种颜色，越靠前设置表示词频越高的词语的颜色
    wordCloud.setColorPalette(
        new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1),
            new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
    wordCloud.setFontScalar(new SqrtFontScalar(10, 70));
    //设置背景图层为圆形
    wordCloud.setBackground(new CircleBackground(300));
    //生成词云
    wordCloud.build(wordFrequencyList);
    //输出到图片文件，用当前的毫秒数作为文件名
    Long milliSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
    //输出到图片文件
    wordCloud.writeToFile("wordCloud-" + artistId + ".png");
  }
}
