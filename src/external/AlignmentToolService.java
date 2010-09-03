package external;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlignmentToolService
{
	/**
	 * The number of CPUs in your system is a good value for this
	 */
	private static final int NUMBER_OF_CONCURRENT_THREADS = 4;
	private ExecutorService pool;

	public AlignmentToolService()
	{
		pool = Executors.newFixedThreadPool(NUMBER_OF_CONCURRENT_THREADS);
	}

	public static void main(String[] args)
	{

	}
}
