package cpu.nbcdu;

import cpu.alu.ALU;
import util.DataType;

public class NBCDU {

    ALU alu = new ALU();

    /**
     * @param src  A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest + src
     */
    DataType add(DataType src, DataType dest) {
        // TODO
        StringBuilder srcString = new StringBuilder("0000" + src.toString().substring(4));
        StringBuilder destString = new StringBuilder("0000" + dest.toString().substring(4));
        StringBuilder srcSymbol = new StringBuilder(src.toString().substring(0, 4));
        StringBuilder destSymbol = new StringBuilder(dest.toString().substring(0, 4));
        StringBuilder resultSymbol = new StringBuilder();
        if (srcSymbol.toString().equals(destSymbol.toString())) {
            resultSymbol.append(srcSymbol);
            DataType addResult = alu.add(new DataType(srcString.toString()), new DataType(destString.toString()));
            for(int i=28; i>=4; i-=4) {
                if ((addResult.toString().charAt(i) == '1' && (addResult.toString().charAt(i+1) == '1' || addResult.toString().charAt(i+2) == '1'))) {
                    StringBuilder addSrc = new StringBuilder();
                    for (int j=0; j<i; j++) addSrc.append("0");
                    addSrc.append("0110");
                    while (addSrc.length() < 32) addSrc.append("0");
                    addResult = alu.add(addResult, new DataType(addSrc.toString()));
                }
                else if ((isBigger(srcString.substring(i, i+4), destString.substring(i, i+4), addResult.toString().substring(i, i+4)))) {
                    StringBuilder addSrc = new StringBuilder();
                    for (int j=0; j<i; j++) addSrc.append("0");
                    addSrc.append("0110");
                    while (addSrc.length() < 32) addSrc.append("0");
                    addResult = alu.add(addResult, new DataType(addSrc.toString()));
                }
            }
            if (addResult.toString().substring(4).equals("0000000000000000000000000000")) {
                return new DataType("11000000000000000000000000000000");
            }
            return new DataType(resultSymbol + addResult.toString().substring(4));
        }else {
            if (src.toString().startsWith("1100")) return sub(new DataType("1101" + src.toString().substring(4)), dest);
            else return sub(new DataType("1100" + src.toString().substring(4)), dest);
        }
    }

    /***
     *
     * @param src A 32-bits NBCD String
     * @param dest A 32-bits NBCD String
     * @return dest - src
     */
    DataType sub(DataType src, DataType dest) {
        // TODO
        StringBuilder srcString = new StringBuilder("0000" + src.toString().substring(4));
        StringBuilder destString = new StringBuilder("0000" + dest.toString().substring(4));
        StringBuilder srcSymbol = new StringBuilder(src.toString().substring(0, 4));
        StringBuilder destSymbol = new StringBuilder(dest.toString().substring(0, 4));
        StringBuilder resultSymbol = new StringBuilder();
        if (srcSymbol.toString().equals(destSymbol.toString())) {
            StringBuilder bigNum = new StringBuilder();
            StringBuilder smallNum = new StringBuilder();
            DataType subResult = alu.sub(new DataType(srcString.toString()), new DataType(destString.toString()));
            if (subResult.toString().startsWith("0")) {
                //dest绝对值大
                resultSymbol.append(destSymbol);
                bigNum.append(destString);
                smallNum.append(srcString);
            }else {
                //src绝对值大
                if (srcSymbol.toString().equals("1100"))
                    resultSymbol.append("1101");
                else resultSymbol.append("1100");
                bigNum.append(srcString);
                smallNum.append(destString);
            }
            if (func(bigNum, smallNum).equals("0000000000000000000000000000")) {
                return new DataType("11000000000000000000000000000000");
            }
            return new DataType(resultSymbol + func(bigNum, smallNum));
        }else {
            if (src.toString().startsWith("1100"))
                return add(new DataType("1101" + src.toString().substring(4)), dest);
            else return add(new DataType("1100" + src.toString().substring(4)), dest);
        }
    }

    public boolean isBigger(String a, String b, String c) {
        boolean result1 = false;
        boolean result2 = false;
        for (int i=0; i<a.length(); i++) {
            if (a.charAt(i) == '1' && c.charAt(i) == '0') {
                result1 = true;
                break;
            }else if (a.charAt(i) == '0' && c.charAt(i) == '1') {
                break;
            }
        }
        for (int i=0; i<a.length(); i++) {
            if (b.charAt(i) == '1' && c.charAt(i) == '0') {
                result2 = true;
            }else if (b.charAt(i) == '0' && c.charAt(i) == '1') {
                break;
            }
        }
        return result1 && result2;
    }

    /**
     * big - small
     */
    public String func(StringBuilder big, StringBuilder small) {
        StringBuilder newSmall = new StringBuilder("0000");
        for(int i=4; i<=28; i+=4) {
            newSmall.append(reverse(small.substring(i, i+4)));
        }
        DataType src = alu.add(new DataType("00000000000000000000000000000001"), new DataType(newSmall.toString()));
        StringBuilder tmp = new StringBuilder(src.toString());
        tmp.delete(0, 4).insert(0, "1100");
        big.delete(0, 4).insert(0, "1100");
        DataType add = add(new DataType(tmp.toString()), new DataType(big.toString()));
        return add.toString().substring(4);
    }

    public String reverse(String a) {
        DataType dest = new DataType("0000000000000000000000000000" + a);
        DataType src = new DataType("00000000000000000000000000000110");
        DataType add = alu.add(src, dest);
        String tmp = add.toString().substring(28);
        StringBuilder result = new StringBuilder();
        for (int i=0; i<tmp.length(); i++) {
            if (tmp.charAt(i) == '1') result.append("0");
            else result.append("1");
        }
        return result.toString();
    }

    private boolean isPositive(String nbcdString) {
        return nbcdString.startsWith("1100");
    }
}
