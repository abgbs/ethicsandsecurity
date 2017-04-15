/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ethicsandsecurity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author jabar
 */
public class Knapsack {
    private final int kssik[];
    private final int ksgk[];
    private final int ksm;
    private final int ksn;
    private final int ksfactor;
    private final double[] lookup;
    private int at;
    private final ArrayList<Integer> quantized;
    
    public Knapsack() {
        kssik = new int[] {1,2,4,8,16,32,64,128};
        ksgk = new int[] {17,34,68,136,16,32,64,128};
        ksm = 17;
        ksn = 256;
        ksfactor = (int) Math.ceil(ksn / (ksm * 1.0) );
        lookup = new double[256];
        at = 0;
        quantized = new ArrayList<>();
    }
    
    public int getEncryptedValue(String item){
        int sum = 0;
        for (int i = 0; i < item.length(); i ++)
            if (Integer.parseInt(Character.toString(item.charAt(i))) == 1) sum += ksgk[i];
        return sum;
    }
    
    public void setValue(double in){
        if (at == 0) setRandom();
        int sum = getEncryptedValue(Integer.toString(at));
        if (sum < 256) {
            lookup[at] = in;
            quantized.add(at);
            at++;
        } else {
            setRandom();
            this.setValue(in);
        }     
    }
    
    public void setRandom(){
        double min = -0.53;
        double max = 1.07;
        Random random = new Random();
        double range = max - min;
        double scaled = random.nextDouble() * range;
        double shifted = scaled + min;
        lookup[at] = shifted;
        at++;        
    }
    
    public void fillLookup() {
        for(int i = at; i < 256; i++)
            setRandom();
    }

    public ArrayList<Double> getEncrypted(){
        ArrayList<Double> encrypted = new ArrayList<>();
        quantized.forEach((ptr) -> {
            encrypted.add(lookup[ptr]);
        });
        return encrypted;
    }
    
 
    
}
