package com.mzbr.business.store.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class StoreSearchDto {
	private SquareLocation squareLocation;
	private String name;
	private int star;

	public static StoreSearchDto of(SquareLocation squareLocation) {
		return StoreSearchDto.builder()
			.squareLocation(squareLocation)
			.build();
	}

	public static StoreSearchDto of(SquareLocation squareLocation, String name, int star) {
		return StoreSearchDto.builder()
			.squareLocation(squareLocation)
			.name(name)
			.star(star)
			.build();
	}

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@Builder
	public static class Response {
		private List<StoreDto> stores;

		public static Response from(List<StoreDto> stores){
			return Response.builder()
				.stores(stores)
				.build();
		}
	}


}
