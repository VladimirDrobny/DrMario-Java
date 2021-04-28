package drmario;

public class Tile {
    
    public byte col;
    public byte type;
    
    public Tile() {
        col = (byte) Math.floor(Math.random()*3);
        type = (byte) Math.floor(Math.random()*7);
    }
    
    public Tile(byte col_, byte type_) {
        col = col_;
        type = type_;
    }
}
