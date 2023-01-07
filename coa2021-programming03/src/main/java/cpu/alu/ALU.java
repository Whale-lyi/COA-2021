package cpu.alu;

import util.DataType;

/**
 * Arithmetic Logic Unit
 * ALU封装类
 */
public class ALU {

    DataType remainderReg;

//    public static void main(String[] args) {
//        ALU alu = new ALU();
//        DataType src = new DataType("00000000000000000000000000000011");
//        DataType dest = new DataType("11111111111111111111111111111001");
//        System.out.println(alu.div1(src,dest));
//    }
    /**
     * 返回两个二进制整数的和
     * dest + src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType add(DataType src, DataType dest) {
        // TODO
        StringBuilder result = new StringBuilder();
        int flg = 0;
        for (int i=31; i>=0; i--) {
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
        result.reverse();
        return new DataType(result.toString());
    }


    /**
     * 返回两个二进制整数的差
     * dest - src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType sub(DataType src, DataType dest) {
        // TODO
        StringBuilder newSrc = new StringBuilder(src.toString());
        for (int i=0; i<32; i++) {
            if (newSrc.charAt(i) == '1') newSrc.setCharAt(i, '0');
            else newSrc.setCharAt(i, '1');
        }
        for (int i=31; i>=0; i--) {
            if (newSrc.charAt(i) == '1') {
                newSrc.setCharAt(i, '0');
            }else {
                newSrc.setCharAt(i, '1');
                break;
            }
        }
        return add(new DataType(newSrc.toString()), dest);
    }


    /**
     * 返回两个二进制整数的乘积(结果低位截取后32位)
     * dest * src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType mul(DataType src, DataType dest) {
        //TODO
        StringBuilder negativeSrcStr = new StringBuilder(src.toString());
        for (int i=0; i<32; i++) {
            if (negativeSrcStr.charAt(i) == '1') negativeSrcStr.setCharAt(i, '0');
            else negativeSrcStr.setCharAt(i, '1');
        }
        int flg = 1;
        for (int i=31; i>=0; i--) {
            if (negativeSrcStr.charAt(i) == '1') {
                negativeSrcStr.setCharAt(i, '0');
            }else {
                negativeSrcStr.setCharAt(i, '1');
                flg = 0;
            }
            if (flg == 0) break;
        }

        StringBuilder newDest = new StringBuilder();
        newDest.append("00000000000000000000000000000000").append(dest.toString()).append('0');
        for (int i=0; i<32; i++) {
            if (newDest.charAt(newDest.length()-1) == '0' && newDest.charAt(newDest.length()-2) == '1') {//-1
                DataType temp = add(new DataType(negativeSrcStr.toString()), new DataType(newDest.substring(0, 32)));
                newDest.delete(0,32).insert(0, temp.toString());
            }else if(newDest.charAt(newDest.length()-1) == '1' && newDest.charAt(newDest.length()-2) == '0'){//1
                DataType temp = add(new DataType(src.toString()), new DataType(newDest.substring(0, 32)));
                newDest.delete(0,32).insert(0, temp.toString());
            }
            newDest.deleteCharAt(newDest.length()-1);
            if (newDest.charAt(0) == '1') newDest.insert(0, '1');
            else newDest.insert(0, '0');
        }
        newDest.deleteCharAt(newDest.length()-1);
        return new DataType(newDest.substring(32, 64));
    }


    /**
     * 返回两个二进制整数的除法结果
     * 请注意使用不恢复余数除法方式实现
     * dest ÷ src
     *
     * @param src  32-bits
     * @param dest 32-bits
     * @return 32-bits
     */
    public DataType div(DataType src, DataType dest) {
        //TODO
        if (src.toString().equals("00000000000000000000000000000000")) {
            throw new ArithmeticException();
        }
        //符号扩展
        StringBuilder newDest = new StringBuilder();
        if (dest.toString().charAt(0) == '0') newDest.append("00000000000000000000000000000000").append(dest.toString());
        else newDest.append("11111111111111111111111111111111").append(dest.toString());
        //判断除数和被除数符号，进行加减法
        DataType temp;
        if (newDest.charAt(0) == src.toString().charAt(0)) {
            temp = sub(src, new DataType(newDest.substring(0,32)));
        }else {
            temp = add(src, new DataType(newDest.substring(0,32)));
        }
        newDest.delete(0,32).insert(0, temp.toString());
        //进循环
        for (int i=0; i<32; i++) {
            //判断余数和除数符号是否相同
            if (newDest.toString().charAt(0) == src.toString().charAt(0)) {
                //补商
                newDest.append("1");
                //左移
                newDest.deleteCharAt(0);
                //减
                temp = sub(src, new DataType(newDest.substring(0,32)));
            }else {
                newDest.append("0");
                newDest.deleteCharAt(0);
                temp = add(src, new DataType(newDest.substring(0,32)));
            }
            newDest.delete(0,32).insert(0, temp.toString());
        }
        //最后一步的补商
        if (newDest.toString().charAt(0) == src.toString().charAt(0)) {
            newDest.append("1");
        }else {
            newDest.append("0");
        }
        //商左移,即删除商的第一位
        newDest.deleteCharAt(32);
        //判断商是否需要加一
        if (dest.toString().charAt(0) != src.toString().charAt(0)) {
            for (int i=63; i>=32; i--) {
                if (newDest.charAt(i) == '0') {
                    newDest.setCharAt(i, '1');
                    break;
                }else {
                    newDest.setCharAt(i, '0');
                }
            }
        }
        //判断是否需要修正余数
        if (dest.toString().charAt(0) != newDest.charAt(0)) {
            if (dest.toString().charAt(0) == src.toString().charAt(0)) {
                temp = add(src, new DataType(newDest.substring(0,32)));
            }else {
                temp = sub(src, new DataType(newDest.substring(0,32)));
            }
            newDest.delete(0,32).insert(0, temp.toString());
        }
        //修正除法本身的bug
        if (add(new DataType(newDest.substring(0, 32)), src).toString().equals("00000000000000000000000000000000")) {
            remainderReg = new DataType("00000000000000000000000000000000");
            return sub(new DataType("00000000000000000000000000000001"), new DataType(newDest.substring(32,64)));
        }else if (newDest.substring(0, 32).equals(src.toString())) {
            remainderReg = new DataType("00000000000000000000000000000000");
            return add(new DataType("00000000000000000000000000000001"), new DataType(newDest.substring(32,64)));
        }else {
            remainderReg = new DataType(newDest.substring(0,32));
            return new DataType(newDest.substring(32,64));
        }
    }

