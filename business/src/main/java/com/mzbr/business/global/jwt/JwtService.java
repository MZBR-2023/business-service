package com.mzbr.business.global.jwt;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.mzbr.business.global.exception.ErrorCode;
import com.mzbr.business.global.exception.custom.AuthException;
import com.mzbr.business.global.util.PasswordUtil;
import com.mzbr.business.member.entity.Member;
import com.mzbr.business.member.repository.MemberRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter
public class JwtService {
	@Value("${jwt.access.header}")
	private String ACCESS_HEADER;

	@Value("${jwt.refresh.header}")
	private String REFRESH_HEADER;

	@Value("${jwt.access.expiration}")
	private int ACCESS_EXPIRATION_TIME;

	@Value("${jwt.refresh.expiration}")
	private int REFRESH_EXPIRATION_TIME;

	@Value("${jwt.secretKey}")
	private String secretKey;

	@Value("${uri.client}")
	private String CLIENT_URI;

	private static final String PREFIX = "Bearer ";
	private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
	private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
	private static final String EXPIRED_TOKEN_SUBJECT = "ExpiredToken";
	private static final String ROLE_MEMBER = "ROLE_MEMBER";

	private final RedisTemplate<String, Object> redisTemplate;
	private final MemberRepository memberRepository;
	private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

	public String createAccessToken(long id) {
		return JWT.create()
			.withSubject(ACCESS_TOKEN_SUBJECT)
			.withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION_TIME))
			.withClaim("id", id)
			.sign(Algorithm.HMAC512(secretKey));
	}

	public String createRefreshToken() {
		return JWT.create()
			.withSubject(REFRESH_TOKEN_SUBJECT)
			.withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
			.sign(Algorithm.HMAC512(secretKey));
	}

	public void saveRefreshToken(String refreshToken, long id) {
		redisTemplate.opsForValue().set(id + "", refreshToken, REFRESH_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
	}

	public Optional<String> extractAccessToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(ACCESS_HEADER))
			.filter(token -> token.startsWith(PREFIX))
			.map(token -> token.replace(PREFIX, ""));
	}

	public Optional<String> extractRefreshToken(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader(REFRESH_HEADER))
			.filter(token -> token.startsWith(PREFIX))
			.map(token -> token.replace(PREFIX, ""));
	}

	public Optional<Long> extractId(String accessToken) {
		return Optional.ofNullable(JWT.decode(accessToken).getClaim("id").asLong());
	}

	public boolean isTokenValid(String token) {
		try {
			JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isTokenBlacklist(String token) {
		if (redisTemplate.opsForValue().get(token) != null) {
			throw new AuthException(ErrorCode.ACCESS_TOKEN_BLACKLIST);
		}
		return false;
	}

	public void sendBothToken(HttpServletResponse response, String accessToken, String refreshToken) throws
		IOException {
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		String jsonBody =
			"{\"" + ACCESS_HEADER + "\": \"" + accessToken + "\", \"" + REFRESH_HEADER + "\": \"" + refreshToken
				+ "\"}";
		response.getWriter().write(jsonBody);
	}

	public void sendRedirect(HttpServletResponse response, String accessToken, String refreshToken,
		boolean isMember) throws
		IOException {
		if (!isMember) {
			response.sendRedirect(CLIENT_URI + "signup?accessToken=" + accessToken);
		} else {
			response.sendRedirect(
				CLIENT_URI + "kakao/redirect?accessToken=" + accessToken + "&refreshToken=" + refreshToken);
		}
	}

	public void checkRefreshToken(HttpServletRequest request, HttpServletResponse response, String refreshToken) throws
		IOException {
		String accessToken = extractAccessToken(request).orElseThrow(
			() -> new AuthException(ErrorCode.ACCESS_TOKEN_NOT_EXIST));
		long id = extractId(accessToken).orElseThrow(() -> new AuthException(ErrorCode.ACCESS_TOKEN_INVALID));

		if (!refreshToken.equals(redisTemplate.opsForValue().get(id + ""))) {
			throw new AuthException(ErrorCode.REFRESH_TOKEN_INVALID);
		}

		String newAccessToken = createAccessToken(id);
		String newRefreshToken = createRefreshToken();

		redisTemplate.opsForValue().set(id + "", newRefreshToken, REFRESH_EXPIRATION_TIME, TimeUnit.MILLISECONDS);
		sendBothToken(response, newAccessToken, newRefreshToken);
	}

	public void checkAccessToken(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws
		ServletException,
		IOException {
		extractAccessToken(request)
			.ifPresentOrElse(accessToken -> {
				if (!isTokenBlacklist(accessToken) && isTokenValid(accessToken)) {
					extractId(accessToken)
						.ifPresent(this::saveAuthentication);
				} else {
					throw new AuthException(ErrorCode.ACCESS_TOKEN_INVALID);
				}
			}, () -> {
				throw new AuthException(ErrorCode.ACCESS_TOKEN_NOT_EXIST);
			});
		filterChain.doFilter(request, response);
	}

	public void saveAuthentication(long id) {
		UserDetails userDetails = User.builder()
			.username(String.valueOf(id))
			.password(PasswordUtil.generateRandomPassword())
			.roles(ROLE_MEMBER)
			.build();

		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
			authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	public long getExpiration(String token) {
		Date expiresAt = JWT.require(Algorithm.HMAC512(secretKey))
			.build().verify(token)
			.getExpiresAt();

		return (expiresAt.getTime() - new Date().getTime()) / (1000 * 60);
	}

}
