package com.tlias.controller;

import com.tlias.common.Result;
import com.tlias.service.ReportService;
import com.tlias.vo.ClazzReportVO;
import com.tlias.vo.JobReportVO;
import com.tlias.vo.NameValueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 统计报表
 */
@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * 员工性别分布
     */
    @GetMapping("/empGenderData")
    public Result<List<NameValueVO>> empGenderData() {
        return Result.success(reportService.empGenderData());
    }

    /**
     * 员工职位分布
     */
    @GetMapping("/empJobData")
    public Result<JobReportVO> empJobData() {
        return Result.success(reportService.empJobData());
    }

    /**
     * 学员学历分布
     */
    @GetMapping("/studentDegreeData")
    public Result<List<NameValueVO>> studentDegreeData() {
        return Result.success(reportService.studentDegreeData());
    }

    /**
     * 班级人数统计
     */
    @GetMapping("/studentCountData")
    public Result<ClazzReportVO> studentCountData() {
        return Result.success(reportService.studentCountData());
    }
}
