package com.mzbr.business.video.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mzbr.business.store.dto.SquareLocation;
import com.mzbr.business.store.dto.StoreSearchDto;
import com.mzbr.business.video.dto.VideoDto;
import com.mzbr.business.video.dto.VideoInfoDto;
import com.mzbr.business.video.dto.VideoViewDto;
import com.mzbr.business.video.service.VideoService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/videos")
@Tag(name = "Video")
public class VideoController {

	private final VideoService videoService;

	@GetMapping("/users/{userId}")
	public ResponseEntity<VideoDto.Response> getUserVideos(@PathVariable long userId) {
		List<VideoDto> userVideos = videoService.getUserVideos(userId);
		return ResponseEntity.ok(VideoDto.Response.from(userVideos));
	}

	@GetMapping("/{videoId}")
	public ResponseEntity<VideoInfoDto.Response> getVideoInfo(@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable long videoId) {
		VideoInfoDto videoInfo = videoService.getVideoinfo(
			VideoViewDto.of(Long.parseLong(userDetails.getUsername()), videoId));
		return ResponseEntity.ok(VideoInfoDto.Response.from(videoInfo));
	}

	@GetMapping
	public ResponseEntity<VideoDto.Response> getNearVideos(@RequestParam double topLat,
		@RequestParam double topLng,
		@RequestParam double bottomLat, @RequestParam double bottomLng) {
		List<VideoDto> nearVideos = videoService.getNearVideos(
			StoreSearchDto.of(SquareLocation.of(topLat, topLng, bottomLat, bottomLng)));
		return ResponseEntity.ok(VideoDto.Response.from(nearVideos));
	}

	@GetMapping("/restaurants/{storeId}")
	public ResponseEntity<VideoDto.Response> getStoreVideos(@PathVariable long storeId) {
		List<VideoDto> storeVideos = videoService.getStoreVideos(storeId);
		return ResponseEntity.ok(VideoDto.Response.from(storeVideos));
	}
}
