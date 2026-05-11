package org.example.gcsj4.controller;

import lombok.RequiredArgsConstructor;
import org.example.gcsj4.service.VideoService;
import org.example.gcsj4.utils.Result;
import org.example.gcsj4.utils.VideoConvertUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/video")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;

    @Value("${file.upload.video-path}")
    private String videoUploadPath;

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

    @GetMapping("/play/{fileName}")
    public ResponseEntity<Resource> playVideo(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(videoUploadPath).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "video/mp4";
            String originalFileName = fileName;

            // 如果是不支持的格式，尝试返回转码后的版本
            if (VideoConvertUtil.needsConversion(fileName)) {
                String mp4FileName = convertToMp4IfNeeded(fileName);
                if (mp4FileName != null) {
                    filePath = Paths.get(videoUploadPath).resolve(mp4FileName).normalize();
                    resource = new UrlResource(filePath.toUri());
                    originalFileName = mp4FileName;
                }
            } else if (fileName.endsWith(".avi")) {
                contentType = "video/x-msvideo";
            } else if (fileName.endsWith(".mov")) {
                contentType = "video/quicktime";
            } else if (fileName.endsWith(".mkv")) {
                contentType = "video/x-matroska";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 如果需要，将视频转换为 MP4
     * @return 转换后的文件名，如果不需要转换或转换失败返回 null
     */
    private String convertToMp4IfNeeded(String originalFileName) {
        try {
            String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
            String mp4FileName = baseName + "_converted.mp4";
            Path mp4Path = Paths.get(videoUploadPath, mp4FileName);

            // 如果已经转换过，直接返回
            if (mp4Path.toFile().exists()) {
                return mp4FileName;
            }

            // 进行转换
            String sourcePath = Paths.get(videoUploadPath, originalFileName).toString();
            String targetPath = mp4Path.toString();

            VideoConvertUtil.convertToMp4(sourcePath, targetPath);

            return mp4FileName;
        } catch (Exception e) {
            System.err.println("视频转换失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
