package com.tlias.service;

import com.tlias.common.PageResult;
import com.tlias.entity.Student;

import java.util.List;

/**
 * 学员业务
 */
public interface StudentService {

    PageResult<Student> page(String name, Integer degree, Integer clazzId, Integer page, Integer pageSize);

    void delete(List<Integer> ids);

    void add(Student student);

    Student getById(Integer id);

    void update(Student student);

    void violation(Integer id, Integer score);
}
