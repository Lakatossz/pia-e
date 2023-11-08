package cz.zcu.kiv.mjakubas.piae.sem.webapplication.security;

import cz.zcu.kiv.mjakubas.piae.sem.core.service.SecurityService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

/**
 * Filter for making sure that the first think than any new user will do is to change their pw.
 */
@Component
@AllArgsConstructor
public class TempUserFilter extends GenericFilterBean {

    private final SecurityService securityService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (securityService.isTemporary(auth.getName())) {
            request = new HttpServletRequestWrapper((HttpServletRequest) request) {
                @Override
                public String getRequestURI() {
                    return "/pw/change";
                }
            };
        }

        chain.doFilter(request, response);
    }
}
