package io.javasmithy.inference;

import org.tensorflow.ndarray.Shape;
import org.tensorflow.types.TFloat32;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class Preprocess {
    static ExecutorService pool = Executors.newFixedThreadPool(128);

    public static TFloat32 preprocess(List<BufferedImage> sourceImages, int imageHeight, int imageWidth, int imageChannels) {
        Shape imageShape = Shape.of(sourceImages.size(), imageHeight, imageWidth, imageChannels);

        return TFloat32.tensorOf(imageShape, tensor -> {
            // Copy all images to the tensor
            int imageIdx = 0;
            for (BufferedImage sourceImage : sourceImages) {
                // Scale the image to required dimensions if needed
                BufferedImage image;
                if (sourceImage.getWidth() != imageWidth || sourceImage.getHeight() != imageHeight) {
                    image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
                    Graphics2D graphics = image.createGraphics();
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    graphics.drawImage(sourceImage, 0, 0, imageWidth, imageHeight, null);
                    graphics.dispose();
                } else {
                    image = sourceImage;
                }

                // Converts the image to floats and normalize by subtracting mean values
                int i = 0;
                System.out.println(tensor.get().size());
                for (long h = 0; h < imageHeight; ++h) {
                    for (long w = 0; w < imageWidth; ++w)  {
                        // "caffe"-style normalization
                        System.out.println("\r h: " + h + " w: " + w + " i: " + i);
                        tensor.setFloat(image.getData().getDataBuffer().getElemFloat(i++) - 103.939f, imageIdx, h, w, 0);
                        tensor.setFloat(image.getData().getDataBuffer().getElemFloat(i++) - 116.779f, imageIdx, h, w, 1);
                        tensor.setFloat(image.getData().getDataBuffer().getElemFloat(i++) - 123.68f, imageIdx, h, w, 2);
                    }
                }
                ++imageIdx;
            }
        });
    }
}