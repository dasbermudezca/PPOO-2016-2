package maze;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import javax.swing.ImageIcon;

/**
 * A very simple example to illustrate how simple tile maps can be used for
 * basic collision. This particular technique only works in certain
 * circumstances and for small time updates. However, this fits many maze based
 * games perfectly.
 *
 * @author Kevin Glass
 */
public class Execute extends Canvas implements KeyListener {

    /**
     * The buffered strategy used for accelerated rendering
     */
    private BufferStrategy strategy;

    /**
     * True if the key is currently pressed
     */
    private boolean left, right, up, down, w, a, s, d;

    /**
     * The map our player will wander round
     */
    private CanvasMaze maze;
    /**
     * The player entity that will be controlled with cursors
     */
    private Entity player1, player2;

    /**
     * Create the simple game - this also starts the game loop
     */
    private Image win;
    private Image loose;

    private final static int FRAME_WIDTH = 856;
    private final static int FRAME_HEIGHT = 720;

    public Execute() {
        // right, I'm going to explain this in detail since it always seems to 
        // confuse. 

        // create the AWT frame. Its going to be fixed size (500x500) 
        // and not resizable - this just gives us less to account for
        Frame frame = new Frame("Maze Runners!");
        frame.setLayout(null);
        setBounds(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        frame.add(this);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setResizable(true);
        frame.setLocationRelativeTo(null);

        // add a listener to respond to the window closing so we can
        // exit the game
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        // add a key listener that allows us to respond to player
        // key presses. We're actually just going to set some flags
        // to indicate the current player direciton
        frame.addKeyListener(this);
        addKeyListener(this);

        // show the frame before creating the buffer strategy!
        frame.setVisible(true);

        // create the strategy used for accelerated rendering. 
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // create our game objects, a map for the player to wander around
        // and an entity to represent out player
        maze = new CanvasMaze(0, 0, 5, 8);
        player1 = new Entity(maze, "PJ1", (float) (-1 + maze.getTotalWIDTH() * 2 - 1.5), 1.1f);
        player2 = new Entity(maze, "PJ2", 1.5f, 1.1f);
        // start the game loop
        player1.fillSprites();
        player2.fillSprites();
        gameLoop();
    }

    /**
     * The game loop handles the basic rendering and tracking of time. Each loop
     * it calls off to the game logic to perform the movement and collision
     * checking.
     */
    public void gameLoop() {
        boolean gameRunning = true;
        long last = System.nanoTime();
        Graphics2D g;
        ImageIcon win = new ImageIcon("pictures/MSGs/win.png");
        this.win = win.getImage();
        ImageIcon loose = new ImageIcon("pictures/MSGs/loose.png");
        this.loose = loose.getImage();

        // keep looking while the game is running
        while (gameRunning) {
            g = (Graphics2D) strategy.getDrawGraphics();

            // clear the screen
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);

            // render our game objects
            g.translate(8, 32);
            maze.paint(g);
            if (left || right || up || down) {
                player1.paint(g);
            } else {
                player1.paintFrame(g);
            }
            if (w || a || s || d) {
                player2.paint(g);
            } else {
                player2.paintFrame(g);
            }
            if (player1.isWinner()) {
                gameRunning = false;
                g.setColor(Color.GREEN);
                g.fillRect(FRAME_WIDTH / 2, 0, FRAME_WIDTH / 2, FRAME_HEIGHT);
                g.drawImage(this.win, FRAME_WIDTH * 3 / 4 - this.win.getWidth(null) / 2,
                         FRAME_HEIGHT / 2 - this.win.getHeight(null) / 2, null);
                g.setColor(Color.RED);
                g.fillRect(0, 0, FRAME_WIDTH / 2, FRAME_HEIGHT);
                g.drawImage(this.loose, FRAME_WIDTH / 4 - this.loose.getWidth(null) / 2,
                         FRAME_HEIGHT / 2 - this.loose.getHeight(null) / 2, null);
            } else if (player2.isWinner()) {
                gameRunning = false;
                g.setColor(Color.RED);
                g.fillRect(FRAME_WIDTH / 2, 0, FRAME_WIDTH / 2, FRAME_HEIGHT);
                g.drawImage(this.loose, FRAME_WIDTH * 3 / 4 - this.loose.getWidth(null) / 2,
                         FRAME_HEIGHT / 2 - this.loose.getHeight(null) / 2, null);
                g.setColor(Color.GREEN);
                g.fillRect(0, 0, FRAME_WIDTH / 2, FRAME_HEIGHT);
                g.drawImage(this.win, FRAME_WIDTH / 4 - this.win.getWidth(null) / 2,
                         FRAME_HEIGHT / 2 - this.win.getHeight(null) / 2, null);
            }
            // flip the buffer so we can see the rendering
            g.dispose();
            strategy.show();

            // pause a bit so that we don't choke the system
            try {
                Thread.sleep(5);
            } catch (Exception e) {
                System.out.println("Exception: " + e);
            }

            // calculate how long its been since we last ran the
            // game logic
            long delta = (System.nanoTime() - last) / 1000000;
            last = System.nanoTime();

            // now this needs a bit of explaining. The amount of time
            // passed between rendering can vary quite alot. If we were
            // to move our player based on the normal delta it would
            // at times jump a long distance (if the delta value got really
            // high). So we divide the amount of time passed into segments
            // of 5 milliseconds and update based on that
            for (int i = 0; i < delta / 5; i++) {
                logic(5);
            }
            // after we've run through the segments if there is anything
            // left over we update for that
            if ((delta % 5) != 0) {
                logic(delta % 5);
            }
        }
    }

    /**
     * Our game logic method - for this example purpose this is very simple.
     * Check the keyboard, and attempt to move the player
     *
     * @param delta The amount of time to update for (in milliseconds)
     */
    public void logic(long delta) {
        // check the keyboard and record which way the player
        // is trying to move this loop
        float dx1 = 0;
        float dy1 = 0;
        float dx2 = 0;
        float dy2 = 0;
        if (left) {
            dx1--;
        }
        if (right) {
            dx1++;
        }
        if (up) {
            dy1--;
        }
        if (down) {
            dy1++;
        }
        if (a) {
            dx2--;
        }
        if (d) {
            dx2++;
        }
        if (w) {
            dy2--;
        }
        if (s) {
            dy2++;
        }

        // if the player needs to move, attempt to move the entity
        // based on the keys multiplied by the amount of time that's
        // passed
        if ((dx1 != 0) || (dy1 != 0)) {
            player1.move(dx1 * delta * 0.003f,
                    dy1 * delta * 0.003f);
        }
        if ((dx2 != 0) || (dy2 != 0)) {
            player2.move(dx2 * delta * 0.003f,
                    dy2 * delta * 0.003f);
        }
    }

    /**
     * @param e
     * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * @param e
     * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
     */
    @Override
    public void keyPressed(KeyEvent e) {
        // check the keyboard and record which keys are pressed
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            a = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_D) {
            d = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_W) {
            w = true;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            s = true;
        }
    }

    /**
     * @param e
     * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
     */
    @Override
    public void keyReleased(KeyEvent e) {
        // check the keyboard and record which keys are released
        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            left = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            down = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            up = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            a = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_D) {
            d = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_W) {
            w = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            s = false;
        }
    }

    /**
     * The entry point to our example code
     *
     * @param argv The arguments passed into the program
     */
    public static void main(String[] argv) {
        Execute execute = new Execute();
    }
}
