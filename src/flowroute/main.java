package flowroute;

import javax.swing.*;
import javax.swing.border.Border;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;


class Cell {
    private int x, y;
    private boolean isFloor = false;
    private boolean isDoor = false;
    private boolean isCorrectDoor = false;
    private boolean isRock = false;
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public void setCorrectDoor() {
        isCorrectDoor = true;
    }
    public boolean isCorrectDoor() {
        return isCorrectDoor;
    }
    public void setFloor() {
        isFloor = true;
    }
    public void setDoor() {
        isFloor = true;
        isDoor = true;
    }
    public void setRock() {
        isRock = true;
    }
    public void removeRock() {
        isRock = false;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public boolean isFloor() {
        return isFloor;
    }
    public boolean isDoor() {
        return isDoor;
    }
    public boolean isRock() {
        return isRock;
    }
}

class GameFrame extends JFrame {
    MainPanel mainPanel = new MainPanel();
    TimerPanel timerPanel = new TimerPanel();
    MidPanel midPanel;
    RockPanel rockPanel;
    ScorePanel scorePanel;
    String currentUser;
    CardLayout cardLayout = new CardLayout();
    JPanel panelContainer = new JPanel(cardLayout);
    GameManager gm;
    
    public GameFrame(String currentUser) {
        super("Game Page");
        this.currentUser = currentUser;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        Semaphore nextRound = new Semaphore(0);
        Semaphore roundEnd = new Semaphore(0);
        gm = new GameManager(this, nextRound, roundEnd, currentUser);
        rockPanel = new RockPanel(mainPanel, roundEnd, nextRound);
        scorePanel = new ScorePanel(gm);
        
        JPanel utilityPanel = new JPanel(new BorderLayout());
        utilityPanel.setBackground(new Color(20, 44, 70));
	    scorePanel.setOpaque(false);
	    rockPanel.setOpaque(false);
	    timerPanel.setOpaque(false);
        utilityPanel.add(scorePanel, BorderLayout.WEST);
        utilityPanel.add(timerPanel, BorderLayout.CENTER);
        utilityPanel.add(rockPanel, BorderLayout.EAST);

        // 파넬 보관함에 두 파넬 추가
        midPanel = new MidPanel(nextRound);
        panelContainer.add(mainPanel, "Game");
        panelContainer.add(midPanel, "Transition");
        add(panelContainer, BorderLayout.CENTER);
        add(utilityPanel, BorderLayout.NORTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        Thread gmThread = new Thread(gm);
        gmThread.start();
        Thread rockCounter = new Thread(rockPanel);
        rockCounter.start();
        Thread scoreCounter = new Thread(scorePanel);
        scoreCounter.start();
    }
    public void showEnd(int finalScore) {

        End endPanel = new End(currentUser, finalScore, this);
        panelContainer.add(endPanel, "End");
        cardLayout.show(panelContainer, "End");

        // 레이아웃/화면 갱신
        panelContainer.revalidate();
        panelContainer.repaint();
    }
}

class MainPanel extends JPanel implements MouseMotionListener, MouseListener {
    static final int GRID_SIZE = 700;
    static final int CELL_SIZE = 50;
    static final int CELL_NUM = GRID_SIZE / CELL_SIZE;

    // 현재 커서가 있는 칸의 좌표값 변수
    // 칸 * CELL_SIZE = 셀의 왼쪽 위 기준 실제 Frame 좌표값
    private int cellRow = -1;
    private int cellCol = -1;

    static final int maxRocks = 5;
    protected ArrayList<ArrayList<Cell>> cellState = new ArrayList<>();
    protected ArrayList<Cell> rockCells = new ArrayList<>();
    protected LinkedList<Cell> waterCells = new LinkedList<>();
    protected ArrayList<Cell> doorCells = new ArrayList<>();
    protected ArrayList<Cell> floorCells = new ArrayList<>();

    BufferedImage rockImage;
    BufferedImage backgroundImage;

