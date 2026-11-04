package com.tlias.controller;

import com.tlias.annotation.Log;
import com.tlias.common.PageResult;
import com.tlias.common.Result;
import com.tlias.entity.Clazz;
import com.tlias.service.ClazzService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 班级管理
 */
@RestController
@RequestMapping("/clazzs")
@RequiredArgsConstructor
public class ClazzController {

    private final ClazzService clazzService;

    /**
     * 条件分页查询（begin/end 匹配结课时间）
     */
    @GetMapping
    public Result<PageResult<Clazz>> page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(clazzService.page(name, begin, end, page, pageSize));
    }

    /**
     * 查询全部班级
     */
    @GetMapping("/list")
    public Result<List<Clazz>> list() {
        return Result.success(clazzService.list());
    }

    /**
     * 根据 ID 删除班级
     */
    @Log
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        clazzService.delete(id);
        return Result.success();
    }

    /**
     * 新增班级
     */
    @Log
    @PostMapping
    public Result<Void> add(@RequestBody Clazz clazz) {
        clazzService.add(clazz);
        return Result.success();
    }

    /**
     * 根据 ID 查询班级
     */
    @GetMapping("/{id}")
    public Result<Clazz> getById(@PathVariable Integer id) {
        return Result.success(clazzService.getById(id));
    }

    /**
     * 修改班级
     */
    @Log
    @PutMapping
    public Result<Void> update(@RequestBody Clazz clazz) {
        clazzService.update(clazz);
        return Result.success();
    }
}
