package com.hz.boot01.config;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.context.request.WebRequest;

import java.util.Locale;
/**
 * 请求参数处理
 * 用来判断conreoller接收参数是否为空字符串  若为空 则转换为null
 */
@ControllerAdvice
public class WebMvcControllerAdvice {
    @InitBinder
    public void initBinder(WebDataBinder binder, WebRequest webRequest, Locale locale) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
