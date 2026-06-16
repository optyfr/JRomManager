package jrm.misc;

/**
 * A functional interface representing an action or task to be executed on an input argument. This can throw any {@link Exception}
 * during processing, allowing exception propagation.
 * 
 * @param <T> the type of the input argument to the operation
 * 
 * @author optyfr
 */
@FunctionalInterface
public interface CalledWith<T> {
    /**
     * Executes the action or operation on the specified object.
     * 
     * @param t the input object to process
     * 
     * @throws Exception if any error occurs during processing
     */
    public void call(final T t) throws Exception; // NOSONAR
}
