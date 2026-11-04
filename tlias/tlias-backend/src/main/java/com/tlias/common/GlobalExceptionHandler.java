package com.tlias.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>所有异常在这里统一处理，返回统一响应结果 Result，保证前端拿到的永远是 {@code {code,msg,data}}。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    /** 参数校验异常（@RequestBody + @Valid） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", msg);
        return Result.error(msg.isEmpty() ? "参数校验失败" : msg);
    }

    /** 参数校验异常（表单对象绑定） */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", msg);
        return Result.error(msg.isEmpty() ? "参数绑定失败" : msg);
    }

    /** 唯一索引冲突，例如部门名称重复 */
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<Void> handleDuplicateKeyException(DuplicateKeyException e) {
        log.warn("唯一约束冲突: {}", e.getMessage());
        String message = e.getMessage() == null ? "" : e.getMessage();
        if (message.contains("uk_dept_name")) {
            return Result.error("部门名称已存在");
        }
        if (message.contains("uk_emp_username")) {
            return Result.error("用户名已存在");
        }
        if (message.contains("uk_student_no")) {
            return Result.error("学号已存在");
        }
        return Result.error("数据已存在，请勿重复添加");
    }

    /** 请求参数缺失 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParameter(MissingServletRequestParameterException e) {
        return Result.error("缺少必要参数: " + e.getParameterName());
    }

    /** 参数类型不匹配 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error("参数格式不正确: " + e.getName());
    }

    /** 请求体格式错误 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleMessageNotReadable(HttpMessageNotReadableException e) {
        return Result.error("请求参数格式错误");
    }

    /** 请求方式不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error("不支持的请求方式: " + e.getMethod());
    }

    /** 上传文件超出限制 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        return Result.error("上传文件超过大小限制（最大 10MB）");
    }

    /** 404 */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result<Void> handleNotFound(NoHandlerFoundException e) {
        return Result.error("请求的资源不存在: " + e.getRequestURL());
    }

    /** 静态资源（例如上传的图片）不存在 */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResource(NoResourceFoundException e) {
        return Result.error("请求的文件不存在: " + e.getResourcePath());
    }

    /** 兜底异常 */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        String message = e.getMessage();
        if (message != null && message.contains("Access denied")) {
            return Result.error("数据库访问被拒绝，请检查数据库账号密码");
        }
        return Result.error("系统繁忙，请稍后重试");
    }
}
