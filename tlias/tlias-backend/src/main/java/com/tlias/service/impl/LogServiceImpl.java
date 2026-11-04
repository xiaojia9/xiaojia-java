package com.tlias.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tlias.common.PageResult;
import com.tlias.entity.LogItem;
import com.tlias.mapper.LogMapper;
import com.tlias.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志业务实现
 */
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogMapper logMapper;

    @Override
    public PageResult<LogItem> page(Integer page, Integer pageSize) {
        PageHelper.startPage(page == null || page < 1 ? 1 : page, pageSize == null || pageSize < 1 ? 10 : pageSize);
        List<LogItem> list = logMapper.page();
        Page<LogItem> p = (Page<LogItem>) list;
        return PageResult.of(p.getTotal(), p.getResult());
    }
}
