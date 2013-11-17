package rx;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import rx.observables.ConnectableObservable;
import rx.subjects.BehaviorSubject;
import rx.util.functions.Action0;
import rx.util.functions.Action1;

/**
 * (Playground)
 *
 */
//@Ignore // since this doesn't do any automatic testing
public class DelayDemo {
	
	long t0;
	
	@Before
	public void init() {
		t0 = System.currentTimeMillis();
	}
	
	String time() {
		return System.currentTimeMillis() - t0 + ": ";
	}
	
	@Test
	public void test1() throws Exception {
		System.out.println("hi");
		Observable.timer(1, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
			public void call(Long l) {
				System.out.println(l);				
			}
		});
		
		Thread.sleep(2000);
		System.out.println("done");
	}
	
	@Test
	public void test1b() throws Exception {
		System.out.println("hi1b");
		Observable.timer(1, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
			public void call(Long l) {
				System.out.println("A");				
			}
		});
		Observable.timer(1, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
			public void call(Long l) {
				System.out.println("B");				
			}
		});
		
		Thread.sleep(2000);
		System.out.println("done");
	}
	
	@Test
	public void test2() throws Exception {
		System.out.println("hi2");
		Observable.timer(1, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
			public void call(Long l) {
				System.out.println(l);				
			}
		});
		
		Thread.sleep(2000);
		System.out.println("done");
	}
	
	@Test
	public void test3() throws Exception {
		System.out.println("hi3" + time());
		Observable<Long> o = Observable.timer(2, TimeUnit.SECONDS).cache();
		Thread.sleep(1000);
		System.out.println("going to subscribe" + time());
		o.subscribe(new Action1<Long>() {
			public void call(Long l) {
				System.out.println(l + time());
			}
		});
		Thread.sleep(2000);
		System.out.println("done" + time());
	}

	@Test
	public void test4() throws Exception {
		// When cache replays its observable to a subscriber, it does so with the same timing as it
		// received the items, but the start time is shifted to the moment when the subscriber subscribes.
		// This actually makes sense.
		// Before I thought that when a subscriber subscribes to the cached observable, it gets a burst
		// of all cached items and the future items at the same time as they are emitted by the original
		// observable (no time shift), but this turned out to be wrong.
		Observable<Long> o = Observable.interval(1, TimeUnit.SECONDS).take(3).cache();
		System.out.println(time() + "created and cached" );
		Thread.sleep(2000);
		o.subscribe(new Action1<Long>() {
			public void call(Long l) {
				System.out.println(time() + l);
			}
		});
		System.out.println(time() + "subscribed");
		Thread.sleep(5000);
		System.out.println("done" + time());
	}

	@Test
	public void test5() throws Exception {
		Observable.interval(1000, TimeUnit.MILLISECONDS).take(6).window(500, TimeUnit.MILLISECONDS).subscribe(new Action1<Observable<Long>>() {
			public void call(Observable<Long> o) {
				System.out.println(time());
			}
		});
		Thread.sleep(7000);
	}
	
	@Test
	public void test5b() throws Exception {
		Observable<Observable<Long>> o = Observable.<Long>never().window(500, TimeUnit.MILLISECONDS);
		
		o.subscribe(new Action1<Observable<Long>>() {
			public void call(Observable<Long> o0) {
				System.out.println(time());
			}
		});
		Thread.sleep(7000);
	}
	
	@Test
	public void test5c() throws Exception {
		Observable<List<Integer>> o = Observable.from(1, 2, 3).buffer(2, 1);
		
		o.subscribe(new Action1<List<Integer>>() {
			public void call(List<Integer> l) {
				System.out.println(l + " " + l.size());
			}
		});
	}

	@Test
	public void test5d() throws Exception {
		Observable<List<Long>> o = Observable.interval(1000, TimeUnit.MILLISECONDS).buffer(500, TimeUnit.MILLISECONDS);
		Thread.sleep(7000);
		o.subscribe(new Action1<List<Long>>() {
			public void call(List<Long> l) {
				System.out.println(l);
			}
		});
		Thread.sleep(7000);
	}
	
	@Test
	public void test6() throws Exception {
		Observable<Long> o = Observable.interval(1, TimeUnit.SECONDS).take(2);
		System.out.println(time() + "created and cached" );
		Thread.sleep(2000);
		o.subscribe(new Action1<Long>() {
			public void call(Long l) {
				System.out.println(time() + l);
			}
		});
		System.out.println(time() + "subscribed");
		Thread.sleep(5000);
		System.out.println("done" + time());
	}
	

	@Test
	public void test7() throws Exception {
		Observable<Integer> o = Observable.from(1, 2, 3);
		BehaviorSubject<Integer> subject = BehaviorSubject.createWithDefaultValue(0);
		o.subscribe(subject);
		subject.subscribe(
			new Action1<Integer>() { public void call(Integer i) {
				System.out.println(i);
			}},
			new Action1<Throwable>() { public void call(Throwable t) {
				t.printStackTrace();
			}},
			new Action0() { public void call() {
				System.out.println("complete");
			}}
		);
		System.out.println("done");
	}
	
	@Test
	public void testN() throws Exception {
		System.out.println("hello");
		Observable.interval(1, TimeUnit.SECONDS).take(3).delay(2, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
			public void call(Long l) {
				System.out.println(l);				
			}
		});
		
		Thread.sleep(7000);
		System.out.println("done");
	}
	
}
