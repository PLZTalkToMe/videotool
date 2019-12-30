
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import util.Encoder;
import util.EncoderException;
import util.InputFormatException;
import util.MultimediaInfo;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;


/**
 * @author lwg
 */
public class Main extends JFrame {

    private static List videoFormats = Arrays.asList("mp4", "mpeg4", "mpg4", "avi", "3gp", "rmvb", "rm",
            "wmv", "mkv", "mpeg", "mpg", "mpeg1", "mpg1", "mpeg2", "mpg2",
            "mpeg4", "mpg4 mp4", "vob", "mov", "swf", "flv", "f4v", "vcd",
            "mpeg1", "dvd", "mpeg2", "drc", "dsm", "dsv", "dsa", "dss",
            "ifo", "d2v", "fli", "flc", "lic", "ivf", "mpe", "mtv", "m1v",
            "m2v", "mpv2", "mp2v", "ts", "tp", "tpr", "pva", "pss", "m4v",
            "m4p", "m4b", "3gpp", "3g2", "3gp2", "ogm", "qt",
            "ratdvd", "rt", "rp", "smi", "m2t", "smil", "amv", "dmv", "navi",
            "ra", "ram", "rpm", "roq", "smk", "bik", "wmp",
            "wm", "asf", "asx", "m3u", "pls", "wvx", "wax", "wmx", "mpcpl");
    private JPanel topPanel = new JPanel();
    private JTextArea jtext = new JTextArea();
    private JScrollPane jScrollPane = new JScrollPane(jtext);
    private JPanel centerPanel = new JPanel();
    private JProgressBar progressBar = new JProgressBar();
    private int currentProgress = 0;
    private int filesCount = 0;
    private int finishFilesCount = 0;
    private java.util.List<String> resultList = new ArrayList<>();
    private int totalFilesCount = 0;
    private JLabel videoFileCountLabel = new JLabel("0");
    private JLabel allFileCountLabel = new JLabel("0");
//    private ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<>(100000);

    public Main() {
        // 顶部 文件选择器
        initTopPanel();

        // 中部 进度条
        initCenterPanel();

        // 底部 处理情况打印
        initBottomPanel();

        // 配置窗体
        this.add(topPanel, BorderLayout.NORTH);
        this.add(centerPanel, BorderLayout.CENTER);
        this.add(jScrollPane, BorderLayout.SOUTH);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setTitle("视频信息");
        this.setResizable(false);
        this.setVisible(true);


    }


