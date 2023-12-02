package MyGradient;

import javax.swing.*;
import java.awt.*;

//======================================================================================================================

public class MyGradComponent extends JPanel {

    final int paletteSize = 256;
    final int pointsNumber = 5;
    int paletteMode;
    Color palette[];
    Color points[];
    boolean inversion;

    //==================================================================================================================

    public void shiftPalette(int n) {
        for (int i = 0; i < n; i++)
        {
            Color t = palette[0];
            for (int j = 0; j < paletteSize-1; j++)
                palette[j] = palette[j+1];
            palette[paletteSize-1] = t;
        }
    }

    //==================================================================================================================

    public int getPaletteSize() {
        return paletteSize;
    }

    //==================================================================================================================

    public MyGradComponent() {
        setOpaque(true);
        inversion = false;
        palette = new Color [paletteSize];
        points = new Color[pointsNumber];
        setPoints(new Color(0,0,0),
                new Color(64,64,64),
                new Color(128,128,128),
                new Color(192,192,192),
                new Color(255,255,255));
        paletteMode = 0;
        calcPalette();
    }

    public void setPresetPalette(int paletteNum) {
        Color c1, c2, c3, c4, c5;
        switch (paletteNum) {
            case 0: {
                c1 = new Color(0,0,100);
                c2 = new Color(255,150,0);
                c3 = new Color(255,50,0);
                c4 = new Color(0,0,0);
                c5 = new Color(255,255,255);
                break;
            }

            case 1: {
                c1 = new Color(231, 215,197);
                c2 = new Color(70,0,150);
                c3 = new Color(140, 57, 197);
                c4 = new Color(169, 234,17);
                c5 = new Color(0, 90,30);
                break;
            }

            case 2: {
                c1 = new Color(100, 20, 0);
                c2 = new Color(247,245,140);
                c3 = new Color(255,150,24);
                c4 = new Color(70,30,0);
                c5 = new Color(50,200,50);
                break;
            }

            case 3: {
                c1 = new Color(80, 0, 70);
                c2 = new Color(144, 38, 113);
                c3 = new Color(104, 173, 44);
                c4 = new Color(145, 235, 59);
                c5 = new Color(0, 50, 0);
                break;
            }

            case 4: {
                c1 = new Color(241,100,204);
                c2 = new Color(0,70,15);
                c3 = new Color(55,183,178);
                c4 = new Color(0,0,30);
                c5 = new Color(76,167,15);
                break;
            }

            case 5: {
                c1 = new Color(171,78,37);
                c2 = new Color(201,204,107);
                c3 = new Color(12,102,161);
                c4 = new Color(116,44,240);
                c5 = new Color(50,20,60);
                break;
            }

            case 6: {
                c1 = new Color(187,172,104);
                c2 = new Color(0,50,170);
                c3 = new Color(126,247,238);
                c4 = new Color(194,251,165);
                c5 = new Color(220,137,216);
                break;
            }

            case 7: {
                c1 = new Color(22,67,85);
                c2 = new Color(178,24,70);
                c3 = new Color(50,0,10);
                c4 = new Color(229,105,136);
                c5 = new Color(85,227,37);
                break;
            }

            case 8: {
                c1 = new Color(90,185,82);
                c2 = new Color(0,70,30);
                c3 = new Color(137,129,189);
                c4 = new Color(251,152,57);
                c5 = new Color(234,180,141);
                break;
            }

            case 9: {
                c1 = new Color(67,81,28);
                c2 = new Color(212,220,238);
                c3 = new Color(194,60,247);
                c4 = new Color(34,19,80);
                c5 = new Color(167,161,107);
                break;
            }

            default: {
                c1 = c2 = c3 = c4 = c5 = null;
            }
        }
        setPoints(c1, c2, c3, c4, c5);
        calcPalette();
    }

    private Color getRandomColor() {
        Color c = new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
        return c;
    }
    //==================================================================================================================

    public void generateRandomPoints() {
        Color c1 = getRandomColor();
        Color c2 = getRandomColor();
        Color c3 = getRandomColor();
        Color c4 = getRandomColor();
        Color c5 = getRandomColor();
        setPoints(c1, c2, c3, c4, c5);
    }

    //==================================================================================================================

    public void createRandomPalette() {
        generateRandomPoints();
        calcPalette();
    }

    //==================================================================================================================

    public void setPoints(Color c1, Color c2, Color c3, Color c4, Color c5) {
        points[0] = c1;
        points[1] = c2;
        points[2] = c3;
        points[3] = c4;
        points[4] = c5;
    }

    //==================================================================================================================

    public void setPaletteInversion(boolean inversion) {
        if (this.inversion != inversion) {
            this.inversion = inversion;
            inversion();
        }
    }

    //==================================================================================================================

    public void inversion() {
        Color tempColor;
        for (int i = 0; i < paletteSize/2; i++) {
            tempColor = palette[i];
            int j = paletteSize - 1 - i;
            palette[i] = palette[j];
            palette[j] = tempColor;
        }
    }

    //==================================================================================================================

    public void calcPalette() {
        for (int i = 0; i < paletteSize; i++)
        {
            int r, g, b;
            int interval = (int)(i / 64.0);
            int j = i % 64;
            r = points[interval].getRed() + (int)(j * (points[interval + 1].getRed() - points[interval].getRed()) / 64.0);
            g = points[interval].getGreen() + (int)(j * (points[interval + 1].getGreen() - points[interval].getGreen()) / 64.0);
            b = points[interval].getBlue() + (int)(j * (points[interval + 1].getBlue() - points[interval].getBlue()) / 64.0);
            Color c = new Color(r,g,b);
            palette[i] = c;
        }
    }

    //==================================================================================================================

    public Color getColor(int i) {
        if (i >= 0 && i < palette.length) {
            return palette[i];
        }
        else {
            return new Color(0,0,0);
        }
    }

    //==================================================================================================================

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.blue);
        g2d.fillRect(0,0, getWidth(), getHeight());
        for (int i = 0; i < getWidth(); i++)
        {
            int j = (int)(((float)i / getWidth()) * paletteSize);
            g2d.setColor(palette[j]);
            g2d.drawLine(i,0, i, getHeight());
        }
        g2d.setColor(Color.black);
        g2d.drawRect(0,0,getWidth()-1, getHeight()-1);
    }
}

//======================================================================================================================
