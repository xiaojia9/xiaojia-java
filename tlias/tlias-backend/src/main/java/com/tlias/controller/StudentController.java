package com.tlias.controller;

import com.tlias.annotation.Log;
import com.tlias.common.PageResult;
import com.tlias.common.Result;
import com.tlias.entity.Student;
import com.tlias.service.StudentService;
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
 * 学员管理
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * 条件分页查询
     */
    @GetMapping
    public Result<PageResult<Student>> page(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer degree,
            @RequestParam(required = false) Integer clazzId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(studentService.page(name, degree, clazzId, page, pageSize));
    }

    /**
     * 批量删除学员：DELETE /students/1,2,3（路径参数）
     */
    @Log
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Integer> ids) {
        studentService.delete(ids);
        return Result.success();
    }

    /**
     * 新增学员
     */
    @Log
    @PostMapping
    public Result<Void> add(@RequestBody Student student) {
        studentService.add(student);
        return Result.success();
    }

    /**
     * 根据 ID 查询学员
     */
    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Integer id) {
        return Result.success(studentService.getById(id));
    }

    /**
     * 修改学员
     */
    @Log
    @PutMapping
    public Result<Void> update(@RequestBody Student student) {
        studentService.update(student);
        return Result.success();
    }

    /**
     * 违纪处理：PUT /students/violation/{id}/{score}
     */
    @Log
    @PutMapping("/violation/{id}/{score}")
    public Result<Void> violation(@PathVariable Integer id, @PathVariable Integer score) {
        studentService.violation(id, score);
        return Result.success();
    }
}
