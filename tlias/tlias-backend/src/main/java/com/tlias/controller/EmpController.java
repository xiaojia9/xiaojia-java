package com.tlias.controller;

import com.tlias.annotation.Log;
import com.tlias.common.PageResult;
import com.tlias.common.Result;
import com.tlias.entity.Emp;
import com.tlias.service.EmpService;
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
 * 员工管理
 */
@RestController
@RequestMapping("/emps")
@RequiredArgsConstructor
public class EmpController {

    private final EmpService empService;

    /**
     * 条件分页查询
     */
    @GetMapping
    public Result<PageResult<Emp>> page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate begin,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(empService.page(name, gender, begin, end, page, pageSize));
    }

    /**
     * 查询全部员工（用于班主任下拉）
     */
    @GetMapping("/list")
    public Result<List<Emp>> list() {
        return Result.success(empService.list());
    }

    /**
     * 批量删除员工：DELETE /emps?ids=1,2,3
     */
    @Log
    @DeleteMapping
    public Result<Void> delete(@RequestParam List<Integer> ids) {
        empService.delete(ids);
        return Result.success();
    }

    /**
     * 新增员工（含工作经历）
     */
    @Log
    @PostMapping
    public Result<Void> add(@RequestBody Emp emp) {
        empService.add(emp);
        return Result.success();
    }

    /**
     * 根据 ID 查询员工（含工作经历）
     */
    @GetMapping("/{id}")
    public Result<Emp> getById(@PathVariable Integer id) {
        return Result.success(empService.getById(id));
    }

    /**
     * 修改员工（含工作经历）
     */
    @Log
    @PutMapping
    public Result<Void> update(@RequestBody Emp emp) {
        empService.update(emp);
        return Result.success();
    }
}
