package cn.vlts.ulid4j;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;

/**
 * @author throwable
 * @version v1
 * @description Default monotonic ULID factory, generate monotonic ULID instance.
 * @since 2022/10/19 17:14
 */
final class MonotonicULIDFactory extends DefaultULIDFactory {

    private transient volatile ULID lastULID;

    MonotonicULIDFactory(LongSupplier timestampSupplier,
                         IntFunction<byte[]> randomnessFunction) {
        super(timestampSupplier, randomnessFunction);
        UPDATER.set(this, new ULID(0L, randomnessFunction.apply(ULID.RANDOMNESS_BYTE_LEN)));
    }

    @Override
    public ULID ulid() {
        long seedTime;
        do {
            seedTime = timestampSupplier.getAsLong();
        } while ((seedTime <= lastULID.getTimestamp() && UPDATER.compareAndSet(this, lastULID, lastULID.increment())) &
                (seedTime > lastULID.getTimestamp() && UPDATER.compareAndSet(this, lastULID,
                        new ULID(seedTime, randomnessFunction.apply(ULID.RANDOMNESS_BYTE_LEN)))));
        return lastULID;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public ULID ulid(long timestamp) {
        do {

        } while ((timestamp <= lastULID.getTimestamp() && UPDATER.compareAndSet(this, lastULID, lastULID.increment())) &
                (timestamp > lastULID.getTimestamp() && UPDATER.compareAndSet(this, lastULID,
                        new ULID(timestamp, randomnessFunction.apply(ULID.RANDOMNESS_BYTE_LEN)))));
        return lastULID;
    }

    private static final AtomicReferenceFieldUpdater<MonotonicULIDFactory, ULID> UPDATER;

    static {
        try {
            UPDATER = AtomicReferenceFieldUpdater.newUpdater(MonotonicULIDFactory.class, ULID.class, "lastULID");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