    //恢复余数
    //dest / src
    public DataType div1(DataType src, DataType dest) {
        StringBuilder newDest = new StringBuilder();
        if (dest.toString().charAt(0) == '0') newDest.append("00000000000000000000000000000000").append(dest);
        else newDest.append("11111111111111111111111111111111").append(dest);
        for(int i=0; i<32; i++) {
            newDest.deleteCharAt(0);
            if (newDest.charAt(0) == src.toString().charAt(0)) {
                DataType temp = sub(src, new DataType(newDest.substring(0,32)));
                if (temp.toString().charAt(0) == newDest.charAt(0)) {
                    newDest.delete(0,32).insert(0, temp.toString());
                    newDest.append('1');
                }else newDest.append('0');
            }else {
                DataType temp = add(src, new DataType(newDest.substring(0,32)));
                if (temp.toString().charAt(0) == newDest.charAt(0)) {
                    newDest.delete(0,32).insert(0, temp.toString());
                    newDest.append('1');
                }else newDest.append('0');
            }
        }
        if (src.toString().charAt(0) != dest.toString().charAt(0)) {
            for (int i=32; i<64; i++) {
                if (newDest.charAt(i) == '1') newDest.setCharAt(i, '0');
                else newDest.setCharAt(i, '1');
            }
            for (int j=63; j>=32; j--) {
                if (newDest.charAt(j) == '1') newDest.setCharAt(j, '0');
                else {
                    newDest.setCharAt(j, '1');
                    break;
                }
            }
        }
        remainderReg = new DataType(newDest.substring(0,32));
        System.out.println(remainderReg);
        return new DataType(newDest.substring(32,64));
    }

}
