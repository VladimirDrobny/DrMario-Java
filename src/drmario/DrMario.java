package drmario;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class DrMario extends JFrame implements KeyListener {

    public static void main(String[] args) {
        new DrMario();
    }
    
    public static JFrame frame = new JFrame("Frame");
    public static JPanel panel;
    
    private Handler handler;
    
    int width = 448;
    int height = 896;
    
    BufferedImage assets;
    Image[][] images = new Image[8][4];
    
    public DrMario() {
        setUpFrame();
        
        try {
            URL assetsURL = getClass().getResource("Assets.png");
            assets = ImageIO.read(new File(assetsURL.getPath()));
            //BG = assets.getSubimage(56, 24, 8, 8);
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 4; j++) {
                    images[i][j] = assets.getSubimage(8*i, 8*j, 8, 8);
                }
            }
        } catch (Exception e) {}
        
        handler = new Handler(width, height);
        handler.start();
    }
    
    private void setUpFrame() {
        //setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width+6, height+28);
        setVisible(true);
        setResizable(false);
        setName("Frame");
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1,1,0,0));
        
        panel = new JPanel() {@Override public void paint(Graphics g) {
            draw(g);
        }};
        
        add(panel);
        addKeyListener(this);
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 16; j++) {
                g.drawImage(images[7][3], 56*i, 56*j, 56, 56, rootPane);
                if (handler != null) {
                    if (handler.tiles[i][j] != null) {
                        Tile t = handler.tiles[i][j];
                        g.drawImage(images[t.type][t.col], 56*i, 56*j, 56, 56, rootPane);
                    }
                }
            }
        }
        
        if (handler != null) {
            if (handler.pillVisible) {
                g.drawImage(images[handler.pillSide ? 2 : 3][handler.pillCol], 56*handler.pillX, 56*handler.pillY + 7, 56, 56, rootPane);
                g.drawImage(images[handler.pillSide ? 0 : 1][handler.pillCol2], 56*(handler.pillX + (handler.pillSide ? 1 : 0)), 56*(handler.pillY - (handler.pillSide ? 0 : 1)) + 7, 56, 56, rootPane);
            }
        }
        
        
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 38) {
            handler.rotate();
        }
        if (e.getKeyCode() == 37) {
            handler.moveLeft();
        }
        if (e.getKeyCode() == 40 && !handler.fastFall) {
            handler.fastFall = true;
            if (handler.commandId == 0) {
                handler.fall();
            }
        }
        if (e.getKeyCode() == 39) {
            handler.moveRight();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == 27) {
            if (handler.paused == true) {
                handler.resume();
            }
            handler.paused = !handler.paused;
        }
        
        if (e.getKeyCode() == 40) {
            handler.fastFall = false;
        }
    }

}
