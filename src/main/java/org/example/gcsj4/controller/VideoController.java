package org.example.gcsj4.controller;

import lombok.RequiredArgsConstructor;
import org.example.gcsj4.service.VideoService;
import org.example.gcsj4.utils.Result;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam Long userId,
                            @RequestParam("file") MultipartFile file) {
        return videoService.uploadVideo(userId, file);
    }

    @GetMapping("/result/{videoId}")
    public Result<?> getResult(@PathVariable Long videoId) {
        return videoService.getVideoResult(videoId);
    }

    @GetMapping("/list/{userId}")
    public Result<?> list(@PathVariable Long userId) {
        return videoService.listByUserId(userId);
    }
}