package com.tlias.service.impl;

import com.tlias.mapper.ReportMapper;
import com.tlias.service.ReportService;
import com.tlias.vo.ClazzReportVO;
import com.tlias.vo.JobReportVO;
import com.tlias.vo.NameValueVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计报表业务实现
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    /** 职位编号 -> 名称 */
    private static final Map<Integer, String> JOB_MAP = new LinkedHashMap<>();
    /** 学历编号 -> 名称 */
    private static final Map<Integer, String> DEGREE_MAP = new LinkedHashMap<>();

    static {
        JOB_MAP.put(1, "班主任");
        JOB_MAP.put(2, "讲师");
        JOB_MAP.put(3, "学工主管");
        JOB_MAP.put(4, "教研主管");
        JOB_MAP.put(5, "咨询师");

        DEGREE_MAP.put(1, "初中");
        DEGREE_MAP.put(2, "高中");
        DEGREE_MAP.put(3, "大专");
        DEGREE_MAP.put(4, "本科");
        DEGREE_MAP.put(5, "硕士");
        DEGREE_MAP.put(6, "博士");
    }

    private final ReportMapper reportMapper;

    @Override
    public List<NameValueVO> empGenderData() {
        List<NameValueVO> result = new ArrayList<>();
        for (Map<String, Object> row : reportMapper.countEmpByGender()) {
            String name = toStr(row.get("name"));
            if (name == null) {
                name = "其他";
            }
            result.add(new NameValueVO(name, toLong(row.get("value"))));
        }
        return result;
    }

    @Override
    public JobReportVO empJobData() {
        // 职位编号 -> 人数
        Map<Integer, Long> counts = new LinkedHashMap<>();
        long other = 0L;
        for (Map<String, Object> row : reportMapper.countEmpByJob()) {
            Object rawJob = row.get("name");
            long count = toLong(row.get("value"));
            if (rawJob == null) {
                other += count;
            } else {
                Integer job = toInt(rawJob);
                if (job == null || !JOB_MAP.containsKey(job)) {
                    other += count;
                } else {
                    counts.put(job, count);
                }
            }
        }

        // 文档示例顺序：教研主管、学工主管、其他、班主任、咨询师、讲师
        List<String> jobList = new ArrayList<>();
        List<Long> dataList = new ArrayList<>();
        jobList.add("教研主管");
        dataList.add(counts.getOrDefault(4, 0L));
        jobList.add("学工主管");
        dataList.add(counts.getOrDefault(3, 0L));
        jobList.add("其他");
        dataList.add(other);
        jobList.add("班主任");
        dataList.add(counts.getOrDefault(1, 0L));
        jobList.add("咨询师");
        dataList.add(counts.getOrDefault(5, 0L));
        jobList.add("讲师");
        dataList.add(counts.getOrDefault(2, 0L));

        JobReportVO vo = new JobReportVO();
        vo.setJobList(jobList);
        vo.setDataList(dataList);
        return vo;
    }

    @Override
    public List<NameValueVO> studentDegreeData() {
        Map<Integer, Long> counts = new LinkedHashMap<>();
        for (Map<String, Object> row : reportMapper.countStudentByDegree()) {
            Integer degree = toInt(row.get("name"));
            if (degree != null) {
                counts.put(degree, toLong(row.get("value")));
            }
        }
        List<NameValueVO> result = new ArrayList<>();
        DEGREE_MAP.forEach((degree, name) ->
                result.add(new NameValueVO(name, counts.getOrDefault(degree, 0L))));
        return result;
    }

    @Override
    public ClazzReportVO studentCountData() {
        List<String> clazzList = new ArrayList<>();
        List<Long> dataList = new ArrayList<>();
        for (Map<String, Object> row : reportMapper.countStudentByClazz()) {
            clazzList.add(toStr(row.get("name")));
            dataList.add(toLong(row.get("value")));
        }
        ClazzReportVO vo = new ClazzReportVO();
        vo.setClazzList(clazzList);
        vo.setDataList(dataList);
        return vo;
    }

    private String toStr(Object value) {
        return value == null ? null : value.toString();
    }

    private Long toLong(Object value) {
        return value instanceof Number number ? number.longValue() : 0L;
    }

    private Integer toInt(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }
}
