package facerecognition.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import facerecognition.javafaces.FaceRec;
import static facerecognition.javafaces.FaceRec.debug;
import static facerecognition.javafaces.FaceRec.printError;
import facerecognition.javafaces.MatchResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.VideoCapture;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import org.opencv.objdetect.CascadeClassifier;
import static org.opencv.core.Core.rectangle;
import static org.opencv.highgui.Highgui.imread;
import static org.opencv.highgui.Highgui.imwrite;

/**
 *
 * @author Du
 */
public class MainMenu extends javax.swing.JFrame {

    File targetFile;
    BufferedImage targetImg;
    String imagePath;

    //String box_text = format("Prediction = %d", prediction);
    Image img;
    private DaemonThread myThread = null;
    int count = 0;
    VideoCapture webSource = null;
    Mat frame = new Mat();
    MatOfByte mem = new MatOfByte();
    JLabel[] jFaceLabel;
    Mat img_hist_equalized;
    Handler fh;
    JFileChooser filechooser = null;
    JFileChooser dirchooser = null;
    BufferedImage buf = null;
    String box_text;
    File file;
//Email
    static Properties mailServerProperties;
    static Session getMailSession;
    static MimeMessage generateMailMessage;

    CascadeClassifier faceDetector = new CascadeClassifier("D:\\FYPfinal - Update\\FYProject\\data\\haarcascade_frontalface_alt.xml");
    //CascadeClassifier faceDetector = new CascadeClassifier("D:\\FYPfinal - Update\\FYProject\\data\\lbpcascade_frontalface.xml");
    MatOfRect faceDetections = new MatOfRect();
    Long startTime = System.currentTimeMillis();

    class DaemonThread implements Runnable {

        protected volatile boolean runnable = false;

