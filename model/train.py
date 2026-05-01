from ultralytics import YOLO

model = YOLO("yolov8s-cls.pt")  # 注意这里！必须是 cls 分类模型！

model.train(
    data="D:/dataset",
    epochs=20,
    imgsz=640,
    batch=4,
    device="cpu"
)

model.export(format="onnx")