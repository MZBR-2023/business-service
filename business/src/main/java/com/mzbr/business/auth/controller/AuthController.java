package com.mzbr.business.auth.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mzbr.business.auth.dto.SignUpDto;
import com.mzbr.business.auth.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "00.Auth", description = "Auth API")
public class AuthController {

	private final AuthService authService;

	@PostMapping("/join")
	@Operation(summary = "회원 가입", description = "회원 가입한다.", responses = {
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "409", description = "이미 가입된 회원입니다."),
		@ApiResponse(responseCode = "409", description = "중복된 닉네임 입니다.")})
	public ResponseEntity<Void> signUp(@RequestBody SignUpDto.Request request,
		@AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails) {
		authService.signup(SignUpDto.of(Integer.parseInt(userDetails.getUsername()), request.getNickname()));
		return ResponseEntity.ok().build();
	}

	@PostMapping("/log-out")
	public ResponseEntity<Void> logout(HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		authService.logout(request.getHeader("Authorization").substring(7), userDetails.getUsername());
		return ResponseEntity.ok().build();
	}
}
