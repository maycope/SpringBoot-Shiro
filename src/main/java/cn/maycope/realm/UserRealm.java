package cn.maycope.realm;


import cn.maycope.entiry.User;
import cn.maycope.service.userService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;



/**
 * @author  maycope
 * @data 2020-08-14
 */
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private userService userService;
    /**
     * 进行权限验证
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("执行了授权 => doGetAuthorizationInfo");

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // 拿到当前登录的对象；
        Subject subject =SecurityUtils.getSubject();
        User currentUser = (User) subject.getPrincipal();// 拿到user对象
        // 从数据库中取出权限信息。
        info.addStringPermission(currentUser.getPerms());
        System.out.println(currentUser.getPerms());
        return info;
    }

    /**
     * 进行身份认证
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken userToken = (UsernamePasswordToken)(authenticationToken);
        String username = userToken.getUsername();
        User user = userService.getUser(username);
        if(user == null) {
            return  null;
        }
        // 表示登录成功过之后将用户的信息放入到session里面
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        session.setAttribute("loginUser",user);

        System.out.println("执行了认证=> doGetAuthenticationInfo");
       return new SimpleAuthenticationInfo(user,user.getPassword(),getName());
    }
}
