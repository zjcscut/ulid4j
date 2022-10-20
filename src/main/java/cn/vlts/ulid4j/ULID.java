package cn.vlts.ulid4j;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

/**
 * A class that represents an immutable Universally Unique Lexicographically Sortable Identifier (ULID).
 * A UUID represents a 128-bit value.
 *
 * @author throwable
 * @since 2022/10/19 16:54
 */
public final class ULID implements Serializable, Comparable<ULID> {

    private static final long serialVersionUID = -2938569386388233525L;

    // static field

    /**
     * Timestamp component mask
     */
    private static final long TIMESTAMP_MASK = 0xffff000000000000L;

    /**
     * The length of randomness component of ULID
     */
    public static final int RANDOMNESS_BYTE_LEN = 10;

    /**
     * Default alphabet of ULID
     */
    private static final char[] DEFAULT_ALPHABET = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C',
            'D', 'E', 'F', 'G', 'H', 'J', 'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'};

    /**
     * Decoding table of ULID
     */
    private static final byte[] DECODING_TABLE = new byte[]{
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, 0x00, 0x01,
            0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, -1, -1,
            -1, -1, -1, -1, -1, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e,
            0x0f, 0x10, 0x11, -1, 0x12, 0x13, -1, 0x14, 0x15, -1,
            0x16, 0x17, 0x18, 0x19, 0x1a, -1, 0x1b, 0x1c, 0x1d, 0x1e,
            0x1f, -1, -1, -1, -1, -1, -1, 0x0a, 0x0b, 0x0c,
            0x0d, 0x0e, 0x0f, 0x10, 0x11, -1, 0x12, 0x13, -1, 0x14,
            0x15, -1, 0x16, 0x17, 0x18, 0x19, 0x1a, -1, 0x1b, 0x1c,
            0x1d, 0x1e, 0x1f, -1, -1, -1, -1, -1
    };

    /**
     * Default alphabet mask
     */
    private static final int DEFAULT_ALPHABET_MASK = 0b11111;

    /**
     * The length of bytes of ULID
     */
    private static final int ULID_BYTE_LEN = 0x1a;

    /**
     * The least significant 64 bits increase overflow, 0xffffffffffffffffL + 1
     */
    private static final long OVERFLOW = 0x0000000000000000L;

    /**
     * Timestamp overflow flag, the 1st char of the input string must be between 0 and 7
     */
    private static final byte TIMESTAMP_OVERFLOW_FLAG = 0b11000;

    // field

    /**
     * The most significant 64 bits of this ULID.
     */
    private final long msb;

    /**
     * The least significant 64 bits of this ULID.
     */
    private final long lsb;

    /**
     * Creates a new ULID with the high 64 bits and low 64 bits as long value.
     *
     * @param msb the high 8 bytes of ULID
     * @param lsb the low 8 bytes of ULID
     */
    ULID(long msb, long lsb) {
        this.msb = msb;
        this.lsb = lsb;
    }

    /**
     * Creates a new ULID with timestamp and randomness.
     *
     * @param timestamp  timestamp
     * @param randomness randomness
     */
    ULID(long timestamp, byte[] randomness) {
        if ((timestamp & TIMESTAMP_MASK) != 0) {
            throw new IllegalArgumentException("Invalid timestamp");
        }
        if (Objects.isNull(randomness) || RANDOMNESS_BYTE_LEN != randomness.length) {
            throw new IllegalArgumentException("Invalid randomness");
        }
        long msb = 0;
        long lsb = 0;
        byte[] bytes = new byte[16];
        byte[] ts = new byte[6];
        for (int i = 0; i < 6; i++) {
            ts[i] = (byte) ((timestamp >>> (40 - i * 8)) & 0xff);
        }
        System.arraycopy(ts, 0, bytes, 0, 6);
        System.arraycopy(randomness, 0, bytes, 6, 10);
        for (int i = 0; i < 8; i++) {
            msb = (msb << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsb = (lsb << 8) | (bytes[i] & 0xff);
        }
        this.msb = msb;
        this.lsb = lsb;
    }

    /**
     * Get the most significant 64 bits of this ULID.
     */
    public long getMostSignificantBits() {
        return this.msb;
    }

    /**
     * Get the least significant 64 bits of this ULID.
     */
    public long getLeastSignificantBits() {
        return this.lsb;
    }

    /**
     * Get the timestamp component of ULID
     *
     * @return the timestamp component
     */
    public long getTimestamp() {
        return this.msb >>> 16;
    }

    /**
     * Get the randomness component of ULID
     *
     * @return the randomness component
     */
    public byte[] getRandomness() {
        byte[] randomness = new byte[RANDOMNESS_BYTE_LEN];
        for (int i = 0; i < 2; i++) {
            randomness[i] = (byte) ((this.msb >>> (8 - i * 8)) & 0xff);
        }
        for (int i = 2; i < 10; i++) {
            randomness[i] = (byte) ((this.lsb >>> (80 - i * 8)) & 0xff);
        }
        return randomness;
    }

    /**
     * Create a new ULID from given string value.
     *
     * @param value The string value
     * @return A new ULID with the specified value
     */
    public static ULID fromString(String value) {
        if (Objects.isNull(value) || ULID_BYTE_LEN != value.length()) {
            throw new IllegalArgumentException("Invalid length of ULID");
        }
        char[] chars = value.toCharArray();
        if ((DECODING_TABLE[chars[0]] & TIMESTAMP_OVERFLOW_FLAG) != 0) {
            throw new IllegalArgumentException("Time overflow");
        }
        for (char c : chars) {
            if (DECODING_TABLE[c] == -1) {
                throw new IllegalArgumentException("Invalid ULID canonical string for char '" + c + "'");
            }
        }
        long timestamp = decodeComponent(0, 0x00, 0x09, 5, 50, DECODING_TABLE, chars);
        long highRandomness = decodeComponent(0, 0x0a, 0x11, 5, 40, DECODING_TABLE, chars);
        long lowRandomness = decodeComponent(0, 0x12, 0x19, 5, 40, DECODING_TABLE, chars);
        return new ULID((timestamp << 16) | (highRandomness >>> 24), (highRandomness << 40) | (lowRandomness & 0xffffffffffL));
    }

    /**
     * Create a new ULID from another one UUID.
     *
     * @param uuid Another one UUID
     * @return A new ULID with the specified value
     */
    public static ULID fromUUID(UUID uuid) {
        return new ULID(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
    }

    /**
     * Create a new UUID from this ULID.
     *
     * @return A new UUID created with msb and lsb from this ULID
     */
    public UUID toUUID() {
        return new UUID(this.msb, this.lsb);
    }

    /**
     * Increment one and create a new ULID
     *
     * @return A new ULID
     */
    public ULID increment() {
        long newMsb = this.msb;
        long newLsb = this.lsb + 1;
        if (newLsb == OVERFLOW) {
            newMsb += 1;
        }
        return new ULID(newMsb, newLsb);
    }

    /**
     * Format ULID to canonical string with default alphabet. Use 'formatUnsignedLong0' from Long.formatUnsignedLong0()
     *
     * @param alphabet The Alphabet used to encode
     * @return canonical string
     */
    private String toCanonicalString(char[] alphabet) {
        byte[] bytes = new byte[ULID_BYTE_LEN];
        formatUnsignedLong0(this.lsb & 0xffffffffffL, 5, bytes, 18, 8, alphabet);
        formatUnsignedLong0(((this.msb & 0xffffL) << 24) | (this.lsb >>> 40), 5, bytes, 10, 8, alphabet);
        formatUnsignedLong0(this.msb >> 16, 5, bytes, 0, 10, alphabet);
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * Reference to java.lang.Long.formatUnsignedLong0()
     */
    private static void formatUnsignedLong0(long val, int shift, byte[] buf, int offset, int len, char[] alphabet) {
        int charPos = offset + len;
        long radix = 1L << shift;
        long mask = radix - 1;
        do {
            buf[--charPos] = (byte) alphabet[(int) (val & mask)];
            val >>>= shift;
        } while (charPos > offset);
    }

    /**
     * Decode component from char buf.
     *
     * @return Decoded unsigned long value
     */
    private static long decodeComponent(long val, int start, int end, int shirt, int ms, byte[] table, char[] buf) {
        for (int i = start; i <= end; i++) {
            val |= (long) table[buf[i]] << (ms = ms - shirt);
        }
        return val;
    }

    @Override
    public String toString() {
        return toCanonicalString(DEFAULT_ALPHABET);
    }

    @Override
    public int compareTo(ULID o) {
        int mostSigBits = Long.compare(this.msb, o.msb);
        return mostSigBits != 0 ? mostSigBits : Long.compare(this.lsb, o.lsb);
    }

    @Override
    public boolean equals(Object obj) {
        if ((Objects.isNull(obj)) || (obj.getClass() != ULID.class)) {
            return false;
        }
        ULID id = (ULID) obj;
        return (this.msb == id.msb && this.lsb == id.lsb);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.msb ^ this.lsb);
    }
}
