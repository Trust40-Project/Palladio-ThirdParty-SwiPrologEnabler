package swiprolog;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import jpl.Query;

/**
 * Test if multiple swi engines can be started in parallel
 *
 * @author W.Pasman 9may16
 */
public class MultiThreadTest {
	private static final int NTHREADS = 88;

	static {
		SwiInstaller.init();
	}

	@Test
	public void singleThreadTest() throws InterruptedException {
		System.out.println("Single-thread test");
		// we do not clear the SWI database. Make sure we do not reuse an
		// existing module.
		testSimpleQueries(9999);

	}

	@Test
	public void multiThreadTest() throws InterruptedException {
		System.out.println("Multi-thread test");
		List<Thread> threads = new ArrayList<Thread>();
		for (int n = 0; n < NTHREADS; n++) {
			threads.add(runThread(n));
		}
		while (!threads.isEmpty()) {
			Thread thread = threads.get(0);
			// System.out.println("Waiting for " + thread);
			thread.join();
			threads.remove(thread);
		}
	}

	private Thread runThread(final int n) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					testSimpleQueries(n);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		thread.start();
		return thread;
	}

	private void testSimpleQueries(int n) throws InterruptedException {
		new Query("set_prolog_flag(debug_on_error,false)").hasSolution();

		String module = "robot_" + n;
		String formula = "test :- member(3,[1,2,3,4,5])";
		insert(module, formula);
		Map<String, Object> res = query(module, "test");

		insert(module, "fibo(0,0)");
		insert(module, "fibo(1,0)");
		insert(module, "fibo(X,Y):-X>1, X1 is X-1, X2 is X-2, fibo(X1,Y1), fibo(X2,Y2), Y is Y1 + Y2");
		query(module, "listing");

		res = query(module, "fibo(22,Y)");
	}

	private void insert(String module, String formula) {
		String f = "assert(" + module + ":(" + formula + "))";
		// System.out.println(f);
		new Query(f).hasSolution();
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> query(String module, String formula) {
		String q = module + ":" + formula;
		// System.out.println(q);
		return new Query(q).allSolutions()[0];
	}
}