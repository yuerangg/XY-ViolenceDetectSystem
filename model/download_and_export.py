from ultralytics import YOLO

# 自动拉取官方预训练 yolov8s 模型
model = YOLO("yolov8s.pt")

# 直接导出为 ONNX，给 Java 项目用
model.export(format="onnx", imgsz=640)
print("导出完成，当前目录生成：yolov8s.onnx")

