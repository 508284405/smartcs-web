package com.leyue.smartcs.common.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.nio.ByteBuffer;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class VectorTypeHandler extends BaseTypeHandler<float[]> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType) throws SQLException {
    // 将 float 数组转换为字节数组
    ByteBuffer buffer = ByteBuffer.allocate(parameter.length * 4);
    for (float f : parameter) {
      buffer.putFloat(f);
    }
    byte[] bytes = buffer.array();
    // 对字节数组进行 Base64 编码
    String base64 = Base64.getEncoder().encodeToString(bytes);
    ps.setString(i, base64);
  }

  @Override
  public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String base64 = rs.getString(columnName);
    if (base64 == null) {
      return null;
    }
    return base64ToFloatArray(base64);
  }

  @Override
  public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String base64 = rs.getString(columnIndex);
    if (base64 == null) {
      return null;
    }
    return base64ToFloatArray(base64);
  }

  @Override
  public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String base64 = cs.getString(columnIndex);
    if (base64 == null) {
      return null;
    }
    return base64ToFloatArray(base64);
  }

  private float[] base64ToFloatArray(String base64) {
    byte[] bytes = Base64.getDecoder().decode(base64);
    int length = bytes.length / 4;
    float[] floats = new float[length];
    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    for (int i = 0; i < length; i++) {
      floats[i] = buffer.getFloat();
    }
    return floats;
  }

} 