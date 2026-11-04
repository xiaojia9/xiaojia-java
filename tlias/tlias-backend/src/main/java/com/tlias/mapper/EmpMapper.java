package com.tlias.mapper;

import com.tlias.entity.Emp;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 员工数据访问
 */
@Mapper
public interface EmpMapper {

    /**
     * 条件分页查询（分页由 PageHelper 完成）
     */
    @Select("""
            <script>
            select e.id, e.username, e.password, e.name, e.gender, e.image, e.job, e.salary,
                   e.entry_date, e.dept_id, e.create_time, e.update_time, d.name as dept_name
            from emp e
            left join dept d on e.dept_id = d.id
            <where>
                <if test="name != null and name != ''">
                    and e.name like concat('%', #{name}, '%')
                </if>
                <if test="gender != null">
                    and e.gender = #{gender}
                </if>
                <if test="begin != null">
                    and e.entry_date &gt;= #{begin}
                </if>
                <if test="end != null">
                    and e.entry_date &lt;= #{end}
                </if>
            </where>
            order by e.update_time desc
            </script>
            """)
    List<Emp> page(@Param("name") String name,
                   @Param("gender") Integer gender,
                   @Param("begin") LocalDate begin,
                   @Param("end") LocalDate end);

    /**
     * 查询全部员工（用于班级班主任下拉框）
     */
    @Select("select id, username, name, gender, image, job, salary, entry_date, dept_id, create_time, update_time from emp order by update_time desc")
    List<Emp> listAll();

    /**
     * 批量删除员工
     */
    @Delete("""
            <script>
            delete from emp where id in
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    int deleteByIds(@Param("ids") List<Integer> ids);

    @Insert("""
            insert into emp(username, password, name, gender, image, job, salary, entry_date, dept_id, create_time, update_time)
            values(#{username}, #{password}, #{name}, #{gender}, #{image}, #{job}, #{salary}, #{entryDate}, #{deptId}, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Emp emp);

    @Select("""
            select e.id, e.username, e.password, e.name, e.gender, e.image, e.job, e.salary,
                   e.entry_date, e.dept_id, e.create_time, e.update_time, d.name as dept_name
            from emp e left join dept d on e.dept_id = d.id
            where e.id = #{id}
            """)
    Emp getById(Integer id);

    @Update("""
            update emp
            set username = #{username}, password = #{password}, name = #{name}, gender = #{gender},
                image = #{image}, job = #{job}, salary = #{salary}, entry_date = #{entryDate},
                dept_id = #{deptId}, update_time = #{updateTime}
            where id = #{id}
            """)
    int update(Emp emp);

    @Select("select count(*) from emp where username = #{username}")
    int countByUsername(@Param("username") String username);

    @Select("select count(*) from emp where username = #{username} and id != #{id}")
    int countByUsernameExcludeId(@Param("username") String username, @Param("id") Integer id);

    @Select("select id, username, password, name from emp where username = #{username}")
    List<Emp> listByUsername(@Param("username") String username);
}
