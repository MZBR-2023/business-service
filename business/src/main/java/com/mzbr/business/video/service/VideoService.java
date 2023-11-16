package com.mzbr.business.video.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.mzbr.business.global.exception.ErrorCode;
import com.mzbr.business.global.exception.custom.BadRequestException;
import com.mzbr.business.global.redis.RedisService;
import com.mzbr.business.member.entity.Member;
import com.mzbr.business.member.repository.MemberRepository;
import com.mzbr.business.store.dto.StoreDto;
import com.mzbr.business.store.dto.StoreSearchDto;
import com.mzbr.business.store.entity.Store;
import com.mzbr.business.store.repository.StoreRepository;
import com.mzbr.business.store.service.StoreService;
import com.mzbr.business.video.dto.VideoDto;
import com.mzbr.business.video.dto.VideoInfoDto;
import com.mzbr.business.video.dto.VideoViewDto;
import com.mzbr.business.video.entity.Video;
import com.mzbr.business.video.repository.VideoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {
	private final VideoRepository videoRepository;
	private final StoreRepository storeRepository;
	private final MemberRepository memberRepository;
	private final StoreService storeService;
	private final RedisService redisService;

	public VideoInfoDto getVideoinfo(VideoViewDto videoViewDto) {
		Video video = videoRepository.findById(videoViewDto.getVideoId())
			.orElseThrow(() -> new BadRequestException(ErrorCode.VIDEO_NOT_FOUND));

		if (!redisService.isView(videoViewDto)) {
			redisService.checkView(videoViewDto);
		}

		return VideoInfoDto.from(video);
	}

	private void addViewCount(long memberId, long videoId) {
	}

	public List<VideoDto> getNearVideos(StoreSearchDto storeSearchDto) {
		List<StoreDto> storeDtos = storeService.searchAroundStores(storeSearchDto);
		// 랜덤으로 근처 영상 뿌리게 구현, 성능 상당히 떨어질 것으로 예상, 추후 변경 필요
		List<VideoDto> videoDtos = new ArrayList<>();
		for (StoreDto storeDto : storeDtos) {
			Store store = storeRepository.findById(storeDto.getStoreId()).get();
			List<Video> videos = store.getVideos();
			for (Video video : videos) {
				videoDtos.add(VideoDto.from(video));
			}
		}
		Collections.shuffle(videoDtos);
		return videoDtos;
	}

	public List<VideoDto> getStoreVideos(long storeId) {
		Store store = storeRepository.findById(storeId)
			.orElseThrow(() -> new BadRequestException(ErrorCode.STORE_NOT_FOUND));
		List<Video> videos = videoRepository.findByStore(store);
		List<VideoDto> videoDtos = videos.stream().map(VideoDto::from).collect(Collectors.toList());
		return videoDtos;
	}

	public List<VideoDto> getUserVideos(long userId) {
		Member member = memberRepository.findById(userId)
			.orElseThrow(() -> new BadRequestException(ErrorCode.USER_NOT_FOUND));
		List<Video> videos = videoRepository.findByMember(member);
		List<VideoDto> videoDtos = videos.stream().map(VideoDto::from).collect(Collectors.toList());
		return videoDtos;
	}
}
