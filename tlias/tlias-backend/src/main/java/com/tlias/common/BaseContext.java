package com.tlias.common;

/**
 * 基于 ThreadLocal 保存当前登录员工 ID，方便业务层与日志切面获取操作人
 */
public class BaseContext {

    private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentId(Integer id) {
        THREAD_LOCAL.set(id);
    }

    public static Integer getCurrentId() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
