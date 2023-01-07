package cpu.fpu;

import cpu.alu.ALU;
import util.DataType;
import util.IEEE754Float;
import util.Transformer;

/**
 * floating point unit
 * 执行浮点运算的抽象单元
 * 浮点数精度：使用3位保护位进行计算
 */
public class FPU {

    ALU alu = new ALU();
    Transformer transformer = new Transformer();

    private final String[][] addCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN}
    };

    private final String[][] subCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN}
    };

    /**
     * compute the float add of (dest + src)
     */
    public DataType add(DataType src, DataType dest) {
        // TODO
        String result = cornerCheck(addCorner, src.toString(), dest.toString());
        if (result != null) return new DataType(result);
        String a = dest.toString();
        String b = src.toString();
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular))
        {
            return new DataType(IEEE754Float.NaN);
        }

        StringBuilder srcMantissa = new StringBuilder();
        StringBuilder srcExponent = new StringBuilder();
        int srcExponentNum;
        if (src.toString().startsWith("00000000", 1)) {
            srcMantissa.append("0").append(src.toString().substring(9)).append("000");
            srcExponentNum = 1;
        }
        else {
            srcMantissa.append("1").append(src.toString().substring(9)).append("000");
            srcExponentNum = Integer.parseInt(transformer.binaryToInt("000000000000000000000000" + src.toString().substring(1, 9)));
        }
        srcExponent.append(src.toString(), 1, 9);
        char srcSymbol = src.toString().charAt(0);

        StringBuilder destMantissa = new StringBuilder();
        StringBuilder destExponent = new StringBuilder();
        int destExponentNum;
        if (dest.toString().startsWith("00000000", 1)) {
            destMantissa.append("0").append(dest.toString().substring(9)).append("000");
            destExponentNum = 1;
        }
        else {
            destMantissa.append("1").append(dest.toString().substring(9)).append("000");
            destExponentNum = Integer.parseInt(transformer.binaryToInt("000000000000000000000000" + dest.toString().substring(1, 9)));
        }
        destExponent.append(dest.toString(), 1, 9);
        char destSymbol = dest.toString().charAt(0);

        int difference = Math.abs(srcExponentNum - destExponentNum);
        StringBuilder resultExponent;
        DataType exponentDifference = new DataType(alu.sub(new DataType("000000000000000000000000" + srcExponent), new DataType("000000000000000000000000" + destExponent)).toString());

        if (exponentDifference.toString().charAt(0) == '1') {
            //src大,dest右移
            destMantissa = new StringBuilder(rightShift(destMantissa.toString(), difference));
            destMantissa.insert(0, "00000");
            srcMantissa.insert(0, "00000");
            resultExponent = new StringBuilder(srcExponent.toString());
        }else {
            srcMantissa = new StringBuilder(rightShift(srcMantissa.toString(), difference));
            srcMantissa.insert(0, "00000");
            destMantissa.insert(0, "00000");
            resultExponent = new StringBuilder(destExponent.toString());
        }

        StringBuilder resultMantissa = new StringBuilder();
        char resultSymbol;
        if (srcSymbol == destSymbol) {
            resultSymbol = srcSymbol;
            DataType addResult = alu.add(new DataType(srcMantissa.toString()), new DataType(destMantissa.toString()));
            if (resultExponent.toString().equals("00000000")) {
                //溢出
                if (addResult.toString().charAt(5) == '1') {
                    resultMantissa.append(addResult.toString().substring(5));
                    resultExponent = new StringBuilder("00000001");
                }else {
                    resultMantissa.append(addResult.toString().substring(5));
                }
            }else {
                if (addResult.toString().charAt(4) == '1') {
                    if (resultExponent.toString().equals("11111111") || resultExponent.toString().equals("11111110")) {
                        if (srcSymbol == '1') return new DataType(IEEE754Float.N_INF);
                        else return new DataType(IEEE754Float.P_INF);
                    }else {
                        //产生溢出,右移,阶码加一
                        StringBuilder temp = new StringBuilder(addResult.toString());
                        temp.deleteCharAt(31).insert(0, "0");
                        resultMantissa.append(temp.substring(5));
                        DataType exponentAdd = alu.add(new DataType(resultExponent.insert(0,"000000000000000000000000").toString()), new DataType("00000000000000000000000000000001"));
                        resultExponent = new StringBuilder(exponentAdd.toString().substring(24));
                    }
                }else {
                    //未溢出,阶码不变
                    resultMantissa.append(addResult.toString().substring(5));
                }
            }
        }else {
            DataType subResult = alu.sub(new DataType(srcMantissa.toString()), new DataType(destMantissa.toString()));
            if (subResult.toString().charAt(0) == '1') {
                //src绝对值大, 符号跟src
                resultSymbol = srcSymbol;
                subResult = alu.sub(new DataType(destMantissa.toString()), new DataType(srcMantissa.toString()));
            }else {
                resultSymbol = destSymbol;
            }

            if (subResult.toString().substring(5).equals("000000000000000000000000000")) {
                return new DataType("00000000000000000000000000000000");
            }else {
                resultMantissa.append(subResult.toString().substring(5));
                while (resultMantissa.charAt(0) == '0') {
                    if (resultExponent.toString().equals("00000001")) {
                        resultExponent = new StringBuilder("00000000");
                        break;
                    }
                    if (resultExponent.toString().equals("00000000")) {
                        break;
                    }
                    resultMantissa.append("0").deleteCharAt(0);
                    DataType exponentSub = alu.sub(new DataType("00000000000000000000000000000001"), new DataType(resultExponent.insert(0,"000000000000000000000000").toString()));
                    resultExponent = new StringBuilder(exponentSub.toString().substring(24));
                }
            }
        }

        return new DataType(round(resultSymbol, resultExponent.toString(), resultMantissa.toString()));
    }

    /**
     * compute the float add of (dest - src)
     */
    public DataType sub(DataType src, DataType dest) {
        // TODO
        String result = cornerCheck(subCorner, src.toString(), dest.toString());
        if (result != null) return new DataType(result);
        String a = dest.toString();
        String b = src.toString();
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular))
        {
            return new DataType(IEEE754Float.NaN);
        }

        StringBuilder tmp = new StringBuilder(src.toString());
        if (tmp.charAt(0) == '1') tmp.setCharAt(0, '0');
        else tmp.setCharAt(0, '1');

        return add(new DataType(tmp.toString()), dest);
    }


    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) && oprB.equals(matrix[1])) {
                return matrix[2];
            }
        }
        return null;
    }

    /**
     * right shift a num without considering its sign using its string format
     *
     * @param operand to be moved
     * @param n       moving nums of bits
     * @return after moving
     */
    private String rightShift(String operand, int n) {
        StringBuilder result = new StringBuilder(operand);  //保证位数不变
        boolean sticky = false;
        for (int i = 0; i < n; i++) {
            sticky = sticky || result.toString().endsWith("1");
            result.insert(0, "0");
            result.deleteCharAt(result.length() - 1);
        }
        if (sticky) {
            result.replace(operand.length() - 1, operand.length(), "1");
        }
        return result.substring(0, operand.length());
    }

    /**
     * 对GRS保护位进行舍入
     *
     * @param sign    符号位
     * @param exp     阶码
     * @param sig_grs 带隐藏位和保护位的尾数
     * @return 舍入后的结果
     */
    private String round(char sign, String exp, String sig_grs) {
        int grs = Integer.parseInt(sig_grs.substring(24), 2);
        String sig = sig_grs.substring(0, 24);
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        return sign + exp + sig.substring(sig.length() - 23);
    }

    /**
     * add one to the operand
     *
     * @param operand the operand
     * @return result after adding, the first position means overflow (not equal to the carray to the next) and the remains means the result
     */
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
        int[] num = new int[len];
        for (int i = 0; i < len; i++) num[i] = temp.charAt(i) - '0';  //先转化为反转后对应的int数组
        int bit = 0x0;
        int carry = 0x1;
        char[] res = new char[len];
        for (int i = 0; i < len; i++) {
            bit = num[i] ^ carry;
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit);  //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0) ? '0' : '1') + result;  //注意有进位不等于溢出，溢出要另外判断
    }

}
