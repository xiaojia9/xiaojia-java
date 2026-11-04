package com.tlias.service;

import com.tlias.common.PageResult;
import com.tlias.entity.LogItem;

/**
 * 操作日志业务
 */
public interface LogService {

    PageResult<LogItem> page(Integer page, Integer pageSize);
}
