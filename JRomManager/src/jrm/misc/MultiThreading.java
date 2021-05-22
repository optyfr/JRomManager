package jrm.misc;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;

/**
 * Class to handle Multithreading via a ThreadPoolExecutor with an optional adaptive algorithm
 * that try to adjust dynamically the number of thread according the cumulated cputime of each thread 
 * known caveat for adaptive mode : native call thru jni is not taken into account for cputime computation
 * NB1 : load avg method is more accurate but may not be available everywhere (ie : not on windows)
 * @param <T> the type of object to process
 * @author opty
 */
public final class MultiThreading<T> extends ThreadPoolExecutor
{
	private final static ThreadMXBean tmxb = ManagementFactory.getThreadMXBean();
	private final static OperatingSystemMXBean osmxb = ManagementFactory.getOperatingSystemMXBean();
	private final int nStartThreads;
	private final boolean adaptive;
	private final long interval;
	private long time = System.currentTimeMillis();

	private final CalledWith<T> calledWith;

	/**
	 * create a new Multithreading according number of thread and a task to do on each object
	 * @param nThreads The requested number of thread, if negative adaptive mode will be used, if 0 all available processors will be used
	 * @param cw the task code to handle each object
	 */
	public MultiThreading(final int nThreads, final CalledWith<T> cw)
	{
		super(getNStartThreads(nThreads), getNStartThreads(nThreads), 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>()); 
		this.nStartThreads = getNStartThreads(nThreads);
		this.adaptive = isAdaptive(nThreads);
		this.interval = 60_000;	// check interval for adaptive mode (expressed in milliseconds)
		this.calledWith = cw;
	}

	/**
	 * Return absolute maximum number of threads to use
	 * @param nThreads
	 * @return
	 */
	private static int getNStartThreads(final int nThreads)
	{
		if (nThreads <= 0)
			return Runtime.getRuntime().availableProcessors();
		return nThreads;
	}

	/**
	 * determine if nThreads is set to adaptive mode
	 * @param nThreads the maximum number of threads to use (or negative for adaptive mode)
	 * @return true if nThreads is negative
	 */
	private static boolean isAdaptive(final int nThreads)
	{
		return nThreads < 0;
	}

