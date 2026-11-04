package com.tlias.mapper;

import com.tlias.entity.Student;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 学员数据访问
 */
@Mapper
public interface StudentMapper {

    /**
     * 条件分页查询（分页由 PageHelper 完成）
     */
    @Select("""
            <script>
            select s.id, s.name, s.no, s.gender, s.phone, s.degree, s.id_card, s.is_college, s.address,
                   s.graduation_date, s.violation_count, s.violation_score, s.clazz_id,
                   s.create_time, s.update_time, c.name as clazz_name
            from student s
            left join clazz c on s.clazz_id = c.id
            <where>
                <if test="name != null and name != ''">
                    and s.name like concat('%', #{name}, '%')
                </if>
                <if test="degree != null">
                    and s.degree = #{degree}
                </if>
                <if test="clazzId != null">
                    and s.clazz_id = #{clazzId}
                </if>
            </where>
            order by s.update_time desc
            </script>
            """)
    List<Student> page(@Param("name") String name,
                       @Param("degree") Integer degree,
                       @Param("clazzId") Integer clazzId);

    @Delete("""
            <script>
            delete from student where id in
            <foreach collection="ids" item="id" open="(" separator="," close=")">
                #{id}
            </foreach>
            </script>
            """)
    int deleteByIds(@Param("ids") List<Integer> ids);

    @Insert("""
            insert into student(name, no, gender, phone, degree, id_card, is_college, address,
                                graduation_date, violation_count, violation_score, clazz_id, create_time, update_time)
            values(#{name}, #{no}, #{gender}, #{phone}, #{degree}, #{idCard}, #{isCollege}, #{address},
                   #{graduationDate}, #{violationCount}, #{violationScore}, #{clazzId}, #{createTime}, #{updateTime})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Student student);

    @Update("""
            update student
            set name = #{name}, no = #{no}, gender = #{gender}, phone = #{phone}, degree = #{degree},
                id_card = #{idCard}, is_college = #{isCollege}, address = #{address},
                graduation_date = #{graduationDate}, clazz_id = #{clazzId}, update_time = #{updateTime}
            where id = #{id}
            """)
    int update(Student student);

    @Select("""
            select s.id, s.name, s.no, s.gender, s.phone, s.degree, s.id_card, s.is_college, s.address,
                   s.graduation_date, s.violation_count, s.violation_score, s.clazz_id,
                   s.create_time, s.update_time, c.name as clazz_name
            from student s left join clazz c on s.clazz_id = c.id
            where s.id = #{id}
            """)
    Student getById(Integer id);

    /**
     * 违纪处理：违纪次数 +1，违纪扣分累加
     */
    @Update("""
            update student
            set violation_count = violation_count + 1,
                violation_score = violation_score + #{score},
                update_time = now()
            where id = #{id}
            """)
    int updateViolation(@Param("id") Integer id, @Param("score") Integer score);

    @Select("select count(*) from student where no = #{no}")
    int countByNo(@Param("no") String no);

    @Select("select count(*) from student where no = #{no} and id != #{id}")
    int countByNoExcludeId(@Param("no") String no, @Param("id") Integer id);
}
