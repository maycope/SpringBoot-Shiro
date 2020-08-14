## SpringBoot-Shiro

### 前言

本篇文章具体讲解`SpringBoot`与`Shiro`的整合操作，同时对于后端数据库中数据的获取使用到`MyBatis`,最后结合`shiro`与`Thymeleaf`完成对于不同用户的登录之际进行不同的展示信息。具体完整的学习可以参见 [张开涛-Shiro](https://www.iteye.com/blog/jinnianshilongnian-2018936)，这里不对具体的原理部分进行深入学习，在学习本文章之前，默认对`Shiro`已经有了基础的认知，来学习与Spring的整合操作。同时也作为一个`Demo`性质的讲解，在遗忘之际进行回顾。

### 前提准备

#### 依赖的导入xian

其中重要的依赖也都有相关联的注释信息。

```xml
  <!--  shiro 和 thymeleaf -->
        <dependency>
            <groupId>com.github.theborakompanioni</groupId>
            <artifactId>thymeleaf-extras-shiro</artifactId>
            <version>2.0.0</version>
        </dependency>
        <!--Shiro-->
        <dependency>
            <groupId>org.apache.shiro</groupId>
            <artifactId>shiro-spring</artifactId>
            <version>1.4.0</version>
        </dependency>
 <!--MyBatis-->
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.1.1</version>
        </dependency>
 <!--更多依赖可以具体查看 pom.xml-->
```

#### 修改application.yml 配置文件

具体可以参看`/src/main/resources/application.yml`中内容

```yml
spring:
  datasource:
    name: springboot
    type: com.alibaba.druid.pool.DruidDataSource
    #druid相关配置
    druid:
      #mysql驱动
      driver-class-name: com.mysql.cj.jdbc.Driver
      #基本属性
      url: jdbc:mysql://127.0.0.1:3306/springboot_shiro?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
      username: root
      password: 123456

```



#### SQL语句的建表

数据库名称为：`springboot_shiro`创建一个表名为`users`的表，并插入一些必要的数据。

```sql
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` int(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `perms` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'root', '123456', 'user:add');
INSERT INTO `users` VALUES (2, 'maycope', '123456', 'user:update');
INSERT INTO `users` VALUES (3, 'test', '123456', 'test');

```

### 主要信息讲解：

创建一个`SpringBoot`项目和其三层架构关系就不再进行具体的讲解，具体架构就是我们的三层架构模型`controller-service-mapper`。

对于`Shiro`来说其主要的配置就是在 `shiroConfig`与`UserRealm`中。	



#### Realm

解释：`域，Shiro从从Realm获取安全数据（如用户、角色、权限），就是说SecurityManager要验证用户身份，那么它需要从Realm获取相应的用户进行比较以确定用户身份是否合法；也需要从Realm得到用户相应的角色/权限进行验证用户是否能进行操作；可以把Realm看成DataSource，即安全数据源`。

需要我们继承**AuthorizingRealm**，然后强制实现两个具体的类，一个是实现授权的认证，一个是身份的认证：

```java
   @Override
// 授权认证
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    @Override
// 身份认证
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        return null;
    }
```

##### 身份认证

```java
   protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
       /**
       * 1. 从token中获取到输入的用户名。
       * 2. 通过获取到的用户名和数据库根据具体信息查询到的进行对比，是否能够获取到具体的用户。
       * 3. 将成功后的user放到对应的session中（注意是shiro的session）
       */
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
```

##### 权限认证

```java
 @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("执行了授权 => doGetAuthorizationInfo");

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        // 拿到当前登录的对象；
        Subject subject =SecurityUtils.getSubject();
        User currentUser = (User) subject.getPrincipal();// 拿到user对象
        // 从数据库中取出权限信息。将对应的权限信息使用addStringPermission 添加到权限认证中。
        info.addStringPermission(currentUser.getPerms());
        System.out.println(currentUser.getPerms());
        return info;
    }

```

#### ShiroConfig

对于Shiro来说很多的部分也都是固定的配置信息。

```java
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
```

### 具体实践

完成以上的基础配置之后，就可以开始具体的实践来进行具体的验证。首先来具体讲解部分的功能。

#### 身份认证

对于身份认证系列，就是通过其帮我们封装好的系列方法，我们来对用户的登录情况进行验证，并智能能帮助我们返回具体的错误信息。

1. 首先进行地址的拦截，对于`/` 或者是`/index`都拦截跳转到`index`页面。

2. 对于我们已经知道的url地址例如`8080/user/add`,平时我们需要来设置具体的拦截器操作，在没有登录时候，跳转到`登录页面`，对于`Shiro`来说我们只需要进行 ` bean.setLoginUrl("/tologin")`表示在未登录时候跳转到`tologin`请求。再在控制层具体接收请求进行登录表单的验证。

3. 在输入完成用户名和密码之后，我们可以具体查看登录源码：

   ```java
      @RequestMapping("/login")
       public  String login(String username,String password,Model model){
           // 获取到当前的用户
           Subject subject = SecurityUtils.getSubject();
           UsernamePasswordToken token = new UsernamePasswordToken(username,password);
          try
          {
              System.out.println("Before");
              subject.login(token);// 执行登录的方法
              System.out.println("After");
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
   ```

   可以查看具体的控制台输出，可以得知在进行登录时候会进行身份的认证。

   ```text
   Before
   执行了认证=> doGetAuthenticationInfo
   After
   ```

   具体的逻辑认证会在`Realm`中进行认证，在不同的错误下会打印出不同的错误信息（这些认证的信息都不需要我们具体的去管理）。

   在如下的代码中，我们只需要返回null，具体的错误信息`Shiro`会帮我们进行处理。

   ```java
     User user = userService.getUser(username);
           if(user == null) {
               return  null;
           }
   ```

#### 权限认证

对于权限认证系列，主要有我们如下配置，表示在访问到具体页面时候，需要我们添加不同的权限。

```java
 filterChainDefinitionMap.put("/user/add","perms[user:add]");
    filterChainDefinitionMap.put("/user/update","perms[user:update]");
```

添加权限信息,通过获取到数据库中的`perms`信息进行具体的动态配置。

```java
  // 拿到当前登录的对象；
        Subject subject =SecurityUtils.getSubject();
        User currentUser = (User) subject.getPrincipal();// 拿到user对象
        // 从数据库中取出权限信息。
        info.addStringPermission(currentUser.getPerms());
```

参见我们的数据库中的`perms`字段，对于不同用户就会有不同的权限，我们通过如下配置：`shiro:hasPermission`表示在不同的权限下不同展示。

可以先行登录root用户，发现只会有`add`,可以跳转到`add`界面。

然后登录`maycope` 用户,会发现只有`update`。

```html
   <div shiro:hasPermission="user:add" class="color">
        <a th:href="@{/user/add}">add</a>
    </div>
    <div shiro:hasPermission="user:update" class="color">
        <a th:href="@{/user/update}">update</a>
    </div>
```

#### 未授权

对于我们的**text**用户而言其**perms**字段为`test`,但是对于 `/user/other`页面来说

需要如下权限才能够访问。

```java
filterChainDefinitionMap.put("/user/other","perms[user:update]");
```

所以对于未授权的时候，我们添加如下语句，表示对未授权的信息进行拦截：

```java
bean.setUnauthorizedUrl("/noauth");
```

这个时候，我们访问时候，就会跳转到未授权对应的请求，我们进行接收，就会跳转到对应未授权页面。

