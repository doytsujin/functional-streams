/*
 *                     functional-streams
 *              Copyright (C) 2018 Varun Anand
 *
 * This file is part of functional-streams.
 *
 * functional-streams is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * at your option) any later version.
 *
 * functional-streams is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.littlesaints.protean.functions.streams;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;
import java.util.stream.IntStream;

public class AggregatorTest {

    @Test
    public void test() {
        final int aggregationThreshold = 500;
        Assert.assertFalse(IntStream.range(0, 100).boxed()
                .map(Aggregator.of(Integer::intValue, l -> l < aggregationThreshold))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(System.out::println)
                .anyMatch(c -> c.stream().mapToLong(Integer::intValue).sum() >= aggregationThreshold));
    }

}