    public MainPanel() {
        // 패널 안에서 MouseListener 이용하기 위해서.
        addMouseMotionListener(this);
        addMouseListener(this);
        initializeGrid();

        try {
            rockImage = ImageIO.read(new File("src/images/rock.png"));
            backgroundImage = ImageIO.read(new File("src/images/background.png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeGrid() {
        cellState.clear();
        rockCells.clear();
        waterCells.clear();
        doorCells.clear();
        floorCells.clear();
        for (int i = 0; i < CELL_NUM;i++) {
            cellState.add(new ArrayList<Cell>());
            for (int j = 0; j < CELL_NUM; j++) {
                cellState.get(i).add(new Cell(j, i));
            }
        }
        MapSelector.getNewMap(cellState, floorCells, doorCells);
    }

    public void reset() {
        initializeGrid();
        repaint();
    }

    public Dimension getPreferredSize() {
        return new Dimension(GRID_SIZE+1, GRID_SIZE+1);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
    
        
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
        
        // 현재 커서가 위치한 셀 색칠
        if (cellRow != -1 && cellCol != -1) {
            g.setColor(new Color(68, 138, 255, 50));
            int xCoord = cellCol * CELL_SIZE;
            int yCoord = cellRow * CELL_SIZE;
            g.fillRect(xCoord+1, yCoord+1, CELL_SIZE, CELL_SIZE);
        }
        
        // 돌 생성 및 소멸
        for (int i = 0; i < rockCells.size(); i++) {
            Cell cell = rockCells.get(i);
            g.setColor(new Color(0,0,0,80));
            int xCoord = cell.getX();
            int yCoord = cell.getY();
            // g.fillRect(xCoord*CELL_SIZE + 1, yCoord*CELL_SIZE + 1, CELL_SIZE, CELL_SIZE);

            g.drawImage(rockImage, xCoord*CELL_SIZE + 1, yCoord*CELL_SIZE + 1, CELL_SIZE, CELL_SIZE, this);
        }
        // 문 색칠
        for (int i = 0; i < doorCells.size(); i++) {
            Cell cell = doorCells.get(i);
            if (cell.isCorrectDoor()) {
                g.setColor(Color.GREEN);
            }
            else {
                g.setColor(Color.RED);
            }
            if (cell.getX() == 0) 
                g.fillRect(0, cell.getY()*CELL_SIZE + 1, 4, CELL_SIZE);
            else
                g.fillRect(GRID_SIZE - 4, cell.getY()*CELL_SIZE, 4, CELL_SIZE);
        }
        // 바닥 색칠
        for (int i = 0; i < floorCells.size(); i++) {
            Cell cell = floorCells.get(i);
            g.setColor(Color.BLACK);
            g.fillRect(cell.getX() * CELL_SIZE + 1, (cell.getY()+1) * CELL_SIZE - 2, CELL_SIZE, 3);
        }
        // 물 흐르기
        int alpha = 255 - (waterCells.size()-1) * 40;
        for (int i = 0; i < waterCells.size(); i++) {
            Cell cell = waterCells.get(i);
            g.setColor(new Color(100, 255, 140, alpha));
            g.fillRect(cell.getX() * CELL_SIZE + (CELL_SIZE/4+1), cell.getY() * CELL_SIZE + (CELL_SIZE/4+1), CELL_SIZE/2, CELL_SIZE/2);
            alpha += 40;
        }

        // 그리드 생성
        g.setColor(new Color(0, 0, 0, 40));
        for (int i=0; i <= GRID_SIZE; i += CELL_SIZE) {
            g.drawLine(i, 0, i, GRID_SIZE);
        }
    
        for (int i=0; i <= GRID_SIZE; i += CELL_SIZE) {
            g.drawLine(0, i, GRID_SIZE, i);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // 실제 좌표 -> 셀 좌표
        int tempRow = e.getY() / CELL_SIZE;
        int tempCol = e.getX() / CELL_SIZE;
        
        // 커서가 있는 셀이 바뀌지 않으면 굳이 다시 그리지 않음.
        if (tempRow != cellRow || tempCol != cellCol) {
            cellRow = tempRow;
            cellCol = tempCol;
            
            repaint();
        }
    }

    // MouseMotionListener 인터페이스에서 무조건 구현해야 하는 메서드
    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {
        // 나가면 리셋
        cellRow = -1;
        cellCol = -1;
        repaint();
    }

    // MouseListener 인터페이스에서 무조건 구현해야 하는 메서드들
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {
        int tempRow = e.getY() / CELL_SIZE;
        int tempCol = e.getX() / CELL_SIZE;

        Cell currentCell = cellState.get(tempRow).get(tempCol);
        if (currentCell.isFloor() && !currentCell.isDoor() && !waterCells.contains(currentCell)) {
            if (rockCells.contains(currentCell)) {
                currentCell.removeRock();
                rockCells.remove(currentCell);
            }
            else {
                if (rockCells.size() < maxRocks) {
                    currentCell.setRock();
                    rockCells.add(currentCell);
                }
            }
            repaint();
        }
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
}

class FlowWater implements Runnable {
    int start;
    MainPanel panel;
    GameManager gm;
    private final int maxLength = 6; // 잔상의 길이
    private final int interval = 100; // 움직임 간의 시간 간격(ms) -> 속도와 반비례
    Queue<Cell> waterQueue;
    boolean isDone = false;

    public FlowWater(MainPanel panel, int start, GameManager gm) {
        this.panel = panel;
        this.start = start;
        this.gm = gm;
        waterQueue = panel.waterCells;
    }
    
    public void run() {
        try {
            Cell currentHead = panel.cellState.get(0).get(start);
            // currentHead.setWater();
            waterQueue.offer(currentHead);
            panel.repaint();
            boolean initialFall = true;
            int direction = 0;

            while(true) {
                Thread.sleep(interval);
                if (!isDone) {
                    int x = currentHead.getX();
                    int y = currentHead.getY();
                    if (currentHead.isFloor()) {
                        // 바닥에 떨어졌을 때 방향 정하기
                        if (initialFall) {
                            direction = MapSelector.getLongDirection(panel.cellState, currentHead, MainPanel.CELL_NUM); // -1: 왼쪽, 0: 동일, 1: 오른쪽
                            // direction이 0이면 랜덤으로 1이나 -1로 배정
                            if (direction == 0) {
                                int random = (int)(Math.random()*2);
                                direction = random==0 ? 1 : -1;
                            }
                            initialFall = false;
                        }
                        // 별개로 양쪽 벽에 위치한 경우 안 나가게 방향 보정
                        if (x == 0 && direction == -1) {
                            if (currentHead.isDoor()) {
                                isDone = true;
                                if (currentHead.isCorrectDoor()) {
                                    gm.score++;
                                }
                                else {
                                    gm.lives--;
                                }
                                continue;
                            }
                            direction = 1;
                        }
                        else if (x == MainPanel.CELL_NUM-1 && direction == 1) {
                            if (currentHead.isDoor()) {
                                isDone = true;
                                if (currentHead.isCorrectDoor()) {
                                    gm.score++;
                                }
                                else {
                                    gm.lives--;
                                }
                                continue;
                            }
                            direction = -1;
                        }
                        // 방향에 따라서 다음 칸 선택
                        if (direction == 1){
                            Cell nextCell = panel.cellState.get(y).get(x+1);
                            if (nextCell.isRock()) {
                                nextCell.removeRock();
                                panel.rockCells.remove(nextCell);
                                direction = -1;
                            }
                            else {
                                currentHead = nextCell;
                            }
                        }
                        else {
                            Cell nextCell = panel.cellState.get(y).get(x-1);
                            if (nextCell.isRock()) {
                                nextCell.removeRock();
                                panel.rockCells.remove(nextCell);
                                direction = 1;
                            }
                            else {
                                currentHead = nextCell;
                            }
                        }
    
                    }
                    else {
                        initialFall = true;
                        if (y < MainPanel.CELL_NUM-1) {
                            currentHead = panel.cellState.get(y+1).get(x);
                            if (currentHead.isRock()) {
                                currentHead.removeRock();
                                panel.rockCells.remove(currentHead);
                            }
                        }
                        else {
                            isDone = true;
                            gm.lives--;
                            continue;
                        }
                    }
                    if (waterQueue.size() == maxLength) {
                        waterQueue.poll();
                    }
                    waterQueue.offer(currentHead);
                    panel.repaint();
                }
                else {
                    waterQueue.poll();
                    panel.repaint();
                }
                if (waterQueue.isEmpty()) {
                    break;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("FlowRoute END");
    }
}

class MapSelector {
    static int mapType;
    public static void getNewMap(ArrayList<ArrayList<Cell>> gridState, ArrayList<Cell> floorCells, ArrayList<Cell> doorCells) {
        mapType = (int)Math.random();
        if (mapType == 0) {
            int array[][] = {{3, 1}, {4, 1}, {5, 1}, {6, 1}, {7, 1}, {7, 3}, {8, 3}, {9, 3}, {9, 5},
                             {10, 5}, {11, 5}, {12, 5}, {13, 5}, {5, 4}, {6, 4}, {7, 4}, {4, 4}, {1, 5}, 
                             {2, 5}, {2, 8}, {3, 8}, {4, 8}, {5, 8}, {5, 7}, {6, 7}, {7, 7}, {8, 7}, 
                             {9, 7}, {10, 8}, {11, 8}, {12, 8}, {13, 8}, {8, 11}, {9, 11}, {10, 11}, 
                             {11, 13}, {12, 13}, {13, 13}, {2, 13}, {3, 13}, {1, 13}, {0, 13}, {1, 11}, 
                             {2, 11}, {5, 12}, {6, 12}, {4, 12}, {6, 10}, {7, 10}, {0, 7}, {1, 7}, 
                             {10, 2}, {11, 2}, {12, 2}, {13, 2}, {0, 11}};
            for (int i = 0; i < array.length; i++) {
                Cell cell = gridState.get(array[i][1]).get(array[i][0]);
                cell.setFloor();
                floorCells.add(cell);
            }
            int arr[][] = {{0, 11}, {13, 8}, {0, 13}, {13, 13}};
            for (int i = 0; i< arr.length; i++) {
                Cell cell = gridState.get(arr[i][1]).get(arr[i][0]);
                cell.setDoor();
                doorCells.add(cell);
            }
            int randNum = (int)(Math.random() * 4);	
            Cell correctDoor = gridState.get(arr[randNum][1]).get(arr[randNum][0]);
            correctDoor.setCorrectDoor();
        }
    }
    public static int getLongDirection(ArrayList<ArrayList<Cell>> gridState, Cell currentCell, int cellNum) {
        int leftCounter = 0;
        int rightCounter = 0;
        int x = currentCell.getX();
        int y = currentCell.getY();
        for (int i = x; gridState.get(y).get(i).isFloor(); i++) {
            rightCounter++;
            if (i == cellNum-1) {
                break;
            }
        }
        for (int i = x; gridState.get(y).get(i).isFloor(); i--) {
            leftCounter++;
            if (i == 0) {
                break;
            }
        }
        return Integer.compare(rightCounter, leftCounter);
//        if (rightCounter > leftCounter)
//            return 1;
//        else if (rightCounter < leftCounter)
//            return -1;
//        else
//            return 0;
    }
}

class RockPanel extends JPanel implements Runnable {
    private JLabel remainingRocksLabel;
    private MainPanel mainPanel;
    Semaphore roundEnd;
    Semaphore nextRound;
    public RockPanel(MainPanel mainPanel, Semaphore roundEnd, Semaphore nextRound) {
        this.mainPanel = mainPanel;
        this.roundEnd = roundEnd;
        this.nextRound = nextRound;

        remainingRocksLabel = new JLabel(mainPanel.rockCells.size() + "");
        remainingRocksLabel.setFont(new Font("TimesRoman", Font.BOLD, 20));
        remainingRocksLabel.setForeground(Color.WHITE);
        add(remainingRocksLabel);
    }

    public Dimension getPreferredSize() {
        return new Dimension(200, 80);
    }

    public void run() {
        while (true) {
            try {
                remainingRocksLabel.setText("Remaining Rocks: " + (MainPanel.maxRocks - mainPanel.rockCells.size()));
                remainingRocksLabel.setForeground(Color.WHITE);
                if (roundEnd.tryAcquire()) {
                    remainingRocksLabel.setText("");
                    nextRound.acquire();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class ScorePanel extends JPanel implements Runnable {
    private JLabel label;
    private GameManager gm;
    private BufferedImage heartImage;
    public ScorePanel(GameManager gm) {
        setLayout(null);
        this.gm = gm;
        setLayout(new FlowLayout(FlowLayout.LEFT));
        label = new JLabel("Score: " + gm.score);
        label.setFont(new Font("TimesRoman", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        add(label);

        try {
            heartImage = ImageIO.read(new File("src/images/heart.png"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Dimension getPreferredSize() {
        return new Dimension(200, 80);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int i = 0;i<gm.lives;i++) {
            int x = 5 + i * 20;
            g.drawImage(heartImage, x, 30, 20, 20, this);
        }
        if (heartImage != null) {
        }

    }
    public void run() {
        while (true) {
            try {
                label.setText("Score: " + gm.score);
                repaint();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class TimerPanel extends JPanel implements Runnable {
    private JLabel label;
    private int timer = 5;
    public TimerPanel() {
        label = new JLabel(timer + "");
        label.setFont(new Font("TimesRoman", Font.ITALIC, 50));
        label.setForeground(Color.RED);
        label.setHorizontalAlignment((JLabel.CENTER));
        add(label);
    }

    public void run() {
        while (true) {
            try {
                label.setText(timer + "");
                Thread.sleep(1000);
                timer--;
                if (timer == 0) {
                    label.setText("");
                    timer = 5;
                    break;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class MidPanel extends JPanel {
	private Image nextImage;
    public MidPanel(Semaphore nextRound) {
    	try {
    	    nextImage = ImageIO.read(getClass().getResource("/images/next_background.png"));
    	} catch (IOException ex) {
    	    ex.printStackTrace();
    	}
    	
        setLayout(null);
        JButton nextRoundButton = new JButton();
        nextRoundButton.setBounds(MainPanel.GRID_SIZE/2-200, (MainPanel.GRID_SIZE-80)/2-75, 300, 150);
        nextRoundButton.setContentAreaFilled(false);
        nextRoundButton.setBorderPainted(false);
        nextRoundButton.setFocusPainted(false);
        nextRoundButton.setOpaque(false);
        add(nextRoundButton);
        nextRoundButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextRound.release(2);
                System.out.println(nextRound.availablePermits());
                System.out.println("PRESSED BUTTON");
            }
        });
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(nextImage, 0, 0, getWidth(), getHeight(), this);
    }

    public Dimension getPreferredSize() {
        return new Dimension(MainPanel.GRID_SIZE+1, MainPanel.GRID_SIZE+10);
    }
}

class GameManager implements Runnable {
    GameFrame frame;
    Semaphore nextRound;
    Semaphore roundEnd;
    String currentUser;
    int score = 0;
    int lives = 3;
    
    public GameManager(GameFrame frame, Semaphore nextRound, Semaphore roundEnd, String currentUser) {
        this.frame = frame;
        this.nextRound = nextRound;
        this.roundEnd = roundEnd;
        this.currentUser = currentUser;
    }
    
    public void run() {
        while (true) {
            try {
                // 실제로 게임을 진행하는 부분
                frame.cardLayout.show(frame.panelContainer, "Game");

                frame.mainPanel.reset();

                Thread timer = new Thread(frame.timerPanel);
                timer.start();
                try {
                    timer.join();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                Thread flow = new Thread(new FlowWater(frame.mainPanel, 4, this));
                flow.start();
                try {
                    flow.join();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                
                roundEnd.release();

                if(lives>0) {
                    frame.cardLayout.show(frame.panelContainer, "Transition");
                }
                else { // 목숨 다 사라지고 다음 화면으로 전환
                    System.out.println("GAME OVER");

                    // 현재 사용자와 점수 가져오기
                    String currentUser = frame.currentUser;
                    int currentScore = score;

                    frame.showEnd(currentScore);
                }
                nextRound.acquire();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

public class main {
    public static void main(String[] args) {
        new start(); // START GAME
    }
}