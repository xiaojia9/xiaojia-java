package com.tlias.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 班级人数统计返回结果：{clazzList:[], dataList:[]}
 */
@Data
public class ClazzReportVO implements Serializable {

    private List<String> clazzList;
    private List<Long> dataList;
}
