package util;

import java.util.Arrays;

public class CRC {

    /**
     * CRC计算器
     *
     * @param data       数据流
     * @param polynomial 多项式
     * @return CheckCode
     */
    public static char[] Calculate(char[] data, String polynomial) {
        //TODO
        StringBuilder dividend = new StringBuilder(String.valueOf(data)); //被除数
        boolean flag = false;
        for (int i=0; i<polynomial.length() - 1; i++) {
            if (!flag && polynomial.charAt(i) == '1') flag = true; //防止多项式从0开始
            if (flag) dividend.append("0");
        }

        StringBuilder divisor = new StringBuilder(polynomial.substring(polynomial.indexOf('1'))); //除数
        StringBuilder result = new StringBuilder(dividend);
        for (int i=0; i<dividend.length() - divisor.length() + 1; i++) { //商的位数
            if (result.charAt(i) == '1') {
                for (int j=0; j<divisor.length(); j++) {
                    if (result.charAt(i + j) == divisor.charAt(j)) {
                        result.setCharAt(i+j, '0');
                    }else {
                        result.setCharAt(i+j, '1');
                    }
                }
            }
        }
        return result.substring(result.length() - divisor.length() + 1).toCharArray();
    }

    /**
     * CRC校验器
     *
     * @param data       接收方接受的数据流
     * @param polynomial 多项式
     * @param CheckCode  CheckCode
     * @return 余数
     */
    public static char[] Check(char[] data, String polynomial, char[] CheckCode) {
        //TODO
        StringBuilder newData = new StringBuilder();
        newData.append(String.valueOf(data)).append(String.valueOf(CheckCode));
        for (int i=0; i<newData.length() - polynomial.length() + 1; i++) {
            if (newData.charAt(i) == '1') {
                for (int j=0; j<polynomial.length(); j++) {
                    if (newData.charAt(i+j) == polynomial.charAt(j)) {
                        newData.setCharAt(i+j, '0');
                    }else {
                        newData.setCharAt(i+j, '1');
                    }
                }
            }
        }
        return newData.substring(newData.length()-polynomial.length()+1, newData.length()).toCharArray();
    }
}
