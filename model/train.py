from ultralytics import YOLO
import os

os.chdir('E:/gcsj4/gcsj4/model')

print("=" * 60)
print("开始训练暴力检测模型")
print("=" * 60)

model = YOLO('yolov8s-cls.pt')

print("\n开始训练...")
results = model.train(
    data='D:/dataset',
    epochs=100,
    imgsz=640,
    batch=16,
    lr0=0.01,
    patience=20,
    augment=True,
    cache=False,
    workers=4,
    verbose=True
)

print("\n✅ 训练完成！")

print("\n验证模型...")
metrics = model.val()
print(f"准确率: {metrics.top1:.2%}")

print("\n导出为 TorchScript...")
export_path = model.export(format='torchscript')
print(f"✅ 已导出到: {export_path}")

print("\n" + "=" * 60)
print("全部完成！新模型在 runs/classify/train/weights/ 目录下")
print("=" * 60)
