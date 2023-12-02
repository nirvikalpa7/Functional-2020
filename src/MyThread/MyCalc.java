package MyThread;

import MyGradient.MyGradComponent;
import java.awt.*;

import static java.lang.Math.sin;
import static java.lang.Math.cos;

// Кдасс для многопоточного рассчета изображения по кускам (делится по y на части для разных потоков)
public class MyCalc implements Runnable {
    public Color arr[][];

    static int frameWidth, picNumber;
    static double step, startX;
    static MyGradComponent grad;

    int y1, y2;
    double startY;

    //==================================================================================================================

    public void run() {
        DrawScenePart();
    }

    public static void setStaticVar(MyGradComponent newGrad, int newFrameWidth, int newPicNumber, double newStep, double newStartX) {
        grad = newGrad;

        // Ширина изображения
        frameWidth = newFrameWidth;
        // Номер рассчитываемого изображения
        picNumber = newPicNumber;

        step = newStep;
        startX = newStartX;
    }

    //==================================================================================================================

    public void setVar(int y1, int y2, double startY) {

        // Границы по y для потока
        this.y1 = y1;
        this.y2 = y2;

        this.startY = startY;

        // Массив для хранения пиксилей
        arr = new Color [frameWidth][y2-y1+1];
    }

    //==================================================================================================================

    private double getScenePixel(double fx, double fy) {
        double cf;
        switch (picNumber) {
            case 0:
                cf = fx * sin(fy) + fy * cos(fx);
                break;
            case 1:
                cf = sin(fx * fx) + cos(fy * fy);
                break;
            case 2:
                cf = sin(fx) + cos(fy) + fx * fy;
                break;
            case 3:
                cf = sin(fy*fx) - cos(fy*fy+fx*fx);
                break;
            case 4:
                cf = (fx+fy)*sin(fx) + (fx-fy)*cos(fy);
                break;
            case 5:
                cf = sin(fx)*cos(fy) + cos(fx+fy)*sin(fy-fx);
                break;
            default:
                cf = 0;
        }
        return cf;
    }

    //==================================================================================================================

    private void DrawScenePart() {
        final double zoom = 100;
        double fx, cf = 0;
        double fy = startY;
        int c;
        Color color;
        for (int y = y1; y <= y2; y++, fy += step)
        {
            fx = startX;
            for (int x = 0; x < frameWidth; x++, fx += step)
            {
                cf = getScenePixel(fx, fy);
                c = Math.abs((int) (zoom *cf)) % grad.getPaletteSize();
                if (c == 0)
                {
                    int a = 10;
                }
                color = new Color(grad.getColor(c).getRGB());

                arr[x][y-y1] = color;
            }
            //System.out.println("Hi from " + Thread.currentThread().getName());
        }
    }

    //==================================================================================================================
}
