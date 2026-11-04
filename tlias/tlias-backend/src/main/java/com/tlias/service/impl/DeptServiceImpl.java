package com.tlias.service.impl;

import com.tlias.common.BizException;
import com.tlias.entity.Dept;
import com.tlias.mapper.DeptMapper;
import com.tlias.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门业务实现
 */
@Service
@RequiredArgsConstructor
public class DeptServiceImpl implements DeptService {

    private final DeptMapper deptMapper;

    @Override
    public List<Dept> list() {
        return deptMapper.list();
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (id == null) {
            throw new BizException("部门ID不能为空");
        }
        if (deptMapper.countById(id) == 0) {
            throw new BizException("部门不存在");
        }
        deptMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void add(Dept dept) {
        if (dept == null || dept.getName() == null || dept.getName().trim().isEmpty()) {
            throw new BizException("部门名称不能为空");
        }
        dept.setName(dept.getName().trim());
        LocalDateTime now = LocalDateTime.now();
        dept.setCreateTime(now);
        dept.setUpdateTime(now);
        deptMapper.insert(dept);
    }

    @Override
    public Dept getById(Integer id) {
        Dept dept = deptMapper.getById(id);
        if (dept == null) {
            throw new BizException("部门不存在");
        }
        return dept;
    }

    @Override
    @Transactional
    public void update(Dept dept) {
        if (dept == null || dept.getId() == null) {
            throw new BizException("部门ID不能为空");
        }
        if (dept.getName() == null || dept.getName().trim().isEmpty()) {
            throw new BizException("部门名称不能为空");
        }
        if (deptMapper.countById(dept.getId()) == 0) {
            throw new BizException("部门不存在");
        }
        dept.setName(dept.getName().trim());
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.update(dept);
    }
}
