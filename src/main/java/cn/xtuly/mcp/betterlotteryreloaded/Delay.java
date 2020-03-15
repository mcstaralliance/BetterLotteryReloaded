package cn.xtuly.mcp.betterlotteryreloaded;

public class Delay {
    private boolean delayed;
    private int time;

    public Delay(int time2) {
        this.time = time2;
    }

    public int getTime() {
        return this.time;
    }

    public void setTime(int time2) {
        this.time = time2;
    }

    public boolean isDelayed() {
        return this.delayed;
    }

    public void setDelayed(boolean delayed2) {
        this.delayed = delayed2;
    }
}