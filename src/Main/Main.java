package Main;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import java.util.Timer;
import java.util.TimerTask;

import MyThread.MyCalc;
import MyGradient.MyGradComponent;

//======================================================================================================================

public class Main extends JFrame
{
    int sizeX = 515; // Image size
    int sizeY = 515;
    final double MoveStep = 1;
    final double ZoomStep = 0.005;
    int xUiStart = 540;
    final int UiWidth = 180;
    boolean isSceneDrawing;

    double startFYArr[]; // Start double Y for every part of image
    int yStart[]; // 4 int
    int yEnd[]; // 4 int


    int PicNumber = 0;
    int PaletteMode = 0;
    int GradNum = 0;

    double step = 0.03;
    double startX = 0;
    double startY = 0;

    BufferedImage imgBuffer;
    ImageIcon imgIcon;
    JLabel imgLabel;
    final byte coreNum = 8; // Threads and image parts number

    // Компонент для рисования текущего градиента
    MyGradComponent grad = new MyGradComponent();
    JLabel statusBar;
    JComboBox firstComboBox;
    JComboBox gradTypeComboBox;
    JCheckBox animCheckBox;
    JCheckBox paletteInverCheckbox; // Palette inversion

    JButton randomGradButton;
    JButton ZoomInButton, ZoomOutButton;
    JButton UpButton, DownButton, LeftButton, RightButton;
    JComboBox gradientComboBox;
    DefaultComboBoxModel cbPrepGradModel;
    Timer animTimer;

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

        // Изображение
        imgBuffer = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
        imgIcon = new ImageIcon(imgBuffer);
        imgLabel = new JLabel(imgIcon);
        imgLabel.setBounds(10,10, sizeX, sizeY);
        contents.add(imgLabel);

        AddPicComboBox(contents);
        AddMoveAndZoomButtons(contents);
        AddPaletteModeComboBox(contents);
        AddGradientComboBox(contents);

        grad.setBounds(xUiStart, 350, UiWidth, 25);
        contents.add(grad);

        randomGradButton = new JButton("Random gradient");
        randomGradButton.setBounds(xUiStart, 385, UiWidth, 25);
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

