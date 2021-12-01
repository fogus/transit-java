// Copyright (c) Cognitect, Inc.
// All rights reserved.

package com.cognitect.transit.impl;

import java.util.function.Function;

public interface TagProvider {
    public String getTag(Object o);

    public default String getTag(Object o, Function<Object, Object> xform) {
        if (xform != null)
            return this.getTag(xform.apply(o));
        else
            return this.getTag(o);
    }

    public default Function<Object, Object> getTransformer() { return null; }
}
