package com.tlias.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 统计报表数据访问
 */
@Mapper
public interface ReportMapper {

    /**
     * 员工性别统计：name（男性员工/女性员工/其他）, value
     */
    @Select("""
            select case gender when 1 then '男性员工' when 2 then '女性员工' else '其他' end as name,
                   count(*) as value
            from emp
            group by gender
            """)
    List<Map<String, Object>> countEmpByGender();

    /**
     * 员工职位统计：name（职位编号或 null）, value
     */
    @Select("""
            select job as name, count(*) as value
            from emp
            group by job
            """)
    List<Map<String, Object>> countEmpByJob();

    /**
     * 学员学历统计：name（学历编号）, value
     */
    @Select("""
            select degree as name, count(*) as value
            from student
            group by degree
            order by degree
            """)
    List<Map<String, Object>> countStudentByDegree();

    /**
     * 班级人数统计：name（班级名称）, value
     */
    @Select("""
            select c.name as name, count(s.id) as value
            from clazz c
            left join student s on s.clazz_id = c.id
            group by c.id, c.name, c.begin_date
            order by c.begin_date, c.id
            """)
    List<Map<String, Object>> countStudentByClazz();
}
