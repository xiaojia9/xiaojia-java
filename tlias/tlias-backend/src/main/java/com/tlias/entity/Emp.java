package com.tlias.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 员工
 */
@Data
public class Emp implements Serializable {

    private Integer id;
    private String username;
    private String password;
    private String name;
    /** 性别 1男 2女 */
    private Integer gender;
    private String image;
    /** 职位 1班主任 2讲师 3学工主管 4教研主管 5咨询师 */
    private Integer job;
    private BigDecimal salary;
    private LocalDate entryDate;
    private Integer deptId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 部门名称（关联查询，非表字段） */
    private String deptName;
    /** 工作经历列表（关联查询，非表字段） */
    private List<EmpExpr> exprList;
}
