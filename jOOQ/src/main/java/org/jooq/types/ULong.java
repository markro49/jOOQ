/*
 * Copyright (c) 2011-2017, Data Geekery GmbH (http://www.datageekery.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jooq.types;

import java.math.BigInteger;

import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.checkerframework.common.value.qual.PolyValue;

/**
 * The <code>unsigned long</code> type
 *
 * @author Lukas Eder
 * @author Jens Nerche
 * @author Ivan Sokolov
 */
public final class ULong extends UNumber implements Comparable<ULong> {

    /**
     * Generated UID
     */
    private static final long      serialVersionUID = -6821055240959745390L;

    /**
     * A constant holding the minimum value an <code>unsigned long</code> can
     * have, 0.
     */
    @SuppressWarnings("signedness:assignment")
    public static final @Unsigned BigInteger MIN_VALUE        = BigInteger.ZERO;

    /**
     * A constant holding the maximum value an <code>unsigned long</code> can
     * have, 2<sup>64</sup>-1.
     */
    @SuppressWarnings("signedness:assignment")
    public static final @Unsigned BigInteger MAX_VALUE        = new BigInteger("18446744073709551615");

    /**
     * A constant holding the maximum value + 1 an <code>signed long</code> can
     * have, 2<sup>63</sup>.
     */
    public static final BigInteger MAX_VALUE_LONG   = new BigInteger("9223372036854775808");

    /**
     * A constant holding the minimum value an <code>unsigned long</code> can
     * have as ULong, 0.
     */
    @SuppressWarnings({"signedness:argument", "signedness:assignment", "signedness:method.invocation"})
    public static final @Unsigned ULong      MIN              = valueOf(MIN_VALUE.longValue());

    /**
     * A constant holding the maximum value + 1 an <code>signed long</code> can
     * have as ULong, 2<sup>63</sup>.
     */
    public static final @Unsigned ULong      MAX              = valueOf(MAX_VALUE);

    /**
     * The value modelling the content of this <code>unsigned long</code>
     */
    private final @Unsigned long             value;

    /**
     * Create an <code>unsigned long</code>
     *
     * @throws NumberFormatException If <code>value</code> does not contain a
     *             parsable <code>unsigned long</code>.
     */
    @SuppressWarnings("signedness:return")
    public static @Unsigned ULong valueOf(String value) throws NumberFormatException {
        return new ULong(value);
    }

    /**
     * Create an <code>unsigned long</code> by masking it with
     * <code>0xFFFFFFFFFFFFFFFF</code> i.e. <code>(long) -1</code> becomes
     * <code>(uint) 18446744073709551615</code>
     */
    @SuppressWarnings("signedness:return")
    public static @Unsigned ULong valueOf(@Unsigned long value) {
        return new ULong(value);
    }

    /**
     * Create an <code>unsigned long</code>
     *
     * @throws NumberFormatException If <code>value</code> is not in the range
     *             of an <code>unsigned long</code>
     */
    @SuppressWarnings("signedness:return")
    public static @Unsigned ULong valueOf(@Unsigned BigInteger value) throws NumberFormatException {
        return new ULong(value);
    }

