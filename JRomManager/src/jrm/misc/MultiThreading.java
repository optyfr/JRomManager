package jrm.misc;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class MultiThreading
{

	public static <T> void execute(int threads, Stream<T> stream, CallableWith<T> task)
	{
		ExecutorService service = Executors.newFixedThreadPool(threads);
		stream.forEach(entry -> service.submit(task.clone().set(entry)));
		service.shutdown();
		try
		{
			service.awaitTermination(1, TimeUnit.DAYS);
		}
		catch (InterruptedException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	public static abstract class CallableWith<T> implements Callable<Void>,Cloneable
	{
		T t;

		private CallableWith<T> set(T t)
		{
			this.t = t;
			return this;
		}

		public T get()
		{
			return t;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected CallableWith<T> clone()
		{
			try
			{
				return (CallableWith<T>)super.clone();
			}
			catch (CloneNotSupportedException e)
			{
				return null;
			}
		}
	}

}
