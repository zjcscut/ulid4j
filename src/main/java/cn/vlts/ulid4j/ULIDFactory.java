package cn.vlts.ulid4j;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.function.LongSupplier;

/**
 * @author throwable
 * @version v1
 * @description ULID factory interface.
 * @since 2022/10/19 16:56
 */
@FunctionalInterface
public interface ULIDFactory {

    /**
     * Create a new ULID instance.
     *
     * @return A new ULID instance
     */
    ULID ulid();

    /**
     * Create a new ULID instance.
     *
     * @param timestamp The seed time
     * @return A new ULID instance
     */
    default ULID ulid(long timestamp) {
        throw new UnsupportedOperationException("ulid with timestamp");
    }

    /**
     * Default implementation for timestamp supplier.
     */
    LongSupplier DEFAULT_TIMESTAMP_SUPPLIER = System::currentTimeMillis;

    /**
     * Default implementation for randomness function.
     */
    IntFunction<byte[]> DEFAULT_RANDOMNESS_FUNCTION = len -> {
        byte[] bytes = new byte[len];
        ThreadLocalRandom.current().nextBytes(bytes);
        return bytes;
    };

    /**
     * Create a new default ULID factory instance.
     *
     * @return A new default ULID factory
     */
    static ULIDFactory factory() {
        return new DefaultULIDFactory(DEFAULT_TIMESTAMP_SUPPLIER, DEFAULT_RANDOMNESS_FUNCTION);
    }

    /**
     * Create a new default ULID factory instance with timestamp supplier.
     *
     * @param timestampSupplier The timestamp supplier
     * @return A new monotonic ULID factory
     */
    static ULIDFactory factory(LongSupplier timestampSupplier) {
        return new DefaultULIDFactory(timestampSupplier, DEFAULT_RANDOMNESS_FUNCTION);
    }

    /**
     * Create a new default ULID factory instance with timestamp supplier and randomness function.
     *
     * @param timestampSupplier  The timestamp supplier
     * @param randomnessFunction The randomness function
     * @return A new monotonic ULID factory
     */
    static ULIDFactory factory(LongSupplier timestampSupplier,
                               IntFunction<byte[]> randomnessFunction) {
        return new DefaultULIDFactory(timestampSupplier, randomnessFunction);
    }

    /**
     * Create a new monotonic ULID factory instance.
     *
     * @return A new monotonic ULID factory
     */
    static ULIDFactory monotonicFactory() {
        return new MonotonicULIDFactory(DEFAULT_TIMESTAMP_SUPPLIER, DEFAULT_RANDOMNESS_FUNCTION);
    }

    /**
     * Create a new monotonic ULID factory instance with timestamp supplier.
     *
     * @param timestampSupplier The timestamp supplier
     * @return A new monotonic ULID factory
     */
    static ULIDFactory monotonicFactory(LongSupplier timestampSupplier) {
        return new MonotonicULIDFactory(timestampSupplier, DEFAULT_RANDOMNESS_FUNCTION);
    }

    /**
     * Create a new monotonic ULID factory instance with timestamp supplier and randomness function.
     *
     * @param timestampSupplier  The timestamp supplier
     * @param randomnessFunction The randomness function
     * @return A new monotonic ULID factory
     */
    static ULIDFactory monotonicFactory(LongSupplier timestampSupplier,
                                        IntFunction<byte[]> randomnessFunction) {
        return new MonotonicULIDFactory(timestampSupplier, randomnessFunction);
    }
}
