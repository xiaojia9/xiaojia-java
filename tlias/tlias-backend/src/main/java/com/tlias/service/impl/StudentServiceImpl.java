package com.tlias.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tlias.common.BizException;
import com.tlias.common.PageResult;
import com.tlias.entity.Student;
import com.tlias.mapper.StudentMapper;
import com.tlias.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 学员业务实现
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    @Override
    public PageResult<Student> page(String name, Integer degree, Integer clazzId, Integer page, Integer pageSize) {
        PageHelper.startPage(page == null || page < 1 ? 1 : page, pageSize == null || pageSize < 1 ? 10 : pageSize);
        List<Student> list = studentMapper.page(name, degree, clazzId);
        Page<Student> p = (Page<Student>) list;
        return PageResult.of(p.getTotal(), p.getResult());
    }

    @Override
    @Transactional
    public void delete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择要删除的学员");
        }
        studentMapper.deleteByIds(ids);
    }

    @Override
    @Transactional
    public void add(Student student) {
        validate(student);
        if (studentMapper.countByNo(student.getNo()) > 0) {
            throw new BizException("学号已存在");
        }
        if (student.getViolationCount() == null) {
            student.setViolationCount(0);
        }
        if (student.getViolationScore() == null) {
            student.setViolationScore(0);
        }
        if (student.getIsCollege() == null) {
            student.setIsCollege(0);
        }
        LocalDateTime now = LocalDateTime.now();
        student.setCreateTime(now);
        student.setUpdateTime(now);
        studentMapper.insert(student);
    }

    @Override
    public Student getById(Integer id) {
        Student student = studentMapper.getById(id);
        if (student == null) {
            throw new BizException("学员不存在");
        }
        return student;
    }

    @Override
    @Transactional
    public void update(Student student) {
        if (student == null || student.getId() == null) {
            throw new BizException("学员ID不能为空");
        }
        if (studentMapper.getById(student.getId()) == null) {
            throw new BizException("学员不存在");
        }
        validate(student);
        if (studentMapper.countByNoExcludeId(student.getNo(), student.getId()) > 0) {
            throw new BizException("学号已存在");
        }
        student.setUpdateTime(LocalDateTime.now());
        studentMapper.update(student);
    }

    @Override
    @Transactional
    public void violation(Integer id, Integer score) {
        if (id == null) {
            throw new BizException("学员ID不能为空");
        }
        if (score == null || score <= 0) {
            throw new BizException("违纪扣分必须大于 0");
        }
        if (studentMapper.getById(id) == null) {
            throw new BizException("学员不存在");
        }
        studentMapper.updateViolation(id, score);
    }

    private void validate(Student student) {
        if (student == null) {
            throw new BizException("学员信息不能为空");
        }
        if (student.getName() == null || student.getName().trim().isEmpty()) {
            throw new BizException("学员姓名不能为空");
        }
        if (student.getNo() == null || student.getNo().trim().isEmpty()) {
            throw new BizException("学号不能为空");
        }
        if (student.getGender() == null) {
            throw new BizException("性别不能为空");
        }
        if (student.getPhone() == null || student.getPhone().trim().isEmpty()) {
            throw new BizException("手机号不能为空");
        }
        if (student.getDegree() == null) {
            throw new BizException("学历不能为空");
        }
        if (student.getClazzId() == null) {
            throw new BizException("班级不能为空");
        }
        student.setName(student.getName().trim());
        student.setNo(student.getNo().trim());
        student.setPhone(student.getPhone().trim());
    }
}
