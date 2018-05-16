package jrm.misc;

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
