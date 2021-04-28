package drmario;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Handler extends Thread {
    
    public Handler(int W, int H) {
        while(virus > 0) {
            int i = (int)Math.floor(Math.random()*8);
            int j = 15 - (int)Math.floor(Math.random()*13);
            
            byte newCol = (byte) Math.floor(Math.random()*3);
            if (tiles[i][j] == null) {
                int xSum = 1;
                
                int x = i-1;
                while(x >= 0 && i-x <= 3 && tiles[x][j] != null) {
                    if (tiles[x][j].col == newCol) {
                        xSum++;
                    } else {
                        break;
                    }
                    
                    x--;
                }
                
                x = i+1;
                while(x < 8 && x-i <= 3 && tiles[x][j] != null) {
                    if (tiles[x][j].col == newCol) {
                        xSum++;
                        if (xSum == 4) {break;}
                    } else {
                        break;
                    }
                    
                    x++;
                }
                
                int ySum = 1;
                
                int y = j-1;
                while(y >= 0 && j-y <= 3 && tiles[i][y] != null) {
                    if (tiles[i][y].col == newCol) {
                        ySum++;
                    } else {
                        break;
                    }
                    
                    y--;
                }
                
                y = j+1;
                while(y < 16 && y-j <= 3 && tiles[i][y] != null) {
                    if (tiles[i][y].col == newCol) {
                        ySum++;
                        if (ySum == 4) {break;}
                    } else {
                        break;
                    }
                    
                    y++;
                }
                
                if (xSum != 4 && ySum != 4) {
                    tiles[i][j] = new Tile(newCol, (byte)6);
                    virus--;
                }
            }
        }
        
        newPill();
    }
    
    public boolean paused = false;
    private long delay = 100L;
    
    public Tile[][] tiles = new Tile[8][16];
    
    private int level = 4;
    private int virus = 4*(level+1);
    
    public byte pillX;
    public byte pillY;
    
    public boolean pillSide = true;
    public byte pillCol;
    public byte pillCol2;
    
    public boolean pillVisible = true;
    
    private int queueId = 0;
    
    public int ticksToWait = 0;
    public int commandId = 0;
    
    private int fallWait = 10;
    private int fallWaitFast = 1;
    
    private int ticksUntilFall = fallWait;
    
    public boolean fastFall = false;
    
    private int gameType = 0;
    
    @Override
    public void run() {
        while(true) {
            long startTime = System.currentTimeMillis();
            virusAnim();
            
            
            if (ticksToWait == 0) {
                switch(commandId) {
                    case 0:
                        if (ticksUntilFall == 0) {
                            fall();
                        }
                        if (ticksUntilFall > 0) {ticksUntilFall--;}
                        break;
                    case 1:
                        checkClears();
                        break;
                    case 2:
                        clearCleared();
                        break;
                    case 3:
                        collapse();
                        break;
                }
            }
            
            if (ticksToWait > 0) {ticksToWait--;}   
            
            
            long endTime = System.currentTimeMillis();
            long elapsed = endTime-startTime;
            if (elapsed < delay) {
                try {
                    Thread.sleep((long) (delay-elapsed));
                } catch (InterruptedException ex) {
                    Logger.getLogger(Handler.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                delay = elapsed;
            }
            if (paused) {
                suspend();
            }
        }        
    }
    
    private void virusAnim() {
        for (int i = 0; i < 8; i++) {
            for (int j = 3; j < 16; j++) {
                if (tiles[i][j] != null) {
                    if (tiles[i][j].type == 6 || tiles[i][j].type == 7) {
                        tiles[i][j].type = (byte) (13-tiles[i][j].type);
                    }
                }
            }
        }
    }
    
    public void moveRight() {
        if (tiles[pillX][pillY] != null || tiles[pillX+(pillSide? 1 : 0)][pillY] != null) {
            return;
        }
        
        if (pillX < (pillSide ? 6 : 7)) {
            if (tiles[pillX+1][pillY] == null && tiles[pillX+1+((pillSide && pillX < 6) ? 1 : 0)][pillY - ((!pillSide && pillY > 0) ? 1 : 0)] == null) {
                pillX++;
            }
        }
    }
    
    public void moveLeft() {
        if (tiles[pillX][pillY] != null || tiles[pillX+(pillSide? 1 : 0)][pillY] != null) {
            return;
        }
        
        if (pillX > 0) {
            if (tiles[pillX-1][pillY] == null && tiles[pillX-1][pillY - ((!pillSide && pillY > 0) ? 1 : 0)] == null) {
                pillX--;
            }
        }
    }
    
    public void rotate() {
        if (pillY < 0) {
            return;
        }
        
        if (pillSide && tiles[pillX][pillY - (pillY > 0 ? 1 : 0)] != null) {
            return;
        }
        if (!pillSide && (tiles[pillX + (pillX < 7 ? 1 : 0)][pillY] != null || pillX == 7)) {
            if (pillX > 0 && tiles[pillX - (pillX > 0 ? 1 : 0)][pillY] == null) {
                pillX--;
            } else {
                return;
            }
        }
        
        if (pillSide) {
            pillCol += pillCol2;
            pillCol2 = (byte) (pillCol - pillCol2);
            pillCol -= pillCol2;
        }

        pillSide = !pillSide;
    }
    
    public void collapse() {
        boolean fell = false;
        
        for (int j = 14; j >= 0; j--) {
            for (int i = 0; i < 8; i++) {
                if (tiles[i][j] != null) {
                    if (tiles[i][j].type < 5) {
                        if (tiles[i][j+1] == null) {
                            if ((tiles[i][j].type == 2 && tiles[i+1][j+1] != null) || (tiles[i][j].type == 0 && tiles[i-1][j] != null)) {
                                continue;
                            }
                            
                            tiles[i][j+1] = new Tile(tiles[i][j].col, tiles[i][j].type);
                            tiles[i][j] = null;
                            fell = true;
                        }
                    }
                }
            }
        }
        
        if (fell) {
            ticksToWait = 2;
            commandId = 3;
        } else {
            ticksToWait = 2;
            commandId = 1;
        }
    }
    
    public void fall() {
        ticksUntilFall = (fastFall) ? fallWaitFast : fallWait;
        if (tiles[pillX][pillY] != null || tiles[pillX + (pillX < 7 ? 1 : 0)][pillY] != null && pillY == 0) {
            placePill();
        } else if (pillY == 15) {
            placePill();
        } else if (tiles[pillX][pillY+1] == null && tiles[pillX + (pillSide ? 1 : 0)][pillY+1] == null) {
            pillY++;
        } else {
            placePill();
        }
    }
    
    private void placePill() {
        boolean ending = false;
        
        if (tiles[pillX][pillY] != null || tiles[pillX + (pillX < 7 ? 1 : 0)][pillY] != null && pillY == 0) {
            ending = true;
        }
        
        tiles[pillX][pillY] = new Tile(pillCol, (byte) (pillSide ? 2 : 3));
        if (!(!pillSide && pillY == 0)) {
            tiles[pillX + (pillSide ? 1 : 0)][pillY - (pillSide ? 0 : 1)] = new Tile(pillCol2, (byte) (pillSide ? 0 : 1));
        }
        
        if (ending) {
            gameOver();
            return;
        }
        
        pillVisible = false;
        ticksToWait = 1;
        commandId = 1;
        //checkClears();
    }
    
    public void gameOver() {
        System.exit(0);
    }
    
    public void gameWon() {
        System.exit(0);
    }
    
    private void clearCleared() {
        boolean cleared = false;
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 16; j++) {
                if (tiles[i][j] != null) {
                    if (tiles[i][j].type == 5) {
                        tiles[i][j] = null;
                        cleared = true;
                    }
                }
            }
        }
        
        if (cleared) {
            ticksToWait = 2;
            commandId = 3;
        } else {
            if (!checkForGameWin()) {
                commandId = 0;
                newPill();
            } else {
                gameWon();
            }
        }
    }
    
    public boolean checkForGameWin() {
        switch (gameType) {
            case 0:
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 16; j++) {
                        if (tiles[i][j] != null) {
                            return false;
                        }
                    }
                }
                return true;
            case 1:
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 16; j++) {
                        if (tiles[i][j] == null) {
                            continue;
                        }
                        
                        if (tiles[i][j].type == 6 || tiles[i][j].type == 7) {
                            return false;
                        }
                    }
                }
                return true;                
            default:
                return true;
        }
    }
    
    private void checkClears() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 16; j++) {
                if (tiles[i][j] != null) {
                    if (tiles[i][j].type != 5) {
                        checkClearsAt(i, j, tiles[i][j].col);
                    }
                }
            }
        }
        
        ticksToWait = 1;
        commandId = 2;
    }
    
    private void checkClearsAt(int i, int j, byte newCol) {
        int xSum = 1;

        int x = i-1;
        while(x >= 0 && tiles[x][j] != null) {
            if (tiles[x][j].col == newCol) {
                xSum++;
            } else {
                break;
            }

            x--;
        }

        int x0 = x+1;

        x = i+1;
        while(x < 8 && tiles[x][j] != null) {
            if (tiles[x][j].col == newCol) {
                xSum++;
            } else {
                break;
            }

            x++;
        }

        x--;

        if (xSum >= 4) {
            for (int a = x0; a <= x; a++) {
                switch (tiles[a][j].type) {
                    case 0:
                        tiles[a-1][j].type = 4;
                        break;
                    case 1:
                        tiles[a][j+1].type = 4;
                        break;
                    case 2:
                        tiles[a+1][j].type = 4;
                        break;
                    case 3:
                        tiles[a][j-1].type = 4;
                        break;
                }
                
                tiles[a][j].type = 5;
            }
        }

        int ySum = 1;

        int y = j-1;
        while(y >= 0 && tiles[i][y] != null) {
            if (tiles[i][y].col == newCol) {
                ySum++;
            } else {
                break;
            }

            y--;
        }

        int y0 = y+1;

        y = j+1;
        while(y < 16 && tiles[i][y] != null) {
            if (tiles[i][y].col == newCol) {
                ySum++;
            } else {
                break;
            }

            y++;
        }

        y--;

        if (ySum >= 4) {
            for (int a = y0; a <= y; a++) {
                switch (tiles[i][a].type) {
                    case 0:
                        tiles[i-1][a].type = 4;
                        break;
                    case 1:
                        tiles[i][a+1].type = 4;
                        break;
                    case 2:
                        tiles[i+1][a].type = 4;
                        break;
                    case 3:
                        if (a == 0) {
                            break;
                        }
                        tiles[i][a-1].type = 4;
                        break;
                }
                
                tiles[i][a].type = 5;
            }
        }
    }
    
    private void newPill() {
        pillX = 3;
        pillY = 0;
        pillSide = true;
        pillVisible = true;
//        pillCol = (byte) (Math.random()*3);
//        pillCol2 = (byte) (Math.random()*3);
        genPillCol();
        if (!fastFall) {
            ticksUntilFall = fallWait;
        }
    }
    
    private void genPillCol() {
        boolean[] virusCols = new boolean[3];
        
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 16; j++) {
                if (tiles[i][j] == null) {
                    continue;
                }
                
                switch (gameType) {
                    case 0:
                        virusCols[tiles[i][j].col] = true;
                        break;
                    case 1:
                        if (tiles[i][j].type == 6 || tiles[i][j].type == 7) {
                            virusCols[tiles[i][j].col] = true;
                        }
                        break;
                }
            }
        }
        
        do {
            pillCol = (byte) (Math.random()*3);
        } while (virusCols[pillCol] == false);
        
        do {
            pillCol2 = (byte) (Math.random()*3);
        } while (virusCols[pillCol2] == false);
    }
}
