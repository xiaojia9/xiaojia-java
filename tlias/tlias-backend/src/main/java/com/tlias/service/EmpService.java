package com.tlias.service;

import com.tlias.common.PageResult;
import com.tlias.dto.LoginDTO;
import com.tlias.entity.Emp;
import com.tlias.vo.LoginVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工业务
 */
public interface EmpService {

    PageResult<Emp> page(String name, Integer gender, LocalDate begin, LocalDate end, Integer page, Integer pageSize);

    List<Emp> list();

    void delete(List<Integer> ids);

    void add(Emp emp);

    Emp getById(Integer id);

    void update(Emp emp);

    LoginVO login(LoginDTO loginDTO);
}
