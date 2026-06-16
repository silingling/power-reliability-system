package com.powerreliability.common.annotation;

import java.lang.annotation.*;

/**
 * Excel 导出字段注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Excel {

    /** 列名称 */
    String name() default "";

    /** 列排序（数值越小越靠前） */
    int sort() default 0;

    /** 列宽度（字符数） */
    int width() default 18;

    /** 日期格式，仅对日期类型有效 */
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";

    /** 数值千分位格式化 */
    boolean useThousandsSeparator() default false;
}
