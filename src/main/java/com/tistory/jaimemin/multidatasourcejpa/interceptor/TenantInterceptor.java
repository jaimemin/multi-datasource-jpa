package com.tistory.jaimemin.multidatasourcejpa.interceptor;

import com.tistory.jaimemin.multidatasourcejpa.multitenancy.config.TenantContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String tenantId = request.getHeader("x-tenant-id");
        TenantContextHolder.setContextHolder(tenantId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContextHolder.clear();
    }
}
