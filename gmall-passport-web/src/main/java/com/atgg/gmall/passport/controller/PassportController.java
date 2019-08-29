package com.atgg.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atgg.gmall.been.UserInfo;
import com.atgg.gmall.passport.util.JwtUtil;
import com.atgg.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

         @Reference
     private  UserInfoService userInfoService;
         @Value("${token.key}")
      public String  key;
    /**
     * 去往登录页面方法
     * @param request
     * @return
     */
     @RequestMapping("index")
     public String index(HttpServletRequest request){
           //记录用户从哪个服务模块跳转到的登录页面。登录完成后回到该页面
          String originUrl = request.getParameter("originUrl");
             request.setAttribute("originUrl",originUrl);
         return "index";
     }

    /**
     * 登录
     * @param userInfo
     * @param request
     * @return
     */
      @RequestMapping("login")
      @ResponseBody
     public String login(UserInfo userInfo,HttpServletRequest request){
          //获取Ip地址作为Jwt中salt
          String remoteAddr  = request.getHeader("X-forwarded-for");
               //认证user信息
          UserInfo user = userInfoService.login(userInfo);
          if(user!=null){
               //用户存在，生成token返回给
              // 创建key，map，salt  // 服务器Ip 地址 在服务器中设置 X-forwarded-for 对应的值
              HashMap<String, Object> map = new HashMap<>();
              map.put("userId",user.getId());
              map.put("nickName",user.getNickName());
              String token = JwtUtil.encode(key, map, remoteAddr);
             // eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.-1_BE1S6a4IKYnuFQL8rt98OlirG_3T3ngtYfGP-ea8
              return token;
          }else {
              //用户不存在
              return "fail";
          }
     }
     //认证方法
    // http://passport.atguigu.com/verify?token=xxx&salt=xxx
    @RequestMapping("verify")
    @ResponseBody
     public String verify(HttpServletRequest request){
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");
              //解密token
        Map<String, Object> decode = JwtUtil.decode(token, key, salt);
        if(decode!=null&&decode.size()>0){
             //获取用户id
            String userId = (String) decode.get("userId");
            //根据Id去redis中查询，认证
         UserInfo userInfo=  userInfoService.verify(userId);
         if(userInfo!=null){
               return "success";
         }
        }
        return "fail";

     }

}
