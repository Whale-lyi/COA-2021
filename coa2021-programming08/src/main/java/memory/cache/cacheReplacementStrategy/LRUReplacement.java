package memory.cache.cacheReplacementStrategy;

/**
 * TODO 最近最少用算法
 */
public class LRUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        cache.setTimeStamp(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        int rowNO = start;
        long minTimeStamp = cache.getTimeStamp(rowNO);
        for (int i=start; i<=end; i++) {
            if (!cache.isValid(i)) {
                cache.update(i, addrTag, input);
                return i;
            }
        }
        for (int i=start+1; i<=end; i++) {
            if (cache.getTimeStamp(i) < minTimeStamp) {
                minTimeStamp = cache.getTimeStamp(i);
                rowNO = i;
            }
        }
        if (cache.isDirty(rowNO)) {
            memory.write(cache.calculatePAddr(rowNO), 1024, cache.getData(rowNO));
        }
        cache.update(rowNO, addrTag, input);
        return rowNO;
    }
}






























