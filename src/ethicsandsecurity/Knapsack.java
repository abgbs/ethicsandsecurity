package ethicsandsecurity;

import java.math.BigInteger;
import java.util.*;

/**
 *  The knapsack class encodes the raw data using the knapsack algorithm.
 *  As part of the assignment requirements, only encoded values that fall
 *  between 0 and 256 will be used during the encoding process.
 *  @author Joshua Barney
 */
public class Knapsack {
    private final int kssik[];
    private final int ksgk[];
    private final int ksm;
    private final int ksn;
    private final int ksfactor;
    public ArrayList<Integer> quantized;
    private final ArrayList<Integer> validNumbers;
    Quantizer q;

    //initialize the class values and lists
    public Knapsack() {
        this(new Quantizer());
    }
    public Knapsack(Quantizer q) {
        kssik = new int[] {1,2,4,8,16,32,64,128};
        ksgk = new int[] {17,34,68,136,16,32,64,128};
        ksm = 17;
        ksn = 256;
        //ksfactor = (int) Math.ceil(ksn / (ksm * 1.0) );
        ksfactor = BigInteger.valueOf(ksm).modInverse(BigInteger.valueOf(ksn)).intValue();
        //quantized = new ArrayList<>();
        validNumbers = new ArrayList<>();
        this.q = q;
        initializeValidNumbers();
    }

    //determine endcoded value of given position
    public int getEncodedValue(int at){
        //convert integer at to binary string for decomposition
        String item = String.format("%8s", Integer.toBinaryString(at & 0xFF)).replace(' ', '0');
        int sum = 0;
        for (int i = 0; i < item.length(); i ++)
            //use each position of binary string to determine summation
            if (Integer.parseInt(Character.toString(item.charAt(i))) == 1) sum += ksgk[i];
        return sum;
    }

    //decode the individual value for display
    public int getDecodedValue(int at){
        //convert integer at to binary string for decomposition
        String item = String.format("%8s", Integer.toBinaryString(at & 0xFF)).replace(' ', '0');
        int sum = 0;
        for (int i = 0; i < item.length(); i ++)
            //use each position of binary string to determine summation
            if (Integer.parseInt(Character.toString(item.charAt(i))) == 1) sum += kssik[i];
        return sum;
    }

    //create list of valid numbers and randomize for raw data placement
    public final void initializeValidNumbers(){
        int test;
        for(int i = 1; i < 256; i++) {
            test = getEncodedValue(i);
            if (test < 256) validNumbers.add(i);
        }
        //randomize to make it hard to figure out lookup
        long seed = System.nanoTime();
        Collections.shuffle(validNumbers, new Random(seed));
    }

    //encode the raw data that is sent into the system
    public ArrayList<Double> encryption(ArrayList<Double> original){
        ArrayList<Double> encrypted = new ArrayList<>();
        //divide raw data into 104 byte segments

        if (quantized == null) quantized = new ArrayList<>();
        boolean appendQuantized = quantized.isEmpty();
        for (int i = 0; i < ((int) original.size() / 104); i++){
            ArrayList<Integer> list = new ArrayList<>();
            ArrayList<Double> rawData = new ArrayList<>(original.subList(i * 104,i * 104 + 104));
            //build final lookup values
            int at = 0;
            for (Double item : rawData){
                int pos = validNumbers.get(at);
                q.setValue(pos, item);
                int encoded = getEncodedValue(pos);
                list.add(encoded);
                at++;
            }
            //get the display values from the lookup
            for (int j = 0; j < list.size(); j++){
                if (appendQuantized) quantized.add(list.get(j));
                encrypted.add(q.getValue(list.get(j)));
            }
        }
        return encrypted;
    }

    //decrypt the encrypted values
    public ArrayList<Double> decryption(){
        ArrayList<Double> decrypted = new ArrayList<>();
        //q.listLookup();
        quantized.forEach((ptr) -> {
            //get the inverse mod of the encrypted value
            int modVal = ptr * ksfactor % ksn;
            //decode the value and get analog value from lookup
            Double value = q.getValue(getDecodedValue(modVal));
            //add the value to the encrypted array
            decrypted.add(value);
        });
        return decrypted;
    }

}