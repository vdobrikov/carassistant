package com.carassistant.utilities;

import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Author: Vladimir Dobrikov (hedin.mail@gmail.com)
 */
public class ExecutionHelpers {

    public static <T> T executeUncatched(ThrowingSupplier<T> supplier) {
        Assert.notNull(supplier, "'supplier' cannot be null");
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T executeWithRetries(ThrowingSupplier<T> supplier, Collection<Class<? extends Exception>> retryableExceptions, int maxRetries) throws Exception {
        Assert.notNull(supplier, "'supplier' cannot be null");
        Assert.isTrue(maxRetries >= 0, "'maxRetries' cannot be negative number");
        int retries = -1;
        Exception caughtEx;
        do {
            try {
                return supplier.get();
            } catch (Exception e) {
                caughtEx = e;
                retries++;
            }
        } while (retries < maxRetries && (retryableExceptions == null ||
            retryableExceptions.stream().anyMatch(caughtEx.getClass()::equals)));
        throw caughtEx;
    }
}
