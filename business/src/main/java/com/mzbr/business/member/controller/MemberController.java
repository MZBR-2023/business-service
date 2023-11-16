package com.mzbr.business.member.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mzbr.business.member.dto.MemberDto;
import com.mzbr.business.member.dto.MemberNicknameChangeDto;
import com.mzbr.business.member.dto.MemberNicknameCheckDto;
import com.mzbr.business.member.dto.MemberSubscribeDto;
import com.mzbr.business.member.dto.MemberSubscribeListDto;
import com.mzbr.business.member.service.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "01.User")
@Slf4j
public class MemberController {
	private final MemberService memberService;

	@GetMapping("/me")
	public ResponseEntity<MemberDto.Response> getMyInfo(@AuthenticationPrincipal UserDetails userDetails) {
		MemberDto userInfo = memberService.getUserInfo(Integer.parseInt(userDetails.getUsername()));
		return ResponseEntity.ok(MemberDto.Response.from(userInfo));
	}

	@GetMapping("/{userId}")
	public ResponseEntity<MemberDto.Response> getOthersInfo(@PathVariable int userId) {
		MemberDto userInfo = memberService.getUserInfo(userId);
		return ResponseEntity.ok(MemberDto.Response.from(userInfo));
	}

	@PostMapping("/nickname/check")
	@Operation(summary = "닉네임 중복 검사", description = "닉네임을 중복 검사한다.", responses = {
		@ApiResponse(responseCode = "200", description = "성공"),
		@ApiResponse(responseCode = "500", description = "잘못된 요청 입니다.")})
	public ResponseEntity<MemberNicknameCheckDto.Response> checkNicknameIsPresent(
		@RequestBody MemberNicknameCheckDto.Request request) {

		boolean isPresent = memberService.checkNicknameIsPresent(request.getNickname());

		return ResponseEntity.ok(MemberNicknameCheckDto.Response.of(isPresent));
	}

	@PatchMapping("/nickname")
	public ResponseEntity<Void> changeNickname(@RequestBody MemberNicknameChangeDto.Request request,
		@AuthenticationPrincipal @Parameter(hidden = true) UserDetails userDetails) {

		memberService.changeNickname(
			MemberNicknameChangeDto.of(Integer.parseInt(userDetails.getUsername()), request.getNickname()));

		return ResponseEntity.ok().build();
	}

	@PatchMapping("/profileImage")
	public ResponseEntity<Void> changeProfileImage(@RequestParam("profileImage") MultipartFile image,
		@AuthenticationPrincipal UserDetails userDetails) {

		memberService.changeProfileImage(image, Long.parseLong(userDetails.getUsername()));

		return ResponseEntity.ok().build();
	}

	@PostMapping("/subscribe/{userId}")
	public ResponseEntity<Void> subscribe(@PathVariable int userId, @AuthenticationPrincipal UserDetails userDetails) {

		memberService.subscribe(MemberSubscribeDto.of(userId, Integer.parseInt(userDetails.getUsername())));

		return ResponseEntity.ok().build();
	}

	@GetMapping("/subscriber/{userId}")
	public ResponseEntity<MemberSubscribeListDto.Response> getSubscribeList(@PathVariable int userId) {

		List<MemberDto> subscribeList = memberService.getSubscribeList(userId);

		return ResponseEntity.ok(MemberSubscribeListDto.Response.from(subscribeList));
	}

	@GetMapping
	public ResponseEntity<?> getMyProfile(@RequestHeader Map<String, String> headers) throws
		JsonProcessingException {
		log.info("headers : {}", headers);
		String uuid = headers.get("uuid");
		return ResponseEntity.ok().build();
	}
}
