package Main;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.imageio.ImageIO;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import MyThread.MyCalc;
import MyGradient.MyGradComponent;

//======================================================================================================================

public class Main extends JFrame
{
    // Rend image size X x Y
    int sizeX = 515;
    int sizeY = 515;

    final double moveStep = 1;
    final double zoomStep = 0.005;
    int xGuiPanelStart = 540;
    final int guiPanelWidth = 180;
    boolean isSceneDrawing;

    double startFYArr[]; // size of arr - coreNum, Start double Y for every part of image
    int yStart[]; // size of arr - coreNum
    int yEnd[]; // size of arr - coreNum


    int drawFunNumber = 0;
    int paletteMode = 0;
    int gradNumber = 0;

    double step = 0.03;
    double startX = 0;
    double startY = 0;

    BufferedImage imgBuffer;
    ImageIcon imgIcon;
    JLabel imgLabel;
    int coreNum = 8; // Threads and image parts number

    MyGradComponent grad; // My gradient GUI component
    JLabel statusBar;
    JComboBox firstComboBox, gradientComboBox, gradTypeComboBox, coreNumComboBox;
    JCheckBox animCheckBox, paletteInversionCheckbox, threadImgBorder;
    JButton randomGradButton, zoomInButton, zoomOutButton, saveImgButton;
    JButton upButton, downButton, leftButton, rightButton;
    DefaultComboBoxModel cbPrepGradModel;
    Timer animTimer;

    Thread threads[];
    MyCalc threadTasks[];

    static Main frame;

    //==================================================================================================================

