package jrm.misc;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class MultiThreading
{
	public static <T> void execute(int nThreads, Stream<T> stream, CallableWith<T> task)
	{
		try
		{
			ExecutorService service = Executors.newFixedThreadPool(nThreads); // allocate a thread pool with n threads
			stream.forEach(entry -> service.submit(task.clone().set(entry))); // submit all entries from stream using a task
			service.shutdown(); // does not accept submission after stream as been consumed
			service.awaitTermination(1, TimeUnit.DAYS); // wait max for 1 day for all tasks to terminate
		}
		catch (InterruptedException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	public static abstract class CallableWith<T> implements Callable<Void>, Cloneable
	{
		T entry;

		private CallableWith<T> set(T entry)
		{
			this.entry = entry;
			return this;
		}

		public T get()
		{
			return entry;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected CallableWith<T> clone()
		{
			try
			{
				return (CallableWith<T>) super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				return null;
			}
		}
	}

}
