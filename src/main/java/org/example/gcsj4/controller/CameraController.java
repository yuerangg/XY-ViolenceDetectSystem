package org.example.gcsj4.controller;

import org.example.gcsj4.service.CameraViolenceDetectService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/camera")
public class CameraController {

    private final CameraViolenceDetectService service;

    public CameraController(CameraViolenceDetectService service) {
        this.service = service;
    }

    @GetMapping("/start")
    public Map<String, Object> startCamera() {
        Map<String, Object> result = new HashMap<>();
        try {
            service.startRealTimeDetect();
            result.put("success", true);
            result.put("message", "摄像头实时暴力监测已启动");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "启动失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/stop")
    public Map<String, Object> stopCamera() {
        Map<String, Object> result = new HashMap<>();
        try {
            service.stopRealTimeDetect();
            result.put("success", true);
            result.put("message", "摄像头实时暴力监测已停止");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "停止失败: " + e.getMessage());
        }
        return result;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("running", service.isRunning());
        return result;
    }

    @GetMapping("/result")
    public Map<String, Object> getDetectionResult() {
        Map<String, Object> result = new HashMap<>();
        CameraViolenceDetectService.DetectionResult detectionResult = service.getLatestResult();

        result.put("isViolence", detectionResult.isViolence());
        result.put("confidence", detectionResult.getConfidence());
        result.put("label", detectionResult.getLabel());
        result.put("timestamp", System.currentTimeMillis());

        return result;
    }
}
