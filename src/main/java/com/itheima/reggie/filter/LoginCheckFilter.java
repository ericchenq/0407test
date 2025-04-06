package com.itheima.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//检查用户是否完成登陆
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        //1.获取本次请求的url
        String requestURI = request.getRequestURI();
        log.info("拦截到请求:{}", requestURI);

        //定义不需要处理的路径
        String [] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };

        //2.判断本次请求路径是否需要处理
        boolean check = check(urls,requestURI);

        //3.如果不需要处理，直接放行
        if (check){
            filterChain.doFilter(request,response);
            return;
        }
        log.info("本次请求不需要处理:{}", requestURI);

        //4.如果登陆状态，直接放行
        if(request.getSession().getAttribute("employee")!= null){
            log.info("用户已登陆，用户id:{}", request.getSession().getAttribute("employee"));
            filterChain.doFilter(request,response);
            return;
        }

        //5.未登陆，通过输出流的方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        log.info("用户未登陆");

        return;


    }

    public boolean check (String[] urls,String requestURI){
        for(String url : urls){
            boolean match = PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
