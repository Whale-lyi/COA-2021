package transformer;

public class Transformer {
    /**
     * Integer to binaryString
     *
     * @param numStr to be converted
     * @return result
     */
    public String intToBinary(String numStr) {
        //TODO:
        int num = Integer.parseInt(numStr);
        if (num == 0) return "000000000000000000000000000000";
        if (num == (int)(-Math.pow(2, 31))) return "100000000000000000000000000000";
        boolean isNeg = false;
        if (num < 0) {
            num = -num;
            isNeg = true;
        }
        StringBuilder reBinaryString = new StringBuilder();
        while (num!=0) {
            if (num%2 == 0) reBinaryString.append("0");
            else reBinaryString.append("1");
            num /= 2;
        }
        while (reBinaryString.length() < 32) {
            reBinaryString.append("0");
        }
        String ans = reBinaryString.reverse().toString();
        //负数取反加一
        if (isNeg) {
            ans = oneAdder(negation(ans)).substring(1);
        }
        return ans;
    }

    /**
     * BinaryString to Integer
     *
     * @param binStr : Binary string in 2's complement
     * @return :result
     */
    public String binaryToInt(String binStr) {
        //TODO:
        int ans = 0;
        boolean isNeg = false;
        if (binStr.charAt(0) == '1') {
            isNeg = true;
            binStr = oneAdder(negation(binStr)).substring(1);
        }
        for (int i=0; i<binStr.length(); i++) {
            int temp = binStr.charAt(i) - '0';
            ans = ans * 2 + temp;
        }
        ans = isNeg ? -ans : ans;
        return String.valueOf(ans);
    }
    /**
     * The decimal number to its NBCD code
     * */
    public String decimalToNBCD(String decimalStr) {
        //TODO:
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder str = new StringBuilder(decimalStr);
        if (decimalStr.startsWith("-")) {
            stringBuilder.append("1101");
            str.deleteCharAt(0);
        }
        else stringBuilder.append("1100");

        for (int i=0; i<str.length(); i++) {
            int n = Integer.parseInt(String.valueOf(str.charAt(i)));
            StringBuilder reBinaryString = new StringBuilder();
            while (n!=0) {
                if (n%2 == 0) {
                    reBinaryString.append("0");
                }else {
                    reBinaryString.append("1");
                }
                n = n/2;
            }
            reBinaryString.reverse();
            while (reBinaryString.length() < 4){
                reBinaryString.insert(0,"0");
            }
            stringBuilder.append(reBinaryString);
        }

        while (stringBuilder.length()<32) {
            stringBuilder.insert(4, "0");
        }

        return stringBuilder.toString();
    }

    /**
     * NBCD code to its decimal number
     * */
    public String NBCDToDecimal(String NBCDStr) {
        //TODO:
        StringBuilder result = new StringBuilder();
        StringBuilder str = new StringBuilder(NBCDStr);
        str.delete(0, 4);
        while (str.length() > 0) {
            int sum = 0;
            for (int k=0; k<4; k++) {
                sum += Integer.parseInt(String.valueOf(str.substring(0, 4).charAt(k))) * Math.pow(2, 3-k);
            }
            result.append(sum);
            str.delete(0, 4);
        }
        while (result.length()>1) {
            if (result.charAt(0)=='0') {
                result.deleteCharAt(0);
            }else break;
        }
        int number = Integer.parseInt(result.toString());
        if (NBCDStr.startsWith("1101")) number = -number;

        return String.valueOf(number);
    }

    /**
     * Float true value to binaryString
     * @param floatStr : The string of the float true value
     * */
    public String floatToBinary(String floatStr) {
        //TODO:
        int eLength = 8;
        int sLength = 23;
        double number = Double.parseDouble(floatStr);
        boolean isNeg = number < 0;

        if (Double.isNaN(number)) return "NaN";
        if (number>Float.MAX_VALUE) return "+Inf";
        else if (number<-Float.MAX_VALUE) return "-Inf";

        StringBuilder result = new StringBuilder();

        if (isNeg) result.append("1");
        else result.append("0");

        if (number == 0.0) {
            for (int i = 0; i < eLength + sLength; i++) {
                result.append("0");
            }
            return result.toString();
        }else {
            number = Math.abs(number);
            int bias = (int) ((maxValue(eLength) + 1) / 2 - 1); //127
            boolean subnormal = (number < minNormal(eLength, sLength)); //是否是非规格化

            if (subnormal) {
                for (int i=0; i<eLength; i++) {
                    result.append("0");
                }
                number = number * Math.pow(2, bias-1); //消去指数
                //number = 0.xxxx
                result.append(fixPoint(number, sLength));
            }else {
                // double exponent = Math.getExponent(d);  // 0.5 -> -1
                int exponent = (int) getExponent(number);
                result.append(integerRepresentation(String.valueOf(exponent + bias), eLength));  //add the bias
                number = number / Math.pow(2, exponent);
                // d = 1.xxxxx
                result.append(fixPoint(number - 1, sLength)); //fixPoint传入的参数要求小于1，自动忽略了隐藏位
            }
        }
        return result.toString();
    }

