package com.leyue.smartcs.domain.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RedisearchUtils {

  /**
   * 转义RediSearch查询中的特殊字符
   *
   * @param input 输入字符串
   * @return 转义后的字符串
   */
  public static String escapeQueryChars(String input) {
    if (input == null) {
      return "";
    }
    return input.replaceAll("([\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\"\\~\\*\\?\\:\\=\\/\\<\\>])", "\\\\$1");
  }

  /**
   * 将float数组转换为字节数组，用于存储向量数据
   * RediSearch使用小端序(LITTLE_ENDIAN)存储向量
   *
   * @param array float数组
   * @return 字节数组
   */
  public static byte[] floatArrayToByteArray(float[] array) {
    if (array == null) {
      return null;
    }
    ByteBuffer bb = ByteBuffer.allocate(Float.BYTES * array.length)
        .order(ByteOrder.LITTLE_ENDIAN);
    for (float f : array) {
      bb.putFloat(f);
    }
    return bb.array();
  }

} 