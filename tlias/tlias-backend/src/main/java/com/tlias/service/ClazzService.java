package com.tlias.service;

import com.tlias.common.PageResult;
import com.tlias.entity.Clazz;

import java.time.LocalDate;
import java.util.List;

/**
 * 班级业务
 */
public interface ClazzService {

    PageResult<Clazz> page(String name, LocalDate begin, LocalDate end, Integer page, Integer pageSize);

    List<Clazz> list();

    void delete(Integer id);

    void add(Clazz clazz);

    Clazz getById(Integer id);

    void update(Clazz clazz);
}
