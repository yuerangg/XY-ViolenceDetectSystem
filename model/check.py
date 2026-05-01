
import os
from pathlib import Path

def check_yolo_dataset():
    """检查YOLO数据集结构和标签格式"""

    base_paths = {
        'train': Path('D:/dataset/train'),
        'val': Path('D:/dataset/val')
    }

    print("=" * 60)
    print("YOLO 数据集结构检查")
    print("=" * 60)

    for split, base_path in base_paths.items():
        print(f"\n{split.upper()} 集检查:")
        print("-" * 60)

        images_dir = base_path / 'images'
        labels_dir = base_path / 'labels'

        # 检查目录是否存在
        if not images_dir.exists():
            print(f"❌ 错误: {images_dir} 不存在!")
            continue
        if not labels_dir.exists():
            print(f"❌ 错误: {labels_dir} 不存在!")
            continue

        print(f"✓ images 目录: {images_dir}")
        print(f"✓ labels 目录: {labels_dir}")

        # 统计文件数量
        image_files = list(images_dir.glob('*.jpg')) + list(images_dir.glob('*.png'))
        label_files = list(labels_dir.glob('*.txt'))

        print(f"  图片数量: {len(image_files)}")
        print(f"  标签数量: {len(label_files)}")

        if len(image_files) != len(label_files):
            print(f"⚠️  警告: 图片和标签数量不匹配!")

        # 检查标签格式
        if label_files:
            print(f"\n  标签格式抽样检查 (前3个):")
            for i, label_file in enumerate(label_files[:3]):
                try:
                    with open(label_file, 'r') as f:
                        lines = f.readlines()

                    print(f"    {label_file.name}: {len(lines)} 个目标")

                    # 检查每行格式
                    for j, line in enumerate(lines):
                        parts = line.strip().split()
                        if len(parts) != 5:
                            print(f"      ❌ 第{j+1}行格式错误: {line.strip()}")
                        else:
                            class_id = int(parts[0])
                            if class_id not in [0, 1]:
                                print(f"      ❌ 第{j+1}行类别ID错误: {class_id} (应为0或1)")
                            else:
                                class_name = "normal" if class_id == 0 else "violence"
                                print(f"      ✓ 类别: {class_name}, 坐标: {parts[1:]}")

                except Exception as e:
                    print(f"    ❌ 读取失败: {e}")

    print("\n" + "=" * 60)
    print("检查完成!")
    print("=" * 60)

if __name__ == "__main__":
    check_yolo_dataset()
