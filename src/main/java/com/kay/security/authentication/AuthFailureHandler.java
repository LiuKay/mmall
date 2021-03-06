package com.kay.security.authentication;

import static com.kay.security.properties.SecurityConstants.ORIGINAL_REQUEST_METHOD;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component("authFailureHandler")
public class AuthFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn(">>onAuthenticationFailure.", exception);
        request.setAttribute(ORIGINAL_REQUEST_METHOD, request.getMethod());
        response.sendError(HttpStatus.UNAUTHORIZED.value(), exception.getMessage());
    }
}
