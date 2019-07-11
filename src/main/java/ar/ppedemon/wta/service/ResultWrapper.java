package ar.ppedemon.wta.service;

/**
 * Dummy class wrapping a result. Used to model computations that can fail.
 * Success is determined by the value of the errorMessage property. Any
 * instance where it's non null is considered fail. This means that a
 * {@code null} result can be regarded as successful.
 *
 * @param <T>  result type
 * @author ppedemon
 */
public final class ResultWrapper<T> {

    private final T result;
    private final String errorMessage;

    /**
     * Create an instance for a successful result.
     * @param result  result
     * @param <T>     result type
     * @return successful wrapper for the given result
     */
    public static <T> ResultWrapper<T> ok(T result) {
        return new ResultWrapper<>(result, null);
    }

    /**
     * Create an instance for a failure result.
     * @param message  error message
     * @param <T>      result type
     * @return fail wrapper for given error message
     */
    public static <T> ResultWrapper<T> error(String message) {
        return new ResultWrapper<>(null, message);
    }

    private ResultWrapper(T result, String errorMessage) {
        this.result = result;
        this.errorMessage = errorMessage;
    }

    /**
     * Answer whether this instance represents success.
     * @return whether this instance represents success
     */
    public boolean success() {
        return this.errorMessage == null;
    }

    /**
     * Answer whether this instance represents failure.
     * @return whether this instance represents failure
     */
    public boolean fail() {
        return !this.success();
    }

    /**
     * Get this instance result.
     * @return  result
     * @throws IllegalStateException if instance is fail
     */
    public T result() {
        if (fail()) {
            throw new IllegalStateException("Attempted to get result from not ok ResultWrapper");
        }
        return result;
    }

    /**
     * Get this instance error message.
     * @return  error message
     * @throws IllegalStateException if instance is successful
     */
    public String errorMessage() {
        if (success()) {
            throw new IllegalStateException("Attempted to get error message from ok ResultWrapper");
        }
        return errorMessage;
    }
}
