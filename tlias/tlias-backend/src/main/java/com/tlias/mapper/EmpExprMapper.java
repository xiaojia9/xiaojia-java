package com.tlias.mapper;

import com.tlias.entity.EmpExpr;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 员工工作经历数据访问
 */
@Mapper
public interface EmpExprMapper {

    /**
     * 批量插入工作经历
     */
    @Insert("""
            <script>
            insert into emp_expr(emp_id, begin, end, company, job) values
            <foreach collection="exprList" item="expr" separator=",">
                (#{expr.empId}, #{expr.begin}, #{expr.end}, #{expr.company}, #{expr.job})
            </foreach>
            </script>
            """)
    int insertBatch(@Param("exprList") List<EmpExpr> exprList);

    @Delete("""
            <script>
            delete from emp_expr where emp_id in
            <foreach collection="empIds" item="empId" open="(" separator="," close=")">
                #{empId}
            </foreach>
            </script>
            """)
    int deleteByEmpIds(@Param("empIds") List<Integer> empIds);

    @Select("select id, emp_id, begin, end, company, job from emp_expr where emp_id = #{empId} order by begin desc")
    List<EmpExpr> listByEmpId(Integer empId);
}
