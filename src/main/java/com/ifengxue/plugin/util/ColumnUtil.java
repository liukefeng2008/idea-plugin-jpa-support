package com.ifengxue.plugin.util;

import com.ifengxue.plugin.entity.Column;
import com.ifengxue.plugin.entity.ColumnSchema;
import com.ifengxue.plugin.entity.ColumnSchemaExtension;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ColumnUtil {

  public static Column columnSchemaToColumn(ColumnSchema columnSchema, String removePrefixes,
      boolean useWrapper, boolean useJava8DateType) {
    if (!(columnSchema instanceof ColumnSchemaExtension)) {
      throw new IllegalStateException(
          columnSchema.getClass().getName() + " is not instance of " + ColumnSchemaExtension.class
              .getName());
    }
    ColumnSchemaExtension<?> extension = (ColumnSchemaExtension<?>) columnSchema;
    Column column = new Column();
    column.setColumnName(columnSchema.getColumnName());
    column.setSort(columnSchema.getOrdinalPosition());
    column.setDbDataType(columnSchema.getDataType());
    column.setPrimary(extension.primary());
    column.setNullable(extension.nullable());
    column.setAutoIncrement(extension.autoIncrement());
    column.setColumnComment(columnSchema.getColumnComment());
    column.setDefaultValue(columnSchema.getColumnDefault());
    column.setJavaDataType(extension.javaTypeClass());
    column.setJdbcType(extension.jdbcType());
    column.setJdbcTypeName(extension.jdbcTypeName());
    column.setSequenceColumn(extension.sequenceColumn());
    ColumnUtil.parseColumn(column, removePrefixes, useWrapper, useJava8DateType);
    return column;
  }

  public static void parseColumn(Column column, String removePrefixes, boolean useWrapper,
      boolean useJava8DateType) {
    column.setFieldName(StringHelper.parseFieldName(column.getColumnName(), removePrefixes));
    Class<?> javaDataType = StringHelper.parseJavaDataType(column.getJavaDataType(),
        column.getJdbcTypeName(), column.getDbDataType(), column.getColumnName(), useWrapper,
        useJava8DateType);
    if ((javaDataType == Integer.class || javaDataType == int.class)
        && (column.getColumnComment().contains("true") || column.getColumnComment()
        .contains("false"))) {
      if (useWrapper) {
        javaDataType = Boolean.class;
      } else {
        javaDataType = boolean.class;
      }
    }
    column.setJavaDataType(javaDataType);
    if (column.getDefaultValue() != null) {
      if (javaDataType == String.class) {
        column.setDefaultValue("\"" + column.getDefaultValue() + "\"");
      }
      Class<?> primitiveClass = StringHelper.getPrimitiveClass(javaDataType);
      if (primitiveClass == long.class) {
        column.setDefaultValue(column.getDefaultValue() + "L");
      }
      if (primitiveClass == float.class) {
        column.setDefaultValue(column.getDefaultValue() + "F");
      }
      if (primitiveClass == double.class) {
        column.setDefaultValue(column.getDefaultValue() + "D");
      }
      if (primitiveClass == boolean.class) {
        if (column.getDefaultValue().equals("1")) {
          if (useWrapper) {
            column.setDefaultValue("Boolean.TRUE");
          } else {
            column.setDefaultValue("true");
          }
        } else {
          if (useWrapper) {
            column.setDefaultValue("Boolean.FALSE");
          } else {
            column.setDefaultValue("false");
          }
        }
      }
      if (javaDataType == BigDecimal.class) {
        BigDecimal amount = new BigDecimal(column.getDefaultValue());
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
          column.setDefaultValue("BigDecimal.ZERO");
        } else if (amount.compareTo(BigDecimal.ONE) == 0) {
          column.setDefaultValue("BigDecimal.ONE");
        } else if (amount.compareTo(BigDecimal.TEN) == 0) {
          column.setDefaultValue("BigDecimal.TEN");
        } else {
          column.setDefaultValue("new BigDecimal(\"" + column.getDefaultValue() + "\")");
        }
      }
      column.setHasDefaultValue(true);
      if (javaDataType == java.util.Date.class ||
          javaDataType == java.sql.Date.class ||
          javaDataType == java.sql.Timestamp.class ||
          javaDataType == LocalDateTime.class) {
        if (isNow(column.getDefaultValue())) {
          if (javaDataType == java.util.Date.class) {
            column.setDefaultValue("new Date()");
          } else if (javaDataType == java.sql.Date.class) {
            column.setDefaultValue("new Date(System.currentTimeMillis())");
          } else if (javaDataType == Timestamp.class) {
            column.setDefaultValue("new Timestamp(System.currentTimeMillis())");
          } else {
            column.setDefaultValue("LocalDateTime.now()");
          }
        } else {
          LocalDateTime dateTime = tryParseDateTime(column.getDefaultValue());
          if (dateTime == null) {
            column.setDefaultValue(null);
            column.setHasDefaultValue(false);
          } else {
            Timestamp timestamp = Timestamp.valueOf(dateTime);
            if (javaDataType == java.util.Date.class || javaDataType == java.sql.Date.class) {
              column.setDefaultValue("new Date(" + timestamp.getTime() + "L)");
            } else if (javaDataType == Timestamp.class) {
              column.setDefaultValue("new Timestamp(" + timestamp.getTime() + "L)");
            } else {
              column.setDefaultValue("new Timestamp(" + timestamp.getTime() + "L).toLocalDateTime()");
            }
          }
        }
      }

      if (javaDataType == LocalDate.class || javaDataType == LocalTime.class) {
        column.setDefaultValue(null);
        column.setHasDefaultValue(false);
      }
    }
  }

  private static boolean isNow(String value) {
    return "CURRENT_TIMESTAMP".equals(value);
  }

  private static LocalDateTime tryParseDateTime(String value) {
    try {
      return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } catch (Exception ex) {
      return null;
    }
  }
}
