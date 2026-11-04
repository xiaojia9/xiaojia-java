package com.tlias.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 * <p>code: 1 成功, 0 失败；msg: 提示信息；data: 数据</p>
 */
@Data
public class Result<T> implements Serializable {

    /** 成功 */
    public static final int SUCCESS = 1;
    /** 失败 */
    public static final int ERROR = 0;

    private Integer code;
    private String msg;
    private T data;

    public Result() {
    }

    public Result(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS, "success", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS, "success", data);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(SUCCESS, msg, data);
    }

    public static <T> Result<T> error() {
        return new Result<>(ERROR, "操作失败", null);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(ERROR, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
