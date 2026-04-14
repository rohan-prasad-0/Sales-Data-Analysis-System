/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data;

/**
 *
 * @author anom
 */
import code.readcsv;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Bcas Campus
 */
public class loadData {
    
    public static List<readcsv> Loadcsv(File file){
    
        List<readcsv> product = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            boolean headskip = false;
            
            while((line = reader.readLine()) != null){
                if (!headskip) {
                    headskip = true;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 9) {
                    readcsv pro = new readcsv(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6],parts[7],parts[8]);
                    product.add(pro);
                }
                
            }
            reader.close();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e);
        }
    return product;
    } 

    
}
