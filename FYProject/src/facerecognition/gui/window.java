/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognition.gui;

/**
 *
 * @author Du
 */
import facerecognition.javafaces.FaceRec;
import static facerecognition.javafaces.FaceRec.debug;
import static facerecognition.javafaces.FaceRec.printError;
import facerecognition.javafaces.MatchResult;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import javax.swing.*;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Core;
import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
import static org.opencv.core.Core.rectangle;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import static org.opencv.highgui.Highgui.imwrite;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import org.opencv.objdetect.CascadeClassifier;

class My_Panel extends JPanel {

    private static final long serialVersionUID = 1L;
    private BufferedImage image;
    // Create a constructor method  

    public My_Panel() {
        super();
    }

    /**
     * Converts/writes a Mat into a BufferedImage.
     *
     * @param matrix Mat of type CV_8UC3 or CV_8UC1
     * @return BufferedImage of type TYPE_3BYTE_BGR or TYPE_BYTE_GRAY
     */
    public boolean MatToBufferedImage(Mat matBGR) {
        long startTime = System.nanoTime();
        int width = matBGR.width(), height = matBGR.height(), channels = matBGR.channels();
        byte[] sourcePixels = new byte[width * height * channels];
        matBGR.get(0, 0, sourcePixels);
        // create new image and get reference to backing data  
        image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.length);
        long endTime = System.nanoTime();
        System.out.println(String.format("Elapsed time: %.2f ms", (float) (endTime - startTime) / 1000000));
        return true;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.image == null) {
            return;
        }
        g.drawImage(this.image, 10, 10, this.image.getWidth() + 250, this.image.getHeight(), null);
        //g.drawString("This is my custom Panel!",10,20);  
    }
}

class processor {

    private CascadeClassifier face_cascade;
    // Create a constructor method  

    public processor() {
        face_cascade = new CascadeClassifier("D:\\FYPfinal\\FYProject\\data\\haarcascade_frontalface_alt.xml");
        if (face_cascade.empty()) {
            System.out.println("--(!)Error loading A\n");
            return;
        } else {
            System.out.println("Face classifier loooaaaaaded up");
        }
    }

    public Mat detect(Mat inputframe) {
        Mat mRgba = new Mat();
        Mat mGrey = new Mat();
        MatOfRect faces = new MatOfRect();
        inputframe.copyTo(mRgba);
        inputframe.copyTo(mGrey);
        Imgproc.cvtColor(mRgba, mGrey, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(mGrey, mGrey);
        face_cascade.detectMultiScale(mGrey, faces);

        Rect rect_Crop = null;
        for (Rect rect : faces.toArray()) {

            rectangle(mRgba, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 255), 2, 3, 0);
            rect_Crop = new Rect(rect.x, rect.y, rect.width, rect.height);

            Mat image_roi = new Mat(mRgba, rect_Crop);
            Size sz = new Size(220, 220);
            Imgproc.resize(image_roi, image_roi, sz);
            Imgproc.cvtColor(image_roi, image_roi, COLOR_BGR2GRAY);
            Imgproc.equalizeHist(image_roi, image_roi);
            imwrite("D:\\FYPfinal\\FYProject\\PreIMG\\CropVideo.jpg", image_roi);

            String imgToCheck = "D:\\FYPfinal\\FYProject\\PreIMG\\CropVideo.jpg";
            String imgDir = "D:\\FYPfinal\\FYProject\\Face Database";

            String numFaces = "4";
            String thresholdVal = "1";
            MatchResult r = new FaceRec().processSelections(imgToCheck, imgDir, numFaces, thresholdVal);
            Path p = Paths.get(r.getMatchFileName());
            String file = p.getFileName().toString();
            String fileNameWithOutExt = FilenameUtils.removeExtension(file);

            if (r.getMatchSuccess()) {
                if (r.getMatchDistance() < 0.2) {

                    debug(imgToCheck + " matches " + r.getMatchFileName() + " at distance=" + r.getMatchDistance());
                    DecimalFormat df = new DecimalFormat("#.00");
                    double s = 100 - r.getMatchDistance() * 100;
                    String box_text = String.format("Prediction = " + fileNameWithOutExt);
                    Core.putText(mRgba, box_text, new Point(rect.x + 20, rect.y - 20), FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 0), 2);
                    MainMenu.VidSimilarity.setText("" + df.format(s) + "%");
                    MainMenu.VideoNameDisplay.setText(file);
                    MainMenu.FaceDetectedDisplay.setText("" + faces.toArray().length);
                    MainMenu.DatabaseStatus.setText("Found");
                    //MainMenu.AddImgToDB.print(MainMenu.g);
                    MainMenu.AddImgToDB.setVisible(true);
                } else {
                    debug(imgToCheck + " matches " + r.getMatchFileName() + " at distance=" + r.getMatchDistance());
                    printError("match failed:" + r.getMatchMessage());
                    String UnknownBox_text = String.format(" Attention: Unknown Person! ");
                    Core.putText(mRgba, UnknownBox_text, new Point(rect.x + 20, rect.y - 20), FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 0, 255), 2);
                    MainMenu.VideoNameDisplay.setText("Not Recognized");
                    MainMenu.FaceDetectedDisplay.setText("" + faces.toArray().length);
                    MainMenu.DatabaseStatus.setText("Not Found");
                    //generateAndSendEmail();
                }
            } else {
                debug(imgToCheck + " matches " + r.getMatchFileName() + " at distance=" + r.getMatchDistance());
                printError("match failed:" + r.getMatchMessage());
                String UnknownBox_text = String.format(" Attention: Unknown Person! ");
                Core.putText(mRgba, UnknownBox_text, new Point(rect.x + 20, rect.y - 20), FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(255, 155, 155), 2);
                MainMenu.VideoNameDisplay.setText(" Not Recognized ");
                MainMenu.FaceDetectedDisplay.setText("" + faces.toArray().length);
                MainMenu.DatabaseStatus.setText(" Not Found ");
                MainMenu.VidSimilarity.setText(" null ");
                //generateAndSendEmail();

            }
            /*
                Point center= new Point(rect.x + rect.width*0.5, rect.y + rect.height*0.5 );  
                Core.ellipse( mRgba, center, new Size( rect.width*0.5, rect.height*0.5), 0, 0, 360, new Scalar( 255, 0, 255 ), 4, 8, 0 ); 
             */
        }
        return mRgba;
    }
}

public class window {

    public static void main(String arg[]) {
        // Load the native library.  
        System.load("C:/opencv/build/java/x64/opencv_java2411.dll");
        String window_name = "Capture - Face detection";
        JFrame frame = new JFrame(window_name);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        processor my_processor = new processor();
        My_Panel my_panel = new My_Panel();
        frame.setContentPane(my_panel);
        frame.setVisible(true);
        //-- 2. Read the video stream  
        Mat webcam_image = new Mat();
        VideoCapture capture = new VideoCapture(0);
        if (capture.isOpened()) {
            while (true) {
                capture.read(webcam_image);
                if (!webcam_image.empty()) {
                    frame.setSize(webcam_image.width() + 40, webcam_image.height() + 60);
                    //-- 3. Apply the classifier to the captured image  
                    webcam_image = my_processor.detect(webcam_image);
                    //-- 4. Display the image  
                    my_panel.MatToBufferedImage(webcam_image); // We could look at the error...  
                    my_panel.repaint();
                } else {
                    System.out.println(" --(!) No captured frame -- Break!");
                    break;
                }
            }
        }
        return;
    }
}
