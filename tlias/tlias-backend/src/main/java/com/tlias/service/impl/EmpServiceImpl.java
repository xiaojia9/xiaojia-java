package com.tlias.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.tlias.common.BizException;
import com.tlias.common.PageResult;
import com.tlias.dto.LoginDTO;
import com.tlias.entity.Emp;
import com.tlias.entity.EmpExpr;
import com.tlias.mapper.EmpExprMapper;
import com.tlias.mapper.EmpMapper;
import com.tlias.service.EmpService;
import com.tlias.utils.JwtUtils;
import com.tlias.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工业务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmpServiceImpl implements EmpService {

    private static final String DEFAULT_PASSWORD = "123456";

    private final EmpMapper empMapper;
    private final EmpExprMapper empExprMapper;
    private final JwtUtils jwtUtils;

    @Override
    public PageResult<Emp> page(String name, Integer gender, LocalDate begin, LocalDate end,
                                Integer page, Integer pageSize) {
        PageHelper.startPage(page == null || page < 1 ? 1 : page, pageSize == null || pageSize < 1 ? 10 : pageSize);
        List<Emp> list = empMapper.page(name, gender, begin, end);
        Page<Emp> p = (Page<Emp>) list;
        return PageResult.of(p.getTotal(), p.getResult());
    }

    @Override
    public List<Emp> list() {
        return empMapper.listAll();
    }

    @Override
    @Transactional
    public void delete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException("请选择要删除的员工");
        }
        empExprMapper.deleteByEmpIds(ids);
        empMapper.deleteByIds(ids);
    }

    @Override
    @Transactional
    public void add(Emp emp) {
        validate(emp, false);
        if (empMapper.countByUsername(emp.getUsername()) > 0) {
            throw new BizException("用户名已存在");
        }
        if (emp.getPassword() == null || emp.getPassword().isEmpty()) {
            emp.setPassword(DEFAULT_PASSWORD);
        }
        LocalDateTime now = LocalDateTime.now();
        emp.setCreateTime(now);
        emp.setUpdateTime(now);
        empMapper.insert(emp);

        List<EmpExpr> exprList = emp.getExprList();
        if (exprList != null && !exprList.isEmpty()) {
            exprList.forEach(expr -> expr.setEmpId(emp.getId()));
            empExprMapper.insertBatch(exprList);
        }
    }

    @Override
    public Emp getById(Integer id) {
        Emp emp = empMapper.getById(id);
        if (emp == null) {
            throw new BizException("员工不存在");
        }
        emp.setExprList(empExprMapper.listByEmpId(id));
        return emp;
    }

    @Override
    @Transactional
    public void update(Emp emp) {
        if (emp == null || emp.getId() == null) {
            throw new BizException("员工ID不能为空");
        }
        validate(emp, true);
        if (empMapper.getById(emp.getId()) == null) {
            throw new BizException("员工不存在");
        }
        if (empMapper.countByUsernameExcludeId(emp.getUsername(), emp.getId()) > 0) {
            throw new BizException("用户名已存在");
        }
        if (emp.getPassword() == null || emp.getPassword().isEmpty()) {
            Emp old = empMapper.getById(emp.getId());
            emp.setPassword(old.getPassword());
        }
        emp.setUpdateTime(LocalDateTime.now());
        empMapper.update(emp);

        // 工作经历：先删后加
        empExprMapper.deleteByEmpIds(List.of(emp.getId()));
        List<EmpExpr> exprList = emp.getExprList();
        if (exprList != null && !exprList.isEmpty()) {
            exprList.forEach(expr -> expr.setEmpId(emp.getId()));
            empExprMapper.insertBatch(exprList);
        }
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getPassword() == null) {
            throw new BizException("用户名或密码错误");
        }
        List<Emp> empList = empMapper.listByUsername(loginDTO.getUsername().trim());
        if (empList.isEmpty()) {
            throw new BizException("用户名或密码错误");
        }
        Emp emp = empList.get(0);
        if (!loginDTO.getPassword().equals(emp.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        String token = jwtUtils.createToken(emp.getId(), emp.getUsername());
        log.info("员工登录成功: {} ({})", emp.getName(), emp.getUsername());
        return new LoginVO(emp.getId(), emp.getUsername(), emp.getName(), token);
    }

    private void validate(Emp emp, boolean update) {
        if (emp == null) {
            throw new BizException("员工信息不能为空");
        }
        if (emp.getName() == null || emp.getName().trim().isEmpty()) {
            throw new BizException("员工姓名不能为空");
        }
        if (emp.getUsername() == null || emp.getUsername().trim().isEmpty()) {
            throw new BizException("用户名不能为空");
        }
        if (emp.getGender() == null) {
            throw new BizException("性别不能为空");
        }
        if (update && emp.getId() == null) {
            throw new BizException("员工ID不能为空");
        }
        emp.setName(emp.getName().trim());
        emp.setUsername(emp.getUsername().trim());
    }
}
