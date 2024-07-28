package com.wz.xlinksnap.common.generation;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 雪花算法实现
 * 参照：https://github.com/beyondfengyu/SnowFlake
 */
public class SnowFlake {

    // 起始时间戳（可选，建议为项目的上线时间）
    private static final long START_STMP = 1480166465631L;

    // 各部分位数
    private static final long SEQUENCE_BIT = 12; // 序列号占用的位数
    private static final long MACHINE_BIT = 5;   // 机器标识占用的位数
    private static final long DATACENTER_BIT = 5;// 数据中心占用的位数

    // 最大值
    private static final long MAX_DATACENTER_NUM = -1L ^ (-1L << DATACENTER_BIT);
    private static final long MAX_MACHINE_NUM = -1L ^ (-1L << MACHINE_BIT);
    private static final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BIT);

    // 各部分向左位移
    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId; // 数据中心
    private final long machineId;    // 机器标识
    private final AtomicLong sequence = new AtomicLong(0L); // 序列号
    private final AtomicLong lastStmp = new AtomicLong(-1L);// 上次生成ID的时间戳

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    public long nextId() {
        long currStmp = getNewstmp();
        long lastTime = lastStmp.get();

        if (currStmp < lastTime) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }

        if (currStmp == lastTime) {
            long currSequence = sequence.incrementAndGet() & MAX_SEQUENCE;
            if (currSequence == 0L) {
                currStmp = getNextMill(lastTime);
            }
            sequence.set(currSequence);
        } else {
            sequence.set(0L);
        }

        lastStmp.set(currStmp);

        return (currStmp - START_STMP) << TIMESTMP_LEFT
                | datacenterId << DATACENTER_LEFT
                | machineId << MACHINE_LEFT
                | sequence.get();
    }

    private long getNextMill(long lastTime) {
        long mill = getNewstmp();
        while (mill <= lastTime) {
            mill = getNewstmp();
        }
        return mill;
    }

    private long getNewstmp() {
        return System.currentTimeMillis();
    }
}
