import cv2
import numpy as np

def cartoonify(input_path: str, output_path: str) -> str:
    img = cv2.imread(input_path)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    gray = cv2.medianBlur(gray, 5)
    edges = cv2.adaptiveThreshold(
        gray, 255,
        cv2.ADAPTIVE_THRESH_MEAN_C, cv2.THRESH_BINARY,
        blockSize=9, C=9
    )
    color = cv2.bilateralFilter(img, 9, 300, 300)
    cartoon = cv2.bitwise_and(color, color, mask=edges)
    cv2.imwrite(output_path, cartoon)
    return output_path

def sketch(input_path: str, output_path: str) -> str:
    gray = cv2.imread(input_path, cv2.IMREAD_GRAYSCALE)
    inv = 255 - gray
    blur = cv2.GaussianBlur(inv, (21, 21), sigmaX=0, sigmaY=0)
    pencil = cv2.divide(gray, 255 - blur, scale=256)
    cv2.imwrite(output_path, pencil)
    return output_path

def comic(input_path: str, output_path: str) -> str:
    img = cv2.imread(input_path)
    h, w = img.shape[:2]

    # Downsample to speed up k-means
    small = cv2.resize(img, (w // 4, h // 4), interpolation=cv2.INTER_AREA)

    data = np.float32(small.reshape((-1, 3)))
    criteria = (cv2.TERM_CRITERIA_EPS + cv2.TERM_CRITERIA_MAX_ITER, 20, 0.001)
    _, labels, centers = cv2.kmeans(
        data, K=8, bestLabels=None,
        criteria=criteria, attempts=10,
        flags=cv2.KMEANS_RANDOM_CENTERS
    )
    centers = np.uint8(centers)
    result_small = centers[labels.flatten()].reshape(small.shape)

    # Upscale back to original size
    result = cv2.resize(result_small, (w, h), interpolation=cv2.INTER_LINEAR)
    cv2.imwrite(output_path, result)
    return output_path
