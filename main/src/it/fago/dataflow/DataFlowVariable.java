package it.fago.dataflow;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * 
 * @author Stefano Fago
 * 
 * @param <T>
 */
public class DataFlowVariable<T> {

	/**
	 * 
	 * @author Stefano Fago
	 * 
	 */
	public static class Node {
		//
		public final Node previous;
		//
		private Thread waiter;

		/**
		 * 
		 * @param previous
		 */
		public Node(Node previous) {
			this.previous = previous;
		}

		public void waitMe() {
			waiter = Thread.currentThread();
			LockSupport.park(this);
		}

		public void notifyMe() {
			LockSupport.unpark(waiter);
		}

	} // END

	//
	private AtomicReference<T> value = new AtomicReference<T>();
	//
	private AtomicReference<Node> waitersHead = new AtomicReference<Node>(
			new Node(null));

	/**
	 * 
	 * @param data
	 */
	public void set(T data) {
		if (CASValueFor(data)) {
			reconciliateWaiters();
			return;
		}
		throw new RuntimeException("Already Assigned!");
	}

	/**
	 * 
	 * @return
	 */
	public T get() {
		if (value.get() != null) {
			return value.get();
		}
		Node n = defineNewWaitersHead();
		n.waitMe();
		return value.get();
	}

	// ============================================================
	//
	// ============================================================

	private final boolean CASValueFor(T data) {
		return value.compareAndSet(null, data);
	}

	private final Node defineNewWaitersHead() {
		Node head = waitersHead.get();
		Node n = new Node(head);
		while (!waitersHead.compareAndSet(head, n)) {
			head = waitersHead.get();
			n = new Node(head);
		}
		return waitersHead.get();
	}

	private final void reconciliateWaiters() {
		Node head = waitersHead.get();
		while (!waitersHead.compareAndSet(waitersHead.get(), head)) {
			head = waitersHead.get();
		}
		Node cursor = head;
		while (cursor != null) {
			cursor.notifyMe();
			cursor = cursor.previous;
		}
	}

	// ---------------------------------------------------------------------------
	//
	// ---------------------------------------------------------------------------

}// END