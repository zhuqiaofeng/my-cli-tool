package com.hz.boot01.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author 12259
 */
@Data
@AllArgsConstructor
public class JsonMassage<T> {
    // 错误代码  0代码成功
    private Integer code;
    private String msg;
    //总条数
    private Integer count;
    //数据
    private T data;

    // 手动加无参构造函数，避免 Lombok 不生效
    public JsonMassage() {
    }
}
