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

import org.checkerframework.checker.signedness.qual.Signed;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;
import org.checkerframework.checker.signedness.qual.Unsigned;
import org.checkerframework.common.value.qual.PolyValue;

/**
 * The <code>unsigned short</code> type
 *
 * @author Lukas Eder
 * @author Jens Nerche
 */
public final class UShort extends UNumber implements Comparable<UShort> {

    /**
     * Generated UID
     */
    private static final long serialVersionUID = -6821055240959745390L;

    /**
     * A constant holding the minimum value an <code>unsigned short</code> can
     * have, 0.
     */
    public static final @Unsigned int   MIN_VALUE        = 0x0000;

    /**
     * A constant holding the maximum value an <code>unsigned short</code> can
     * have, 2<sup>16</sup>-1.
     */
    public static final @Unsigned int   MAX_VALUE        = 0xffff;

    /**
     * A constant holding the minimum value an <code>unsigned short</code> can
     * have as UShort, 0.
     */
    public static final @Unsigned UShort MIN             = valueOf(MIN_VALUE);

    /**
     * A constant holding the maximum value an <code>unsigned short</code> can
     * have as UShort, 2<sup>16</sup>-1.
     */
    public static final @Unsigned UShort MAX             = valueOf(MAX_VALUE);

    /**
     * The value modelling the content of this <code>unsigned short</code>
     */
    private final @Unsigned int         value;

    /**
     * Create an <code>unsigned short</code>
     *
     * @throws NumberFormatException If <code>value</code> does not contain a
     *             parsable <code>unsigned short</code>.
     */
    @SuppressWarnings("signedness:return")
    public static @Unsigned UShort valueOf(String value) throws NumberFormatException {
        return new UShort(value);
    }

    /**
     * Create an <code>unsigned short</code> by masking it with
     * <code>0xFFFF</code> i.e. <code>(short) -1</code> becomes
     * <code>(ushort) 65535</code>
     */
    @SuppressWarnings("signedness:return")
    public static @Unsigned UShort valueOf(@Unsigned short value) {
        return new UShort(value);
    }

    /**
     * Create an <code>unsigned short</code>
     *
     * @throws NumberFormatException If <code>value</code> is not in the range
     *             of an <code>unsigned short</code>
     */
    @SuppressWarnings("signedness:return")
    public static @Unsigned UShort valueOf(@Unsigned int value) throws NumberFormatException {
        return new UShort(value);
    }

    /**
     * Create an <code>unsigned short</code>
     *
     * @throws NumberFormatException If <code>value</code> is not in the range
     *             of an <code>unsigned short</code>
     */
    private UShort(@Unsigned int value) throws NumberFormatException {
        this.value = value;
        rangeCheck();
    }

    /**
     * Create an <code>unsigned short</code> by masking it with
     * <code>0xFFFF</code> i.e. <code>(short) -1</code> becomes
     * <code>(ushort) 65535</code>
     */
    private UShort(@Unsigned short value) {
        this.value = value & MAX_VALUE;
    }

    /**
     * Create an <code>unsigned short</code>
     *
     * @throws NumberFormatException If <code>value</code> does not contain a
     *             parsable <code>unsigned short</code>.
     */
    @SuppressWarnings("signedness:assignment")
    private UShort(String value) throws NumberFormatException {
        this.value = Integer.parseInt(value);
        rangeCheck();
    }

    @SuppressWarnings({"signedness:comparison", "signedness:cast"})
    private void rangeCheck() throws NumberFormatException {
        if (value < MIN_VALUE || value > MAX_VALUE)
            throw new NumberFormatException("Value is out of range : " + (@Signed int)value);
    }

    @Override
    @SuppressWarnings("allcheckers:return")
    public @PolyValue int intValue(@PolyValue UShort this) {
        return value;
    }

    @Override
    @SuppressWarnings("allcheckers:return")
    public @PolyValue long longValue(@PolyValue UShort this) {
        return value;
    }

    @Override
    @SuppressWarnings("value:return")
    public @PolyValue float floatValue(@PolyValue UShort this) {
        return value;
    }

    @Override
    @SuppressWarnings("value:return")
    public @PolyValue double doubleValue(@PolyValue UShort this) {
        return value;
    }

    @Override
    @SuppressWarnings("signedness:argument")
    public BigInteger toBigInteger() {
        return BigInteger.valueOf(value);
    }

    @Override
    @SuppressWarnings("signedness:method.invocation")
    public int hashCode(@UnknownSignedness UShort this) {
        return Integer.valueOf(value).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UShort)
            return value == ((UShort) obj).value;

        return false;
    }

    @Override
    @SuppressWarnings("signedness:method.invocation")
    public String toString() {
        return Integer.valueOf(value).toString();
    }

    @Override
    @SuppressWarnings("signedness:comparison")
    public int compareTo(UShort o) {
        return (value < o.value ? -1 : (value == o.value ? 0 : 1));
    }

    public @Unsigned UShort add(@Unsigned UShort val) throws NumberFormatException {
        return valueOf(value + val.value);
    }

    public @Unsigned UShort add(@Unsigned int val) throws NumberFormatException {
        return valueOf(value + val);
    }

    public @Unsigned UShort subtract(final @Unsigned UShort val) {
        return valueOf(value - val.value);
    }

    public @Unsigned UShort subtract(final @Unsigned int val) {
        return valueOf(value - val);
    }
}
