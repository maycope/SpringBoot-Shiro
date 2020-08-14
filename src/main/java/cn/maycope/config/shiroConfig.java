package cn.maycope.config;


import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import cn.maycope.realm.UserRealm;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.EnterpriseCacheSessionDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import sun.awt.image.ImageWatched;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @author  maycope
 * @data 2020-08-14
 */
@Configuration
public class shiroConfig {

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean (@Qualifier("securityManager") DefaultWebSecurityManager defaultWebSecurityManager){
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();


        // 设置安全管理器
        bean.setSecurityManager(defaultWebSecurityManager);

        // 设置登录的请求信息。表示在未进行登录时候，需要进行登录地址。
        bean.setLoginUrl("/tologin");
        // 设置未授权页面，在访问一些需要授权的页面，但是当前用户未授权时候，会走如下请求。
        bean.setUnauthorizedUrl("/noauth");

        // 添加内置的过滤器
    /**
     * anon: 不需要进行任何的验证
     * authc: 必须进行验证之后才能够访问。
     * user: 必须拥有记住我这个功能才能够访问
     * perms: 拥有对某个资源的权限才能够访问
     * role: 拥有某个角色权限才能够访问。
     */
    Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
    /**
     * 授权操作
     */
    // 为不同的页面添加具体的授权请求的信息。
    filterChainDefinitionMap.put("/user/add","perms[user:add]");
    filterChainDefinitionMap.put("/user/update","perms[user:update]");


    // 验证未授权时候的显示（不适用shiro:hasPermission）表示有无授权都会显示，但是无授权时候会跳转到未授权页面。
    filterChainDefinitionMap.put("/user/other","perms[user:update]");


    //静态资源，对应`/resources/static`文件夹下的资源
    filterChainDefinitionMap.put("/css/**", "anon");
    filterChainDefinitionMap.put("/js/**", "anon");

    bean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return  bean;
    }


    @Bean(name="securityManager")
    public DefaultWebSecurityManager getdefaultWebSecurityManager(@Qualifier("userRealm") UserRealm userRealm){

        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        // 关联userRealm
        securityManager.setRealm(userRealm());
        return  securityManager;
    }
    @Bean
    public SessionManager sessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        sessionManager.setGlobalSessionTimeout(60 * 60 * 10); //10分钟
        sessionManager.setSessionDAO(new EnterpriseCacheSessionDAO());
        return sessionManager;
    }

    @Bean
    public UserRealm userRealm(){
        return  new UserRealm();
    }


    //整合shiroDialect ；用来整合shiro和 thymeleaf
    @Bean
    public ShiroDialect getshiroDialect(){
        return new ShiroDialect();
    }
}
