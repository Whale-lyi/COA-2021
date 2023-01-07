package cpu.nbcdu;

import org.junit.Test;
import util.DataType;
import util.Transformer;

import static org.junit.Assert.assertEquals;

public class NBCDUSubTest {

    private final NBCDU nbcdu = new NBCDU();
    private final Transformer transformer = new Transformer();
    private DataType src;
    private DataType dest;
    private DataType result;

    @Test
    public void SubTest1() {
        src = new DataType("11000000000000000000000100100101");
        dest = new DataType("11000000000000000000001100001001");
        result = nbcdu.sub(src, dest);
        assertEquals("11000000000000000000000110000100", result.toString());
    }

    @Test
    public void SubTest2() {
        src = new DataType("11000000000000000000001100001001");
        dest = new DataType("11000000000000000000000100100101");
        result = nbcdu.sub(src, dest);
        assertEquals("11010000000000000000000110000100", result.toString());
    }
}
