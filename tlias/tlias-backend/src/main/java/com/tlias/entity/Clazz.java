package com.tlias.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 班级
 */
@Data
public class Clazz implements Serializable {

    private Integer id;
    private String name;
    private String room;
    private LocalDate beginDate;
    private LocalDate endDate;
    private Integer masterId;
    /** 学科 1java 2前端 3大数据 4Python 5Go 6嵌入式 */
    private Integer subject;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 班主任姓名（关联查询，非表字段） */
    private String masterName;
    /** 班级状态：未开班 / 已开班 / 已结课（计算字段，非表字段） */
    private String status;
}
