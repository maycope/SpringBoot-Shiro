package cn.maycope.service.impl;


import cn.maycope.entiry.User;
import cn.maycope.mapper.userMapper;
import cn.maycope.service.userService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



/**
 * @author  maycope
 * @data 2020-08-14
 */

@Service
public class useServiceImpl implements userService {


    @Autowired
    private userMapper userMapper;

    @Override
    public User getUser(String username) {
        return userMapper.getUser(username);
    }
}
