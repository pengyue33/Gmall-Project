package com.atgg.gmall.config;


import com.alibaba.fastjson.JSON;
import com.atgg.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 自定义拦截器
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
         //登录成功后
        //https://www.jd.com/?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkFkbWluaXN0cmF0b3IiLCJ1c2VySWQiOiIyIn0.-1_BE1S6a4IKYnuFQL8rt98OlirG_3T3ngtYfGP-ea8
        String token = request.getParameter("newToken");
        if(token!=null){
            //获取newToken ,将其存入cookie中
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        //http://item.gmall.com/33.html
        //当用户访问其他业务模块的时候，此时没有newToken ,但是cookie 有可能存在了token
        if(token==null){
            token  =  CookieUtil.getCookieValue(request, "token", false);
        }
        // 从token中获取用户昵称！
        if(token!=null){
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
              //将昵称放入域中，用于前台显示
            request.setAttribute("nickName",nickName);
        }

         //判断当前控制器上是否有自定义注解
//        @LoginRequire(autoRedirect = true)
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        // 看方法上是否有注解
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if(methodAnnotation!=null){
            // 认证：用户是否登录的认证调用PassPortController中verify 控制器
            // http://passport.atguigu.com/verify?token=xxx&salt=xxx
             //获取salt
            String salt = request.getHeader("X-forwarded-for");
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            if("success".equals(result)){
                //认证成功
                // 保存一下userId
                Map userMapByToken = getUserMapByToken(token);
                String userId = (String) userMapByToken.get("userId");
                request.setAttribute("userId",userId);
                return  true;
            }else {
                  //认证失败,跳转到登录页面
                if(methodAnnotation.autoRedirect()){
                     // 必须登录！ 获取跳转过来的页面，登录完成后，回到之前页面
                    String requestURL = request.getRequestURL().toString();
                    System.out.println("requestURL:"+requestURL);
                    // 对url 进行转码
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    //重定向到登录页面 http://passport.atguigu.com/index
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                      //多个拦截器执行顺序
                    return  false;
                }

            }
        }

        return true;
    }
      //将token中用户信息取出来，放入map中
    private Map getUserMapByToken(String token) {
         //截取token中第二部分
        String subToken = StringUtils.substringBetween(token,".");
        // 创建base64 对象,进行解码
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] decode = base64UrlCodec.decode(subToken);
        // 将字节数组转化为String
        String tokenJson=null;
        try {
            tokenJson = new String(decode, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson, Map.class);
          return map;
    }

    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
