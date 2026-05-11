
import torch
from ultralytics import YOLO

print("=" * 60)
print("检查 YOLOv8 模型信息")
print("=" * 60)

model_path = 'E:/gcsj4/gcsj4/model/yolov8s-cls.pt'

try:
    model = YOLO(model_path)

    print(f"\n模型类型: {type(model)}")
    print(f"模型任务: {model.task}")

    if hasattr(model, 'names'):
        print(f"\n模型类别名称:")
        print(model.names)
        print(f"类别数量: {len(model.names)}")
    else:
        print("\n警告: 模型没有 'names' 属性")

    if hasattr(model, 'model'):
        print(f"\n模型架构: {model.model}")

        if hasattr(model.model, 'names'):
            print(f"\n内部类别名称:")
            print(model.model.names)

    print("\n" + "=" * 60)
    print("模型检查完成")
    print("=" * 60)

except Exception as e:
    print(f"\n错误: {e}")
    print("\n尝试直接加载 TorchScript...")

    try:
        ts_model = torch.jit.load(model_path)
        print(f"TorchScript 模型加载成功")

        dummy_input = torch.randn(1, 3, 640, 640)
        output = ts_model(dummy_input)

        print(f"输入形状: {dummy_input.shape}")
        print(f"输出形状: {output.shape}")
        print(f"输出类别数: {output.shape[1]}")

    except Exception as e2:
        print(f"直接加载也失败: {e2}")

