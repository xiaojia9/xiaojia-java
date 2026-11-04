package com.tlias.config;

import com.tlias.common.BaseContext;
import com.tlias.common.Result;
import com.tlias.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 登录校验拦截器
 * <p>请求头 token 校验通过后，把员工 ID 放入 {@link BaseContext}，供业务层 / 日志切面使用；
 * 校验失败返回 401 与统一 JSON 响应。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 CORS 预检请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("token");
        if (!StringUtils.hasText(token)) {
            log.debug("未携带 token，请求被拦截: {}", request.getRequestURI());
            writeUnauthorized(response);
            return false;
        }
        Claims claims = jwtUtils.parseToken(token);
        if (claims == null) {
            log.debug("token 无效或已过期，请求被拦截: {}", request.getRequestURI());
            writeUnauthorized(response);
            return false;
        }
        Object id = claims.get("id");
        if (id == null) {
            writeUnauthorized(response);
            return false;
        }
        BaseContext.setCurrentId(Integer.valueOf(id.toString()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.remove();
    }

    private void writeUnauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String json = objectMapper.writeValueAsString(Result.error(0, "未登录或登录已过期，请重新登录"));
        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
