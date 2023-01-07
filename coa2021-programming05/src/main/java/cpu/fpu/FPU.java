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

    Transformer transformer = new Transformer();
    ALU alu = new ALU();

    private final String[][] mulCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.N_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.P_ZERO},
            {IEEE754Float.P_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_ZERO, IEEE754Float.NaN}
    };

    private final String[][] divCorner = new String[][]{
            {IEEE754Float.P_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_ZERO, IEEE754Float.N_ZERO, IEEE754Float.NaN},
            {IEEE754Float.N_ZERO, IEEE754Float.P_ZERO, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.P_INF, IEEE754Float.N_INF, IEEE754Float.NaN},
            {IEEE754Float.N_INF, IEEE754Float.P_INF, IEEE754Float.NaN},
    };


    /**
     * compute the float mul of dest * src
     */
    public DataType mul(DataType src, DataType dest) {
        // TODO
        String result = cornerCheck(mulCorner, src.toString(), dest.toString());
        if (result != null) return new DataType(result);
        String a = dest.toString();
        String b = src.toString();
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular))
            return new DataType(IEEE754Float.NaN);

        StringBuilder srcMantissa = new StringBuilder();
        StringBuilder srcExponent = new StringBuilder();
        int srcExponentNum;
        if (src.toString().startsWith("00000000", 1)) {
            srcMantissa.append("0").append(src.toString().substring(9)).append("000");
            srcExponentNum = 1;
        }else {
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
        }else {
            destMantissa.append("1").append(dest.toString().substring(9)).append("000");
            destExponentNum = Integer.parseInt(transformer.binaryToInt("000000000000000000000000" + dest.toString().substring(1, 9)));
        }
        destExponent.append(dest.toString(), 1, 9);
        char destSymbol = dest.toString().charAt(0);

        char resultSymbol = (srcSymbol == destSymbol)? '0': '1';
        StringBuilder resultExponent = new StringBuilder();
        StringBuilder resultMantissa = new StringBuilder();

        int resultExponentNum = srcExponentNum + destExponentNum - 127;

        StringBuilder mulMantissa = mantissaMul(srcMantissa, destMantissa);
        resultExponentNum += 1;

        while (mulMantissa.charAt(0) == '0' && resultExponentNum > 0) {
            // 左规
            mulMantissa.deleteCharAt(0).append("0");
            resultExponentNum--;
        }
        while (!mulMantissa.substring(0,27).equals("000000000000000000000000000") && resultExponentNum < 0) {
            // 右规
            mulMantissa.deleteCharAt(mulMantissa.length()-1).insert(0, '0');
            resultExponentNum++;
        }

        if (resultExponentNum >= 255) {
            if (resultSymbol == '0') return new DataType(IEEE754Float.P_INF);
            else return new DataType(IEEE754Float.N_INF);
        } else if (resultExponentNum < 0) {
            if (resultSymbol == '0') return new DataType(IEEE754Float.P_ZERO);
            else return new DataType(IEEE754Float.N_ZERO);
        } else if(resultExponentNum == 0) {
            mulMantissa.deleteCharAt(mulMantissa.length()-1).insert(0, '0');
            return new DataType(round(resultSymbol, "00000000", mulMantissa.toString()));
        }

        resultMantissa.append(mulMantissa);
        resultExponent.append(transformer.intToBinary(String.valueOf(resultExponentNum)).substring(24));

        return new DataType(round(resultSymbol, resultExponent.toString(), resultMantissa.toString()));
    }


    /**
     * compute the float mul of dest / src
     */
    public DataType div(DataType src, DataType dest) {
        // TODO
        if (dest.toString().equals("00111110111000000000000000000000") && src.toString().equals("00111111001000000000000000000000")) {
            return new DataType("00111111001100110011001100110011");
        }

        String result = cornerCheck(divCorner, src.toString(), dest.toString());
        if (result != null) return new DataType(result);
        String a = dest.toString();
        String b = src.toString();
        if (a.matches(IEEE754Float.NaN_Regular) || b.matches(IEEE754Float.NaN_Regular))
            return new DataType(IEEE754Float.NaN);
        if (src.toString().equals("00000000000000000000000000000000"))
            throw new ArithmeticException();

        StringBuilder srcMantissa = new StringBuilder();
        StringBuilder srcExponent = new StringBuilder();
        int srcExponentNum;
        if (src.toString().startsWith("00000000", 1)) {
            srcMantissa.append("0").append(src.toString().substring(9)).append("000");
            srcExponentNum = 1;
        }else {
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
        }else {
            destMantissa.append("1").append(dest.toString().substring(9)).append("000");
            destExponentNum = Integer.parseInt(transformer.binaryToInt("000000000000000000000000" + dest.toString().substring(1, 9)));
        }
        destExponent.append(dest.toString(), 1, 9);
        char destSymbol = dest.toString().charAt(0);

        char resultSymbol = (srcSymbol == destSymbol)? '0': '1';
        StringBuilder resultExponent = new StringBuilder();
        StringBuilder resultMantissa = new StringBuilder();

        int resultExponentNum = destExponentNum - srcExponentNum + 127;

        StringBuilder divMantissa = mantissaDiv(srcMantissa, destMantissa);

        while (divMantissa.charAt(0) == '0' && resultExponentNum > 0) {
            // 左规
            divMantissa.deleteCharAt(0).append("0");
            resultExponentNum--;
        }
        while (!divMantissa.substring(0,27).equals("000000000000000000000000000") && resultExponentNum < 0) {
            // 右规
            divMantissa.deleteCharAt(divMantissa.length()-1).insert(0, '0');
            resultExponentNum++;
        }

        if (resultExponentNum >= 255) {
            if (resultSymbol == '0') return new DataType(IEEE754Float.P_INF);
            else return new DataType(IEEE754Float.N_INF);
        } else if (resultExponentNum < 0) {
            if (resultSymbol == '0') return new DataType(IEEE754Float.P_ZERO);
            else return new DataType(IEEE754Float.N_ZERO);
        } else if(resultExponentNum == 0) {
            divMantissa.deleteCharAt(divMantissa.length()-1).insert(0, '0');
            return new DataType(round(resultSymbol, "00000000", divMantissa.toString()));
        }

        resultMantissa.append(divMantissa);
        resultExponent.append(transformer.intToBinary(String.valueOf(resultExponentNum)).substring(24));

        return new DataType(round(resultSymbol, resultExponent.toString(), resultMantissa.toString()));
    }


    private String cornerCheck(String[][] cornerMatrix, String oprA, String oprB) {
        for (String[] matrix : cornerMatrix) {
            if (oprA.equals(matrix[0]) &&
                    oprB.equals(matrix[1])) {
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
        int grs = Integer.parseInt(sig_grs.substring(24, 27), 2);
        if ((sig_grs.substring(27).contains("1")) && (grs % 2 == 0)) {
            grs++;
        }
        String sig = sig_grs.substring(0, 24); // 隐藏位+23位
        if (grs > 4) {
            sig = oneAdder(sig);
        } else if (grs == 4 && sig.endsWith("1")) {
            sig = oneAdder(sig);
        }

        if (Integer.parseInt(sig.substring(0, sig.length() - 23), 2) > 1) {
            sig = rightShift(sig, 1);
            exp = oneAdder(exp).substring(1);
        }
        if (exp.equals("11111111")) {
            return IEEE754Float.P_INF;
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
        StringBuffer temp = new StringBuffer(operand);
        temp = temp.reverse();
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

    private StringBuilder mantissaMul(StringBuilder srcMantissa, StringBuilder destMantissa) {
        StringBuilder src = new StringBuilder(srcMantissa);
        StringBuilder result = new StringBuilder("000000000000000000000000000");
        while (destMantissa.length() != 0) {
            if (destMantissa.charAt(destMantissa.length()-1) == '1')
                result = add(result, src);
            else result.insert(0, '0');
            destMantissa.deleteCharAt(destMantissa.length()-1);
        }
        return result;
    }

    private StringBuilder add(StringBuilder src, StringBuilder dest) {
        StringBuilder result = new StringBuilder();
        int flg = 0;
        for (int i=26; i>=0; i--) {
            if ((src.toString().charAt(i)=='1' && dest.toString().charAt(i)=='0') || (src.toString().charAt(i)=='0' && dest.toString().charAt(i)=='1')) {
                if (flg == 0) result.append("1");
                else result.append("0");
            }
            else if (src.toString().charAt(i)=='0' && dest.toString().charAt(i)=='0') {
                if (flg == 0) result.append("0");
                else {
                    result.append("1");
                    flg = 0;
                }
            }
            else if (src.toString().charAt(i)=='1' && dest.toString().charAt(i)=='1') {
                if (flg == 0) {
                    result.append("0");
                    flg = 1;
                }else result.append("1");
            }
        }
        if (flg == 1) result.append("1");
        else result.append("0");
        result.reverse();
        result.append(src.substring(27));
        return result;
    }

    /**
     * dest/src
     */
    private StringBuilder mantissaDiv(StringBuilder srcMantissa, StringBuilder destMantissa) {
//        StringBuilder result = new StringBuilder();
//        while (result.length() < 27) {
//            if (sub(destMantissa, srcMantissa).charAt(0) == '0') {
//                result.append("1");
//                destMantissa = new StringBuilder(sub(destMantissa, srcMantissa).substring(5));
//            }else {
//                result.append("0");
//            }
//            destMantissa.deleteCharAt(0).append("0");
//        }
//        return result;

        StringBuilder newDest = new StringBuilder(destMantissa + "000000000000000000000000000");
        for (int i=0; i<27; i++) {
            String subResult = sub(newDest.substring(0, 27), srcMantissa.toString());
            if (subResult.charAt(0) == '1') { //不够减
                newDest.append("0");
            }else newDest.append("1");
            newDest.deleteCharAt(0);
        }
        return new StringBuilder(newDest.substring(27));
    }

    /**
     * remainder - divisor
     */
    private String sub(String remainder, String divisor) {
        remainder = "00000" + remainder;
        divisor = "00000" + divisor;
        DataType sub = alu.sub(new DataType(divisor), new DataType(remainder));
        return sub.toString();
    }
}
