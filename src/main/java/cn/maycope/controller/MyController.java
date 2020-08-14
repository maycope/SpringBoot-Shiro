package cn.maycope.controller;


import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author  maycope
 * @data 2020-08-14
 */

@Controller
public class MyController {


    @RequestMapping({"/","/index"})
    public  String myIndex(Model model){
        model.addAttribute("msg","Hello Shiro");
        return "index";
    }


    @RequestMapping("user/update")
    public String update(){
        return  "user/update";
    }
    @RequestMapping("user/add")
    public String add(){
        return  "user/add";
    }
    @RequestMapping("user/other")
    public String other(){
        return  "user/other";
    }

    @RequestMapping("/tologin")
    public  String toLogin(){
        return  "login";
    }

    @RequestMapping("/login")
    public  String login(String username,String password,Model model){
        // 获取到当前的用户
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(username,password);
       try
       {
           subject.login(token);// 执行登录的方法
           return "index";
       }catch (UnknownAccountException e){
           model.addAttribute("msg","用户名错误");
           return "/login";
       }catch (IncorrectCredentialsException e)
       {
           model.addAttribute("msg","密码错误");
           return  "/login";
       }

    }

    @GetMapping("/noauth")
    @ResponseBody
    public  String unautorized(){
        return  "未授权 无法访问此页面";
    }
}
