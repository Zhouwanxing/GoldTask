package com.zhou.goldtask.exception;

import cn.dev33.satoken.util.SaResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public SaResult fileTooLarge(MaxUploadSizeExceededException e) {
        log.warn("上传超过大小限制", e);
        return SaResult.error("文件超过大小限制");
    }

    // 全局异常拦截
    @ExceptionHandler
    public SaResult handlerException(Exception e) {
        log.warn("", e);
        return SaResult.error(e.getMessage()).setCode(401);
    }
}