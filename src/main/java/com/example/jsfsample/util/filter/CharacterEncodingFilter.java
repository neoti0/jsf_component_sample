package com.example.jsfsample.util.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * 全リクエスト・レスポンスに UTF-8 を強制する。
 * web.xml の request-character-encoding は WildFly/Undertow の遅延処理タイミングによって
 * 効かない場合があるため、Filter でパラメーター読み取り前に明示的に設定する。
 */
@WebFilter("/*")
public class CharacterEncodingFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