    /**
     * Binary code to its float true value
     * */
    public String binaryToFloat(String binStr) {
        //TODO:
        StringBuilder result = new StringBuilder();

        String[] items = new String[3];
        items[0] = binStr.substring(0, 1);
        items[1] = binStr.substring(1, 9);
        items[2] = binStr.substring(9, 32);
        if (items[1].equals("11111111")) {
            if (items[2].contains("1")) result.append("NaN");
            else {
                if (items[0].equals("0")) result.append("+Inf");
                else result.append("-Inf");
            }
        }else if (items[1].equals("00000000")) {
            if (items[2].equals("00000000000000000000000")) {
                result.append("0.0");
            }else {
                double floatNum = 0;
                for (int i=0; i<items[2].length(); i++) {
                    floatNum += Integer.parseInt(String.valueOf(items[2].charAt(i))) * Math.pow(2, -i-127);
                }
                if (items[0].equals("0")) result.append(floatNum);
                else result.append("-").append(floatNum);
            }
        }else {
            int exponent = -127;
            for (int i=0; i<items[1].length(); i++) {
                exponent += Integer.parseInt(String.valueOf(items[1].charAt(i))) * Math.pow(2, 7-i);
            }
            double floatNum = 0;
            for (int i=0; i<items[2].length(); i++) {
                floatNum += Integer.parseInt(String.valueOf(items[2].charAt(i))) * Math.pow(2, -i-1+exponent);
            }
            //隐藏位
            floatNum += Math.pow(2, exponent);
            if (items[0].equals("0")) result.append(floatNum);
            else result.append("-").append(floatNum);
        }
        return result.toString();
    }

    //加一
    private String oneAdder(String operand) {
        int len = operand.length();
        StringBuilder temp = new StringBuilder(operand);
        temp.reverse();
        int[] num = new int[len];
        for (int i=0; i<len; i++) num[i] = temp.charAt(i) - '0';
        int bit = 0x0;
        int carry = 0x1; //进位
        char[] res = new char[len];
        for (int i=0; i<len; i++) {
            bit = num[i] ^ carry; //每一位等于该位与进位进行异或操作
            carry = num[i] & carry;
            res[i] = (char) ('0' + bit); //显示转化为char
        }
        String result = new StringBuffer(new String(res)).reverse().toString();
        return "" + (result.charAt(0) == operand.charAt(0)? '0' : '1' ) + result;
    }

    //取反
    private String negation(String operand) {
        StringBuilder result = new StringBuilder();
        for (int i=0; i<operand.length(); i++) {
            result.append(operand.charAt(i)=='1'? "0" : "1");
        }
        return result.toString();
    }

    private double maxValue(int length) {
        //不能使用移位操作
        return Math.pow(2, length) - 1;
    }

    //最小规格化数
    private double minNormal(int eLength, int sLength) {
        int bias = (int) ((maxValue(eLength) + 1) / 2 - 1);  //bias
        return Math.pow(2, 1 - bias);  //指数为1，阶码全0
    }

    //小数部分转二进制
    private String fixPoint(double d, int sLength) {
        d = d < 1 ? d : d - (int) d;  //d = 0.xxxxx
        StringBuilder res = new StringBuilder();
        int count = 0;
        while (d != 0 && count < sLength) {
            d *= 2;
            if (d < 1) {
                res.append("0");
            } else {
                d -= 1;
                res.append("1");
            }
            count++;  //最长为sLength的长度
        }
        int len = res.length();  //不能直接用res.length()
        for (int i = 0; i < sLength - len; i++) res.append(0);
        return res.toString();
    }

    private double getExponent(double d) {
        if (d == 0) return 0;  //0不能得到正确结果，即-bias
        int exponent = 0;
        while (d >= 2) {
            d /= 2;
            exponent++;
        }
        while (d < 1) {
            d *= 2;
            exponent--;
        }
        return exponent;
    }

    private String integerRepresentation(String number, int length) {
        int num = Integer.parseInt(number);
        // num = number.charAt(0) == '-' ? -Integer.valueOf(number.substring(1)) : Integer.valueOf(number);
        if (num < 0) return Integer.toBinaryString(num).substring(32 - length);
        else {
            String result = Integer.toBinaryString(num);
            int len = length - result.length();  //这一步要先提取出来，不然下面会实时计算len
            for (int i = 0; i < len; i++) {
                result = "0" + result;
            }
            return result;
        }
    }
}
