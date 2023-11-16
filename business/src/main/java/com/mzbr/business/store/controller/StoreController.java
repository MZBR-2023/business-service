package com.mzbr.business.store.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mzbr.business.store.dto.SquareLocation;
import com.mzbr.business.store.dto.StoreDto;
import com.mzbr.business.store.dto.StoreSearchDto;
import com.mzbr.business.store.service.StoreService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/restaurants")
@Tag(name = "02.Restaurant")
public class StoreController {

	private final StoreService storeService;

	@GetMapping
	@Operation(summary = "식당 조회", description = "주변 식당을 조회한다.")
	public ResponseEntity<StoreSearchDto.Response> searchAroundStores(@RequestParam double topLat,
		@RequestParam double topLng,
		@RequestParam double bottomLat, @RequestParam double bottomLng) throws
		IOException {
		List<StoreDto> storeDtos = storeService.searchAroundStores(
			StoreSearchDto.of(SquareLocation.of(topLat, topLng, bottomLat, bottomLng)));
		return ResponseEntity.ok(StoreSearchDto.Response.from(storeDtos));
	}

	@GetMapping("/search")
	public ResponseEntity<StoreSearchDto.Response> searchStoresByCondition(@RequestParam double topLat,
		@RequestParam double topLng,
		@RequestParam double bottomLat, @RequestParam double bottomLng, @RequestParam(defaultValue = "") String keyword,
		@RequestParam(defaultValue = "0") int star) throws
		IOException {
		List<StoreDto> storeDtos = storeService.searchByCondition(
			StoreSearchDto.of(SquareLocation.of(topLat, topLng, bottomLat, bottomLng), keyword, star));
		return ResponseEntity.ok(StoreSearchDto.Response.from(storeDtos));
	}

}
