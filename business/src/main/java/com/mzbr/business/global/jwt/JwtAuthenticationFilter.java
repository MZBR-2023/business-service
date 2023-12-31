package com.mzbr.business.global.jwt;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	@Value("${uri.permits}")
	private final List<String> permitUrl;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws
		ServletException,
		IOException {
		if (isPermitURI(request.getRequestURI(), request.getContextPath())) {
			filterChain.doFilter(request, response);
			return;
		}
		String refreshToken = jwtService.extractRefreshToken(request)
			.filter(jwtService::isTokenValid)
			.orElse(null);

		if (refreshToken != null) {
			jwtService.checkRefreshToken(request, response, refreshToken);
		} else {
			jwtService.checkAccessToken(request, response, filterChain);
		}
	}

	public boolean isPermitURI(String uri, String contextPath) {
		for (int i = 0; i < permitUrl.size(); i++) {
			if (pathMatcher.match(contextPath + permitUrl.get(i), uri)) {
				return true;
			}
		}
		return false;
	}
}
