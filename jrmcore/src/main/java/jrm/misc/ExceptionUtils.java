package jrm.misc;

import java.util.function.Consumer;
import java.util.function.Function;

import lombok.experimental.UtilityClass;

public @UtilityClass class ExceptionUtils
{
	public <T, R> void unthrow(Consumer<R> result, Function<T, R> test, T t)
	{
		unthrow(result, test, t, null);
	}

	public <T, R> void unthrow(Consumer<R> result, Function<T, R> test, T t, R def)
	{
		final var r = ExceptionUtils.test(test, t, def);
		if (r != null)
			result.accept(r);
	}

	public <T, R> void unthrowF(Consumer<R> result, Function<T, R> test, T t, Function<Function<T, R>, R> def)
	{
		final var r = ExceptionUtils.testF(test, t, def);
		if (r != null)
			result.accept(r);
	}

	public <T, R> R test(Function<T, R> test, T t, R def)
	{
		try
		{
			return test.apply(t);
		}
		catch (Exception e)
		{
			return def;
		}
	}

	public <T, R> R testF(Function<T, R> test, T t, Function<Function<T, R>, R> def)
	{
		try
		{
			return test.apply(t);
		}
		catch (Exception e)
		{
			return def.apply(test);
		}
	}
}
