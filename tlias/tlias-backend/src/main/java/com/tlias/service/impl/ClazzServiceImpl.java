package com.tlias.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tlias.common.BizException;
import com.tlias.common.PageResult;
import com.tlias.entity.Clazz;
import com.tlias.mapper.ClazzMapper;
import com.tlias.service.ClazzService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 班级业务实现
 */
@Service
@RequiredArgsConstructor
public class ClazzServiceImpl implements ClazzService {

    private final ClazzMapper clazzMapper;

    @Override
    public PageResult<Clazz> page(String name, LocalDate begin, LocalDate end, Integer page, Integer pageSize) {
        PageHelper.startPage(page == null || page < 1 ? 1 : page, pageSize == null || pageSize < 1 ? 10 : pageSize);
        List<Clazz> list = clazzMapper.page(name, begin, end);
        Page<Clazz> p = (Page<Clazz>) list;
        List<Clazz> rows = p.getResult();
        rows.forEach(this::fillStatus);
        return PageResult.of(p.getTotal(), rows);
    }

    @Override
    public List<Clazz> list() {
        List<Clazz> list = clazzMapper.listAll();
        list.forEach(this::fillStatus);
        return list;
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (id == null) {
            throw new BizException("班级ID不能为空");
        }
        if (clazzMapper.getById(id) == null) {
            throw new BizException("班级不存在");
        }
        if (clazzMapper.countStudentByClazzId(id) > 0) {
            throw new BizException("该班级下存在学员，无法删除");
        }
        clazzMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void add(Clazz clazz) {
        validate(clazz);
        LocalDateTime now = LocalDateTime.now();
        clazz.setCreateTime(now);
        clazz.setUpdateTime(now);
        clazzMapper.insert(clazz);
    }

    @Override
    public Clazz getById(Integer id) {
        Clazz clazz = clazzMapper.getById(id);
        if (clazz == null) {
            throw new BizException("班级不存在");
        }
        fillStatus(clazz);
        return clazz;
    }

    @Override
    @Transactional
    public void update(Clazz clazz) {
        if (clazz == null || clazz.getId() == null) {
            throw new BizException("班级ID不能为空");
        }
        if (clazzMapper.getById(clazz.getId()) == null) {
            throw new BizException("班级不存在");
        }
        validate(clazz);
        clazz.setUpdateTime(LocalDateTime.now());
        clazzMapper.update(clazz);
    }

    /**
     * 根据当前日期计算班级状态：未开班 / 已开班 / 已结课
     */
    private void fillStatus(Clazz clazz) {
        LocalDate today = LocalDate.now();
        if (clazz.getBeginDate() != null && today.isBefore(clazz.getBeginDate())) {
            clazz.setStatus("未开班");
        } else if (clazz.getEndDate() != null && today.isAfter(clazz.getEndDate())) {
            clazz.setStatus("已结课");
        } else {
            clazz.setStatus("已开班");
        }
    }

    private void validate(Clazz clazz) {
        if (clazz == null) {
            throw new BizException("班级信息不能为空");
        }
        if (clazz.getName() == null || clazz.getName().trim().isEmpty()) {
            throw new BizException("班级名称不能为空");
        }
        if (clazz.getBeginDate() == null || clazz.getEndDate() == null) {
            throw new BizException("开课时间与结课时间不能为空");
        }
        if (clazz.getEndDate().isBefore(clazz.getBeginDate())) {
            throw new BizException("结课时间不能早于开课时间");
        }
        clazz.setName(clazz.getName().trim());
    }
}
