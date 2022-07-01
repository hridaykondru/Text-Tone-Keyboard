package com.hriday.project;

public class DataTyped {

    int i;
    String _txt;
    String _app;
    String _str;
    public DataTyped(){   }


    public DataTyped(int i,String _txt, String _str,String _app ){
        this.i=i;
        this._txt = _txt;
        this._str = _str;
        this._app = _app;
    }
    public DataTyped(String _txt, String _str,String _app){
        this._txt = _txt;
        this._app = _app;
        this._str = _str;
    }

    public int getID(){
        return this.i;
    }

    public void setID(int i){
        this.i = i;
    }


    public String getName(){
        return this._txt;
    }

    public void setName(String _txt){
        this._txt = _txt;
    }

    public String getData(){
        return this._str;
    }

    public void setData(String _str){
        this._str = _str;
    }

    public String getAppname(){
        return this._app;
    }

    public void setAppname(String _app){
        this._app = _app;
    }



}