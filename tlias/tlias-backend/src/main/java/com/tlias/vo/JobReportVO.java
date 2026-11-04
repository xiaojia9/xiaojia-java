package com.tlias.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 职位统计返回结果：{jobList:[], dataList:[]}
 */
@Data
public class JobReportVO implements Serializable {

    private List<String> jobList;
    private List<Long> dataList;
}
