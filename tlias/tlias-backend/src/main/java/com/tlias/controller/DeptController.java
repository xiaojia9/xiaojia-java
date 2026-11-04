package com.tlias.controller;

import com.tlias.annotation.Log;
import com.tlias.common.Result;
import com.tlias.entity.Dept;
import com.tlias.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理
 */
@RestController
@RequestMapping("/depts")
@RequiredArgsConstructor
public class DeptController {

    private final DeptService deptService;

    /**
     * 查询全部部门
     */
    @GetMapping
    public Result<List<Dept>> list() {
        return Result.success(deptService.list());
    }

    /**
     * 根据 ID 删除部门
     */
    @Log
    @DeleteMapping
    public Result<Void> delete(@RequestParam Integer id) {
        deptService.delete(id);
        return Result.success();
    }

    /**
     * 新增部门
     */
    @Log
    @PostMapping
    public Result<Void> add(@RequestBody Dept dept) {
        deptService.add(dept);
        return Result.success();
    }

    /**
     * 根据 ID 查询部门
     */
    @GetMapping("/{id}")
    public Result<Dept> getById(@PathVariable Integer id) {
        return Result.success(deptService.getById(id));
    }

    /**
     * 修改部门
     */
    @Log
    @PutMapping
    public Result<Void> update(@RequestBody Dept dept) {
        deptService.update(dept);
        return Result.success();
    }
}
