#!/usr/bin/env python3
"""
Download ssd_mobilenet_v2_coco from TensorFlow Model Zoo and convert to ONNX.
Outputs: ssd_mobilenet_v2.onnx with post-processed detection outputs.

Usage:
    pip install tf2onnx tensorflow --break-system-packages
    python3 convert_ssd_v2.py
"""
import os
import sys
import tarfile
import urllib.request
import subprocess
import shutil

MODEL_URL = "http://download.tensorflow.org/models/object_detection/ssd_mobilenet_v2_coco_2018_03_29.tar.gz"
TAR_FILE = "ssd_mobilenet_v2_coco.tar.gz"
EXTRACT_DIR = "ssd_mobilenet_v2_coco_2018_03_29"
FROZEN_GRAPH = os.path.join(EXTRACT_DIR, "frozen_inference_graph.pb")
OUTPUT_ONNX = "ssd_mobilenet_v2.onnx"

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)

    # 1. Download
    if not os.path.exists(TAR_FILE):
        print(f"Downloading {MODEL_URL} ...")
        urllib.request.urlretrieve(MODEL_URL, TAR_FILE)
        print(f"Downloaded {TAR_FILE}")

    # 2. Extract
    if not os.path.exists(FROZEN_GRAPH):
        print("Extracting...")
        with tarfile.open(TAR_FILE, "r:gz") as tar:
            tar.extractall()
        print(f"Extracted to {EXTRACT_DIR}")

    # 3. Convert to ONNX
    print("Converting to ONNX with tf2onnx...")
    cmd = [
        sys.executable, "-m", "tf2onnx.convert",
        "--input", FROZEN_GRAPH,
        "--output", OUTPUT_ONNX,
        "--inputs", "image_tensor:0",
        "--outputs",
        "detection_boxes:0,detection_scores:0,num_detections:0,detection_classes:0",
        "--opset", "13",
    ]
    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print("STDERR:", result.stderr)
        sys.exit(1)

    print(f"Created {OUTPUT_ONNX} ({os.path.getsize(OUTPUT_ONNX) / 1e6:.1f} MB)")

    # 4. Verify
    import onnxruntime as ort
    session = ort.InferenceSession(OUTPUT_ONNX)
    print("Inputs:")
    for inp in session.get_inputs():
        print(f"  {inp.name}: {inp.shape} {inp.type}")
    print("Outputs:")
    for out in session.get_outputs():
        print(f"  {out.name}: {out.shape} {out.type}")

    # 5. Cleanup
    shutil.rmtree(EXTRACT_DIR, ignore_errors=True)
    os.remove(TAR_FILE)
    print("Done! Cleaned up temp files.")

if __name__ == "__main__":
    main()
