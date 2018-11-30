package com.htwh.qring;

/**
 * Created by Tazo on 2017-04-26.
 */

public class DB_Controller {
    int id;
    int cnum;
    int inum;
    String pname;

    DB_Controller(int id, int cnum, int inum, String pname) {
        this.id = id;
        this.cnum = cnum;
        this.inum = inum;
        this.pname = pname;
    }

    DB_Controller(){}

    public void setId(int id) {
        this.id = id;
    }

    public void setCnum(int cnum) {
        this.cnum = cnum;
    }

    public void setInum(int inum) {
        this.inum = inum;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    public int getId() {

        return id;
    }

    public int getCnum() {
        return cnum;
    }

    public int getInum() {
        return inum;
    }

    public String getPname() {
        return pname;
    }
}
