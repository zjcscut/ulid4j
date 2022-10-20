# Universally Unique Lexicographically Sortable Identifier

A Java port of [alizain/ulid](https://github.com/alizain/ulid) with binary format implemented.

## Background

> The following comparison between UUID and ULID comes from ulid/spec

UUID can be suboptimal for many use-cases because:

- It isn't the most character efficient way of encoding 128 bits of randomness
- UUID v1/v2 is impractical in many environments, as it requires access to a unique, stable MAC address
- UUID v3/v5 requires a unique seed and produces randomly distributed IDs, which can cause fragmentation in many data structures
- UUID v4 provides no other information than randomness which can cause fragmentation in many data structures

Instead, herein is proposed ULID:

```javascript
ulid() // 01ARZ3NDEKTSV4RRFFQ69G5FAV
```

- 128-bit compatibility with UUID
- 1.21e+24 unique ULIDs per millisecond
- Lexicographically sortable!
- Canonically encoded as a 26 character string, as opposed to the 36 character UUID
- Uses Crockford's base32 for better efficiency and readability (5 bits per character)
- Case insensitive
- No special characters (URL safe)
- Monotonic sort order (correctly detects and handles the same millisecond)

## Install

```shell
mvn install -Dmaven.test.skip=true
```

Install this module in the local Maven repository under the Maven coordinates `cn.vlts.ulid4j:ulid4j:[version]` after executing `install` cmd

## Usage

Create default ULID factory.

```java
// use default timestamp and randomness provider
ULIDFactory ulidFactory = ULIDFactory.factory();

// use custom timestamp provider and default randomness provider
ULIDFactory ulidFactory = ULIDFactory.factory(System::currentTimeMillis);

// use custom timestamp and randomness provider
final SecureRandom secureRandom = new SecureRandom();
ULIDFactory ulidFactory = ULIDFactory.factory(System::currentTimeMillis, len -> {
    byte[] bytes = new byte[len];
    secureRandom.nextBytes(bytes);
    return bytes;
});
```

Create monotonic ULID factory.

```java
// use default timestamp and randomness provider
ULIDFactory ulidFactory = ULIDFactory.monotonicFactory();

// use custom timestamp provider and default randomness provider
ULIDFactory ulidFactory = ULIDFactory.monotonicFactory(System::currentTimeMillis);

// use custom timestamp and randomness provider
final SecureRandom secureRandom = new SecureRandom();
ULIDFactory ulidFactory = ULIDFactory.monotonicFactory(System::currentTimeMillis, len -> {
    byte[] bytes = new byte[len];
    secureRandom.nextBytes(bytes);
    return bytes;
});
```
Generate a new ULID instance with ULIDFactory.

```java
ULIDFactory ulidFactory = ...

// use default seed time provider        
ULID ulid = ulidFactory.ulid();

// use custom seed time    
ULID ulid = ulidFactory.ulid(15000);
```

Other useful methods from an ULID instance.

```java
ULIDFactory ulidFactory = ...
ULID ulid = ulidFactory.ulid();

// get timestamp component
long ts = ulid.getTimestamp();

// get randomness component
byte[] rand = ulid.getRandomness();

// convert to UUID
UUID uuid = ulid.toUUID();

// parse from UUID
ULID ulid = ULID.fromUUID(uuid);

// parse from ULID string
ULID ulid = ULID.fromString("01GFSN3QBEYCMFVMCD4NMJ9G0C");
```

You can find more examples from test class `cn.vlts.ulid4j.example.ULIDExampleTest`

## Benchmark

On 2.90GHz Intel Core i5-9400 and Java 11.0.1.

```shell
# Warmup: 1 iterations, 1 s each
# Measurement: 5 iterations, 3 s each
# Timeout: 10 min per iteration
# Threads: 10 threads, will synchronize iterations
# Benchmark mode: Throughput, ops/time

Benchmark                                   Mode  Cnt      Score       Error   Units
BenchmarkTest.createMonotonicULID          thrpt    5  22970.430 ±  5791.744  ops/ms
BenchmarkTest.createMonotonicULIDToString  thrpt    5  13242.072 ±  8473.168  ops/ms
BenchmarkTest.createULID                   thrpt    5  47355.605 ± 17538.797  ops/ms
BenchmarkTest.createULIDToString           thrpt    5  23644.328 ± 22879.085  ops/ms
BenchmarkTest.createUUID                   thrpt    5    808.353 ±     9.790  ops/ms
BenchmarkTest.createUUIDToString           thrpt    5    811.292 ±    65.002  ops/ms
```

See more info in `cn.vlts.ulid4j.benchmark.BenchmarkTest`

## Specification

Below is the current specification of ULID as implemented in [ulid/javascript](https://github.com/ulid/javascript).

*Note: the binary format has not been implemented in JavaScript as of yet.*

```
 01AN4Z07BY      79KA1307SR9X4MV3

|----------|    |----------------|
 Timestamp          Randomness
   48bits             80bits
```

### Components

**Timestamp**
- 48 bit integer
- UNIX-time in milliseconds
- Won't run out of space 'til the year 10889 AD.

**Randomness**
- 80 bits
- Cryptographically secure source of randomness, if possible

### Sorting

The left-most character must be sorted first, and the right-most character sorted last (lexical order). The default ASCII character set must be used. Within the same millisecond, sort order is not guaranteed

### Canonical String Representation

```
ttttttttttrrrrrrrrrrrrrrrr

where
t is Timestamp (10 characters)
r is Randomness (16 characters)
```

#### Encoding

Crockford's Base32 is used as shown. This alphabet excludes the letters I, L, O, and U to avoid confusion and abuse.

```
0123456789ABCDEFGHJKMNPQRSTVWXYZ
```

### Monotonicity

When generating a ULID within the same millisecond, we can provide some
guarantees regarding sort order. Namely, if the same millisecond is detected, the `random` component is incremented by 1 bit in the least significant bit position (with carrying). For example:

```javascript
import { monotonicFactory } from 'ulid'

const ulid = monotonicFactory()

// Assume that these calls occur within the same millisecond
ulid() // 01BX5ZZKBKACTAV9WEVGEMMVRZ
ulid() // 01BX5ZZKBKACTAV9WEVGEMMVS0
```

If, in the extremely unlikely event that, you manage to generate more than 2<sup>80</sup> ULIDs within the same millisecond, or cause the random component to overflow with less, the generation will fail.

```javascript
import { monotonicFactory } from 'ulid'

const ulid = monotonicFactory()

// Assume that these calls occur within the same millisecond
ulid() // 01BX5ZZKBKACTAV9WEVGEMMVRY
ulid() // 01BX5ZZKBKACTAV9WEVGEMMVRZ
ulid() // 01BX5ZZKBKACTAV9WEVGEMMVS0
ulid() // 01BX5ZZKBKACTAV9WEVGEMMVS1
...
ulid() // 01BX5ZZKBKZZZZZZZZZZZZZZZX
ulid() // 01BX5ZZKBKZZZZZZZZZZZZZZZY
ulid() // 01BX5ZZKBKZZZZZZZZZZZZZZZZ
ulid() // throw new Error()!
```

#### Overflow Errors when Parsing Base32 Strings

Technically, a 26-character Base32 encoded string can contain 130 bits of information, whereas a ULID must only contain 128 bits. Therefore, the largest valid ULID encoded in Base32 is `7ZZZZZZZZZZZZZZZZZZZZZZZZZ`, which corresponds to an epoch time of `281474976710655` or `2 ^ 48 - 1`.

Any attempt to decode or encode a ULID larger than this should be rejected by all implementations, to prevent overflow bugs.

### Binary Layout and Byte Order

The components are encoded as 16 octets. Each component is encoded with the Most Significant Byte first (network byte order).

```
0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      32_bit_uint_time_high                    |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     16_bit_uint_time_low      |       16_bit_uint_random      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                       32_bit_uint_random                      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                       32_bit_uint_random                      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

## Prior Art

Partly inspired by:
- [alizain/ulid](https://github.com/alizain/ulid)
- [ulid/spec](https://github.com/ulid/spec)