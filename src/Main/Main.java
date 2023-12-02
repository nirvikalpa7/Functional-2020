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
    final int size = 515;
    final double MoveStep = 1;
    final double ZoomStep = 0.005;
    final int xUiStart = 540;
    final int UiWidth = 180;

    int PicNumber = 0;
    int PaletteMode = 0;
    int GradNum = 0;

    double step = 0.03;
    double startX = 0;
    double startY = 0;

    BufferedImage BufImage;

    // Компонент для рисования текущего градиента
    MyGradComponent grad = new MyGradComponent();
    JLabel statusBar;
    JButton randomGradButton;
    JComboBox gradientComboBox;
    DefaultComboBoxModel cbPrepGradModel;
    Timer animTimer;

    static Main frame;

    //==================================================================================================================

    private void CopyPartOfFrameToImg(int y1, int y2, MyCalc mc, boolean theLastCycle) {
        // В последнем цикле исключается конечная граница
        if (theLastCycle) {
            for (int y = y1; y < y2; y++) {
                for (int x = 0; x < size; x++) {
                    BufImage.setRGB(x, y, mc.arr[x][y - y1].getRGB());
                }
            }
        }
        else {
            for (int y = y1; y <= y2; y++) {
                for (int x = 0; x < size; x++) {
                    BufImage.setRGB(x, y, mc.arr[x][y - y1].getRGB());
                }
            }
        }
    }

    //==================================================================================================================

    private void DrawImageBorder() {
        // Рисование черной рамки
        Color color = new Color(0, 0, 0);
        for (int y = 0; y < size; y++) {
            BufImage.setRGB(0, y, color.getRGB());
            BufImage.setRGB(size - 1, y, color.getRGB());
            BufImage.setRGB(y, 0, color.getRGB());
            BufImage.setRGB(y, size - 1, color.getRGB());
        }
    }

    //==================================================================================================================

    public void DrawScene() {

        long startTime = System.currentTimeMillis();

        // Для все объектов
        MyCalc.setStaticVar(grad, size, PicNumber, step, startX);

        final byte coreNum = 4;
        Thread threads[] = new Thread[coreNum];
        MyCalc mc[] = new MyCalc[coreNum];
        double startYArr[] = {startY, startY+step*101, startY+step*201, startY+step*301};
        int yStart[] = {0, 101, 201, 301};
        int yEnd[] = {100, 200, 300, size};

        for (byte i = 0; i < coreNum; i++) {
            mc[i] = new MyCalc();
            mc[i].setVar(yStart[i], yEnd[i], startYArr[i]);
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

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;

        statusBar.setText(" The image is calculated in 4 threads | Rendering took a while (ms): " + elapsedTime);
    }

    //==================================================================================================================

    private void AddPicComboBox(JPanel contents) {
        // Комбобокс выбора изображения
        DefaultComboBoxModel cbModel = new DefaultComboBoxModel<String>();
        for (int i = 1; i <= 6; i++) {
            cbModel.addElement("Picture " + i);
        }
        JComboBox cbFirst = new JComboBox<String>(cbModel);
        cbModel.setSelectedItem("Picture 1");
        cbFirst.setBounds(xUiStart, 10, UiWidth, 25);
        cbFirst.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                PicNumber = cbModel.getIndexOf(cbModel.getSelectedItem());
                DrawScene();
                frame.repaint();
            }
        });
        contents.add(cbFirst);
    }

    //==================================================================================================================

    private void AddPaletteModeComboBox(JPanel contents) {
        // Комбобокс выбора изображения
        DefaultComboBoxModel cbModel = new DefaultComboBoxModel<String>();
        cbModel.addElement("Black-white gradient");
        cbModel.addElement("Prepared gradient");
        cbModel.addElement("Random gradient");
        JComboBox cbFirst = new JComboBox<String>(cbModel);
        cbModel.setSelectedItem("Black-white gradient");
        cbFirst.setBounds(xUiStart, 280, UiWidth, 25);
        cbFirst.addActionListener(new ActionListener() {
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
        contents.add(cbFirst);
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

        JButton ZoomInButton = new JButton("Zoom In");
        ZoomInButton.setBounds(xUiStart, 55, UiWidth, 25);
        ZoomInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Zoom In")) {
                    double centerX = startX + (size / 2.0) * step;
                    double centerY = startY + (size / 2.0) * step;
                    step = step - ZoomStep;
                    startX = centerX - (size / 2.0) * step;
                    startY = centerY - (size / 2.0) * step;
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(ZoomInButton);

        JButton ZoomOutButton = new JButton("Zoom Out");
        ZoomOutButton.setBounds(xUiStart, 90, UiWidth, 25);
        ZoomOutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Zoom Out")) {
                    double centerX = startX + (size / 2.0) * step;
                    double centerY = startY + (size / 2.0) * step;
                    step = step + ZoomStep;
                    startX = centerX - (size / 2.0) * step;
                    startY = centerY - (size / 2.0) * step;
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(ZoomOutButton);

        JButton UpButton = new JButton("Up");
        UpButton.setBounds(xUiStart, 135, UiWidth, 25);
        UpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Up")) {
                    startY += MoveStep;
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(UpButton);

        JButton DownButton = new JButton("Down");
        DownButton.setBounds(xUiStart, 170, UiWidth, 25);
        DownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getActionCommand().toString().equals("Down")) {
                    startY -= MoveStep;
                    DrawScene();
                    frame.repaint();
                }
            }});
        contents.add(DownButton);

        JButton LeftButton = new JButton("Left");
        LeftButton.setBounds(xUiStart, 200, UiWidth, 25);
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

        JButton RightButton = new JButton("Right");
        RightButton.setBounds(xUiStart, 235, UiWidth, 25);
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

    public Main()
    {
        super("Functional 2020 Ver.3 / (c) Dmitry Sidelnikov");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        startX = -size / 2 * step;
        startY = -size / 2 * step;

        JPanel contents = new JPanel(null);

        // Изображение
        BufImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        ImageIcon icon = new ImageIcon(BufImage);
        JLabel ImageLabel = new JLabel(icon);
        ImageLabel.setBounds(10,10, size, size);
        contents.add(ImageLabel);

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

        JCheckBox checkbox = new JCheckBox("Palette inversion");
        checkbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (actionEvent.getActionCommand().toString().equals("Palette inversion")) {

                    grad.setPaletteInversion(checkbox.isSelected());
                    DrawScene();
                    frame.repaint();
                }
            }
        });
        checkbox.setBounds(xUiStart, 420, UiWidth, 25);
        contents.add(checkbox);

        JCheckBox animCheckBox = new JCheckBox("Animation");
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

        DrawScene();

        setContentPane(contents);
        // Определяем размер окна и выводим его на экран
        setSize(750, 600);
        setVisible(true);
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