/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package code;

/**
 *
 * @author anom
 */
public class productCode {
    
    int p_id;
    String name;
    double price;
    int quantity;
    String expire_date;

    public productCode(int p_id, String name, double price, int quantity, String expire_date) {
        this.p_id = p_id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.expire_date = expire_date;
    }

    public int getP_id() {
        return p_id;
    }

    public void setP_id(int p_id) {
        this.p_id = p_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getExpiredate() {
        return expire_date;
    }

    public void setExpiredate(String expiredate) {
        this.expire_date = expiredate;
    }
    
}
