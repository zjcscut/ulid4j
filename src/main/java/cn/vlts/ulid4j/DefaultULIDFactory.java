package cn.vlts.ulid4j;

import java.util.function.IntFunction;
import java.util.function.LongSupplier;

/**
 * @author throwable
 * @version v1
 * @description Default ULID factory.
 * @since 2022/10/19 17:14
 */
class DefaultULIDFactory implements ULIDFactory {

    /**
     * The timestamp supplier, to provide timestamp component of ULID.
     */
    protected final LongSupplier timestampSupplier;

    /**
     * The randomness function, to provide randomness component of ULID.
     */
    protected final IntFunction<byte[]> randomnessFunction;

    DefaultULIDFactory(LongSupplier timestampSupplier, IntFunction<byte[]> randomnessFunction) {
        this.timestampSupplier = timestampSupplier;
        this.randomnessFunction = randomnessFunction;
    }

    @Override
    public ULID ulid() {
        return new ULID(timestampSupplier.getAsLong(), randomnessFunction.apply(ULID.RANDOMNESS_BYTE_LEN));
    }

    @Override
    public ULID ulid(long timestamp) {
        return new ULID(timestamp, randomnessFunction.apply(ULID.RANDOMNESS_BYTE_LEN));
    }
}
