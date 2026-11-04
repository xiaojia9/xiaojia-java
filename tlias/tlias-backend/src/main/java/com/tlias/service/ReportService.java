package com.tlias.service;

import com.tlias.vo.ClazzReportVO;
import com.tlias.vo.JobReportVO;
import com.tlias.vo.NameValueVO;

import java.util.List;

/**
 * 统计报表业务
 */
public interface ReportService {

    /** 员工性别分布 */
    List<NameValueVO> empGenderData();

    /** 员工职位分布 */
    JobReportVO empJobData();

    /** 学员学历分布 */
    List<NameValueVO> studentDegreeData();

    /** 班级人数统计 */
    ClazzReportVO studentCountData();
}
