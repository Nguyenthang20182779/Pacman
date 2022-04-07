

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Board extends JPanel implements ActionListener {

    private Dimension dimension;
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14);
    private Image ii;
    private final Color dotColor = new Color(192, 192, 0);
    private Color mazeColor;
    private boolean inGame = false;
    private boolean dying = false;
    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int PAC_ANIM_DELAY = 2;
    private final int PAC_ANIM_COUNT = 4;
    private final int MAX_GHOSTS = 12;
    private final int PACMAN_SPEED = 6;
    private int pacAnimCount = PAC_ANIM_COUNT;
    private int pacAnimDir = 1;
    private int pacAnimPos = 0;
    private int N_GHOSTS = 6;
    private int pacsLeft, score;
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;
    private Image ghost, pacman;
    private Image pacman1up, pacman1left, pacman1right, pacman1down;
    private Image pacman3up, pacman3left, pacman3right, pacman3down;
    private Image pacman2up, pacman2left, pacman2right, pacman2down;
    private int pacman_x, pacman_y, pacman_dx, pacman_dy;
    private int req_dx, req_dy, view_dx, view_dy;
    private final short levelData[] = {
            19, 26, 26, 26, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            21, 0, 0, 0, 17, 16, 16, 24, 16, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 18, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 20,
            25, 16, 16, 16, 24, 24, 28, 0, 25, 24, 24, 16, 20, 0, 21,
            1, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 20, 0, 21,
            1, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 18, 16, 16, 16, 16, 20, 0, 21,
            1, 17, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0, 21,
            1, 25, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 18, 20,
            9, 8, 8, 8, 8, 8, 8, 8, 8, 8, 25, 24, 24, 24, 28
    };
    private final int validSpeeds[] = {1, 2, 3, 4, 5, 6, 8};
    private final int maxSpeed = 6;
    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    public Board(){
        loadImages();
        initVariables();
        initBoard();
    }

    private void loadImages(){
        ghost = new ImageIcon(getClass().getResource("ghost.gif")).getImage();
        pacman = new ImageIcon(getClass().getResource("pacman.gif")).getImage();
        pacman1up = new ImageIcon(getClass().getResource("up1.gif")).getImage();
        pacman2up = new ImageIcon(getClass().getResource("up2.gif")).getImage();
        pacman3up = new ImageIcon(getClass().getResource("up3.gif")).getImage();
        pacman1down = new ImageIcon(getClass().getResource("down1.gif")).getImage();
        pacman2down = new ImageIcon(getClass().getResource("down2.gif")).getImage();
        pacman3down = new ImageIcon(getClass().getResource("down3.gif")).getImage();
        pacman1right = new ImageIcon(getClass().getResource("right1.gif")).getImage();
        pacman2right = new ImageIcon(getClass().getResource("right2.gif")).getImage();
        pacman3right = new ImageIcon(getClass().getResource("right3.gif")).getImage();
        pacman1left = new ImageIcon(getClass().getResource("left1.gif")).getImage();
        pacman2left = new ImageIcon(getClass().getResource("left2.gif")).getImage();
        pacman3left = new ImageIcon(getClass().getResource("left3.gif")).getImage();
    }

    private void initBoard(){
        addKeyListener(new TAdapter());
        setFocusable(true);
        setBackground(Color.BLACK);
    }

    private void initVariables(){
        screenData = new short[N_BLOCKS * N_BLOCKS];
        mazeColor = new Color(5, 100, 5);
        dimension = new Dimension(400, 400);
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];
        timer = new Timer(40,this);
        timer.start();
    }

    @Override
    public void addNotify(){
        super.addNotify();
        initGame();
    }

    private void initGame(){
        pacsLeft = 3;
        score = 0;
        initLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }

    private void initLevel(){
        for(int i=0; i< N_BLOCKS * N_BLOCKS; i++){
            screenData[i] = levelData[i];
        }
        continueLevel();
    }

    private void continueLevel(){
        short i;
        int dx = 1;
        int random;
        for(i=0; i<N_GHOSTS; i++){
            ghost_y[i] = 4 * BLOCK_SIZE;
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));
            if( random > currentSpeed){
                random = currentSpeed;
            }
            ghostSpeed[i] = validSpeeds[random];
        }
        pacman_x = 7 * BLOCK_SIZE;
        pacman_y = 11 * BLOCK_SIZE;
        pacman_dx = 0;
        pacman_dy = 0;
        req_dx = 0;
        req_dy = 0;
        view_dx = -1;
        view_dy = 0;
        dying = false;

    }




    private void doAnim(){
        pacAnimCount--;
        if(pacAnimCount <= 0){
            pacAnimCount = PAC_ANIM_DELAY;
            pacAnimPos = pacAnimPos + pacAnimDir;
            if(pacAnimPos == (PAC_ANIM_COUNT-1) || pacAnimPos == 0){
                pacAnimDir = -pacAnimDir;
            }
        }

    }

    private void playGame(Graphics2D g2d){
        if(dying){
            death();
        }
        else{
            movePacman();
            drawPacman(g2d);
            moveGhost(g2d);
            checkMaze();
        }

    }

    private void death(){
        pacsLeft--;
        if(pacsLeft == 0){
            inGame = false;
        }
        continueLevel();
    }

    private void movePacman(){
        int pos;
        short ch;
        if(req_dx == -pacman_dx && req_dy == -pacman_dy){
            pacman_dx = req_dx;
            pacman_dy = req_dy;
            view_dx = pacman_dx;
            view_dy = pacman_dy;
        }
        if(pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0){
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
            ch = screenData[pos];
            if((ch & 16) != 0){
                screenData[pos] = (short) (ch & 15);
                score++;
            }
            if(req_dx != 0 || req_dy != 0){
                if(  !( (req_dx == -1 && req_dy == 0 && (ch & 1) != 0) || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0) ||
                        (req_dx == 0 && req_dy == 1 && (ch & 2) != 0) || (req_dx == 0 && req_dy == -1 && (ch & 8) != 0) )){

                    pacman_dx = req_dx;
                    pacman_dy = req_dy;
                    view_dx = pacman_dx;
                    view_dy = pacman_dy;
                }
            }
            // Kiem tra su ngung lai
            if( (pacman_dx == -1 && pacman_dy == 0 && (ch & 1) != 0) || (pacman_dx == 1 && pacman_dy == 0 && (ch & 4) != 0) ||
                    (pacman_dx == 0 && pacman_dy == -1 && (ch & 2) != 0) || (pacman_dx == 0 && pacman_dy == 1 && (ch & 8) != 0) ){
               pacman_dx = 0;
               pacman_dy = 0;
            }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacman_dx;
        pacman_y = pacman_y + PACMAN_SPEED * pacman_dy;
    }

    private void drawPacman(Graphics2D g2d){
        if(view_dx == -1){
            drawPacmanLeft(g2d);
        }
        if(view_dx == 1){
            drawPacmanRight(g2d);
        }
        if(view_dy == -1){
            drawPacmanUp(g2d);
        }
        if(view_dy == 1){
            drawPacmanDown(g2d);
        }
    }

    private void drawPacmanLeft(Graphics2D g2d){
        switch(pacAnimPos){
            case 1:
                g2d.drawImage(pacman1left, pacman_x+1, pacman_y+1, this);
                break;

            case 2:
                g2d.drawImage(pacman2left, pacman_x+1, pacman_y+1, this);
                break;
            case 3:
                g2d.drawImage(pacman3left, pacman_x+1, pacman_y+1, this);
                break;
            default:
                g2d.drawImage(pacman, pacman_x+1, pacman_y+1, this);
                break;
        }
    }

    private void drawPacmanRight(Graphics2D g2d){
        switch(pacAnimPos){
            case 1:
                g2d.drawImage(pacman1right, pacman_x+1, pacman_y+1, this);
                break;

            case 2:
                g2d.drawImage(pacman2right, pacman_x+1, pacman_y+1, this);
                break;
            case 3:
                g2d.drawImage(pacman3right, pacman_x+1, pacman_y+1, this);
                break;
            default:
                g2d.drawImage(pacman, pacman_x+1, pacman_y+1, this);
                break;
        }
    }

    private void drawPacmanUp(Graphics2D g2d){
        switch(pacAnimPos){
            case 1:
                g2d.drawImage(pacman1up, pacman_x+1, pacman_y+1, this);
                break;

            case 2:
                g2d.drawImage(pacman2up, pacman_x+1, pacman_y+1, this);
                break;
            case 3:
                g2d.drawImage(pacman3up, pacman_x+1, pacman_y+1, this);
                break;
            default:
                g2d.drawImage(pacman, pacman_x+1, pacman_y+1, this);
                break;
        }
    }

    private void drawPacmanDown(Graphics2D g2d){
        switch(pacAnimPos){
            case 1:
                g2d.drawImage(pacman1down, pacman_x+1, pacman_y+1, this);
                break;
            case 2:
                g2d.drawImage(pacman2down, pacman_x+1, pacman_y+1, this);
                break;
            case 3:
                g2d.drawImage(pacman3down, pacman_x+1, pacman_y+1, this);
                break;
            default:
                g2d.drawImage(pacman, pacman_x+1, pacman_y+1, this);
               break;
        }
    }

    private void moveGhost(Graphics2D g2d){
        int pos;
        int count;
        for(short i=0; i<N_GHOSTS; i++){
            if(ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0){
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);
                count = 0;
                if((screenData[pos] & 1) == 0 && ghost_dx[i] != 1){
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }
                if((screenData[pos] & 2) == 0 && ghost_dx[i] != 1){
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }
                if((screenData[pos] & 4) == 0 && ghost_dx[i] != -1){
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }
                if((screenData[pos] & 8) == 0 && ghost_dx[i] != -1){
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if(count == 0){
                    if((screenData[pos] & 15) == 15){
                        ghost_dx[i] = 0;
                        ghost_dy[i] = 0;
                    }
                    else {
                        ghost_dx[i] = -ghost_dx[i];
                        ghost_dy[i] = -ghost_dy[i];
                    }
                }

                else{
                    count = (int) (Math.random() * count);
                    if(count > 3){
                        count = 3;
                    }
                    ghost_dx[i] = dx[count];
                    ghost_dy[i] = dy[count];
                }
            }
            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);

            //If there is a collision between ghosts and Pacman, Pacman dies
            if(pacman_x > (ghost_x[i] - 12) && pacman_x < ghost_x[i] + 12 && pacman_y > (ghost_y[i] - 12) && pacman_y > (ghost_y[i] - 12)){
                dying = true;
            }
        }

    }

    private void drawGhost(Graphics2D g2d, int x, int  y){
        g2d.drawImage(ghost, x, y, this);
    }

    private void checkMaze(){
        short i = 0;
        boolean finished = true;
        while(i < N_BLOCKS * N_BLOCKS && finished){
            if((screenData[i] & 48) != 0){
                finished = false;
            }
            i++;
        }
        if(finished){
            score += 50;
            if(N_GHOSTS < MAX_GHOSTS){
                N_GHOSTS++;
            }
            if(currentSpeed < maxSpeed){
                currentSpeed++;
            }
            initLevel();
        }

    }




    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, dimension.width, dimension.height);
        drawMaze(g2d);
        drawScore(g2d);
        doAnim();
        if(inGame){
            playGame(g2d);
        }
        else{
            showIntroScreen(g2d);
        }
        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    private void drawMaze(Graphics2D g2d){
        short i = 0;
        int x, y;
        for(y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE){
            for(x = 0; x < SCREEN_SIZE; x+= BLOCK_SIZE){
                g2d.setColor(mazeColor);
                g2d.setStroke(new BasicStroke(2));
                if((screenData[i] & 1) != 0){
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }
                if((screenData[i] & 2) != 0){
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }
                if((screenData[i] & 4) != 0){
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
                }
                if((screenData[i] & 8) != 0){
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1, y + BLOCK_SIZE - 1);
                }
                if((screenData[i] & 16) != 0) {
                    g2d.setColor(dotColor);
                    g2d.fillRect(x + 11, y + 11, 2, 2);
                }
                i++;
            }
        }

    }

    private void drawScore(Graphics2D g2d){
        int i;
        String s;
        g2d.setFont(smallFont);
        g2d.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g2d.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);
        for(i=0; i < pacsLeft; i++){
            g2d.drawImage(pacman2left, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void showIntroScreen(Graphics2D g2d){
        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
        String s = "Press s to start";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics fm = this.getFontMetrics(small);
        g2d.setColor(Color.WHITE);
        g2d.setFont(small);
        g2d.drawString(s, (SCREEN_SIZE - fm.stringWidth(s)) / 2, SCREEN_SIZE / 2);
    }






    //control
    class TAdapter extends KeyAdapter{

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            if(inGame){
                if(key == KeyEvent.VK_LEFT){
                    req_dx = -1;
                    req_dy = 0;
                }
                if(key == KeyEvent.VK_RIGHT){
                    req_dx = 1;
                    req_dy = 0;
                }
                if(key == KeyEvent.VK_UP){
                    req_dx = 0;
                    req_dy = -1;
                }
                if(key == KeyEvent.VK_DOWN){
                    req_dx = 0;
                    req_dy = 1;
                }
                if(key == KeyEvent.VK_ESCAPE && timer.isRunning()){
                    inGame = false;
                }
                if(key == KeyEvent.VK_PAUSE){
                    if(timer.isRunning()){
                        timer.stop();
                    }
                    else{
                        timer.start();
                    }
                }

            }

            else{
                if(key == 's' || key == 'S'){
                    inGame = true;
                    initGame();
                }
            }

        }

        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();
            if(key == Event.LEFT || key == Event.RIGHT || key == Event.UP || key == Event.DOWN){
                req_dx = 0;
                req_dy = 0;
            }

        }
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }


}
