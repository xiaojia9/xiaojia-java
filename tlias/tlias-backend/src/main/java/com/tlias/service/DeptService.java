package com.tlias.service;

import com.tlias.entity.Dept;

import java.util.List;

/**
 * 部门业务
 */
public interface DeptService {

    List<Dept> list();

    void delete(Integer id);

    void add(Dept dept);

    Dept getById(Integer id);

    void update(Dept dept);
}
