package org.example.gcsj4.service;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgcodecs.Imgcodecs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CameraViolenceDetectService {

    @Value("${yolo.model-path}")
    private String modelPath;

    private ZooModel<Image, Classifications> model;
    private VideoCapture camera;
    private ExecutorService executorService;
    private AtomicBoolean isRunning = new AtomicBoolean(false);

    private volatile DetectionResult latestResult = new DetectionResult();
    // 添加 DJL Manager 用于直接转换
    private NDManager manager;
    // 用于静止画面检测
    private Mat prevFrame = null;
    @PostConstruct
    public void init() throws Exception {
        List<String> classNames = Arrays.asList("normal", "violence","fight");

        ImageClassificationTranslator translator = ImageClassificationTranslator.builder()
                .addTransform(new Resize(640, 640))
                .addTransform(new ToTensor())
                .optApplySoftmax(true)
                .optSynset(classNames)
                .build();

        Criteria<Image, Classifications> criteria;

        if (modelPath.endsWith(".torchscript")) {
            System.out.println("检测到 TorchScript 模型格式");
            System.out.println("加载模型路径: " + modelPath);

            criteria = Criteria.builder()
                    .setTypes(Image.class, Classifications.class)
                    .optModelPath(Paths.get(modelPath))
                    .optTranslator(translator)
                    .optEngine("PyTorch")
                    .build();
        } else if (modelPath.endsWith(".onnx")) {
            String torchscriptPath = modelPath.replace(".onnx", ".torchscript");
            System.out.println("配置为 ONNX 格式，尝试加载对应的 TorchScript 模型: " + torchscriptPath);

            criteria = Criteria.builder()
                    .setTypes(Image.class, Classifications.class)
                    .optModelPath(Paths.get(torchscriptPath))
                    .optTranslator(translator)
                    .optEngine("PyTorch")
                    .build();
        } else {
            throw new IllegalArgumentException("不支持的模型格式: " + modelPath + "，请使用 .onnx 或 .torchscript 格式");
        }

        try {
            model = criteria.loadModel();
            System.out.println("✅ 模型加载成功");
        } catch (Exception e) {
            System.err.println("❌ 模型加载失败: " + e.getMessage());
            System.err.println("请检查:");
            System.err.println("   1. 模型文件是否存在于指定路径");
            System.err.println("   2. 模型格式是否正确");
            System.err.println("   3. 当前配置的模型路径: " + modelPath);
            throw new RuntimeException("模型加载失败，请检查配置和文件路径", e);
        }

        nu.pattern.OpenCV.loadLocally();

        executorService = Executors.newSingleThreadExecutor();

        System.out.println("✅ 摄像头初始化成功，模型加载完成");
        System.out.println("类别顺序: " + classNames);
        System.out.println("使用引擎: PyTorch (TorchScript)");

        testModel();
    }
    private void testModel() {
        try {
            System.out.println("\n=== 开始模型测试 ===");

            // 使用 OpenCV 创建测试图像
            Mat blackMat = new Mat(640, 640, org.opencv.core.CvType.CV_8UC3, new org.opencv.core.Scalar(0, 0, 0));
            Mat whiteMat = new Mat(640, 640, org.opencv.core.CvType.CV_8UC3, new org.opencv.core.Scalar(255, 255, 255));

            System.out.println("创建测试图像完成");

            // 转换为 DJL Image
            Image djlBlackImage = matToImage(blackMat);
            Image djlWhiteImage = matToImage(whiteMat);

            System.out.println("DJL Image 转换完成");

            try (Predictor<Image, Classifications> predictor = model.newPredictor()) {
                System.out.println("开始推理全黑图像...");
                Classifications result1 = predictor.predict(djlBlackImage);
                System.out.println("全黑图像推理结果:");
                System.out.println(result1.toString());

                System.out.println("\n开始推理全白图像...");
                Classifications result2 = predictor.predict(djlWhiteImage);
                System.out.println("全白图像推理结果:");
                System.out.println(result2.toString());

                boolean same = result1.toString().equals(result2.toString());
                System.out.println("\n两次推理结果是否相同: " + same);

                if (same) {
                    System.err.println("\n严重错误: 模型对不同输入返回相同结果！");
                    System.err.println("   可能原因:");
                    System.err.println("   1. OnnxRuntime 引擎不支持图像变换 (Resize/ToTensor)");
                    System.err.println("   2. 模型文件损坏或格式不正确");
                    System.err.println("   3. 模型训练有问题");
                    System.err.println("\n   建议解决方案:");
                    System.err.println("   - 方案1: 将 ONNX 模型转换为 PyTorch (.pt) 格式");
                    System.err.println("   - 方案2: 手动实现图像预处理，不使用 DJL Translator");
                    System.err.println("   - 方案3: 检查模型导出是否正确");
                } else {
                    System.out.println("\n模型工作正常，不同输入产生不同输出");
                }
            }

            System.out.println("=== 模型测试完成 ===\n");
        } catch (Exception e) {
            System.err.println("模型测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void startRealTimeDetect() {
        if (isRunning.get()) {
            System.out.println("实时检测已在运行中");
            return;
        }

        isRunning.set(true);

        executorService.submit(() -> {
            camera = initializeCamera();

            if (camera == null || !camera.isOpened()) {
                System.err.println("无法打开摄像头，请检查：");
                System.err.println("   1. 摄像头是否被其他程序占用");
                System.err.println("   2. 摄像头驱动是否正常");
                System.err.println("   3. 是否允许应用访问摄像头");
                isRunning.set(false);
                return;
            }

            camera.set(Videoio.CAP_PROP_FRAME_WIDTH, 640);
            camera.set(Videoio.CAP_PROP_FRAME_HEIGHT, 480);
            camera.set(Videoio.CAP_PROP_FPS, 30);

            System.out.println("🎥 摄像头实时暴力监测已启动...");
            System.out.println("   分辨率: 640x480");
            System.out.println("   帧率: 30 FPS");

            Mat frame = new Mat();
            int frameCount = 0;
            long startTime = System.currentTimeMillis();
            try (Predictor<Image, Classifications> predictor = model.newPredictor()) {
                while (isRunning.get()) {
                    boolean success = camera.read(frame);
                    if (!success || frame.empty()) {
                        System.err.println("读取摄像头帧失败，尝试重新连接...");

                        if (!reconnectCamera()) {
                            System.err.println("无法重新连接摄像头，停止检测");
                            break;
                        }
                        continue;
                    }

                    frameCount++;

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - startTime >= 1000) {
                        double fps = frameCount * 1000.0 / (currentTime - startTime);
                        System.out.println(String.format("📊 性能统计 - FPS: %.2f, 总帧数: %d", fps, frameCount));
                        frameCount = 0;
                        startTime = currentTime;
                    }

                    try {
                        long startMs = System.currentTimeMillis();

                        // 1. 静止画面检测 (通过计算前后帧差异)
                        if (prevFrame != null && prevFrame.size().equals(frame.size())) {
                            Mat diff = new Mat();
                            org.opencv.core.Core.absdiff(prevFrame, frame, diff);
                            org.opencv.core.Scalar mean = org.opencv.core.Core.mean(diff);
                            double motionScore = mean.val[0];
                            diff.release();

                            // 如果运动分数低于 5.0，说明画面基本静止，跳过 AI 推理
                            if (motionScore < 5.0) {
                                if (frameCount % 50 == 0) {
                                    System.out.println("画面静止，跳过检测 (运动分: " + motionScore + ")");
                                }
                                // 更新上一帧并跳过本次循环
                                frame.copyTo(prevFrame);
                                Thread.sleep(500);
                                continue;
                            }
                        }

                        // 更新上一帧
                        if (prevFrame == null) {
                            prevFrame = new Mat();
                        }
                        frame.copyTo(prevFrame);

                        // 2. 亮度检测（保留作为第二道防线）
                        org.opencv.core.Mat gray = new org.opencv.core.Mat();
                        org.opencv.imgproc.Imgproc.cvtColor(frame, gray, org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY);
                        org.opencv.core.Scalar meanBrightness = org.opencv.core.Core.mean(gray);
                        double brightness = meanBrightness.val[0];
                        gray.release();

                        if (brightness < 10) {
                            if (frameCount % 50 == 0) {
                                System.out.println("画面过暗，跳过检测 (亮度: " + brightness + ")");
                            }
                            Thread.sleep(500);
                            continue;
                        }

                        Image img = matToImage(frame);

                        Classifications res = predictor.predict(img);

                        long endMs = System.currentTimeMillis();
                        if (frameCount % 10 == 0) {
                            System.out.println("单帧处理耗时: " + (endMs - startMs) + "ms");
                        }
                        String label = "unknown";
                        double confidence = 0.0;

                        if (res != null) {
                            List<String> classNames = res.getClassNames();
                            List<Double> probabilities = res.getProbabilities();

                            if (classNames != null && probabilities != null && !classNames.isEmpty()) {

                                int actualSize = Math.min(classNames.size(), probabilities.size());

                                if (actualSize == 0) {
                                    System.err.println("警告: 类别名称或概率列表为空");
                                    continue;
                                }

                                int maxIndex = 0;
                                double maxProb = probabilities.get(0);
                                for (int i = 1; i < actualSize; i++) {
                                    if (probabilities.get(i) > maxProb) {
                                        maxProb = probabilities.get(i);
                                        maxIndex = i;
                                    }
                                }
                                label = classNames.get(maxIndex);
                                confidence = maxProb;

                                // 调试：打印所有类别的概率分布
                                if (frameCount % 10 == 0) {
                                    StringBuilder sb = new StringBuilder("概率分布: ");
                                    for (int i = 0; i < actualSize; i++) {
                                        sb.append(classNames.get(i)).append("=").append(String.format("%.4f", probabilities.get(i))).append(" ");
                                    }
                                    System.out.println(sb.toString());
                                }

                                if (frameCount % 50 == 0) {
                                    System.out.println(String.format("调试信息 - 类别数: %d, 概率数: %d, 实际使用: %d, 最佳索引: %d",
                                            classNames.size(), probabilities.size(), actualSize, maxIndex));
                                }
                            }
                        }

                        boolean isViolence = "violence".equalsIgnoreCase(label) && confidence > 0.5;

                        if (isViolence) {
                            System.out.println(String.format("【暴力警告】标签: %s, 置信度: %.2f%%, 耗时: %dms",
                                    label, confidence * 100, endMs - startMs));
                        } else if (frameCount % 50 == 0) {
                            System.out.println(String.format("正常画面 - 标签: %s, 置信度: %.2f%%, 耗时: %dms",
                                    label, confidence * 100, endMs - startMs));
                        }
                        latestResult = new DetectionResult(isViolence, confidence, label);

                        // 降低检测频率到每秒2帧
                        Thread.sleep(500);

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        System.err.println("检测过程中发生错误: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                System.err.println("创建预测器失败: " + e.getMessage());
                e.printStackTrace();
            } finally {
                releaseResources();
            }
        });
    }
    /**
     * 初始化摄像头，尝试不同的后端
     */
    private VideoCapture initializeCamera() {
        System.out.println("正在尝试初始化摄像头...");

        // 尝试1: 使用 DSHOW 后端（Windows 推荐）
        System.out.println("   尝试 1: 使用 DSHOW 后端...");
        VideoCapture cap = new VideoCapture(0, Videoio.CAP_DSHOW);
        if (cap.isOpened()) {
            System.out.println("DSHOW 后端成功");
            return cap;
        }
        cap.release();

        // 尝试2: 使用 MSMF 后端
        System.out.println("   尝试 2: 使用 MSMF 后端...");
        cap = new VideoCapture(0, Videoio.CAP_MSMF);
        if (cap.isOpened()) {
            System.out.println("MSMF 后端成功");
            return cap;
        }
        cap.release();

        // 尝试3: 使用默认后端
        System.out.println("   尝试 3: 使用默认后端...");
        cap = new VideoCapture(0);
        if (cap.isOpened()) {
            System.out.println("默认后端成功");
            return cap;
        }
        cap.release();

        System.err.println("所有后端都失败");
        return null;
    }

    /**
     * 重新连接摄像头
     */
    private boolean reconnectCamera() {
        System.out.println("尝试重新连接摄像头...");

        if (camera != null) {
            camera.release();
        }

        // 等待一下再重试
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        camera = initializeCamera();
        return camera != null && camera.isOpened();
    }

    // 停止实时监测
    public void stopRealTimeDetect() {
        if (isRunning.get()) {
            System.out.println("正在停止实时检测...");
            isRunning.set(false);

            // 等待一段时间让线程结束
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("实时检测已停止");
        }
    }

    // 检查是否正在运行
    public boolean isRunning() {
        return isRunning.get();
    }

    /**
     * 获取最新的检测结果
     */
    public DetectionResult getLatestResult() {
        return latestResult;
    }
    // 释放资源
    private void releaseResources() {
        if (camera != null) {
            camera.release();
            System.out.println("摄像头已释放");
        }
        isRunning.set(false);
        System.out.println("资源已释放");
    }

    @PreDestroy
    public void cleanup() {
        stopRealTimeDetect();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        System.out.println("服务资源清理完成");
    }

    // OpenCV Mat 转 DJL Image (保留旧方法作为备用)
    private Image matToImage(Mat frame) throws Exception {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", frame, mob);
        byte[] bytes = mob.toArray();
        return ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(bytes));
    }

    public static class DetectionResult {
        private boolean isViolence;
        private double confidence;
        private String label;

        public DetectionResult() {
            this.isViolence = false;
            this.confidence = 0.0;
            this.label = "unknown";
        }

        public DetectionResult(boolean isViolence, double confidence, String label) {
            this.isViolence = isViolence;
            this.confidence = confidence;
            this.label = label;
        }

        public boolean isViolence() {
            return isViolence;
        }

        public void setViolence(boolean violence) {
            isViolence = violence;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}

