
package jrm.misc;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.commons.lang3.time.DurationFormatUtils;

import jrm.aui.progress.ProgressHandler;
import lombok.RequiredArgsConstructor;

public class MultiThreadingVirtual<K> implements ExecutorService, OffsetProvider
{

	private final ExecutorService service;
	private final String name;
	private final int threadLimit;

	private final AtomicLong maxActive = new AtomicLong();
	private final AtomicLong count = new AtomicLong();
	private final HashMap<Long,Integer> activeThreads = new HashMap<>();
	private final Deque<Integer> freeOffsets = new ArrayDeque<>();
	private final Semaphore semaphore;

	private final CalledWith<K> calledWith;

	private static final AtomicInteger poolNumber = new AtomicInteger(1);

	public MultiThreadingVirtual(final String name, final ProgressHandler progress, final int nThreads, final CalledWith<K> calledWith)
	{
		this.calledWith = calledWith;
		this.name = name;
		this.threadLimit = getThreadLimit(nThreads);
		this.semaphore = new Semaphore(threadLimit);
		this.service = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(name + "-" + poolNumber.getAndIncrement() + "-thread-", 1).factory());
		progress.setOffsetProvider(this);
	}

	private int getThreadLimit(final int nThreads)
	{
		if (nThreads < 0)
			return Runtime.getRuntime().availableProcessors() * (-nThreads + 1);
		if (nThreads == 0)
			return 256;
		return nThreads;
	}
	
	@Override
	public void execute(Runnable command)
	{
		service.execute(command);

	}

	@Override
	public void shutdown()
	{
		service.shutdown();

	}

	@Override
	public List<Runnable> shutdownNow()
	{
		return service.shutdownNow();
	}

	@Override
	public boolean isShutdown()
	{
		return service.isShutdown();
	}

	@Override
	public boolean isTerminated()
	{
		return service.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
	{
		return service.awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task)
	{
		return service.submit(task);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result)
	{
		return service.submit(task, result);
	}

	@Override
	public Future<?> submit(Runnable task)
	{
		return service.submit(task);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
	{
		return service.invokeAll(tasks);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException
	{
		return service.invokeAll(tasks, timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
	{
		return service.invokeAny(tasks);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		return service.invokeAny(tasks, timeout, unit);
	}

	/**
	 * Start processing all objects from a stream according the task<br>
	 * <b>-== This is the main method to call ==-</b>
	 * 
	 * @param stream
	 *            the stream of objects to process
	 */
	public void start(final Stream<K> stream)
	{
		try
		{
			Objects.requireNonNull(calledWith);
			final var start = System.currentTimeMillis();
			stream.forEach(entry -> submit(new CallableWith(entry))); // submit all entries from stream using a task
			shutdown(); // does not accept submission after stream as been consumed
			awaitTermination(1, TimeUnit.DAYS); // wait max for 1 day for all tasks to terminate
			System.err.println(name + "-%d : %d vthreads for %d tasks in %s".formatted(poolNumber.get(), maxActive.get(), count.get(), DurationFormatUtils.formatDurationHMS(System.currentTimeMillis()-start)));
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
	
	@Override
	public int getOffset()
	{
		synchronized (activeThreads)
		{
			final var id = Thread.currentThread().threadId();
			final var offset = activeThreads.get(id);
			if(offset == null)
				return -1;
			return offset;
		}
	}

	@Override
	public int[] freeOffsets()
	{
		synchronized (activeThreads)
		{
			return freeOffsets.stream().mapToInt(i -> i).toArray();
		}
	}
	
	@RequiredArgsConstructor
	private class CallableWith implements Callable<Void>
	{
		private final K entry;

		private long allocOffset()
		{
			synchronized (activeThreads)
			{
				final var id = Thread.currentThread().threadId();
				final var offset = freeOffsets.poll();
				activeThreads.put(id, offset == null ? activeThreads.size() : offset);
				final var currentCount = activeThreads.size();
				if (maxActive.get() < currentCount)
					maxActive.set(currentCount);
				return id;
			}
		}

		private void freeOffset(long id)
		{
			synchronized (activeThreads)
			{
				count.incrementAndGet();
				freeOffsets.add(activeThreads.remove(id));
			}
		}

		@Override
		public Void call() throws Exception
		{
			semaphore.acquire();
			try
			{
				final var id = allocOffset();
				calledWith.call(entry);
				freeOffset(id);
			}
			finally
			{
				semaphore.release();
			}
			return null;
		}
	}

}
