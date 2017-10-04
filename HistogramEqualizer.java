/*
    Vittorio Longi
    CS 559 - Computer Vision
    Assignment 2, Problem B
    9/16/2017
 */
package Histogram;

import java.awt.*;
import java.awt.image.*;
import javax.imageio.*;
import java.util.Scanner;
import java.io.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.io.IOException;
import javax.swing.*;


public class HistogramEqualizer extends Component {

    private void startHistogram() {
        System.out.println("Starting HistogramEqualizer...");
        BufferedImage image = getImage();
        display(image, "Original Image");

        // HISTOGRAM
        System.out.println("\nGetting histogram...");
        int[] histogramArray = calculateHistogram(image);
        BufferedImage histogram = getHistogram(image, histogramArray);
        display(histogram, "Histogram");
        try {
            File outputfile1 = new File("histogram.png");
            ImageIO.write(histogram, "png", outputfile1);
        } catch (IOException e) {
            System.out.println("Error exporting histogram");
        }

        // EQUALIZE IMAGE
        System.out.println("\nEqualizing image...");
        double[] cumulativeHistogramArray = calculateCumulativeHistogram(image, histogram, histogramArray);
        BufferedImage newImage = applyEqualization(image, cumulativeHistogramArray);
        display(newImage, "Equalized Image");
        try {
            File outputfile2 = new File("equalizedImage.png");
            ImageIO.write(newImage, "png", outputfile2);
        } catch (IOException e) {
            System.out.println("Error exporting equalizedImage");
        }

        // EQUALIZED HISTOGRAM
        System.out.println("\nGetting equalized histogram...");
        int[] newHistogramArray = calculateHistogram(newImage);
        BufferedImage tempEqualizedHistogram = getHistogram(newImage, newHistogramArray);

        // Resize
        AffineTransform at = new AffineTransform();
        at.scale(1.0, 0.5);
        AffineTransformOp scaleOp =
                new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage equalizedHistogram = new BufferedImage(tempEqualizedHistogram.getWidth(),
                tempEqualizedHistogram.getHeight()/2, BufferedImage.TYPE_INT_ARGB);
        equalizedHistogram = scaleOp.filter(tempEqualizedHistogram, equalizedHistogram);

        display(equalizedHistogram, "Equalized Histogram");
        //BufferedImage equalizedHistogram = getEqualizedHistogram(image, histogram, equalizedArray);
        try {
            File outputfile3 = new File("equalizedHistogram.png");
            ImageIO.write(equalizedHistogram, "png", outputfile3);
        } catch (IOException e) {
            System.out.println("Error exporting equalizedHistogram");
        }

        System.out.println("\nProgram end");

    }

    public BufferedImage getImage() {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter filename.extension: ");
        String fileName = scan.next();
        File imageFile = new File(fileName);
        //Scanner inputFile = new Scanner(imageFile);
        BufferedImage image = null;
        try {
            image = ImageIO.read(imageFile);
            System.out.println("Successfully opened image (size: " + image.getWidth() + "x" + image.getHeight() + ")");
        } catch (Exception e){
            System.out.println("Error reading image");
        }
        return image;
    }

    public int[] calculateHistogram(BufferedImage image) {
        int imageDepth = image.getColorModel().getPixelSize();
        int imageValues = (int) (Math.pow(2, imageDepth));
        int[] values = new int[imageValues];

        for (int i = 0; i < image.getWidth(); i++) {              // loop from 0 to width-1
            for (int j = 0; j < image.getHeight(); j++) {         // loop from 0 to height-1
                values[image.getData().getSample(i, j, 0)]++;
            }
        }
        return values;
    }

    public BufferedImage getHistogram(BufferedImage image, int[] values) {
        int imageDepth = image.getColorModel().getPixelSize();
        int imageValues = (int)(Math.pow(2,imageDepth));
        int maxValue = 0;
        for (int value : values) {
            if (value > maxValue)
                maxValue = value;
        }

        BufferedImage histogram =
                new BufferedImage(imageValues*3, maxValue, BufferedImage.TYPE_BYTE_GRAY);

        for (int i = 0; i < values.length; i++) {       // loop through each value bin
            for (int j = 0; j < values[i]; j++) {       // loop from 0 to value in bin i
                histogram.setRGB((i*3), histogram.getHeight() - j - 1, 255);
                histogram.setRGB((i*3)+1, histogram.getHeight() - j - 1, 255);
            }
        }

        // Resize histogram
        AffineTransform at = new AffineTransform();
        at.scale(1.0, 0.25);
        AffineTransformOp scaleOp =
                new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        BufferedImage scaledHistogram = new BufferedImage(imageValues*3, maxValue/4, BufferedImage.TYPE_INT_ARGB);
        scaledHistogram = scaleOp.filter(histogram, scaledHistogram);
        return scaledHistogram;
    }

    public double[] calculateCumulativeHistogram(BufferedImage image, BufferedImage histogram, int[] values) {
        float alpha = 255.0f / (image.getWidth()*image.getHeight());
        double[] cumulativeHistogram = new double[values.length];    // array for cumulative histogram

        cumulativeHistogram[0] = alpha*values[0];                   // first value of cumulative histogram
        for (int i = 1; i < values.length; i++) {
            cumulativeHistogram[i] = cumulativeHistogram[i - 1] + (alpha * values[i]);
        }
        return cumulativeHistogram;
    }

    public BufferedImage applyEqualization(BufferedImage image, double[] equalizedArray){
        BufferedImage newImage = image;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                newImage.setRGB(i, j, 3*(int)equalizedArray[image.getData().getSample(i, j, 0)]);
            }
        }
        return newImage;
    }

    private void display(BufferedImage image, String title) {
        JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new JLabel(new ImageIcon(image)), BorderLayout.WEST);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    public static void main(String[] argv) {
        try {
            new HistogramEqualizer().startHistogram();
        }
        catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

}
