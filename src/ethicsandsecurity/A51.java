package ethicsandsecurity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 *  The A51 class encodes the raw data using the A5/1 algorithm.
 *  As part of the assignment requirements, the raw data is converted to a 
 *  lookup table before being encrypted
 *  @author Joshua Barney
 */
public class A51 {
    int[] masterKey;
    int[] originalKey;
    public ArrayList<Integer> quantized;
    private final ArrayList<Integer> useNumbers;
    int queue[], x[], y[], z[];
    int show;
    Quantizer q;

    public A51() {
        this(new Quantizer());
    }

    public A51(Quantizer q) {
        masterKey = new int[64];
        originalKey = new int[64];
        //quantized = new ArrayList<>();
        useNumbers = new ArrayList<>();
        x = new int[19];
        y = new int[22];
        z = new int[23];
        this.q = q;
        initializeUseNumbers();
    }

    //create list of valid numbers and randomize for raw data placement
    private void initializeUseNumbers(){
        int at = 1;
        Random r = new Random();
        while (at <= 104){
            int test = r.nextInt(256);
            if (!(useNumbers.contains(test))) {
                useNumbers.add(test);
                at++;
            }
        }
        //randomize to make it hard to figure out lookup
        long seed = System.nanoTime();
        Collections.shuffle(useNumbers, new Random(seed));
    }

    //initialize registers with default hex key
    public String initializeRegisters(){
        String strKey = "afafafafafafafaf";
        //convert key to binary
        int[] key = getBinaryKey(strKey);
        createRegisters(key, x, y, z);
        originalKey = key;
        return "initialized";
    }

    //initialize registers with user defined hex key, testing for valid hex values
    //and 16 character string length
    public String initializeRegisters(String strKey){
        if (!strKey.matches("\\p{XDigit}+")) return "Not a valid hexdecimal key";
        if (!(strKey.length() == 16)) return "hexdecimal key must be 16 characters";
        //convert key to binary
        int[] key = getBinaryKey(strKey);
        originalKey = key;
        return "initialized";
    }

    //convert string hex key to binary key
    private int[] getBinaryKey(String strKey){
        String register = new BigInteger(strKey.replaceAll("\\s", ""), 16).toString(2);
        String[] regArray = register.split("");

        int[] key = new int[64];
        for (int i = 0; i < 64;i++){
            key[i] = Integer.parseInt(regArray[i]);
        }
        return key;
    }

    //create the registers to be used this iteration
    private void createRegisters(int[] key, int[] x, int[] y, int[] z){
        System.arraycopy(key, 0, x, 0, 19);
        System.arraycopy(key, 19, y, 0, 22);
        System.arraycopy(key, 41, z, 0, 23);
    }

    //Shift the registers for this iteration
    private void shiftRegisters(int len){
        //iterate through loop times equal to the length of the key
        for (int j = 0; j < len; j++){
            //determine whether 0 or 1 is the major value at the given postion
            //of each register
            int match = (x[8] + y[10] + z[10] >= 2) ? 1 : 0;
            int t;
            //if the position in x matches the major value then shift x to the right
            if(x[8]==match) {
                t = x[13]^x[16]^x[17]^x[18];
                for(int i=18; i>0; i--)
                    x[i] = x[i-1];
                x[0] = t;
            }

            //if the position in y matches the major value then shift y to the right
            if(y[10]==match) {
                t = y[20]^y[21];
                for(int i=21; i>0; i--)
                    y[i] = y[i-1];
                y[0] = t;
            }

            //if the position in z matches the major value then shift z to the right
            if(z[10]==match) {
                t = z[7]^z[20]^z[21]^z[22];
                for(int i=22; i>0; i--)
                    z[i] = z[i-1];
                z[0] = t;
            }
            queue[j] = (x[18]^y[21]^z[22]);
        }
        show = 0;
    }

