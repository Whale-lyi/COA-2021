package memory.disk;

import java.util.Arrays;

public class Scheduler {

    /**
     * 先来先服务算法
     * @param start 磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double FCFS(int start, int[] request) {
        // TODO
        double sum = 0;
        for (int num : request) {
            sum += Math.abs(start - num);
            start = num;
        }
        return sum/request.length;
    }

    /**
     * 最短寻道时间优先算法
     * @param start 磁头初始位置
     * @param request 请求访问的磁道号
     * @return 平均寻道长度
     */
    public double SSTF(int start, int[] request) {
        // TODO
        double sum = 0;
        boolean setMin = true;
        int closestNumIndex = 0;
        int closestNum = request[0];
        int min=0;
        boolean[] isUsed = new boolean[request.length];
        for (int i=0; i<request.length; i++) {
            for (int j=0; j<request.length; j++) {
                if (setMin && !isUsed[j]) {
                    setMin = false;
                    min = Math.abs(start-request[j]);
                    closestNumIndex = j;
                    closestNum = request[j];
                }
                else if (!isUsed[j]) {
                    int tmp = Math.abs(start-request[j]);
                    if (tmp<min) {
                        min = tmp;
                        closestNumIndex = j;
                        closestNum = request[j];
                    }
                }
            }
            sum += min;
            isUsed[closestNumIndex] = true;
            start = closestNum;
            setMin = true;
        }
        return sum/request.length;
    }

    /**
     * 扫描算法
     * @param start 磁头初始位置
     * @param request 请求访问的磁道号
     * @param direction 磁头初始移动方向，true表示磁道号增大的方向，false表示磁道号减小的方向
     * @return 平均寻道长度
     */
    public double SCAN(int start, int[] request, boolean direction) {
        // TODO
        if (request == null) return 0;
        double sum = 0;
        Arrays.sort(request);
        int i=0;
        while (i<request.length && start > request[i]) i++;
        if (direction) {
            for (int j=i; j<request.length; j++) {
                sum += Math.abs(start-request[j]);
                start = request[j];
            }
            if (i != 0) {
                sum += Disk.TRACK_NUM - 1 - start;
                start = Disk.TRACK_NUM - 1;
                for (int j=i-1; j>=0; j--) {
                    sum += Math.abs(start-request[j]);
                    start = request[j];
                }
            }
        }else {
            for (int j=i-1; j>=0; j--) {
                sum += Math.abs(start-request[j]);
                start = request[j];
            }
            if (i != request.length) {
                sum += start;
                start = 0;
                for (int j=i; j<request.length; j++) {
                    sum += Math.abs(start-request[j]);
                    start = request[j];
                }
            }
        }
        return sum/request.length;
    }

}