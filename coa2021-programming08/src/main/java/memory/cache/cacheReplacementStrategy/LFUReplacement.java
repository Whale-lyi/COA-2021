package memory.cache.cacheReplacementStrategy;

/**
 * TODO 最近不经常使用算法
 */
public class LFUReplacement implements ReplacementStrategy {

    @Override
    public void hit(int rowNO) {
        cache.addVisited(rowNO);
    }

    @Override
    public int replace(int start, int end, char[] addrTag, char[] input) {
        int rowNO = start;
        int minVisited = cache.getVisited(start);
        for (int i=start; i<=end; i++) {
            if (!cache.isValid(i)) {
                cache.update(i, addrTag, input);
                return i;
            }
        }
        for (int i=start+1; i<=end; i++) {
            if (cache.getVisited(i) < minVisited) {
                minVisited = cache.getVisited(i);
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
