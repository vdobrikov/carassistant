package com.carassistant.utilities;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Exception;
}