    public void initTopPanel() {
        topPanel.setPreferredSize(new Dimension(600, 40));
        JLabel openFileLabel = new JLabel("选择文件");
        openFileLabel.setPreferredSize(new Dimension(60, 20));

        JTextField filePathText = new JTextField();
        filePathText.setPreferredSize(new Dimension(320, 20));

        JButton openFileButton = new JButton("选择");
        openFileButton.setPreferredSize(new Dimension(60, 20));
        openFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 文件选择
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                fileChooser.showDialog(new JLabel(), "选择");
                File file = fileChooser.getSelectedFile();
                if (file == null) {
                    return;
                } else {
                    filePathText.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }

            }
        });

        JButton startButton = new JButton("开始");
        startButton.setPreferredSize(new Dimension(60, 20));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        filesCount = 0;
                        totalFilesCount = 0;
                        finishFilesCount = 0;
                        jtext.setText("");
                        videoFileCountLabel.setText("0");
                        allFileCountLabel.setText("0");
                        resultList.clear();
                        String inputFilePath = filePathText.getText();
                        if (StrUtil.isBlank(inputFilePath)) {
                            JOptionPane.showMessageDialog(null, "请先选择文件或者文件夹", "错误", JOptionPane.ERROR_MESSAGE);
                        } else {
                            initVideoFiles(inputFilePath);
                            System.out.println(filesCount);
                            analyseVideoInfo();
                        }
                    }
                }).start();


            }
        });

        topPanel.add(openFileLabel, BorderLayout.CENTER);
        topPanel.add(filePathText, BorderLayout.CENTER);
        topPanel.add(openFileButton, BorderLayout.CENTER);
        topPanel.add(startButton);
        topPanel.setBorder(BorderFactory.createEtchedBorder());
    }


    /**
     * 初始化中部panel
     */
    public void initCenterPanel() {
        centerPanel.setPreferredSize(new Dimension(500, 40));
        JLabel progressLabel = new JLabel("当前进度");
        progressLabel.setPreferredSize(new Dimension(60, 20));
        allFileCountLabel.setPreferredSize(new Dimension(60, 20));
        allFileCountLabel.setHorizontalAlignment(JLabel.RIGHT);
        videoFileCountLabel.setPreferredSize(new Dimension(60, 20));
        videoFileCountLabel.setHorizontalAlignment(JLabel.RIGHT);
        progressBar.setMinimum(0);
        progressBar.setMaximum(100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(320, 20));
        centerPanel.add(progressLabel, BorderLayout.CENTER);
        centerPanel.add(progressBar, BorderLayout.CENTER);
        centerPanel.add(allFileCountLabel);
        centerPanel.add(videoFileCountLabel);
        centerPanel.setBorder(BorderFactory.createEtchedBorder());
    }


    public void initBottomPanel() {
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPane.setPreferredSize(new Dimension(400, 300));
//        jtext.setLineWrap(true);
        jtext.setEditable(false);
    }


    public void initVideoFiles(String inputFilePath) {
        jtext.append("======== 【扫描文件中 请稍后】 ========\r\n");
        File[] topLevelFiles;
        File file = new File(inputFilePath);
        if(file.isDirectory()){
            topLevelFiles = FileUtil.ls(inputFilePath);
        }else{
            topLevelFiles = new File[]{file};
        }
        if (topLevelFiles == null) {
            return;
        }
        for (File topLevelFile : topLevelFiles) {
            try {
                if (topLevelFile.isDirectory()) {
                    recIntoDir(topLevelFile, resultList);
                } else {
                    if (videoFormats.contains(FileUtil.extName(topLevelFile).toLowerCase())) {
                        filesCount++;
                        videoFileCountLabel.setText(String.valueOf(filesCount));
                        resultList.add(topLevelFile.getAbsolutePath());
                    }
                    totalFilesCount ++;
                    allFileCountLabel.setText(String.valueOf(totalFilesCount));
                }
            } catch (Exception e) {
                System.out.println("错误");
            }
        }
        return;

    }

    /**
     * 递归遍历目录
     */
    public void recIntoDir(File lastLevelFile, java.util.List<String> resultList) {
        File[] currentLevelFiles = FileUtil.ls(lastLevelFile.getAbsolutePath());
        for (File currentLevelFile : currentLevelFiles) {
            if (currentLevelFile.isDirectory()) {
                recIntoDir(currentLevelFile, resultList);
            } else {
                if (videoFormats.contains(FileUtil.extName(currentLevelFile).toLowerCase())) {
                    filesCount++;
                    videoFileCountLabel.setText(String.valueOf(filesCount));
                    resultList.add(currentLevelFile.getAbsolutePath());
                }
                totalFilesCount ++;
                allFileCountLabel.setText(String.valueOf(totalFilesCount));
            }
        }
    }


    public void analyseVideoInfo() {
        Encoder encoder = new Encoder();

//        new Timer(10, new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                progressBar.setValue(currentProgress);
//                String content = queue.poll();
//                if(StrUtil.isNotBlank(content)){
//                    jtext.append(content);
//                }
//            }
//        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                java.util.List<String> limitSizeContentList = new ArrayList<>(1000);
                for (String filePath : resultList) {
                    try {
                        StringBuilder content = new StringBuilder();
                        File file = new File(filePath);
                        MultimediaInfo m = encoder.getInfo(file);
                        long ls = m.getDuration() / 1000;
                        int hour = (int) (ls / 3600);
                        String hourStr = hour == 0 ? "00" : (hour < 10 ? ("0" + hour) : hour + "");
                        int minute = (int) (ls % 3600) / 60;
                        String minuteStr = minute == 0 ? "00" : (minute < 10 ? ("0" + minute) : minute + "");
                        int second = (int) (ls - hour * 3600 - minute * 60);
                        String secondStr = second == 0 ? "00" : (second < 10 ? ("0" + second) : second + "");
                        long fileSize = file.length() / 1000;
                        content = content.append(file.getName())
                                .append("\t")
                                .append(hourStr)
                                .append(":")
                                .append(minuteStr)
                                .append(":")
                                .append(secondStr)
                                .append("\t")
                                .append(fileSize)
                                .append("\t")
                                .append(filePath);
                        System.out.println(fileSize);
                        limitSizeContentList.add(content.toString());
                        if (limitSizeContentList.size() >= 1000) {
                            System.out.println(limitSizeContentList.size());
                            String currentTime = DateUtil.format(new Date(), "yyyy-MM-dd-HH-ss-mm");
                            String rand = RandomUtil.randomNumbers(6);
                            FileUtil.appendLines(limitSizeContentList, new File("C:\\video\\video-info-" + currentTime + "-" + rand + ".txt"), CharsetUtil.UTF_8);
                            limitSizeContentList.clear();
                            jtext.setText("");
                        }
                        finishFilesCount++;
                        currentProgress = (int) (((float) finishFilesCount / (float) filesCount) * 100);
                        progressBar.setValue(currentProgress);
                        jtext.append(content.append("\r\n").toString());
//                        queue.add(content.append("\r\n").toString());
                    } catch (Exception e) {
                        finishFilesCount++;
                        currentProgress = (int) (((float) finishFilesCount / (float) filesCount) * 100);
                        progressBar.setValue(currentProgress);
                        jtext.append(filePath + " 不是视频文件\r\n");
//                        queue.add(filePath + " 不是视频文件\r\n");
                        continue;
                    }
                }
                if(limitSizeContentList.size() != 0){
                    System.out.println(limitSizeContentList.size());
                    String currentTime = DateUtil.format(new Date(), "yyyy-MM-dd-HH-ss-mm");
                    String rand = RandomUtil.randomNumbers(6);
                    FileUtil.appendLines(limitSizeContentList, new File("C:\\video\\video-info-" + currentTime + "-" + rand + ".txt"), CharsetUtil.UTF_8);
                    limitSizeContentList.clear();
                }
                jtext.append("======== 【完成】 ========\r\n");
            }
        }).start();


    }


    public static void main(String[] args) {
        new Main();
    }
}