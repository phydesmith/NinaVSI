# NinaVSI

## What's in the name?
 It is a visual-simulation interface and acts as a bridge between a first-person POV game with WASD input and an object detection model. The problem constraint was to only gather 
 information about the "simulation" via image data, with no access to any sort of in-game memory. It is dedicated to my eldest daughter Nina.

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


 
