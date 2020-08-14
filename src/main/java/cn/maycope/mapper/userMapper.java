package cn.maycope.mapper;


import cn.maycope.entiry.User;
import org.apache.ibatis.annotations.Mapper;



/**
 * @author  maycope
 * @data 2020-08-14
 */


@Mapper
public interface userMapper {

    User getUser(String username);
}
