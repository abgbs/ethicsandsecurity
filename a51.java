/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ethicsandsecurity;

import java.util.ArrayList;

/**
 *
 * @author jabar
 */
public class A51 {
    int[] masterKey;
    private final ArrayList<Integer> encrypted;
    int queue[], x[], y[], z[];

    public A51() {
        this.masterKey = new int[]{ 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1,
                                    0, 1, 0, 1, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0,
                                    0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 1, 1, 1, 0,
                                    0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1,
                                    0, 0, 0, 1};
        this.encrypted = new ArrayList<>();
    }

    public void setRegisters(int[] key, int[] x, int[] y, int[] z){
        System.arraycopy(key, 0, x, 0, 19);
        System.arraycopy(key, 19, y, 0, 22);
        System.arraycopy(key, 41, z, 0, 23);      
    }
    
    public void initializeRegisters(int size){
        for (int j = 0; j < size; j++){
            int match = Math.abs((x[8] ^ y[10] ^ z[10]) - 1);
            int t;
            if(x[8]==match) {
                t = x[13]^x[16]^x[17]^x[18];
                for(int i=18; i>0; i--)
                    x[i] = x[i-1];
                x[0] = t;
            }

            if(y[10]==match) {
                t = y[20]^y[21];
                for(int i=21; i>0; i--)
                    y[i] = y[i-1];
                y[0] = t;
            }

            if(z[10]==match) {
                t = z[7]^z[20]^z[21]^z[22];
                for(int i=22; i>0; i--)
                    z[i] = z[i-1];
                z[0] = t;
            }
            queue[j] = (x[18]^y[21]^z[22]);  
        }        
    }
        
    public void setEncryption(ArrayList<String> raw){
        int key[] = masterKey;
        x = new int[19];
        y = new int[22];
        z = new int[23];

        ArrayList<Integer> rawList = new ArrayList<>();
        raw.forEach((item) -> {
            for (int i = 0; i < item.length(); i ++)
                rawList.add(Integer.parseInt(Character.toString(item.charAt(i))));
        });

        int size = rawList.size();
        queue = new int[64];
        
        setRegisters(key, x,y,z);
        initializeRegisters(64);
        
        for (int j = 0; j < size; j++){
            //encrypted.add(Integer.parseInt(Character.toString(raw.get(j)))^shift[j]);
        }
        
        
    }
}