        @Override
        public void run() {
            synchronized (this) {
                while (runnable) {
                    if (webSource.grab()) {

                        processor my_processor = new processor();
                        My_Panel my_panel = new My_Panel();
                        MainMenu.jInternalFrame1.setContentPane(my_panel);
                        jInternalFrame1.setVisible(true);
                        //-- 2. Read the video stream  
                        Mat webcam_image = new Mat();
                        VideoCapture capture = new VideoCapture(0);
                        if (capture.isOpened()) {
                            while (true) {
                                capture.read(webcam_image);
                                if (!webcam_image.empty()) {
                                    
                                    
                                    jInternalFrame1.setSize(webcam_image.width() + 280, webcam_image.height() - 30);
                                    //-- 3. Apply the classifier to the captured image  
                                    webcam_image = my_processor.detect(webcam_image);
                                    //-- 4. Display the image  
                                    my_panel.MatToBufferedImage(webcam_image); // We could look at the error...  
                                    //jInternalFrame1.paint(jInternalFrame1.getGraphics());
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
            }
        }
    }

    /*
                            webSource.retrieve(frame);

                            Graphics g = Video_Display.getGraphics();

                            Font font = new Font("Arial", 15, 15);
                            Color myredcolor = new Color(255, 255, 255);
                            g.setColor(myredcolor);
                            g.setFont(font);

                            g.drawString("Camera is processing frame by frame : [ Frame : " + (count++) + "]", 20, 20);
                            g.drawString("Detected  " + faceDetections.toArray().length + " Faces", 20, 40);

                            faceDetector.detectMultiScale(frame, faceDetections);

                            Rect rect_Crop = null;
                            for (Rect rect : faceDetections.toArray()) {

                                rectangle(frame, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                                        new Scalar(0, 255, 255), 2, 3, 0);

                                rect_Crop = new Rect(rect.x, rect.y, rect.width, rect.height);

                                Mat image_roi = new Mat(frame, rect_Crop);
                                Size sz = new Size(220, 220);
                                Imgproc.resize(image_roi, image_roi, sz);
                                Imgproc.cvtColor(image_roi, image_roi, COLOR_BGR2GRAY);
                                Imgproc.equalizeHist(image_roi, image_roi);
                                imwrite("D:\\FYPfinal - Update\\FYProject\\PreIMG\\CropVideo.jpg", image_roi);

                                String imgToCheck = "D:\\FYPfinal - Update\\FYProject\\PreIMG\\CropVideo.jpg";
                                String imgDir = "D:\\FYPfinal - Update\\FYProject\\Face Database";

                                String numFaces = "4";
                                String thresholdVal = "1";
                                MatchResult r = new FaceRec().processSelections(imgToCheck, imgDir, numFaces, thresholdVal);
                                Path p = Paths.get(r.getMatchFileName());
                                String file = p.getFileName().toString();
                                String fileNameWithOutExt = FilenameUtils.removeExtension(file);
                               
                                if (r.getMatchSuccess()) {
                                    if (r.getMatchDistance() < 0.28) {

                                        debug(imgToCheck + " matches " + r.getMatchFileName() + " at distance=" + r.getMatchDistance());
                                        DecimalFormat df = new DecimalFormat("#.00");
                                        double s = 100 - r.getMatchDistance() * 100;
                                        box_text = String.format("Prediction = " + fileNameWithOutExt);
                                        Core.putText(frame, box_text, new Point(rect.x + 20, rect.y - 20), FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 255, 0), 2);
                                        VidSimilarity.setText("" + df.format(s) + "%");
                                        VideoNameDisplay.setText(file);
                                        FaceDetectedDisplay.setText("" + faceDetections.toArray().length);
                                        DatabaseStatus.setText("Found");
                                        AddImgToDB.print(g);
                                        AddImgToDB.setVisible(true);
                                    } else {
                                        printError("match failed:" + r.getMatchMessage());
                                        String UnknownBox_text = String.format(" Attention: Unknown Person! ");
                                        Core.putText(frame, UnknownBox_text, new Point(rect.x + 20, rect.y - 20), FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(0, 0, 255), 2);
                                        VideoNameDisplay.setText("Not Recognized");
                                        FaceDetectedDisplay.setText("" + faceDetections.toArray().length);
                                        DatabaseStatus.setText("Not Found");
                                        //generateAndSendEmail();
                                    }
                                }
                                else {
                                    printError("match failed:" + r.getMatchMessage());
                                    String UnknownBox_text = String.format(" Attention: Unknown Person! ");
                                    Core.putText(frame, UnknownBox_text, new Point(rect.x + 20, rect.y - 20), FONT_HERSHEY_SIMPLEX, 0.8, new Scalar(255, 155, 155), 2);
                                    VideoNameDisplay.setText(" Not Recognized ");
                                    FaceDetectedDisplay.setText("" + faceDetections.toArray().length);
                                    DatabaseStatus.setText(" Not Found ");
                                    VidSimilarity.setText(" null ");
                                    //generateAndSendEmail();
                                
                                    
                            }

                            imencode(".bmp", frame, mem);
                            Image im = ImageIO.read(new ByteArrayInputStream(mem.toArray()));

                            //Long endTime = System.currentTimeMillis();
                            //System.out.println("Detected Face in " + (endTime - startTime) + " milliseconds");
                            BufferedImage buff = (BufferedImage) im;
                            if (g.drawImage(buff, 0, 0, getWidth() - 300, getHeight() - 150, 0, 0, buff.getWidth(), buff.getHeight(), null)) {
                                if (runnable == false) {
                                    System.out.println("Paused ..... ");
                                    this.wait();
                                }
                            
                            }
                            }
                        } catch (Exception ex) {
                            System.out.println("Error");
                        }

                    }
                }       
            }
        }
    }

    /**
     * Creates new form MainMenu
     */
    public MainMenu() {
        initComponents();

    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFrame1 = new javax.swing.JFrame();
        jFrame2 = new javax.swing.JFrame();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        Imag_Jframe = new javax.swing.JPanel();
        Load_Imgs = new javax.swing.JButton();
        Processing_Imgs = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        ImgName = new javax.swing.JTextField();
        FR_Imgs = new javax.swing.JButton();
        FD_Imgs = new javax.swing.JButton();
        FR_img = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        Dis_Images = new javax.swing.JLabel();
        ProcessImg = new javax.swing.JLabel();
        FD_Result = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        ImgdbStatus = new javax.swing.JTextField();
        ImgSimilarity = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        ImgAddFace = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        ImgPanelAddToDB = new javax.swing.JTextField();
        AddTemplates = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        VideoNameDisplay = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        FaceDetectedDisplay = new javax.swing.JTextField();
        DatabaseStatus = new javax.swing.JTextField();
        VidSimilarity = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        Cropface = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        AddImgToDB = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        BackGroundRemove = new javax.swing.JCheckBox();
        jInternalFrame1 = new javax.swing.JInternalFrame();
        jPanel5 = new javax.swing.JPanel();
        jframe = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        AddTodbBth = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel15 = new javax.swing.JLabel();
        fdImgName = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        SearchName = new javax.swing.JTextField();
        SearchImg = new javax.swing.JButton();
        FdataImgDisplay = new javax.swing.JPanel();

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jFrame2Layout = new javax.swing.GroupLayout(jFrame2.getContentPane());
        jFrame2.getContentPane().setLayout(jFrame2Layout);
        jFrame2Layout.setHorizontalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jFrame2Layout.setVerticalGroup(
            jFrame2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setLocation(new java.awt.Point(120, 60));
        setResizable(false);
        setSize(new java.awt.Dimension(1100, 700));

        jPanel1.setBorder(javax.swing.BorderFactory.createCompoundBorder());

        jLabel1.setFont(new java.awt.Font("Georgia", 0, 36)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Automated Face Detection and  Recognition Surveillance System");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        jTabbedPane1.setBackground(new java.awt.Color(0, 102, 204));
        jTabbedPane1.setToolTipText("");
        jTabbedPane1.setFont(new java.awt.Font("Georgia", 0, 16)); // NOI18N

        Imag_Jframe.setPreferredSize(new java.awt.Dimension(0, 446));

        Load_Imgs.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        Load_Imgs.setText("Load Image");
        Load_Imgs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Load_ImgsActionPerformed(evt);
            }
        });

        Processing_Imgs.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        Processing_Imgs.setText("Processing Image");
        Processing_Imgs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Processing_ImgsActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Georgia", 0, 16)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Original Image");

        jLabel3.setFont(new java.awt.Font("Georgia", 0, 16)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Processing Image");

        jLabel4.setFont(new java.awt.Font("Georgia", 0, 16)); // NOI18N
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Face Detection Result");

        jLabel6.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel6.setText("Predicted Name");

        ImgName.setEditable(false);
        ImgName.setFont(new java.awt.Font("Georgia", 0, 11)); // NOI18N

        FR_Imgs.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        FR_Imgs.setText("Face Reocognition");
        FR_Imgs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FR_ImgsActionPerformed(evt);
            }
        });

        FD_Imgs.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        FD_Imgs.setText("Face Detection");
        FD_Imgs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FD_ImgsActionPerformed(evt);
            }
        });

        FR_img.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        FR_img.setPreferredSize(new java.awt.Dimension(200, 200));

        javax.swing.GroupLayout FR_imgLayout = new javax.swing.GroupLayout(FR_img);
        FR_img.setLayout(FR_imgLayout);
        FR_imgLayout.setHorizontalGroup(
            FR_imgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 216, Short.MAX_VALUE)
        );
        FR_imgLayout.setVerticalGroup(
            FR_imgLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 216, Short.MAX_VALUE)
        );

        jLabel9.setFont(new java.awt.Font("Georgia", 0, 16)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Face Recognition Result");

        Dis_Images.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        ProcessImg.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));

        FD_Result.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        jLabel8.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel8.setText("Database");

        ImgdbStatus.setEditable(false);
        ImgdbStatus.setFont(new java.awt.Font("Georgia", 0, 11)); // NOI18N

        ImgSimilarity.setEditable(false);
        ImgSimilarity.setFont(new java.awt.Font("Georgia", 0, 11)); // NOI18N

        jLabel11.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel11.setText("Similarity");

        ImgAddFace.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Add Face Templates", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Georgia", 1, 14), new java.awt.Color(1, 1, 1))); // NOI18N

        jLabel14.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel14.setText("Name");

        ImgPanelAddToDB.setFont(new java.awt.Font("Georgia", 0, 11)); // NOI18N

        AddTemplates.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        AddTemplates.setText("Add Templates");
        AddTemplates.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddTemplatesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout ImgAddFaceLayout = new javax.swing.GroupLayout(ImgAddFace);
        ImgAddFace.setLayout(ImgAddFaceLayout);
        ImgAddFaceLayout.setHorizontalGroup(
            ImgAddFaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ImgAddFaceLayout.createSequentialGroup()
                .addComponent(jLabel14)
                .addGap(18, 18, 18)
                .addComponent(ImgPanelAddToDB, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
                .addComponent(AddTemplates)
                .addGap(29, 29, 29))
        );
        ImgAddFaceLayout.setVerticalGroup(
            ImgAddFaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ImgAddFaceLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ImgAddFaceLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(ImgPanelAddToDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AddTemplates))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout Imag_JframeLayout = new javax.swing.GroupLayout(Imag_Jframe);
        Imag_Jframe.setLayout(Imag_JframeLayout);
        Imag_JframeLayout.setHorizontalGroup(
            Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Imag_JframeLayout.createSequentialGroup()
                .addGap(55, 55, 55)
                .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Imag_JframeLayout.createSequentialGroup()
                        .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Dis_Images, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(Load_Imgs, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                        .addGap(48, 48, 48)
                        .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(Processing_Imgs, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                            .addComponent(ProcessImg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(Imag_JframeLayout.createSequentialGroup()
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ImgSimilarity, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Imag_JframeLayout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ImgName, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(Imag_JframeLayout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ImgdbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(48, 48, 48)
                .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(Imag_JframeLayout.createSequentialGroup()
                        .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 199, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(FD_Result, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(FD_Imgs, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE))
                        .addGap(46, 46, 46)
                        .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(FR_img, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                            .addComponent(FR_Imgs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(ImgAddFace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(85, Short.MAX_VALUE))
        );
        Imag_JframeLayout.setVerticalGroup(
            Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(Imag_JframeLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel9)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(Dis_Images, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FR_img, javax.swing.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)
                    .addComponent(ProcessImg, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(FD_Result, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Load_Imgs)
                    .addComponent(Processing_Imgs)
                    .addComponent(FD_Imgs, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FR_Imgs))
                .addGap(18, 18, 18)
                .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(Imag_JframeLayout.createSequentialGroup()
                        .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ImgName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ImgdbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(Imag_JframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ImgSimilarity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(ImgAddFace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(153, 153, 153))
        );

        jTabbedPane1.addTab("Static Image Face Recognition", Imag_Jframe);

        jPanel4.setPreferredSize(new java.awt.Dimension(0, 633));

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Face Recognition Result", javax.swing.border.TitledBorder.RIGHT, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Georgia", 1, 16))); // NOI18N
        jPanel3.setRequestFocusEnabled(false);

        jLabel5.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel5.setText("Name");

        jLabel7.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel7.setText("Face Detected");

        VideoNameDisplay.setEnabled(false);

        jLabel10.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel10.setText("Database");

        FaceDetectedDisplay.setFont(new java.awt.Font("Georgia", 0, 12)); // NOI18N
        FaceDetectedDisplay.setEnabled(false);
        FaceDetectedDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FaceDetectedDisplayActionPerformed(evt);
            }
        });

        DatabaseStatus.setEnabled(false);

        VidSimilarity.setEnabled(false);

        jLabel12.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel12.setText("Similarity");

        Cropface.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        Cropface.setText("Crop Face Image");
        Cropface.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CropfaceActionPerformed(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel13.setText("Name");

        jTextField1.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton5.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jButton5.setText("Add Face Templates");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        AddImgToDB.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));

        javax.swing.GroupLayout AddImgToDBLayout = new javax.swing.GroupLayout(AddImgToDB);
        AddImgToDB.setLayout(AddImgToDBLayout);
        AddImgToDBLayout.setHorizontalGroup(
            AddImgToDBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 96, Short.MAX_VALUE)
        );
        AddImgToDBLayout.setVerticalGroup(
            AddImgToDBLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 96, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel10))
                        .addGap(19, 19, 19))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addGap(1, 1, 1)))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(DatabaseStatus)
                    .addComponent(FaceDetectedDisplay, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(VideoNameDisplay, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(VidSimilarity, javax.swing.GroupLayout.Alignment.TRAILING)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField1))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(AddImgToDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 53, Short.MAX_VALUE)))
                .addContainerGap())
            .addComponent(Cropface, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(VideoNameDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(FaceDetectedDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(DatabaseStatus)
                    .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(VidSimilarity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Cropface)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(AddImgToDB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5)
                .addGap(146, 146, 146))
        );

        jButton1.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jButton1.setText("Start Face Recognition");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jButton2.setText("Pause");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        BackGroundRemove.setFont(new java.awt.Font("Georgia", 0, 12)); // NOI18N
        BackGroundRemove.setText("Remove BackGround");
        BackGroundRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackGroundRemoveActionPerformed(evt);
            }
        });

        jInternalFrame1.setVisible(true);

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 904, Short.MAX_VALUE)
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(187, 187, 187)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41)
                        .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(31, 31, 31)
                        .addComponent(BackGroundRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(156, 156, 156))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jInternalFrame1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jInternalFrame1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(BackGroundRemove)))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 424, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 69, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Video Surveillance", jPanel4);

        jPanel5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jPanel5MouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                jPanel5MouseEntered(evt);
            }
        });

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Templates Management", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Georgia", 0, 18))); // NOI18N

        AddTodbBth.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        AddTodbBth.setText("Add Face Template");
        AddTodbBth.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddTodbBthActionPerformed(evt);
            }
        });

        jButton4.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jButton4.setText("Display Database");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        jLabel15.setText("Name");

        jLabel16.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
        jLabel16.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel16MouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AddTodbBth, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                        .addGap(0, 22, Short.MAX_VALUE)
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jLabel15)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(fdImgName, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(19, 19, 19)))
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fdImgName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(28, 28, 28)
                .addComponent(AddTodbBth)
                .addGap(18, 18, 18)
                .addComponent(jButton4)
                .addContainerGap(32, Short.MAX_VALUE))
        );

        SearchName.setFont(new java.awt.Font("Georgia", 0, 12)); // NOI18N

        SearchImg.setFont(new java.awt.Font("Georgia", 0, 14)); // NOI18N
        SearchImg.setText("Search");
        SearchImg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SearchImgActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout FdataImgDisplayLayout = new javax.swing.GroupLayout(FdataImgDisplay);
        FdataImgDisplay.setLayout(FdataImgDisplayLayout);
        FdataImgDisplayLayout.setHorizontalGroup(
            FdataImgDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 782, Short.MAX_VALUE)
        );
        FdataImgDisplayLayout.setVerticalGroup(
            FdataImgDisplayLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 431, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jframeLayout = new javax.swing.GroupLayout(jframe);
        jframe.setLayout(jframeLayout);
        jframeLayout.setHorizontalGroup(
            jframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jframeLayout.createSequentialGroup()
                .addGroup(jframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jframeLayout.createSequentialGroup()
                        .addGap(527, 527, 527)
                        .addComponent(SearchName, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(SearchImg))
                    .addGroup(jframeLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(FdataImgDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jframeLayout.setVerticalGroup(
            jframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jframeLayout.createSequentialGroup()
                .addGroup(jframeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SearchImg)
                    .addComponent(SearchName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(FdataImgDisplay, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
            .addGroup(jframeLayout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(jframe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jframe, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Face Templates Management", jPanel5);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 526, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    
    
    private Mat doBackgroundRemoval(Mat frame) {
        // init
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();

        // threshold the image with the histogram average value
        hsvImg.create(frame.size(), CvType.CV_8U);
        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
        Core.split(hsvImg, hsvPlanes);

        double threshValue = this.getHistAverage(hsvImg, hsvPlanes.get(0));

        if (this.BackGroundRemove.isSelected()) {
            Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY_INV);
        } else {
            Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);
        }

        Imgproc.blur(thresholdImg, thresholdImg, new Size(5, 5));

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, 1), 6);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, 1), 6);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);

        // create the new image
        Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        frame.copyTo(foreground, thresholdImg);

        return foreground;
    }

    private double getHistAverage(Mat hsvImg, Mat hueValues) {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute the histogram
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // get the average for each bin
        for (int h = 0; h < 180; h++) {
            average += (hist_hue.get(h, 0)[0] * h);
        }

        return average = average / hsvImg.size().height / hsvImg.size().width;
    }

    public void ImgdetectFace() throws IOException {

        System.load("C:/opencv/build/java/x64/opencv_java2411.dll");
        CascadeClassifier faceImageDetector = new CascadeClassifier("D:\\FYPfinal - Update\\FYProject\\data\\haarcascade_frontalface_alt.xml");
        Mat image = imread("D:\\FYPfinal - Update\\FYProject\\PreIMG\\GrayscaleImg.jpg");

        MatOfRect faceImageDetections = new MatOfRect();

        faceImageDetector.detectMultiScale(image, faceImageDetections);
        //System.out.println(String.format("Detected %s faces", faceImageDetections.toArray().length));

        if (faceImageDetections.toArray().length <= 0) {

            FD_Result.setLayout(new BorderLayout(0, 0));
            FD_Result.add(new JLabel(new ImageIcon("D:\\FYPfinal - Update\\FYProject\\PreIMG\\Output.jpg")));
            setVisible(true);

            FR_img.setLayout(new BorderLayout(0, 0));
            FR_img.add(new JLabel(new ImageIcon("D:\\FYPfinal - Update\\FYProject\\Face Database\\Unknown.jpg")));
            setVisible(true);

            ImgName.setText("Unidentified Person");

        }

        Rect rectCrop = null;
        for (Rect rect : faceImageDetections.toArray()) {
            rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 255), 10, 10, 0);
            rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);

        }

        String filename = "ouput.jpg";
        System.out.println(String.format("Writing %s", filename));
        imwrite(filename, image);

        Mat image_roi = new Mat(image, rectCrop);

        Size sz = new Size(220, 220);
        Imgproc.resize(image_roi, image_roi, sz);
        Imgproc.cvtColor(image_roi, image_roi, COLOR_BGR2GRAY);
        Imgproc.equalizeHist(image_roi, image_roi);
        Highgui.imwrite("D:\\FYPfinal - Update\\FYProject\\PreIMG\\CropImg.jpg", image_roi);

        buf = ImageIO.read(new File("D:\\FYPfinal - Update\\FYProject\\ouput.jpg"));
        ImageIcon icon = new ImageIcon(file.getAbsolutePath());

        Image img = getScaledImage(buf, FD_Result.getWidth(), FD_Result.getHeight());
        icon = new ImageIcon(img);

        FD_Result.setIcon(icon);
    }

    private Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }


    private void BackGroundRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackGroundRemoveActionPerformed
        // TODO add your handling code here:
        if (!frame.empty()) {

            if (this.BackGroundRemove.isSelected()) {

                frame = this.doBackgroundRemoval(frame);

            }

        }
    }//GEN-LAST:event_BackGroundRemoveActionPerformed


    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:

        myThread.runnable = false;            // stop thread
        jButton2.setEnabled(false);   // activate start button
        jButton1.setEnabled(true);     // deactivate stop button
        webSource.release();  // stop caturing fron cam
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:

        webSource = new VideoCapture(0); // video capture from default cam
        myThread = new MainMenu.DaemonThread(); //create object of threat class
        Thread t = new Thread(myThread);
        t.setDaemon(true);
        myThread.runnable = true;
        t.start();                 //start thrad
        jButton1.setEnabled(false);  // deactivate start button
        jButton2.setEnabled(true);  //  activate stop button


    }//GEN-LAST:event_jButton1ActionPerformed


    private void FD_ImgsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FD_ImgsActionPerformed
        try {
            // TODO add your handling code here:
            ImgdetectFace();
        } catch (IOException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }

    }//GEN-LAST:event_FD_ImgsActionPerformed

    private void FR_ImgsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FR_ImgsActionPerformed

        long start = System.currentTimeMillis();

        String imgToCheck = "D:\\FYPfinal - Update\\FYProject\\PreIMG\\CropImg.jpg";
        String imgDir = "D:\\FYPfinal - Update\\FYProject\\Face Database";

        String numFaces = "4";
        String thresholdVal = "36000";
        MatchResult r = new FaceRec().processSelections(imgToCheck, imgDir, numFaces, thresholdVal);
        if (r.getMatchSuccess()) {
            if (r.getMatchDistance() > 0.3) {
                printError("match failed:" + r.getMatchMessage());
                ImgName.setText("Unknown Person");
                ImgdbStatus.setText("Not Found");
                ImgSimilarity.setText("");
            } else {
                DecimalFormat df = new DecimalFormat("#.00");
                //double d = r.getMatchDistance();
                debug(imgToCheck + " matches " + r.getMatchFileName() + " at distance=" + r.getMatchDistance());

                double s = 100 - r.getMatchDistance() * 100;

                ImgSimilarity.setText("" + df.format(s) + "%");

                FR_img.removeAll();
                FR_img.repaint();
                FR_img.setLayout(new BorderLayout(0, 0));
                FR_img.add(new JLabel(new ImageIcon(r.getMatchFileName())));
                setVisible(true);

                Path p = Paths.get(r.getMatchFileName());
                String file = p.getFileName().toString();

                ImgName.setText(file);
                ImgdbStatus.setText("Found");

            }
            /* else {
            FR_img.setLayout(new BorderLayout(0, 0));
            FR_img.add(new JLabel(new ImageIcon("D:\\FYPfinal - Update\\FYProject\\Face Database\\Unknown.jpg")));
            setVisible(true);
             */
        }
        long end = System.currentTimeMillis();
        debug("time taken =" + (end - start) / 1000.0 + " seconds");


    }//GEN-LAST:event_FR_ImgsActionPerformed

    private void Processing_ImgsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Processing_ImgsActionPerformed

        try {
            BufferedImage image = ImageIO.read(file);
            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
            mat.put(0, 0, data);

            Mat mat1 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
            Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);

            byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int) (mat1.elemSize())];
            mat1.get(0, 0, data1);
            BufferedImage image1 = new BufferedImage(mat1.cols(), mat1.rows(), BufferedImage.TYPE_BYTE_GRAY);
            image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);

            File ouptut = new File("D:\\FYPfinal - Update\\FYProject\\PreIMG\\GrayscaleImg.jpg");
            ImageIO.write(image1, "PNG", ouptut);
            ImageIcon icon = new ImageIcon(image1);
            Image img = getScaledImage(icon.getImage(), ProcessImg.getWidth(), ProcessImg.getHeight());
            icon = new ImageIcon(img);

            ProcessImg.setIcon(icon);

        } catch (IOException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_Processing_ImgsActionPerformed

    private void Load_ImgsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Load_ImgsActionPerformed
        // TODO add your handling code here:
        int returnVal = 99;

        filechooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "JPG");
        filechooser.setFileFilter(filter);

        returnVal = filechooser.showDialog(MainMenu.this, "Select an image");
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            file = filechooser.getSelectedFile();

            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image img = getScaledImage(icon.getImage(), Dis_Images.getWidth(), Dis_Images.getHeight());
            icon = new ImageIcon(img);
            File ouptut = new File("D:\\FYPfinal - Update\\FYProject\\PreIMG\\OriginalImg.jpg");

            Dis_Images.setIcon(icon);
        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "Sorry, The Input Image Must be JPG/JPEG Format", "Notification", 2);
        }


    }//GEN-LAST:event_Load_ImgsActionPerformed

    private void jPanel5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel5MouseClicked
        // TODO add your handling code here:


    }//GEN-LAST:event_jPanel5MouseClicked

    private void jPanel5MouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel5MouseEntered
        // TODO add your handling code here:

    }//GEN-LAST:event_jPanel5MouseEntered

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void CropfaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CropfaceActionPerformed
        // TODO add your handling code here:

        File inputFile = new File("D:\\FYPfinal - Update\\FYProject\\PreIMG\\CropVideo.jpg");
        try {
            AddImgToDB.removeAll();
            AddImgToDB.repaint();
            BufferedImage inputImage = ImageIO.read(inputFile);
            BufferedImage outputImage = new BufferedImage(100, 100, inputImage.getType());
            Graphics2D g2d = outputImage.createGraphics();
            g2d.drawImage(inputImage, 0, 0, 100, 100, null);
            g2d.dispose();
            ImageIO.write(outputImage, "jpg", new File("D:\\FYPfinal - Update\\FYProject\\PreIMG\\CropVideoResize.jpg"));

            AddImgToDB.setLayout(new BorderLayout(0, 0));

            AddImgToDB.add(new JLabel(new ImageIcon("D:\\FYPfinal - Update\\FYProject\\PreIMG\\CropVideoResize.jpg")));
            setVisible(true);

        } catch (IOException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_CropfaceActionPerformed

    private void AddTemplatesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddTemplatesActionPerformed

        // TODO add your handling code here:
        String newNames = null;
        if (ImgPanelAddToDB.getText().equals("")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Sorry, The Face Name Cannot be Blank ", "Notification", 2);
        } else if (ImgdbStatus.getText().equals("Found")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Sorry, Only the Unknown Faces can be added into Face Database ", "Notification", 2);
        } else {
            newNames = ImgPanelAddToDB.getText() + ".jpg";
            javax.swing.JOptionPane.showMessageDialog(null, "The Image has been saved into Face Database  ", "Notification", 1);
        }

        Path source = Paths.get("D:\\FYPfinal - Update\\FYProject\\PreIMG\\CropImg.jpg");
        Path targetDir = Paths.get("D:\\FYPfinal - Update\\FYProject\\Face Database");

        try {
            Files.createDirectories(targetDir);//in case target directory didn't exist

            Path target = targetDir.resolve(newNames);// create new path ending with `name` content
            System.out.println("Saved Face Templates into " + target);

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            // I decided to replace already existing files with same name
        } catch (IOException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_AddTemplatesActionPerformed

    private void FaceDetectedDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FaceDetectedDisplayActionPerformed

    }//GEN-LAST:event_FaceDetectedDisplayActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        String newNames = null;
        if (jTextField1.getText().equals("")) {
            javax.swing.JOptionPane.showMessageDialog(null, "Sorry, The Face Name Cannot be Blank", "Notification", 2);
        } else {
            newNames = jTextField1.getText() + ".jpg";
            javax.swing.JOptionPane.showMessageDialog(null, "The Image has been saved into Face Database  ", "Notification", 1);
        }

        Path source = Paths.get("D:\\FYPfinal - Update\\FYProject\\PreIMG\\CropVideo.jpg");
        Path targetDir = Paths.get("D:\\FYPfinal - Update\\FYProject\\Face Database");

        try {
            Files.createDirectories(targetDir);//in case target directory didn't exist

            Path target = targetDir.resolve(newNames);// create new path ending with `name` content
            System.out.println("Saved Face Templates into " + target);

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            // I decided to replace already existing files with same name
        } catch (IOException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_jButton5ActionPerformed

    private void SearchImgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SearchImgActionPerformed
        FdataImgDisplay.removeAll();
        String n = SearchName.getText().trim();
        String path = "D:\\FYPfinal - Update\\FYProject\\Face Database\\";
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        DefaultListModel listModel = new DefaultListModel();
        int count = 0;
        for (int i = 0; i < listOfFiles.length; i++) {
            System.out.println("check path" + listOfFiles[i]);
            String name = listOfFiles[i].toString();
            // load only JPEGs
            if (name.endsWith("jpg") && name.contains("" + n)) {
                ImageIcon ii = null;
                try {
                    ii = new ImageIcon(ImageIO.read(listOfFiles[i]));
                } catch (IOException ex) {
                    Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
                }
                listModel.add(count++, ii);
            }
        }

        JList lsm = new JList(listModel);
        lsm.setVisibleRowCount(2);

        lsm.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        lsm.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        FdataImgDisplay.setLayout(new BorderLayout(5, 5));
        FdataImgDisplay.add(new JScrollPane(lsm));

        FdataImgDisplay.revalidate();
        FdataImgDisplay.setVisible(true);
        FdataImgDisplay.repaint();
    }//GEN-LAST:event_SearchImgActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:

        String FolderName = "D:\\FYPfinal - Update\\FYProject\\Face Database\\";//Write your complete path here
        try {
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + FolderName);
        } catch (IOException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_jButton4ActionPerformed

    private void AddTodbBthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddTodbBthActionPerformed
        try {
            // TODO add your handling code here:

            String newNames = null;
            if (fdImgName.getText().equals("")) {
                javax.swing.JOptionPane.showMessageDialog(null, "Sorry, The Face Name Cannot be Blank", "Notification", 2);
            } else {
                newNames = fdImgName.getText() + ".JPEG";
                javax.swing.JOptionPane.showMessageDialog(null, "The Image has been saved into Face Database  ", "Notification", 1);
            }
            //grayscale Image
            File input = new File("D:\\FYPfinal - Update\\FYProject\\PreIMG\\loadImgToDB.jpg");
            BufferedImage image = null;

            image = ImageIO.read(input);

            byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
            mat.put(0, 0, data);

            Mat mat1 = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC1);
            Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2GRAY);

            byte[] data1 = new byte[mat1.rows() * mat1.cols() * (int) (mat1.elemSize())];
            mat1.get(0, 0, data1);
            BufferedImage image1 = new BufferedImage(mat1.cols(), mat1.rows(), BufferedImage.TYPE_BYTE_GRAY);
            image1.getRaster().setDataElements(0, 0, mat1.cols(), mat1.rows(), data1);

            File ouptut = new File("D:\\FYPfinal - Update\\FYProject\\PreIMG\\processingImgToDB.jpg");
            ImageIO.write(image1, "jpg", ouptut);

            //Crop fACE
            System.load("C:/opencv/build/java/x64/opencv_java2411.dll");
            CascadeClassifier faceImageDetector = new CascadeClassifier("D:\\FYPfinal - Update\\FYProject\\data\\haarcascade_frontalface_alt.xml");
            Mat image3 = imread("D:\\FYPfinal - Update\\FYProject\\PreIMG\\loadImgToDB.jpg");

            MatOfRect faceImageDetections = new MatOfRect();

            faceImageDetector.detectMultiScale(image3, faceImageDetections);
            System.out.println(String.format("Detected %s faces", faceImageDetections.toArray().length));

            if (faceImageDetections.toArray().length <= 0) {

                javax.swing.JOptionPane.showMessageDialog(null, "Sorry, No Face Detected", "Notification", 2);

            } else {
                Rect rectCrop = null;
                for (Rect rect : faceImageDetections.toArray()) {
                    rectangle(image3, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                            new Scalar(0, 255, 255), 10, 10, 0);
                    rectCrop = new Rect(rect.x, rect.y, rect.width, rect.height);

                }
                /*
                String filename = "ouput.jpg";
                System.out.println(String.format("Writing %s", filename));
                imwrite(filename, image);
                 */

                Mat image_roi = new Mat(image3, rectCrop);

                Size sz = new Size(220, 220);
                Imgproc.resize(image_roi, image_roi, sz);
                Imgproc.cvtColor(image_roi, image_roi, COLOR_BGR2GRAY);
                Imgproc.equalizeHist(image_roi, image_roi);
                Highgui.imwrite("D:\\FYPfinal - Update\\FYProject\\PreIMG\\ImageReady.jpg", image_roi);

                //copy
                Path source = Paths.get("D:\\FYPfinal - Update\\FYProject\\PreIMG\\ImageReady.jpg");
                Path targetDir = Paths.get("D:\\FYPfinal - Update\\FYProject\\Face Database");

                try {
                    Files.createDirectories(targetDir);//in case target directory didn't exist

                    Path target = targetDir.resolve(newNames);// create new path ending with `name` content
                    System.out.println("Saved Face Templates into " + target);

                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    // I decided to replace already existing files with same name
                } catch (IOException ex) {
                    Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (IOException ex) {
            Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_AddTodbBthActionPerformed

    private void jLabel16MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jLabel16MouseClicked
        // TODO add your handling code here:

        int returnVal = 99;

        filechooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Images", "JPG");
        filechooser.setFileFilter(filter);

        returnVal = filechooser.showDialog(MainMenu.this, "Select an image");
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            file = filechooser.getSelectedFile();

            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image img = getScaledImage(icon.getImage(), jLabel16.getWidth(), jLabel16.getHeight());
            icon = new ImageIcon(img);
            jLabel16.setIcon(icon);

            Image image2 = icon.getImage();
            BufferedImage buffered = (BufferedImage) image2;
            File outputfile = new File("D:\\FYPfinal - Update\\FYProject\\PreIMG\\loadImgToDB.jpg");
            try {
                ImageIO.write(buffered, "jpg", outputfile);
            } catch (IOException ex) {
                Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            javax.swing.JOptionPane.showMessageDialog(null, "Sorry, The Input Image Must be JPG/JPEG Format", "Notification", 2);
        }

    }//GEN-LAST:event_jLabel16MouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainMenu.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainMenu.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainMenu.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainMenu.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //load openCV 3.0 library
        System.load("C:/opencv/build/java/x64/opencv_java2411.dll");


        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainMenu().setVisible(true);

            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected static javax.swing.JPanel AddImgToDB;
    private javax.swing.JButton AddTemplates;
    private javax.swing.JButton AddTodbBth;
    private javax.swing.JCheckBox BackGroundRemove;
    private javax.swing.JButton Cropface;
    protected static javax.swing.JTextField DatabaseStatus;
    private javax.swing.JLabel Dis_Images;
    private javax.swing.JButton FD_Imgs;
    private javax.swing.JLabel FD_Result;
    private javax.swing.JButton FR_Imgs;
    private javax.swing.JPanel FR_img;
    protected static javax.swing.JTextField FaceDetectedDisplay;
    private javax.swing.JPanel FdataImgDisplay;
    protected static javax.swing.JPanel Imag_Jframe;
    private javax.swing.JPanel ImgAddFace;
    private javax.swing.JTextField ImgName;
    protected static javax.swing.JTextField ImgPanelAddToDB;
    private javax.swing.JTextField ImgSimilarity;
    private javax.swing.JTextField ImgdbStatus;
    protected static javax.swing.JButton Load_Imgs;
    private javax.swing.JLabel ProcessImg;
    private javax.swing.JButton Processing_Imgs;
    private javax.swing.JButton SearchImg;
    protected static javax.swing.JTextField SearchName;
    protected static javax.swing.JTextField VidSimilarity;
    protected static javax.swing.JTextField VideoNameDisplay;
    private javax.swing.JTextField fdImgName;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JFrame jFrame2;
    protected static javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    protected static javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    protected static javax.swing.JPanel jframe;
    // End of variables declaration//GEN-END:variables
}
