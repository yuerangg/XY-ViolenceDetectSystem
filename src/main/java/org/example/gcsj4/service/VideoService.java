package org.example.gcsj4.service;


import org.example.gcsj4.utils.Result;
import org.springframework.web.multipart.MultipartFile;

public interface VideoService {
    Result<?> uploadVideo(Long userId, MultipartFile file);
    void asyncAuditVideo(Long videoId);
    Result<?> getVideoResult(Long videoId);

    Result<?> listByUserId(Long userId);
}