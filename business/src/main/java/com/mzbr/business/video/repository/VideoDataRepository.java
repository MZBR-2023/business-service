package com.mzbr.business.video.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mzbr.business.video.entity.VideoData;

@Repository
public interface VideoDataRepository extends JpaRepository<VideoData, Long> {
}