    public Main()
    {
        super("Functional 2024 Ver.4 / (c) Dmitry Sidelnikov");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(true);

        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                resizeGui();
                resizeImage();
                DrawScene();
            }
        });

        JPanel contents = new JPanel(null);

        // Image for function rendering
        imgBuffer = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
        imgIcon = new ImageIcon(imgBuffer);
        imgLabel = new JLabel(imgIcon);
        imgLabel.setBounds(10,10, sizeX, sizeY);
        contents.add(imgLabel);

        AddPicComboBox(contents);
        AddMoveAndZoomButtons(contents);
        AddSaveImageButton(contents);
        AddPaletteModeComboBox(contents);
        AddGradientComboBox(contents);
        AddCoreNumComboBox(contents);

        grad = new MyGradComponent();
        contents.add(grad);

        randomGradButton = new JButton("Random gradient");
        randomGradButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Random gradient")) {
                    grad.createRandomPalette();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(randomGradButton);

        randomGradButton.setEnabled(false);
        gradientComboBox.setEnabled(false);

        paletteInversionCheckbox = new JCheckBox("Palette inversion");
        paletteInversionCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().toString().equals("Palette inversion")) {

                    grad.setPaletteInversion(paletteInversionCheckbox.isSelected());
                    DrawScene();
                    frame.repaint();
                }
            }
        });
        contents.add(paletteInversionCheckbox);

        animCheckBox = new JCheckBox("Animation");
        animCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().toString().equals("Animation")) {
                    if (animCheckBox.isSelected()) {
                        animTimer = new Timer();
                        MyTimeTask myTask = new MyTimeTask(frame, animTimer);
                        animTimer.schedule(myTask , 20, 20);
                    }
                    else {
                        animTimer.cancel();
                    }
                }
            }
        });
        contents.add(animCheckBox);

        threadImgBorder = new JCheckBox("Thread image border");
        threadImgBorder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().toString().equals("Thread image border")) {
                    DrawScene();
                    frame.repaint();
                }
            }
        });
        contents.add(threadImgBorder);

        statusBar = new JLabel(" The image (500 x 500) is calculated in 8 threads. Rendering took a while: 0 ms");
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        contents.add(statusBar);

        setContentPane(contents);

        // Window size and position
        final int winSizeX = 1000;
        final int winSizeY = 700;
        setSize(winSizeX, winSizeY);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - winSizeX) / 2);
        int y = (int) ((dimension.getHeight() - winSizeY) / 2);
        setLocation(x, y);

        setVisible(true);
    }

    //==================================================================================================================

    private void resizeGui() {
        Dimension winSize;
        winSize = getSize();

        xGuiPanelStart = winSize.width - guiPanelWidth - 30;

        grad.setBounds(xGuiPanelStart, 350, guiPanelWidth, 25);

        firstComboBox.setBounds(xGuiPanelStart, 10, guiPanelWidth, 25);
        gradTypeComboBox.setBounds(xGuiPanelStart, 280, guiPanelWidth, 25);
        gradientComboBox.setBounds(xGuiPanelStart, 315, guiPanelWidth, 25);
        coreNumComboBox.setBounds(xGuiPanelStart, 515, guiPanelWidth, 25);

        paletteInversionCheckbox.setBounds(xGuiPanelStart, 420, guiPanelWidth, 25);
        animCheckBox.setBounds(xGuiPanelStart, 450, guiPanelWidth, 25);
        threadImgBorder.setBounds(xGuiPanelStart, 480, guiPanelWidth, 25);

        randomGradButton.setBounds(xGuiPanelStart, 385, guiPanelWidth, 25);
        zoomInButton.setBounds(xGuiPanelStart, 55, guiPanelWidth, 25);
        zoomOutButton.setBounds(xGuiPanelStart, 90, guiPanelWidth, 25);
        upButton.setBounds(xGuiPanelStart, 135, guiPanelWidth, 25);
        downButton.setBounds(xGuiPanelStart, 170, guiPanelWidth, 25);
        leftButton.setBounds(xGuiPanelStart, 205, guiPanelWidth, 25);
        rightButton.setBounds(xGuiPanelStart, 240, guiPanelWidth, 25);
        saveImgButton.setBounds(xGuiPanelStart, 555, guiPanelWidth, 25);

        statusBar.setBounds(1,winSize.height - 65,winSize.width - 18, 25);
    }

    //==================================================================================================================

    private void resizeImage() {

        waitDrawSceneFinish();

        Dimension newWinSize;
        newWinSize = getSize();

        sizeX = newWinSize.width - 50 - guiPanelWidth;
        sizeY = newWinSize.height - 85;

        imgBuffer = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
        imgIcon.setImage(imgBuffer);
        imgLabel.setIcon(imgIcon);
        imgLabel.setBounds(10,10, sizeX, sizeY);

        UpdateRendImageArrays();
    }

    //==================================================================================================================

    public void DrawScene() {

        long startTime = System.nanoTime();
        isSceneDrawing = true;

        for (int i = 0; i < coreNum; i++) {
            threads[i] = new Thread(threadTasks[i], "Thread " + (i+1));
            threads[i].start();
        }

        try {
            for (int i = 0; i < coreNum; i++) {
                threads[i].join();
            }
        }
        catch(InterruptedException ex) {
            System.out.println("Ошибка при выполении join: " + ex);
        }

        boolean theLastCycle = false;
        for (int i = 0; i < coreNum; i++) {
            if (i == coreNum - 1) {
                theLastCycle = true;
            }
            CopyPartOfFrameToImg(yStart[i], yEnd[i], threadTasks[i], theLastCycle);
        }

        DrawImageBorder();

        isSceneDrawing = false;
        long stopTime = System.nanoTime();
        long elapsedTime = Math.round((stopTime - startTime) / 1000000.0);

        statusBar.setText(" The image (" + sizeX + " x " + sizeY + ") is calculated in " + coreNum + " threads. Rendering took a while: " + elapsedTime + " ms");
    }

    //==================================================================================================================

    private void CopyPartOfFrameToImg(int y1, int y2, MyCalc mc, boolean theLastCycle) {
        // В последнем цикле исключается конечная граница
        if (theLastCycle) {
            for (int y = y1; y < y2; y++) {
                for (int x = 0; x < sizeX; x++) {
                    imgBuffer.setRGB(x, y, mc.arr[x][y - y1].getRGB());
                }
            }
        }
        else {
            int y = y1;
            for (; y <= y2; y++) {
                for (int x = 0; x < sizeX; x++) {
                    imgBuffer.setRGB(x, y, mc.arr[x][y - y1].getRGB());
                }
            }
            if (threadImgBorder.isSelected()) { // Thread image border
                y--;
                for (int x = 0; x < sizeX; x++) {
                    imgBuffer.setRGB(x, y, 0);
                }
            }
        }
    }

    //==================================================================================================================

    private void DrawImageBorder() {
        // Рисование черной рамки
        Color color = new Color(0, 0, 0);
        for (int y = 0; y < sizeY; y++) {
            imgBuffer.setRGB(0, y, color.getRGB());
            imgBuffer.setRGB(sizeX - 1, y, color.getRGB());
        }
        for (int x = 0; x < sizeX; x++) {
            imgBuffer.setRGB(x, 0, color.getRGB());
            imgBuffer.setRGB(x, sizeY - 1, color.getRGB());
        }
    }

    //==================================================================================================================
    private void UpdateRenderTasks() {
        for (int k = 0; k < coreNum; k++) {
            startFYArr[k] = startY + step * yStart[k]; // 0, 101, 201, 301, if dy = 100
        }

        MyCalc.setStaticVar(grad, sizeX, drawFunNumber, step, startX);
        for (int i = 0; i < coreNum; i++) {
            threadTasks[i].setVar(yStart[i], yEnd[i], startFYArr[i]);
        }
    }

    //==================================================================================================================

    private void UpdateRendImageArrays() {
        startX = -sizeX / 2 * step;
        startY = -sizeY / 2 * step;

        final int dy = sizeY / coreNum;

        yStart = new int[coreNum];
        yStart[0] = 0;
        for (int i = 1; i < coreNum; i++) {
            yStart[i] = dy * i + 1; // 101, 201, 301... if dx = 100
        }

        yEnd = new int[coreNum];
        for (int j = 0; j < coreNum - 1; j++) {
            yEnd[j] = dy * (j + 1);
        }
        yEnd[coreNum - 1] = sizeY; // the last Y border

        threads = new Thread[coreNum];
        threadTasks = new MyCalc[coreNum];
        for (int i = 0; i < coreNum; i++) {
            threadTasks[i] = new MyCalc();
        }

        startFYArr = new double[coreNum];
        UpdateRenderTasks();
    }

    //==================================================================================================================

    private void waitDrawSceneFinish() {
        if (animCheckBox.isSelected())
            animCheckBox.setSelected(false);

        while (isSceneDrawing) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                break; // Don't worry about it
            }
        }
    }

    //==================================================================================================================

    private void AddSaveImageButton(JPanel contents) {
        saveImgButton = new JButton("Save image");
        saveImgButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Save image")) {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Specify a PNG file to save");
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG images", "png");
                    fileChooser.addChoosableFileFilter(filter);
                    final int userSelection = fileChooser.showSaveDialog(frame);
                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File fileToSave = fileChooser.getSelectedFile();
                        String fn = fileToSave.toString();
                        if (!fn.toUpperCase().endsWith(".PNG")) {
                            fn += ".png";
                            fileToSave = new File(fn);
                        }
                        try {
                            ImageIO.write(imgBuffer, "png", fileToSave);
                        } catch (IOException exc) {

                        }
                    }
                }
            }
        });
        contents.add(saveImgButton);
    }

    //==================================================================================================================

    private void AddCoreNumComboBox(JPanel contents) {

        DefaultComboBoxModel coreNumCBModel = new DefaultComboBoxModel<String>();
        coreNumCBModel.addElement("2 threads");
        coreNumCBModel.addElement("4 threads");
        coreNumCBModel.addElement("8 threads");
        coreNumCBModel.addElement("16 threads");

        coreNumComboBox = new JComboBox<String>(coreNumCBModel);
        coreNumCBModel.setSelectedItem("8 threads");
        coreNumComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                waitDrawSceneFinish();

                final int coreNumbers[] = {2, 4, 8, 16};
                final int coreNumSelectedIndex = coreNumCBModel.getIndexOf(coreNumCBModel.getSelectedItem());
                coreNum = coreNumbers[coreNumSelectedIndex];

                UpdateRendImageArrays();
                DrawScene();
                frame.repaint();
            }
        });
        contents.add(coreNumComboBox);
    }

    //==================================================================================================================

    private void AddPicComboBox(JPanel contents) {
        // Комбобокс выбора изображения
        DefaultComboBoxModel cbModel = new DefaultComboBoxModel<String>();
        for (int i = 1; i <= 6; i++) {
            cbModel.addElement("Picture " + i);
        }
        firstComboBox = new JComboBox<String>(cbModel);
        cbModel.setSelectedItem("Picture 1");
        firstComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                drawFunNumber = cbModel.getIndexOf(cbModel.getSelectedItem());
                UpdateRenderTasks();
                DrawScene();
                frame.repaint();
            }
        });
        contents.add(firstComboBox);
    }

    //==================================================================================================================

    private void AddPaletteModeComboBox(JPanel contents) {
        // Комбобокс выбора изображения
        DefaultComboBoxModel cbModel = new DefaultComboBoxModel<String>();
        cbModel.addElement("Black-white gradient");
        cbModel.addElement("Prepared gradient");
        cbModel.addElement("Random gradient");
        gradTypeComboBox = new JComboBox<String>(cbModel);
        cbModel.setSelectedItem("Black-white gradient");
        gradTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (cbModel.getSelectedItem().toString().equals("Black-white gradient")) {
                    paletteMode = 0;
                    randomGradButton.setEnabled(false);
                    gradientComboBox.setEnabled(false);
                    grad.setPoints(new Color(0,0,0),
                            new Color(64,64,64),
                            new Color(128,128,128),
                            new Color(192,192,192),
                            new Color(255,255,255));
                    grad.calcPalette();
                }
                else if (cbModel.getSelectedItem().toString().equals("Prepared gradient")) {
                    paletteMode = 1;
                    randomGradButton.setEnabled(false);
                    gradientComboBox.setEnabled(true);
                    gradNumber = cbPrepGradModel.getIndexOf(cbPrepGradModel.getSelectedItem());
                    grad.setPresetPalette(gradNumber);
                }
                else if (cbModel.getSelectedItem().toString().equals("Random gradient")) {
                    paletteMode = 2;
                    randomGradButton.setEnabled(true);
                    gradientComboBox.setEnabled(false);
                    grad.createRandomPalette();
                }
                DrawScene();
                frame.repaint();
            }
        });
        contents.add(gradTypeComboBox);
    }

    //==================================================================================================================

    private void AddGradientComboBox(JPanel contents) {
        // Комбобокс выбора изображения
        cbPrepGradModel = new DefaultComboBoxModel<String>();
        for (int i = 1; i <= 10; i++) {
            cbPrepGradModel.addElement("Gradient " + i);
        }
        gradientComboBox = new JComboBox<String>(cbPrepGradModel);
        cbPrepGradModel.setSelectedItem("Gradient 1");
        gradientComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                gradNumber = cbPrepGradModel.getIndexOf(cbPrepGradModel.getSelectedItem());
                grad.setPresetPalette(gradNumber);
                DrawScene();
                frame.repaint();
            }
        });
        contents.add(gradientComboBox);
    }

    //==================================================================================================================

    private void AddMoveAndZoomButtons(JPanel contents) {

        zoomInButton = new JButton("Zoom In");
        zoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Zoom In")) {
                    double centerX = startX + (sizeX / 2.0) * step;
                    double centerY = startY + (sizeY / 2.0) * step;
                    step = step - zoomStep;
                    startX = centerX - (sizeX / 2.0) * step;
                    startY = centerY - (sizeY / 2.0) * step;
                    UpdateRenderTasks();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(zoomInButton);

        zoomOutButton = new JButton("Zoom Out");
        zoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Zoom Out")) {
                    double centerX = startX + (sizeX / 2.0) * step;
                    double centerY = startY + (sizeY / 2.0) * step;
                    step = step + zoomStep;
                    startX = centerX - (sizeX / 2.0) * step;
                    startY = centerY - (sizeY / 2.0) * step;
                    UpdateRenderTasks();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(zoomOutButton);

        upButton = new JButton("Up");
        upButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Up")) {
                    startY += moveStep;
                    UpdateRenderTasks();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(upButton);

        downButton = new JButton("Down");
        downButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Down")) {
                    startY -= moveStep;
                    UpdateRenderTasks();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(downButton);

        leftButton = new JButton("Left");
        leftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Left")) {
                    startX += moveStep;
                    UpdateRenderTasks();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(leftButton);

        rightButton = new JButton("Right");
        rightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Right")) {
                    startX -= moveStep;
                    UpdateRenderTasks();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(rightButton);
    }

    //==================================================================================================================

    public void shiftPalette(int n) {
        grad.shiftPalette(n);
    }

    //==================================================================================================================

    public void repaintWin() {
        frame.repaint();
    }

    //==================================================================================================================

    public static void main(String[] args) {
        frame = new Main();
    }
}

//======================================================================================================================

class MyTimeTask extends TimerTask {
    private Main m;
    private Timer t;

    //==================================================================================================================

    public MyTimeTask(Main m, Timer t) {
        this.m = m;
        this.t = t;
    }

    //==================================================================================================================

    @Override
    public void run() {
        m.shiftPalette(5);
        m.DrawScene();
        m.repaintWin();
    }
}

//======================================================================================================================