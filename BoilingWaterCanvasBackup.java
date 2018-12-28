import java.text.NumberFormat;
import java.awt.*;
import java.awt.Color;
import java.awt.image.BufferStrategy;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;

class RenderController
implements ComponentListener, WindowListener, KeyListener, 
           ItemListener, MouseListener, MouseMotionListener, ActionListener {

    BoilingWaterCanvas waterCanvas;
    Frame frame;
    Button startbutton;
    Button resetbutton;
    Scrollbar scrollbar;

    public RenderController(BoilingWaterCanvas waterCanvas, Frame frame, Button startbutton, Button resetbutton) {

        this.waterCanvas = waterCanvas;
        this.frame = frame;
        this.startbutton = startbutton;
        this.resetbutton = resetbutton;
    }

    public void componentResized(ComponentEvent e) { 
        if (e.getSource().equals(frame)) {
            waterCanvas.WIDTH = waterCanvas.getWidth();
            waterCanvas.HEIGHT = waterCanvas.getHeight();
            waterCanvas.RWIDTH = waterCanvas.WIDTH - 2 * waterCanvas.HORIZONTAL_BORDER_OFFSET;
            waterCanvas.RHEIGHT = waterCanvas.HEIGHT - 2 * waterCanvas.VERTICAL_BORDER_OFFSET;
        }
        waterCanvas.render();
    }

    public void keyPressed(KeyEvent e) {
    }
   
    public void mouseMoved(MouseEvent e) {
    }
    
    public void mousePressed(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {

    }

    public void itemStateChanged(ItemEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        frame.dispose();
        System.exit(0);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startbutton) { 
            try {
                waterCanvas.start();
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        } if (e.getSource() == resetbutton) { 
            waterCanvas.reset();
        } if (e.getSource() == scrollbar) {
            waterCanvas.updateTemperature(scrollbar.getValue());
        }
    }

    //unimplemented methods
    public void keyTyped(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void componentHidden(ComponentEvent e) {}
    public void componentMoved(ComponentEvent e) {}
    public void componentShown(ComponentEvent e) {}
}

public class BoilingWaterCanvas extends Canvas {

    Frame frame = new Frame();
    Button startbutton = new Button();
    Button resetbutton = new Button();
    Scrollbar scrollbar = new Scrollbar(java.awt.Scrollbar.HORIZONTAL,50,1,0,100);

    BufferStrategy strategy;

    int WIDTH; //canvas width
    int HEIGHT; //canvas height

    int RWIDTH; //render area width
    int RHEIGHT; //render area height

    static final int HORIZONTAL_BORDER_OFFSET = 1;
    static final int VERTICAL_BORDER_OFFSET = 1;

    int N = 10; //number of molecules

    double cx = 0.0;
    double cy = 0.0;

    double[] moleculex = new double[10];
    double[] moleculey = new double[10];

    double[] moleculevx = new double[10];
    double[] moleculevy = new double[10];
    
    double dt = 1E-2;
    double moleculeradius = 1.0;

    double m = 1.0; //molecule mass

    double xmin = -5;
    double xmax = 5;
    double ymin = -5;
    double ymax = 5;

    double tolerance = 1E-2;

    double xrange = xmax - xmin;
    double yrange = ymax - ymin;
    double xspeedrange = 10.0;
    double yspeedrange = 10.0;

    RenderController G;
    java.util.Random generator = new java.util.Random();

    boolean startingiteration = false;

    public BoilingWaterCanvas() {

        super();

        assembleLayout();

        WIDTH = this.getWidth();
        HEIGHT = this.getHeight();
        RWIDTH = WIDTH - 2 * HORIZONTAL_BORDER_OFFSET;
        RHEIGHT = HEIGHT - 2 * VERTICAL_BORDER_OFFSET;

        this.setIgnoreRepaint(true);
        this.createBufferStrategy(2);
        strategy = this.getBufferStrategy();

        G = new RenderController(this, frame, startbutton, resetbutton); 

        frame.addComponentListener(G);
        startbutton.addActionListener(G);
        resetbutton.addActionListener(G);
        frame.addWindowListener(G);
    }

    private void assembleLayout() {

        frame.setLayout(new GridBagLayout());
        GridBagConstraints oc = new GridBagConstraints();

        oc.fill = GridBagConstraints.BOTH;
        oc.gridx = 0;
        oc.gridy = 0;
        oc.weightx = 1.0; 
        oc.weighty = 1.0;
        frame.add(this, oc);

        Container bottom = new Container();
        oc.fill = GridBagConstraints.HORIZONTAL;
        oc.gridx = 0;
        oc.gridy = 1;
        oc.weightx = 1.0;   //request any extra horizontal space
        oc.weighty = 0;
        bottom.setPreferredSize(new Dimension(1500,80));
        frame.add(bottom, oc);

        //bottom layout; 
        GridBagConstraints bc = new GridBagConstraints();
        bottom.setLayout(new GridBagLayout());
        //bc.ipadx = 10;
        //bc.ipady = 0;

        startbutton.setLabel("start");
        bc.gridx = 0;
        bc.gridy = 0;
        bc.gridwidth = 3;
        bottom.add(startbutton, bc);
        bc.gridwidth = 1;

        resetbutton.setLabel("reset");
        bc.gridx = 3;
        bc.gridy = 0;
        bc.gridwidth = 3;
        bottom.add(resetbutton, bc);
        bc.gridwidth = 1;

        bc.gridx = 0;
        bc.gridy = 1;
        bc.gridwidth = 3;
        bottom.add(scrollbar, bc);
        bc.gridwidth = 1;

        frame.setSize(800,800);
        frame.setVisible(true);
    }

    public void updateViewingWindow(double xmin, double xmax, double ymin, double ymax) {
    }

    public void updateViewingWindow(double xshift, double yshift) { 
    }

    public void start() throws InterruptedException {

        startingiteration = true;

        for (int i=0; i<N; i++) {
            moleculex[i] = generator.nextDouble() * xrange + xmin;
            moleculey[i] = generator.nextDouble() * yrange + ymin;
            moleculevx[i] = generator.nextDouble() * xspeedrange - xspeedrange / 2.0;
            moleculevy[i] = generator.nextDouble() * yspeedrange - yspeedrange / 2.0;
        }

        //print(moleculex);
        //print(moleculey);
        //print(moleculevx);
        //print(moleculevy);

        while(true) {
            update();
        }
    }

    // the container is centered at (0,0) and is a square of side length 10
    public void update() throws InterruptedException {
        System.out.println("update() being called");
        updatePositions();
        updateUponCollisions();
        render();
        Thread.sleep(2);
    }

    public void updatePositions() {
        for (int i=0; i<N; i++) {
            moleculex[i] += moleculevx[i] * dt;
            moleculey[i] += moleculevy[i] * dt;
        }
        //print(moleculex);
        //print(moleculey);
    }

    public void updateUponCollisions() {
        System.out.println("updateUponCollisions() being called");

        for (int i=0; i<N; i++) {

            if (moleculex[i] - moleculeradius <= xmin) { 
                moleculevx[i] = -moleculevx[i];
            }
            if (moleculex[i] + moleculeradius >= xmax) {
                moleculevx[i] = -moleculevx[i];
            }
            if (moleculey[i] - moleculeradius <= ymin) {
                moleculevy[i] = -moleculevy[i];
            }
            if (moleculey[i] + moleculeradius >= ymax) {
                moleculevy[i] = -moleculevy[i];
            }

            //if (Math.abs(moleculex[i] - moleculeradius - xmin) < tolerance) { 
            //    moleculevx[i] = -moleculevx[i];
            //}
            //if (Math.abs(moleculex[i] + moleculeradius - xmax) < tolerance) { 
            //    moleculevx[i] = -moleculevx[i];
            //}
            //if (Math.abs(moleculey[i] - moleculeradius - ymin) < tolerance) {
            //    moleculevy[i] = -moleculevy[i];
            //}
            //if (Math.abs(moleculey[i] + moleculeradius - ymax) < tolerance) {
            //    moleculevy[i] = -moleculevy[i];
            //}
            //
            //print(moleculevx);
            //print(moleculevy);

            for (int j=i+1; j<N; j++) {
                if (i != j) {
                    if (dist(i,j) <= 2.0 * moleculeradius) {
                        System.out.println("collision between molecules " + i + " and " + j);
                        double tmpvxi = moleculevx[i];
                        double tmpvyi = moleculevy[i];
                        moleculevx[i] = moleculevx[j];
                        moleculevy[i] = moleculevy[j];
                        moleculevx[j] = tmpvxi;
                        moleculevy[j] = tmpvyi;
                    }
                }
            }
        }
        //print(moleculex);
        //print(moleculey);
    }

    public void print(double[] arr) {
        for (int i=0; i<N; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }

    public double dist(int i,int j) {
        if (0 <= i && i < N && 0 <= j && j < N) {
            return(Math.sqrt( (moleculex[i]-moleculex[j])*(moleculex[i]-moleculex[j]) + (moleculey[i]-moleculey[j])*(moleculey[i]-moleculey[j]) )  );
        } else return -1;
    }

    public void reset() {
        System.out.println("reset method called");
    }

    public void updateTemperature(double temp) {
    }

    public void render() {
        do { 
            do {
                Graphics g = strategy.getDrawGraphics();
                paint(g);
                g.dispose();
                try { 
                    Thread.sleep(1);
                }
                catch (Exception e) { 
                    e.printStackTrace();
                }
            }
            while (strategy.contentsRestored());
            strategy.show();
        }
        while (strategy.contentsLost());
    }

    public void paint(Graphics g) {
        g.clearRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.fillRect(0,0, WIDTH, HEIGHT);
        System.out.println("painting canvas");

        g.setColor(Color.BLACK);
        if (startingiteration) { //paintboundingbox
            int[] xpoints = {mathematicalXToRenderX(xmin), mathematicalXToRenderX(xmin), mathematicalXToRenderX(xmax), mathematicalXToRenderX(xmax), mathematicalXToRenderX(xmin)};
            int[] ypoints = {mathematicalYToRenderY(ymin), mathematicalYToRenderY(ymax), mathematicalYToRenderY(ymax), mathematicalYToRenderY(ymin), mathematicalYToRenderY(ymin)};

            g.drawPolyline(xpoints, ypoints, 5);
            startingiteration = false;
        }

        int moleculeradiusinrendercoordinates = Math.abs(mathematicalXToRenderX(cx + moleculeradius) - mathematicalXToRenderX(cx));

        for (int i=0; i<N; i++) {
            int rxi = mathematicalXToRenderX(moleculex[i]) - moleculeradiusinrendercoordinates;
            int ryi = mathematicalYToRenderY(moleculey[i]) - moleculeradiusinrendercoordinates;
            System.out.println("(" + rxi + ", " + ryi + ")");
            g.fillOval(rxi,ryi,moleculeradiusinrendercoordinates*2,moleculeradiusinrendercoordinates*2);
        }

    }
   
   //convert from mathematical coordinates to coordinates on the canvas
    public int mathematicalXToRenderX(double px) {
        int rx = (int) (HORIZONTAL_BORDER_OFFSET + RWIDTH / 2.0 + (px - cx) * (RWIDTH / xrange) );
        return rx;
    }

    public int mathematicalYToRenderY(double py) {
        int ry = (int) (VERTICAL_BORDER_OFFSET + RHEIGHT / 2.0 + (cy - py) * (RHEIGHT / yrange) );
        return ry;
    }

    //convert back
    public double renderYToMathematicalY(int ry) {
        double py = cy - (yrange / RHEIGHT) * (ry - VERTICAL_BORDER_OFFSET - RHEIGHT / 2.0 ); 
        return py;
    }

    public double renderXToMathematicalX(int rx) {
        double px = (xrange / RWIDTH) * (rx - HORIZONTAL_BORDER_OFFSET - RWIDTH / 2.0 ) + cx;
        return px;
    }

    //helper
    public int[] toIntArray(ArrayList<Integer> A) {
        int[] ret = new int[A.size()];
        for (int i=0; i<A.size(); i++) {
            ret[i] = A.get(i);
        }
        return ret;
    }

    public static void main(String[] args) {
        BoilingWaterCanvas g = new BoilingWaterCanvas();
     }

}