    @SuppressWarnings("signedness:comparison")
    public static int compare(@Unsigned long x, @Unsigned long y) {
        x += Long.MIN_VALUE;
        y += Long.MIN_VALUE;
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     * Create an <code>unsigned long</code>
     *
     * @throws NumberFormatException If <code>value</code> is not in the range
     *             of an <code>unsigned long</code>
     */
    @SuppressWarnings({"signedness:argument", "signedness:assignment", "signedness:method.invocation"})
    private ULong(@Unsigned BigInteger value) throws NumberFormatException {
        if (value.compareTo(MIN_VALUE) < 0 || value.compareTo(MAX_VALUE) > 0)
            throw new NumberFormatException();
        else
            this.value = value.longValue();
    }

    /**
     * Create an <code>unsigned long</code> by masking it with
     * <code>0xFFFFFFFFFFFFFFFF</code> i.e. <code>(long) -1</code> becomes
     * <code>(uint) 18446744073709551615</code>
     */
    private ULong(@Unsigned long value) {
        this.value = value;
    }

    /**
     * Create an <code>unsigned long</code>
     *
     * @throws NumberFormatException If <code>value</code> does not contain a
     *             parsable <code>unsigned long</code>.
     */
    @SuppressWarnings({"signedness:argument", "signedness:assignment"})
    private ULong(String value) throws NumberFormatException {
        if (value == null)
            throw new NumberFormatException("null");

        int length = value.length();

        if (length == 0)
            throw new NumberFormatException("Empty input string");

        if (value.charAt(0) == '-')
            throw new NumberFormatException(
                String.format("Illegal leading minus sign on unsigned string %s", value));

        if (length <= 18) {
            this.value = Long.parseLong(value, 10);
            return;
        }

        final long first = Long.parseLong(value.substring(0, length - 1), 10);
        final int second = Character.digit(value.charAt(length - 1), 10);
        if (second < 0)
            throw new NumberFormatException("Bad digit at end of " + value);

        long result = first * 10 + second;
        if (compare(result, first) < 0)
            throw new NumberFormatException(
                String.format("String value %s exceeds range of unsigned long", value));

        this.value = result;
    }

    @Override
    @SuppressWarnings("allcheckers:return")
    public @PolyValue int intValue(@PolyValue ULong this) {
        return (int) value;
    }

    @Override
    @SuppressWarnings("allcheckers:return")
    public @PolyValue long longValue(@PolyValue ULong this) {
        return value;
    }

    @Override
    @SuppressWarnings({"signedness:comparison", "value:return"})
    public @PolyValue float floatValue(@PolyValue ULong this) {
        if (value < 0)
            return ((float) (value & Long.MAX_VALUE)) + Long.MAX_VALUE;
        else
            return value;
    }

    @Override
    @SuppressWarnings({"signedness:comparison", "value:return"})
    public @PolyValue double doubleValue(@PolyValue ULong this) {
        if (value < 0)
            return ((double) (value & Long.MAX_VALUE)) + Long.MAX_VALUE;
        else
            return value;
    }

    @Override
    @SuppressWarnings("signedness:method.invocation")
    public int hashCode(@UnknownSignedness ULong this) {
        return Long.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ULong)
            return value == ((ULong) obj).value;

        return false;
    }

    @Override
    @SuppressWarnings("signedness:comparison")
    public String toString() {
        if (value >= 0)
            return Long.toString(value);
        else
            return BigInteger.valueOf(value & Long.MAX_VALUE).add(MAX_VALUE_LONG).toString();
    }

    @Override
    public int compareTo(ULong o) {
        return compare(value, o.value);
    }

    @SuppressWarnings({"signedness:comparison", "signedness:return"})
    public ULong add(ULong val) throws NumberFormatException {
        if (value < 0 && val.value < 0)
            throw new NumberFormatException();

        final long result = value + val.value;
        if ((value < 0 || val.value < 0) && result >= 0)
            throw new NumberFormatException();

        return valueOf(result);
    }

    public @Unsigned ULong add(@Unsigned int val) throws NumberFormatException {
        return add((long) val);
    }

    @SuppressWarnings({"signedness:comparison", "signedness:argument"})
    public @Unsigned ULong add(@Unsigned long val) throws NumberFormatException {
        if (val < 0)
            return subtract(Math.abs(val));

        final long result = value + val;
        if (value < 0 && result >= 0)
            throw new NumberFormatException();

        return valueOf(result);
    }

    @SuppressWarnings({"signedness:comparison", "signedness:argument"})
    public @Unsigned ULong subtract(final @Unsigned ULong val) {
        if (this.compareTo(val) < 0)
            throw new NumberFormatException();

        final long result = value - val.value;
        if (value < 0 && result >= 0)
            throw new NumberFormatException();

        return valueOf(result);
    }

    public @Unsigned ULong subtract(final @Unsigned int val) {
        return subtract((long) val);
    }

    @SuppressWarnings({"signedness:comparison", "signedness:argument"})
    public @Unsigned ULong subtract(final @Unsigned long val) {
        if (val < 0)
            return add(-val);

        if (compare(value, val) < 0)
            throw new NumberFormatException();

        final long result = value - val;
        if (value < 0 && result >= 0)
            throw new NumberFormatException();

        return valueOf(result);
    }
}
