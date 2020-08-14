package jrm.misc;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public final class MultiThreading extends ThreadPoolExecutor
{
	private final ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
	private final int nStartThreads;
	private final boolean adaptive;
	private long time = System.currentTimeMillis();

	public MultiThreading(int nThreads)
	{
		super(getNStartThreads(nThreads), getNStartThreads(nThreads), 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()); 
		this.nStartThreads = getNStartThreads(nThreads);
		this.adaptive = isAdaptive(nThreads);
	}

	private static int getNStartThreads(int nThreads)
	{
		if (nThreads <= 0)
			return Runtime.getRuntime().availableProcessors();
		return nThreads;
	}

	private static boolean isAdaptive(int nThreads)
	{
		return true;// nThreads < 0;
	}

	public <T> void execute(Stream<T> stream, CallableWith<T> task)
	{
		try
		{
			System.out.format("Starting MultiThreading with %d threads...\n", nStartThreads);
			stream.forEach(entry -> submit(task.clone().set(entry))); // submit all entries from stream using a task
			shutdown(); // does not accept submission after stream as been consumed
			awaitTermination(1, TimeUnit.DAYS); // wait max for 1 day for all tasks to terminate
			System.out.format("Ending MultiThreading...\n");
		}
		catch (InterruptedException e)
		{
			Log.err(e.getMessage(), e);
		}
	}

	private final HashMap<Thread, Long> statusByThread = new HashMap<>();
	
	@Override
	protected void beforeExecute(Thread t, Runnable r)
	{
		super.beforeExecute(t, r);
		if (adaptive)
		{
			synchronized (this)
			{
				if (!statusByThread.containsKey(t))
					statusByThread.put(t, tmxb.getCurrentThreadCpuTime());
			}
		}
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t)
	{
		super.afterExecute(r, t);
		if (adaptive)
		{
			final var elapsed = System.currentTimeMillis() - time;
			if (elapsed > 10_000)
			{
				synchronized (this)
				{
					double load = (statusByThread.entrySet().stream().filter(e->e.getKey().isAlive()).mapToLong(e -> {
						return tmxb.getThreadCpuTime(e.getKey().getId()) - e.getValue();
					}).sum() / 1_000_000.0) / elapsed;
					double usage = load / getActiveCount();
					System.out.format("load is %.03f so usage ratio is %.03f with %d active threads\n", load, usage, getActiveCount());
					if (usage <= .8)
					{
						if (getMaximumPoolSize() >= getActiveCount())
						{
							final var newThreadCnt = (int) Math.ceil(usage * getMaximumPoolSize());
							if (newThreadCnt < getMaximumPoolSize())
							{
								System.out.format("setting down to %d from %d threads...\n", newThreadCnt, getMaximumPoolSize());
								setCorePoolSize(newThreadCnt);
								setMaximumPoolSize(newThreadCnt);
							}
						}
					}
					else
					{
						final var newThreadCnt = Math.min(nStartThreads, (int) Math.ceil(usage * getMaximumPoolSize()) + 1);
						if (newThreadCnt > getMaximumPoolSize())
						{
							System.out.format("setting up to %d from %d threads...\n", newThreadCnt, getMaximumPoolSize());
							setMaximumPoolSize(newThreadCnt);
							setCorePoolSize(newThreadCnt);
						}
					}
					time = System.currentTimeMillis();
					statusByThread.entrySet().removeIf(e->!e.getKey().isAlive());
					statusByThread.entrySet().stream().forEach(e -> e.setValue(tmxb.getThreadCpuTime(e.getKey().getId())));
				}
			}
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
