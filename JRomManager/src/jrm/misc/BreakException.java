package jrm.misc;

/**
 * The only way to break out from a lambda loop in Java 8 is throwing a RuntimeException
 * @author optyfr
 *
 */
@SuppressWarnings("serial")
public class BreakException extends RuntimeException
{

	public BreakException()
	{
	}

	public BreakException(final String message)
	{
		super(message);
	}

	public BreakException(final Throwable cause)
	{
		super(cause);
	}

	public BreakException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public BreakException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
