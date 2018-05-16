package jrm.misc;

@SuppressWarnings("serial")
public class BreakException extends RuntimeException
{

	public BreakException()
	{
	}

	public BreakException(String message)
	{
		super(message);
	}

	public BreakException(Throwable cause)
	{
		super(cause);
	}

	public BreakException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public BreakException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
