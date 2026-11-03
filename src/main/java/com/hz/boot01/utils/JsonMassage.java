package com.hz.boot01.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JsonMassage<T> {
    private Integer code; // 错误代码  0代码成功
    private String msg;
    private Integer count;//总条数
    private T data; //数据
}
