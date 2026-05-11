package org.example.gcsj4.utils;

import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class YoloDetectUtil {

    @Value("E:\\model\\best.onnx")
    private String modelPath;

    private ZooModel<Image, Classifications> model;

    @PostConstruct
    public void loadModel() throws Exception {
        List<String> classNames = Arrays.asList("violence", "normal", "fight");

        ImageClassificationTranslator translator = ImageClassificationTranslator.builder()
                .addTransform(new Resize(640, 640))
                .addTransform(new ToTensor())
                .optApplySoftmax(true)
                .optSynset(classNames)
                .build();

        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optModelPath(Paths.get(modelPath))
                .optTranslator(translator)
                .optEngine("OnnxRuntime")
                .build();

        model = criteria.loadModel();
    }


    public DetectResult detect(String imgPath) throws Exception {
        Image image = ImageFactory.getInstance().fromFile(Paths.get(imgPath));

        try (Predictor<Image, Classifications> predictor = model.newPredictor()) {
            Classifications result = predictor.predict(image);
            List<String> classNames = result.getClassNames();
            List<Double> probabilities = result.getProbabilities();

            log.info("模型输出类别数: {}", classNames.size());
            log.info("类别名称: {}", classNames);
            log.info("概率分布: {}", probabilities);

            int maxIndex = 0;
            double maxProb = probabilities.get(0);
            for (int i = 1; i < probabilities.size(); i++) {
                if (probabilities.get(i) > maxProb) {
                    maxProb = probabilities.get(i);
                    maxIndex = i;
                }
            }

            String bestCls = classNames.get(maxIndex);
            log.info("预测结果: {}, 概率: {:.2f}%", bestCls, maxProb * 100);

            boolean isViolence = "violence".equalsIgnoreCase(bestCls) ||
                    "fight".equalsIgnoreCase(bestCls);
            return new DetectResult(isViolence, bestCls + "(" + String.format("%.2f", maxProb * 100) + "%)");
        }
    }
    public static class DetectResult {
        private final boolean violence;
        private final String label;

        public DetectResult(boolean violence, String label) {
            this.violence = violence;
            this.label = label;
        }

        public boolean isViolence() {
            return violence;
        }

        public String getLabels() {
            return label;
        }
    }
}