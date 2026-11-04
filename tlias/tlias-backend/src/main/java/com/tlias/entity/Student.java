package com.tlias.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学员
 */
@Data
public class Student implements Serializable {

    private Integer id;
    private String name;
    private String no;
    /** 性别 1男 2女 */
    private Integer gender;
    private String phone;
    /** 学历 1初中 2高中 3大专 4本科 5硕士 6博士 */
    private Integer degree;
    private String idCard;
    /** 是否院校学生 1是 0否 */
    private Integer isCollege;
    private String address;
    private LocalDate graduationDate;
    private Integer violationCount;
    private Integer violationScore;
    private Integer clazzId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 班级名称（关联查询，非表字段） */
    private String clazzName;
}
