/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Other licenses:
 * -----------------------------------------------------------------------------
 * Commercial licenses for this work are available. These replace the above
 * ASL 2.0 and offer limited warranties, support, maintenance, and commercial
 * database integrations.
 *
 * For more information, please visit: https://www.jooq.org/legal/licensing
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package org.jooq.impl;

import static java.lang.Boolean.TRUE;
import static org.jooq.impl.Tools.EMPTY_QUERYPART;
import static org.jooq.impl.Tools.allMatch;
import static org.jooq.impl.Tools.anyMatch;
import static org.jooq.impl.Tools.isRendersSeparator;
import static org.jooq.impl.Tools.last;
import static org.jooq.impl.Tools.BooleanDataKey.DATA_LIST_ALREADY_INDENTED;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jooq.Condition;
import org.jooq.Context;
import org.jooq.Function0;
import org.jooq.Function1;
import org.jooq.QueryPart;
import org.jooq.QueryPartInternal;
// ...
// ...
import org.jooq.impl.QOM.UnmodifiableCollection;

import org.jetbrains.annotations.NotNull;

import org.checkerframework.checker.signedness.qual.PolySigned;
import org.checkerframework.checker.signedness.qual.UnknownSignedness;

/**
 * A {@link List} view, delegating all calls to a wrapped list, but acting like
 * a {@link QueryPart}.
 *
 * @author Lukas Eder
 */
sealed class QueryPartCollectionView<T extends QueryPart>
extends
    AbstractQueryPart
implements
    UnmodifiableCollection<T>,
    SimpleCheckQueryPart,
    SeparatedQueryPart
