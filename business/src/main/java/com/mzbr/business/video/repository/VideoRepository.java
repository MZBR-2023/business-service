package com.mzbr.business.video.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.business.member.entity.Member;
import com.mzbr.business.store.entity.Store;
import com.mzbr.business.video.entity.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
	List<Video> findByStore(Store store);

	List<Video> findByMember(Member member);
}
