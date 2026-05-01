import os
import cv2
import uuid


def extract(video_folder, save_folder):
    os.makedirs(save_folder, exist_ok=True)
    for name in os.listdir(video_folder):
        path = os.path.join(video_folder, name)
        cap = cv2.VideoCapture(path)
        fps = int(cap.get(cv2.CAP_PROP_FPS))
        count = 0
        while True:
            ret, frame = cap.read()
            if not ret: break
            if count % (fps * 2) == 0:
                img_path = os.path.join(save_folder, f"{uuid.uuid4()}.jpg")
                cv2.imwrite(img_path, frame)
            count += 1
        cap.release()


# 把你的暴力视频 → 转图片
extract("D:/videos/train/violence", "D:/dataset/train/violence")

# 把你的正常视频 → 转图片
extract("D:/videos/train/normal", "D:/dataset/train/normal")