	/**
	 * Start processing all objects from a stream according the task<br>
	 * <b>-== This is the main method to call ==-</b>
	 * @param stream the stream of objects to process
	 */
	public void start(final Stream<T> stream)
	{
		try
		{
			Objects.requireNonNull(calledWith);
			stream.forEach(entry -> submit(new CallableWith(entry))); // submit all entries from stream using a task
			shutdown(); // does not accept submission after stream as been consumed
			awaitTermination(1, TimeUnit.DAYS); // wait max for 1 day for all tasks to terminate
		}
		catch (InterruptedException e)
		{
			Log.err(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
		catch (NullPointerException e)
		{
			Log.err(e.getMessage(), e);
		}
	}
	
	/**
	 * keep track of start cputime for each thread
	 */
	private final HashMap<Thread, Long> startCPUTimeByThread = new HashMap<>();
	
	@Override
	protected void beforeExecute(final Thread t, final Runnable r)
	{
		super.beforeExecute(t, r);
		if (adaptive)
		{
			synchronized (this)
			{
				if (!startCPUTimeByThread.containsKey(t))
					startCPUTimeByThread.put(t, tmxb.getCurrentThreadCpuTime());
			}
		}
	}

	@Override
	protected void afterExecute(final Runnable r, final Throwable t)
	{
		super.afterExecute(r, t);
		if (adaptive)
		{
			final var elapsed = System.currentTimeMillis() - time;
			if (elapsed > interval)
			{
				synchronized (this)
				{
					double load = osmxb.getSystemLoadAverage();
					if (load < 0)	// sys load is not computable
					{
						// Compute cumulated CPUTime of all alive threads (number between 0.0 and n with n the count of active threads)
						load = (startCPUTimeByThread.entrySet().stream().filter(e->e.getKey().isAlive()).mapToLong(e -> {
							return tmxb.getThreadCpuTime(e.getKey().getId()) - e.getValue();
						}).sum() / 1_000_000.0 /* ns to ms */) / elapsed;
						// Compute usage ratio (number between 0.0 and 1.0)
						double usage = load / getPoolSize();
						System.out.format("cpu load is %.03f so usage ratio is %.03f with %d active threads\n", load, usage, getPoolSize());
						if (getMaximumPoolSize() == getPoolSize())	// make sure that used threads is not different than current maximum (as a result of a former thread reduction not yet taken into account or because we are near the end)
						{
							final var threshold = 1.0 / getMaximumPoolSize();
							if (usage < 1.0 - threshold) // there was at least 1 unused thread during the last sample 
							{
								final var newThreadCnt = Math.max(1, (int)Math.ceil(load));	// down to the rounded up used thread (equiv. to the load)
								if (newThreadCnt < getMaximumPoolSize())
								{
									System.out.format("setting down to %d from %d threads...\n", newThreadCnt, getMaximumPoolSize());
									setCorePoolSize(newThreadCnt);	// core pool must be lowered first or we will get illegalArgumentException
									setMaximumPoolSize(newThreadCnt);
								}
							}
							else if(usage > 1.0 - 0.5 * threshold)	// there was less than half of a thread unused
							{
								final var newThreadCnt = Math.min(nStartThreads, getMaximumPoolSize() + 1);	// add 1 thread
								if (newThreadCnt > getMaximumPoolSize())
								{
									System.out.format("setting up to %d from %d threads...\n", newThreadCnt, getMaximumPoolSize());
									setMaximumPoolSize(newThreadCnt);
									setCorePoolSize(newThreadCnt);	// core pool must be augmented last or we will get illegalArgumentException
								}
							}
						}
						else
							System.out.format("pools size of %d <> max pool size of %d...\n", getPoolSize(), getMaximumPoolSize());
					}
					else
					{
						// Compute usage ratio (number between 0.0 and 1.0)
						double usage = load / getPoolSize();
						System.out.format("sys load avg is %.03f so usage ratio is %.03f with %d active threads\n", load, usage, getPoolSize());
						if (getMaximumPoolSize() == getPoolSize())	// make sure that used threads is not different than current maximum (as a result of a former thread reduction not yet taken into account or because we are near the end)
						{
							if(load > getMaximumPoolSize() + 1)
							{
								final var newThreadCnt = Math.max(1, getMaximumPoolSize() - 1);	// sub 1 thread
								if (newThreadCnt < getMaximumPoolSize())
								{
									System.out.format("setting down to %d from %d threads...\n", newThreadCnt, getMaximumPoolSize());
									setCorePoolSize(newThreadCnt);	// core pool must be lowered first or we will get illegalArgumentException
									setMaximumPoolSize(newThreadCnt);
								}
							}
							else if(load < getMaximumPoolSize() - 1)
							{
								final var newThreadCnt = Math.min(nStartThreads, getMaximumPoolSize() + 1);	// add 1 thread
								if (newThreadCnt > getMaximumPoolSize())
								{
									System.out.format("setting up to %d from %d threads...\n", newThreadCnt, getMaximumPoolSize());
									setMaximumPoolSize(newThreadCnt);
									setCorePoolSize(newThreadCnt);	// core pool must be augmented last or we will get illegalArgumentException
								}
							}
						}
						else
							System.out.format("pools size of %d <> max pool size of %d...\n", getPoolSize(), getMaximumPoolSize());
					}
					time = System.currentTimeMillis();
					startCPUTimeByThread.entrySet().removeIf(e->!e.getKey().isAlive());	// cleanup dead thread from list
					startCPUTimeByThread.entrySet().stream().forEach(e -> e.setValue(tmxb.getThreadCpuTime(e.getKey().getId())));	// reset startCPUTime
				}
			}
		}
	}

	@FunctionalInterface
	public interface CalledWith<T>
	{
		public void call(final T t) throws Exception;
	}
	
	@RequiredArgsConstructor
	private class CallableWith implements Callable<Void>
	{
		private final T entry;
		
		@Override
		public Void call() throws Exception
		{
			calledWith.call(entry);
			return null;
		}
	}
}
