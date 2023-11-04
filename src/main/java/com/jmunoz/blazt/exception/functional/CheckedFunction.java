package com.jmunoz.blazt.exception.functional;

import io.micrometer.common.lang.Nullable;

@FunctionalInterface
public interface CheckedFunction<T, R, E extends Throwable> {

    @Nullable
    R apply(T t) throws E;

}
