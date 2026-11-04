package com.tlias.mapper;

import com.tlias.entity.LogItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 操作日志数据访问
 */
@Mapper
public interface LogMapper {

    @Insert("""
            insert into `log`(operate_emp_id, operate_time, class_name, method_name, method_params, return_value, cost_time)
            values(#{operateEmpId}, #{operateTime}, #{className}, #{methodName}, #{methodParams}, #{returnValue}, #{costTime})
            """)
    int insert(LogItem logItem);

    @Select("""
            select l.id, l.operate_emp_id, l.operate_time, l.class_name, l.method_name, l.method_params,
                   l.return_value, l.cost_time, e.name as operate_emp_name
            from `log` l left join emp e on l.operate_emp_id = e.id
            order by l.operate_time desc, l.id desc
            """)
    List<LogItem> page();
}
