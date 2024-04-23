package org.klimashin.ga.segmented.trajectory.domain.util.common;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class ConstrainedScope<T> extends StructuredTaskScope<T> {

    private static final VarHandle FIRST_EXCEPTION;

    private volatile Throwable firstException;
    private Semaphore semaphore;
    private int permits;

    static {
        try {
            var lookup = MethodHandles.lookup();
            FIRST_EXCEPTION = lookup.findVarHandle(ConstrainedScope.class, "firstException", Throwable.class);
        } catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    public ConstrainedScope(String name, ThreadFactory factory) {
        super(name, factory);
    }

    public ConstrainedScope(int permits) {
        this(null, Thread.ofVirtual().factory());
        this.permits = permits;
        this.semaphore = new Semaphore(permits);
    }

    @Override
    protected void handleComplete(Subtask<? extends T> subtask) {
        if (subtask.state() == Subtask.State.FAILED && firstException == null && FIRST_EXCEPTION.compareAndSet(this, null, subtask.exception())) {
            semaphore.release(permits - semaphore.availablePermits());
            super.shutdown();
        }

        semaphore.release();
    }

    @Override
    public <U extends T> Subtask<U> fork(Callable<? extends U> task) {
        if (!super.isShutdown()) {
            try {
                semaphore.acquire();
                return super.fork(task);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }

        return null;
    }

    @Override
    public ConstrainedScope<T> join() throws InterruptedException {
        super.join();
        return this;
    }

    @Override
    public ConstrainedScope<T> joinUntil(Instant deadline) throws InterruptedException, TimeoutException {
        super.joinUntil(deadline);
        return this;
    }

    public Optional<Throwable> exception() {
        ensureOwnerAndJoined();
        return Optional.ofNullable(firstException);
    }

    public void throwIfFailed() throws ExecutionException {
        throwIfFailed(ExecutionException::new);
    }

    public <X extends Throwable> void throwIfFailed(Function<Throwable, ? extends X> exceptionFunction) throws X {
        ensureOwnerAndJoined();
        Objects.requireNonNull(exceptionFunction);
        var exception = firstException;
        if (exception != null) {
            X ex = exceptionFunction.apply(exception);
            Objects.requireNonNull(ex, "exceptionFunction returned null");
            throw ex;
        }
    }
}
