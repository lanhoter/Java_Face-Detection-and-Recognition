/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facerecognition.gui;

import facerecognition.javafaces.FaceRec;
import static facerecognition.javafaces.FaceRec.debug;
import static facerecognition.javafaces.FaceRec.printError;
import facerecognition.javafaces.MatchResult;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.io.FilenameUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import org.opencv.objdetect.CascadeClassifier;

/**
 *
 * @author OldSpice
 */
public class Login extends JPanel implements ActionListener {

    private BufferedImage image;
    private JButton button = new JButton("Recognize and Enter the System");
    int count = 1;
    String timeStamp = new SimpleDateFormat("dd/MM/yyyy    HH:mm:ss").format(Calendar.getInstance().getTime());
    int i = 0;

    public Login() {
        super();
        button.addActionListener((ActionListener) this);
        this.add(button);

    }

    private BufferedImage getimage() {
        return image;
    }

    private void setimage(BufferedImage newimage) {
        image = newimage;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.image == null) {
            return;
        }
        g.drawImage(this.image, 100, 50, 400, 300, null);
        button.setLocation(200, 400);
    }

    public static void main(String args[]) throws Exception {

        int Timer = 0;
        //ProgressBaR pr = new ProgressBaR();
        Rect rect_Crop = null;

        JFrame frame = new JFrame("Login Face Verification");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 380);
        frame.setLocation(400, 150);
        System.load("C:/opencv/build/java/x64/opencv_java2411.dll");
        CascadeClassifier faceDetector = new CascadeClassifier("D:\\FYPfinal - Update\\FYProject\\data\\haarcascade_frontalface_alt.xml");
        //CascadeClassifier faceDetector = new CascadeClassifier("D:\\NetBeansProjects\\FYProject\\data\\lbpcascade_frontalface.xml");
        Login toc = new Login();

        frame.add(toc);;
        frame.setVisible(true);
        Mat webcam_image = new Mat();
        MatToBufImg mat2Buf = new MatToBufImg();
        VideoCapture capture = null;
        try {
            capture = new VideoCapture(0);
        } catch (Exception xx) {
            xx.printStackTrace();
        }
        if (capture.open(0)) {
            while (true) {
                capture.read(webcam_image);
                if (!webcam_image.empty()) {
                    frame.setSize(webcam_image.width(), webcam_image.height());
                    MatOfRect faceDetections = new MatOfRect();
                    faceDetector.detectMultiScale(webcam_image, faceDetections);
                    for (Rect rect : faceDetections.toArray()) {
                        Core.rectangle(webcam_image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 255, 255), 2);// mat2Buf, mat2Buf);
                        rect_Crop = new Rect(rect.x, rect.y, rect.width, rect.height);

                        Mat image_roi = new Mat(webcam_image, rect_Crop);
                        Size sz = new Size(220, 220);
                        Imgproc.resize(image_roi, image_roi, sz);
                        Imgproc.cvtColor(image_roi, image_roi, COLOR_BGR2GRAY);
                        Imgproc.equalizeHist(image_roi, image_roi);
                        Highgui.imwrite("D:\\FYPfinal - Update\\FYProject\\LoginPerson.jpg", image_roi);
                    }
                    mat2Buf.setMatrix(webcam_image, ".jpg");
                    toc.setimage(mat2Buf.getBufferedImage());
                    toc.repaint();
                } else {
                    System.out.println("problems with webcam image capture");
                    break;
                }
            }
        }
        capture.release();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String imgToCheck = "D:\\FYPfinal - Update\\FYProject\\LoginPerson.jpg";
        String imgDir = "D:\\FYPfinal - Update\\FYProject\\Face Database";
        String numFaces = "2";
        String thresholdVal = "1";
        MatchResult r = new FaceRec().processSelections(imgToCheck, imgDir, numFaces, thresholdVal);
        Path p = Paths.get(r.getMatchFileName());
        String file = p.getFileName().toString();
        String fileNameWithOutExt = FilenameUtils.removeExtension(file);
        DecimalFormat df = new DecimalFormat("#.00");
        double s = 100 - r.getMatchDistance() * 100;

        if (r.getMatchSuccess()) {

            if (r.getMatchDistance() < 0.048) {
                String box_text;
                debug(imgToCheck + " matches " + r.getMatchFileName() + " at distance=" + r.getMatchDistance());

                box_text = String.format("Prediction = " + fileNameWithOutExt);
                //Core.putText(webcam_image, box_text, new Point(rect.x + 20, rect.y - 20), FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 255, 0), 2);

                /*
                 VidSimilarity.setText("" + df.format(s));
                 VideoNameDisplay.setText(file);
                 FaceDetectedDisplay.setText("" + faceDetections.toArray().length);
                 DatabaseStatus.setText("Found");
                 AddImgToDB.print(g);
                 AddImgToDB.setVisible(true);
                 */
                javax.swing.JOptionPane.showMessageDialog(null,
                        "Welcome to Automated Face Detection and Recognition System\n"
                        + "\nThe Person has been idenfied as " + fileNameWithOutExt
                        + "\nSimilarity: " + df.format(s) + "%"
                        + "\nLoginTime: " + timeStamp
                        + "\nStatus: Verified", "Notification", 2);

                Path source = Paths.get("D:\\FYPfinal - Update\\FYProject\\LoginPerson.jpg");
                Path targetDir = Paths.get("D:\\FYPfinal - Update\\FYProject\\LoginRecords");

                try {
                    Files.createDirectories(targetDir);//in case target directory didn't exist

                    Path target = targetDir.resolve(fileNameWithOutExt + i + ".jpg");// create new path ending with `name` content
                    System.out.println("Saved Login Person Image into " + target);

                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    // I decided to replace already existing files with same name
                } catch (IOException ex) {
                    Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
                }

                //l.dispose();
                VideoCapture camera = new VideoCapture(0);
                camera.release();
                //MainMenu frame = new MainMenu();
                //frame.setVisible(true);
                // mm.setVisible(true);
                new MainMenu().setVisible(true);
            } else {
                printError("match failed:" + r.getMatchMessage());
                //String UnknownBox_text = String.format(" Attention: Unknown Person! ");
                //Core.putText(webcam_image, UnknownBox_text, new Point(rect.x + 20, rect.y - 20), FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(0, 0, 255), 2);
                //VideoNameDisplay.setText("Not Recognized");
                // FaceDetectedDisplay.setText("" + faceDetections.toArray().length);
                //DatabaseStatus.setText("Not Found");
                javax.swing.JOptionPane.showMessageDialog(null, "Sorry, You dont have the perssion to access the system \nPlease Try Again \n LoginTime: " + timeStamp, "Notification", 2);
                /*
                try {
                    generateAndSendAlert();
                } catch (MessagingException ex) {
                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                }
                 */

            }

        } else {
            printError("match failed:" + r.getMatchMessage());
            //String UnknownBox_text = String.format(" Attention: Unknown Person! ");
            //Core.putText(webcam_image, UnknownBox_text, new Point(rect.x + 20, rect.y - 20), FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(255, 155, 155), 2);
            //VideoNameDisplay.setText(" Not Recognized ");
            //FaceDetectedDisplay.setText("" + faceDetections.toArray().length);
            //DatabaseStatus.setText(" Not Found ");
            //VidSimilarity.setText(" null ");

            javax.swing.JOptionPane.showMessageDialog(null, "Sorry, You dont have the perssion to access the system \nPlease Try Again", "Notification", 2);

            /*
                try {
                    generateAndSendAlert();
                } catch (MessagingException ex) {
                    Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
                }
             */
        }

    }
}
