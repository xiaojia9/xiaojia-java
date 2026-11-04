package com.tlias.entity;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 操作日志
 */
@Data
public class LogItem implements Serializable {

    private Integer id;
    private Integer operateEmpId;
    private LocalDateTime operateTime;
    private String className;
    private String methodName;
    private String methodParams;
    private String returnValue;
    private Long costTime;

    /** 操作人姓名（关联查询，非表字段） */
    private String operateEmpName;
}
