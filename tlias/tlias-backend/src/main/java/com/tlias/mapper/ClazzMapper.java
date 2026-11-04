package com.tlias.mapper;

import com.tlias.entity.Clazz;
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
 * 班级数据访问
 */
@Mapper
public interface ClazzMapper {

    /**
     * 条件分页查询：name 模糊匹配，begin/end 匹配结课时间
     */
    @Select("""
            <script>
            select c.id, c.name, c.room, c.begin_date, c.end_date, c.master_id, c.subject,
                   c.create_time, c.update_time, e.name as master_name
            from clazz c
            left join emp e on c.master_id = e.id
            <where>
                <if test="name != null and name != ''">
                    and c.name like concat('%', #{name}, '%')
                </if>
                <if test="begin != null">
                    and c.end_date &gt;= #{begin}
                </if>
                <if test="end != null">
                    and c.end_date &lt;= #{end}
                </if>
            </where>
            order by c.begin_date desc, c.id desc
            </script>
            """)
    List<Clazz> page(@Param("name") String name,
                     @Param("begin") LocalDate begin,
                     @Param("end") LocalDate end);

    /**
     * 查询全部班级
     */
    @Select("""
            select c.id, c.name, c.room, c.begin_date, c.end_date, c.master_id, c.subject,
                   c.create_time, c.update_time, e.name as master_name
            from clazz c left join emp e on c.master_id = e.id
            order by c.begin_date desc, c.id desc
            """)
    List<Clazz> listAll();

    @Delete("delete from clazz where id = #{id}")
    int deleteById(Integer id);

    @Insert("""
            insert into clazz(name, room, begin_date, end_date, master_id, subject, create_time, update_time)
            values(#{name}, #{room}, #{beginDate}, #{endDate}, #{masterId}, #{subject}, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Clazz clazz);

    @Update("""
            update clazz
            set name = #{name}, room = #{room}, begin_date = #{beginDate}, end_date = #{endDate},
                master_id = #{masterId}, subject = #{subject}, update_time = #{updateTime}
            where id = #{id}
            """)
    int update(Clazz clazz);

    @Select("""
            select c.id, c.name, c.room, c.begin_date, c.end_date, c.master_id, c.subject,
                   c.create_time, c.update_time, e.name as master_name
            from clazz c left join emp e on c.master_id = e.id
            where c.id = #{id}
            """)
    Clazz getById(Integer id);

    @Select("select count(*) from student where clazz_id = #{clazzId}")
    int countStudentByClazzId(Integer clazzId);
}