permits



    QueryPartListView
{

    final Collection<T>                    wrapped;
    Boolean                                qualify;
    String                                 separator;
    ObjIntFunction<? super T, ? extends T> mapper;

    static final <T extends QueryPart> QueryPartCollectionView<T> wrap(Collection<T> wrapped) {
        return new QueryPartCollectionView<>(wrapped);
    }

    QueryPartCollectionView(Collection<T> wrapped) {
        this.wrapped = wrapped != null ? wrapped : Collections.emptyList();

        if (wrapped instanceof QueryPartCollectionView<T> v) {
            this.qualify = v.qualify;
            this.separator = v.separator;
            this.mapper = v.mapper;
        }
        else
            this.separator = ",";
    }

    QueryPartCollectionView<T> qualify(boolean newQualify) {
        this.qualify = newQualify;
        return this;
    }

    QueryPartCollectionView<T> map(Function<? super T, ? extends T> newMapper) {
        return map((t, i) -> newMapper.apply(t));
    }

    QueryPartCollectionView<T> map(ObjIntFunction<? super T, ? extends T> newMapper) {
        this.mapper = mapper == null ? newMapper : mapper.andThen(newMapper);
        return this;
    }

    QueryPartCollectionView<T> separator(String newSeparator) {
        this.separator = newSeparator;
        return this;
    }

    Collection<T> wrapped() {
        return wrapped;
    }

    @Override
    public boolean isSimple(Context<?> ctx) {
        return allMatch(this, e -> Tools.isSimple(ctx, e));
    }

    @Override
    public boolean rendersSeparator() {
        if (isEmpty())
            return false;
        else
            return isRendersSeparator(last(wrapped));
    }

    @Override
    public boolean rendersContent(Context<?> ctx) {
        return !isEmpty();
    }

    @Override
    public /* non-final */ void accept(Context<?> ctx) {
        BitSet rendersContent = new BitSet(size());
        int i = 0;

        for (T e : this)
            rendersContent.set(i++, ((QueryPartInternal) e).rendersContent(ctx));

        int size = rendersContent.cardinality();
        boolean format = ctx.format() && (size >= 2 && !isSimple(ctx) || size > 4);
        boolean previousQualify = ctx.qualify();
        boolean previousAlreadyIndented = TRUE.equals(ctx.data(DATA_LIST_ALREADY_INDENTED));
        boolean indent = format && !previousAlreadyIndented;

        if (previousAlreadyIndented)
            ctx.data(DATA_LIST_ALREADY_INDENTED, false);

        if (qualify != null)
            ctx.qualify(qualify);

        if (indent)
            ctx.formatIndentStart();

        if (ctx.separatorRequired())
            if (format)
                ctx.formatSeparator();
            else
                ctx.sql(' ');

        // Some lists render different SQL when empty
        if (size == 0) {
            toSQLEmptyList(ctx);
        }

        else {
            int j = 0;
            int k = 0;
            T prev = null;

            for (T part : this) {
                try {
                    int j0 = j;
                    if (!rendersContent.get(j++))
                        continue;

                    if (mapper != null)
                        part = mapper.apply(part, j0);

                    if (k++ > 0) {

                        // [#3607] Procedures and functions are not separated by comma
                        if (!(prev instanceof SeparatedQueryPart && ((SeparatedQueryPart) prev).rendersSeparator()))
                            ctx.sql(separator);

                        if (format)
                            ctx.formatSeparator();
                        else
                            ctx.sql(' ');
                    }
                    else if (indent)
                        ctx.formatNewLine();

                    if (indent) {
                        T t = part;

                        ctx.data(
                            DATA_LIST_ALREADY_INDENTED,
                            t instanceof QueryPartCollectionView && ((QueryPartCollectionView<?>) t).size() > 1,
                            c -> acceptElement(c, t)
                        );
                    }
                    else
                        acceptElement(ctx, part);
                }
                finally {
                    prev = part;
                }
            }
        }

        if (indent)
            ctx.formatIndentEnd().formatNewLine();

        if (qualify != null)
            ctx.qualify(previousQualify);

        if (previousAlreadyIndented)
            ctx.data(DATA_LIST_ALREADY_INDENTED, previousAlreadyIndented);
    }

    /**
     * Subclasses may override this method
     */
    protected void acceptElement(Context<?> ctx, T part) {
        // [#11969] As of jOOQ 3.17, *all* QueryPartCollectionView<Field<?>> are
        //          declared as such, there are no QueryPartCollectionView<Condition>
        //          declarations. Hence, it is currently safe to assume that
        //          if (part instanceof Condition), then it must have been passed
        //          as a subtype of Field<?> and must be wrapped
        if (part instanceof Condition c)
            ctx.visit((QueryPart) DSL.field(c));
        else
            ctx.visit(part);
    }

    /**
     * Subclasses may override this method
     */
    @SuppressWarnings("unused")
    protected void toSQLEmptyList(Context<?> context) {
    }

    // -------------------------------------------------------------------------
    // Implementations from the List API
    // -------------------------------------------------------------------------

    @Override
    public final int size() {
        return wrapped.size();
    }

    @Override
    public final boolean isEmpty() {
        return wrapped.isEmpty();
    }

    @Override
    public final boolean contains(@UnknownSignedness Object o) {
        return wrapped.contains(o);
    }

    @Override
    public final Iterator<T> iterator() {
        return wrapped.iterator();
    }

    @Override
    public final @PolySigned Object[] toArray() {
        return wrapped.toArray();
    }

    @Override
    public final <E> E[] toArray(E[] a) {
        return wrapped.toArray(a);
    }

    /* non-final */ boolean canAdd(T e) {
        return e != null;
    }

    @Override
    public final boolean add(T e) {
        if (canAdd(e))
            return wrapped.add(e);

        return false;
    }

    @Override
    public final boolean remove(@UnknownSignedness Object o) {
        return wrapped.remove(o);
    }

    @Override
    public final boolean containsAll(Collection<?> c) {
        return wrapped.containsAll(c);
    }

    final boolean addAll0(Iterable<? extends T> c) {
        boolean modified = false;

        if (c != null)
            for (T e : c)
                if (add(e))
                    modified = true;

        return modified;
    }

    @Override
    public final boolean addAll(Collection<? extends T> c) {
        return addAll0(c);
    }

    @Override
    public final boolean removeAll(Collection<?> c) {
        return wrapped.removeAll(c);
    }

    @Override
    public final boolean retainAll(Collection<?> c) {
        return wrapped.retainAll(c);
    }

    @Override
    public final void clear() {
        wrapped.clear();
    }
























    // -------------------------------------------------------------------------
    // The Object API
    // -------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return wrapped.hashCode();
    }

    @Override
    public boolean equals(@UnknownSignedness Object that) {
        // [#11126] Speed up comparisons of two QueryPartCollectionViews of the same type

        if (this == that)
            return true;

        // Maintain List::equals and Set::equals contracts
        else if (that instanceof List && !(this instanceof List))
            return false;
        else if (that instanceof QueryPartCollectionView<?> q)
            return wrapped.equals(q.wrapped);
        else
            return super.equals(that);
    }
}
