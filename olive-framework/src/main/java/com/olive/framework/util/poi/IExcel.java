package com.olive.framework.util.poi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * excel
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface IExcel {

    /**
     * 列名称,不能重复
     * @return
     */
    String name() default "";

    /**
     * 是否是子集
     * @return
     */
    boolean isSon() default false;

    /**
     * 隐藏字段
     * @return
     */
    boolean hiddenField() default false;

    /**
     * 排序号
     * @return
     */
    int sortNo() default 0;

    /**
     * 字段必填,不为空
     * @return
     */
    boolean isNotBlank() default false;

    //处理布尔值的转换，注意：请将表示true的标识写在分割符"|"的左边，表示false的标识写在分割符“|”的右边 ；默认"是|否"
    String booleanFormatter() default "是|否";

    /**
     * 日期格式化
     * @return
     */
    String dateFormat() default IExcelUtils.IEXCEL_DATE_DEFAULT_FORMAT;
}

