package it.fago.dataflow.test;

public class WorkersUtil {

	public static Thread[] create(Runnable r, int numOfWorkers) {
		Thread[] tmp = new Thread[numOfWorkers];
		for (int idx = 0; idx < numOfWorkers; idx++) {
			tmp[idx] = new Thread(r, "WORKER##" + idx);
		}
		return tmp;
	}

	public static Thread[] create(Runnable[] rs) {
		Thread[] tmp = new Thread[rs.length];
		for (int i = 0; i < rs.length; i++) {
			tmp[i] = new Thread(rs[i], "WORKER##" + i);
		}
		return tmp;
	}

	public static Thread[] start(Thread[] rs) {
		for (int i = 0; i < rs.length; i++) {
			rs[i].start();
		}
		return rs;
	}

	public static void stop(Thread[] workers) {
		for (int i = 0; i < workers.length; i++) {
			workers[i].interrupt();
		}
	}

	public static void pause(long millis) {
		try {
			Thread.currentThread().sleep(millis);
		} catch (InterruptedException e) {
			System.err.println(Thread.currentThread().getName()
					+ " awaked abruptally!");
		}
	}

}// END
