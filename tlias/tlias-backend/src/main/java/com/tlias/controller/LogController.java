package com.tlias.controller;

import com.tlias.common.PageResult;
import com.tlias.common.Result;
import com.tlias.entity.LogItem;
import com.tlias.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志
 */
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    /**
     * 分页查询操作日志
     */
    @GetMapping("/page")
    public Result<PageResult<LogItem>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(logService.page(page, pageSize));
    }
}
