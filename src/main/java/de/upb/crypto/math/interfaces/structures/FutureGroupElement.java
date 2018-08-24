package de.upb.crypto.math.interfaces.structures;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * A Future for a group element whose value is being evaluated on another thread.
 * <p>
 * cf. Group::evaluateConcurrent().
 * <p>
 * This class exists for convenience to spare the user from handling exceptions in Future&lt;GroupElement&gt;
 */
public class FutureGroupElement {
    Future<GroupElement> baseFuture;
    Callable<GroupElement> baseCallable;

    /**
     * Posts the callable to Group.executor.
     *
     * @param callable the method to be run concurrently that returns a GroupElement.
     *                 If something goes wrong, this method may be called again on the main thread.
     */
    public FutureGroupElement(Callable<GroupElement> callable) {
        this.baseFuture = Group.executor.submit(callable);
    }

    public GroupElement get() {
        try {
            return baseFuture.get();
        } catch (InterruptedException e) {
            try {
                return baseCallable.call();
            } catch (Exception e2) {
                throw new IllegalArgumentException(e2);
            }
        } catch (ExecutionException e) {
            throw new IllegalArgumentException(e.getCause());
        }
    }

    @Override
    public String toString() {
        return get().toString();
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return get().equals(obj);
    }
}