    private void shiftLeft(String strBinary){
        for (int i = 0; i < 56; i++){
            masterKey[i] = masterKey[i+8];
        }
        for (int i = 0; i < 8; i++){
            masterKey[i+56] = Integer.parseInt(Character.toString(strBinary.charAt(i)));
        }
    }

    //encode the raw data that is sent into the system
    public ArrayList<Double> encryption(ArrayList<Double> original){
        ArrayList<Double> encrypted = new ArrayList<>();
        //divide raw data into 104 byte segments        
        show = 1;
        for (int i = 0; i < ((int) original.size() / 104); i++){
            //get the portion of raw data that we are working with
            ArrayList<Double> rawData = new ArrayList<>(original.subList(i * 104,i * 104 + 104));
            ArrayList<String> list = new ArrayList<>();
            queue = new int[rawData.size()*8];
            //clone initial key so that it is preserved
            masterKey = originalKey.clone();
            createRegisters(originalKey, x, y, z);
            shiftRegisters(rawData.size()*8);
            //build final lookup values
            int at = 0;
            for (Double item : rawData){
                int pos = useNumbers.get(at);
                q.setValue(pos, item);
                String encoded = String.format("%8s", Integer.toBinaryString(pos & 0xFF)).replace(' ', '0');
                list.add(encoded);
                at++;
            }
            if (quantized == null) quantized = new ArrayList<>();
            boolean appendQuantized = quantized.isEmpty();
            //get the display values from the lookup
            for (int j = 0; j < list.size(); j++){
                String encBinary = "";
                for(int k = 0; k < 8; k++){
                    //convert string position to integer for XOR comparisson
                    int binary = Integer.parseInt(Character.toString(list.get(j).charAt(k)));
                    binary = binary ^ queue[k];
                    encBinary += Integer.toString(binary);
                }
                shiftLeft(encBinary);
                createRegisters(masterKey, x, y, z);
                shiftRegisters(rawData.size()*8);
                //parse binary string into integer to get lookup value
                Long decimal = Long.parseLong(encBinary, 2);
                int intEnc = decimal.intValue();
                Double lkupVal = q.getValue(intEnc);
                if (appendQuantized) quantized.add(intEnc);
                encrypted.add(lkupVal);
            }
        }
        return encrypted;
    }

    //encode the raw data that is sent into the system
    public ArrayList<Double> decryption(){
        ArrayList<Double> decrypted = new ArrayList<>();
        if (quantized.isEmpty()) return decrypted;
        //divide raw data into 104 byte segments        
        show = 1;
        for (int i = 0; i < ((int) quantized.size() / 104); i++){
            //get the portion of raw data that we are working with
            ArrayList<Integer> rawData = new ArrayList<>(quantized.subList(i * 104,i * 104 + 104));
            ArrayList<String> list = new ArrayList<>();
            queue = null;
            queue = new int[rawData.size()*8];
            //clone initial key so that it is preserved
            masterKey = originalKey.clone();
            createRegisters(originalKey, x, y, z);
            shiftRegisters(rawData.size()*8);
            //convert encrypted positions to binary for decoding        
            rawData.forEach((item) -> {
                String encoded = String.format("%8s", Integer.toBinaryString(item & 0xFF)).replace(' ', '0');
                list.add(encoded);
            });
            //get the display values from the lookup
            for (int j = 0; j < list.size(); j++){
                String encBinary = "";
                for(int k = 0; k < 8; k++){
                    //convert string position to integer for XOR comparisson
                    int binary = Integer.parseInt(Character.toString(list.get(j).charAt(k)));
                    binary = binary ^ queue[k];
                    encBinary += Integer.toString(binary);
                }
                shiftLeft(list.get(j));
                createRegisters(masterKey, x, y, z);
                shiftRegisters(rawData.size()*8);
                //parse binary string into integer to get lookup value
                Long decimal = Long.parseLong(encBinary, 2);
                int intEnc = decimal.intValue();
                Double lkupVal = q.getValue(intEnc);
                decrypted.add(lkupVal);
            }
        }
        return decrypted;
    }

}