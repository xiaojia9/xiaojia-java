package com.tlias.mapper;

import com.tlias.entity.Dept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 部门数据访问
 */
@Mapper
public interface DeptMapper {

    @Select("select id, name, create_time, update_time from dept order by update_time desc")
    List<Dept> list();

    @Delete("delete from dept where id = #{id}")
    int deleteById(Integer id);

    @Insert("insert into dept(name, create_time, update_time) values(#{name}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Dept dept);

    @Select("select id, name, create_time, update_time from dept where id = #{id}")
    Dept getById(Integer id);

    @Update("update dept set name = #{name}, update_time = #{updateTime} where id = #{id}")
    int update(Dept dept);

    @Select("select count(*) from dept where id = #{id}")
    int countById(Integer id);
}
