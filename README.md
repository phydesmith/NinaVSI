# NinaVSI

## What's in the name?
 This project is dedicated to, and named after, my awesome and amazing eldest daughter Nina.

 VSI means visual-simulation interface and acts as a bridge between a first-person POV game with WASD input and an object detection model. The problem constraint was to only gather 
 information about the "simulation" via image data, with no access to any sort of in-game memory.

This application was written for use with my Master's Thesis found [here](https://epublications.marquette.edu/theses_open/737/).
As such, classes are currently hardcoded, as well as the triggering of the object avoidance algorithm during detection.

## How it works
This was written to use models exported with the TensorFlow Object Detection API version 2.7.
The models were trained using transfer learning on a novel dataset. It consisted of 6,600 images and 17,000 annotations.
Some of the annotations were synthetic objects (generated with GAN). Benchmarks can be found in the 
previously linked paper.

To interact with the exported models, the Java TensorFlow API is used to load them. The Java AWT Robot API
is used to capture screenshots and write it to disk. When continuous capture is toggled, the robot periodically
captures and the images are sent to the model for inference. All of this is then displayed using 
JavaFX. Finally, the detections are avoided using key-inputs by the Java AWT Robot API, with the 
assumption. 

## How to run
From root project folder:
-  `./scripts/shade-run.sh`
- requires maven

## warning: toggling `object avoidance` may result in keystrokes being sent to the main monitor. It may be tricky to stop the captures. 
## Also, this program is GPU intensive and may lag a bit at first. 

## How to use
- When the program starts, you need to go to file -> load model
- in the project directory, choose the `sample-files/mobilenet/saved-model` folder, this contains the .pb
- go to `capture -> toggle continuous capture` and it will start to detect in the viewport, which is a 640x640 square on the system's primary monitor.
- Parameters under the viewport dictate how fast screenshots are captured, what confidence threshold you want to prune at, the pulse for sending inferences to the model.
- The environment the model was trained on was 'Nal Hutta' in SWTOR. It probably will not work, or will get spurious detections on anything else. 
- ![image](https://github.com/phydesmith/NinaVSI/assets/31049044/268f1f55-63d4-4e59-8d02-b23cc5b85520)


 