        paletteInverCheckbox = new JCheckBox("Palette inversion");
        paletteInverCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().toString().equals("Palette inversion")) {

                    grad.setPaletteInversion(paletteInverCheckbox.isSelected());
                    DrawScene();
                    frame.repaint();
                }
            }
        });
        paletteInverCheckbox.setBounds(xUiStart, 420, UiWidth, 25);
        contents.add(paletteInverCheckbox);

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
        animCheckBox.setBounds(xUiStart, 455, UiWidth, 25);
        contents.add(animCheckBox);

        statusBar = new JLabel(" The image is calculated in 4 threads | Rendering took a while (ms): 0");
        statusBar.setBorder(new BevelBorder(BevelBorder.LOWERED));
        statusBar.setBounds(1,535,732, 25);
        contents.add(statusBar);

        setContentPane(contents);

        // Window size and position
        final int winSizeX = 900;
        final int winSizeY = 600;
        setSize(winSizeX, winSizeY);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - winSizeX) / 2);
        int y = (int) ((dimension.getHeight() - winSizeY) / 2);
        setLocation(x, y);

        UpdateImageBounds();
        DrawScene();

        setVisible(true);
    }

    //==================================================================================================================

    public void resizeGui() {
        Dimension winSize;
        winSize = getSize();

        xUiStart = winSize.width - UiWidth - 30;

        grad.setBounds(xUiStart, 350, UiWidth, 25);

        firstComboBox.setBounds(xUiStart, 10, UiWidth, 25);
        gradTypeComboBox.setBounds(xUiStart, 280, UiWidth, 25);
        gradientComboBox.setBounds(xUiStart, 315, UiWidth, 25);

        animCheckBox.setBounds(xUiStart, 455, UiWidth, 25);
        paletteInverCheckbox.setBounds(xUiStart, 420, UiWidth, 25);

        randomGradButton.setBounds(xUiStart, 385, UiWidth, 25);
        ZoomInButton.setBounds(xUiStart, 55, UiWidth, 25);
        ZoomOutButton.setBounds(xUiStart, 90, UiWidth, 25);
        UpButton.setBounds(xUiStart, 135, UiWidth, 25);
        DownButton.setBounds(xUiStart, 170, UiWidth, 25);
        LeftButton.setBounds(xUiStart, 205, UiWidth, 25);
        RightButton.setBounds(xUiStart, 240, UiWidth, 25);

        statusBar.setBounds(1,winSize.height - 65,winSize.width - 18, 25);
    }

    //==================================================================================================================

    public void resizeImage() {

        if (animCheckBox.isSelected())
            animCheckBox.setSelected(false);

        while(isSceneDrawing)
        {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                break; // Don't worry about it
            }
        }

        Dimension newWinSize;
        newWinSize = getSize();

        sizeX = newWinSize.width - 50 - UiWidth;
        sizeY = newWinSize.height - 85;

        imgBuffer = new BufferedImage(sizeX, sizeY, BufferedImage.TYPE_INT_RGB);
        imgIcon.setImage(imgBuffer);
        imgLabel.setIcon(imgIcon);
        imgLabel.setBounds(10,10, sizeX, sizeY);

        UpdateImageBounds();
    }

    //==================================================================================================================

    public void DrawScene() {

        long startTime = System.currentTimeMillis();
        isSceneDrawing = true;

        // Для все объектов
        MyCalc.setStaticVar(grad, sizeX, PicNumber, step, startX);

        Thread threads[] = new Thread[coreNum];
        MyCalc mc[] = new MyCalc[coreNum];

        for (byte i = 0; i < coreNum; i++) {
            mc[i] = new MyCalc();
            mc[i].setVar(yStart[i], yEnd[i], startFYArr[i]);
            threads[i] = new Thread(mc[i], "Thread " + (i+1));
            threads[i].start();
        }

        try {
            for (byte i = 0; i < coreNum; i++) {
                threads[i].join();
            }
        }
        catch(InterruptedException ex) {
            System.out.println("Ошибка при выполении join: " + ex);
        }

        boolean theLastCycle = false;
        for (byte i = 0; i < coreNum; i++) {
            if (i == coreNum - 1) {
                theLastCycle = true;
            }
            CopyPartOfFrameToImg(yStart[i], yEnd[i], mc[i], theLastCycle);
        }

        DrawImageBorder();

        isSceneDrawing = false;
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        statusBar.setText(" The image (" + sizeX + " x " + sizeY + ") is calculated in " + coreNum + " threads | Rendering took a while (ms): " + elapsedTime);
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
            for (int y = y1; y <= y2; y++) {
                for (int x = 0; x < sizeX; x++) {
                    imgBuffer.setRGB(x, y, mc.arr[x][y - y1].getRGB());
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
    public void UpdateFYArray() {
        for (int k = 0; k < coreNum; k++) {
            startFYArr[k] = startY + step * yStart[k]; // 0, 101, 201, 301, if dy = 100
        }
    }

    //==================================================================================================================

        public void UpdateImageBounds() {
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

            startFYArr = new double[coreNum];
            UpdateFYArray();
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
        firstComboBox.setBounds(xUiStart, 10, UiWidth, 25);
        firstComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                PicNumber = cbModel.getIndexOf(cbModel.getSelectedItem());
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
        gradTypeComboBox.setBounds(xUiStart, 280, UiWidth, 25);
        gradTypeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (cbModel.getSelectedItem().toString().equals("Black-white gradient")) {
                    PaletteMode = 0;
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
                    PaletteMode = 1;
                    randomGradButton.setEnabled(false);
                    gradientComboBox.setEnabled(true);
                    GradNum = cbPrepGradModel.getIndexOf(cbPrepGradModel.getSelectedItem());
                    grad.setPresetPalette(GradNum);
                }
                else if (cbModel.getSelectedItem().toString().equals("Random gradient")) {
                    PaletteMode = 2;
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
        gradientComboBox.setBounds(xUiStart, 315, UiWidth, 25);
        gradientComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                GradNum = cbPrepGradModel.getIndexOf(cbPrepGradModel.getSelectedItem());
                grad.setPresetPalette(GradNum);
                DrawScene();
                frame.repaint();
            }
        });
        contents.add(gradientComboBox);
    }

    //==================================================================================================================

    private void AddMoveAndZoomButtons(JPanel contents) {

        ZoomInButton = new JButton("Zoom In");
        ZoomInButton.setBounds(xUiStart, 55, UiWidth, 25);
        ZoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Zoom In")) {
                    double centerX = startX + (sizeX / 2.0) * step;
                    double centerY = startY + (sizeY / 2.0) * step;
                    step = step - ZoomStep;
                    startX = centerX - (sizeX / 2.0) * step;
                    startY = centerY - (sizeY / 2.0) * step;
                    UpdateFYArray();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(ZoomInButton);

        ZoomOutButton = new JButton("Zoom Out");
        ZoomOutButton.setBounds(xUiStart, 90, UiWidth, 25);
        ZoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Zoom Out")) {
                    double centerX = startX + (sizeX / 2.0) * step;
                    double centerY = startY + (sizeY / 2.0) * step;
                    step = step + ZoomStep;
                    startX = centerX - (sizeX / 2.0) * step;
                    startY = centerY - (sizeY / 2.0) * step;
                    UpdateFYArray();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(ZoomOutButton);

        UpButton = new JButton("Up");
        UpButton.setBounds(xUiStart, 135, UiWidth, 25);
        UpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Up")) {
                    startY += MoveStep;
                    UpdateFYArray();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(UpButton);

        DownButton = new JButton("Down");
        DownButton.setBounds(xUiStart, 170, UiWidth, 25);
        DownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Down")) {
                    startY -= MoveStep;
                    UpdateFYArray();
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(DownButton);

        LeftButton = new JButton("Left");
        LeftButton.setBounds(xUiStart, 205, UiWidth, 25);
        LeftButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Left")) {
                    startX += MoveStep;
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(LeftButton);

        RightButton = new JButton("Right");
        RightButton.setBounds(xUiStart, 240, UiWidth, 25);
        RightButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Right")) {
                    startX -= MoveStep;
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(RightButton);
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