package cn.maycope.entiry;


import lombok.Data;

/**
 * @author  maycope
 * @data 2020-08-14
 */

@Data
public class User {


    private  Integer id;
    private  String  username;
    private  String password;
    private  String perms;

}
