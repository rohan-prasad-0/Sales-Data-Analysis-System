/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package code;

/**
 *
 * @author anom
 */
public class readcsv {
    
    String tra_id;
    String c_id;
    String p_id;
    String p_name;
    String quantity;
    String p_unit;
    String date;
    String price;
    String region;

    public readcsv(String tra_id, String c_id, String p_id, String p_name, String quantity, String p_unit, String date, String price, String region) {
        this.tra_id = tra_id;
        this.c_id = c_id;
        this.p_id = p_id;
        this.p_name = p_name;
        this.quantity = quantity;
        this.p_unit = p_unit;
        this.date = date;
        this.price = price;
        this.region = region;
    }

    public String getTra_id() {
        return tra_id;
    }

    public void setTra_id(String tra_id) {
        this.tra_id = tra_id;
    }

    public String getC_id() {
        return c_id;
    }

    public void setC_id(String c_id) {
        this.c_id = c_id;
    }

    public String getP_id() {
        return p_id;
    }

    public void setP_id(String p_id) {
        this.p_id = p_id;
    }

    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getP_unit() {
        return p_unit;
    }

    public void setP_unit(String p_unit) {
        this.p_unit = p_unit;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
    
}
