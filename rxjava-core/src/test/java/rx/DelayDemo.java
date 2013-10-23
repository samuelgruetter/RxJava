package rx;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import rx.util.functions.Action1;

//@Ignore // since this doesn't do any automatic testing
public class DelayDemo {
	
	@Test
	public void test1() throws Exception {
		System.out.println("hello");
		Observable.interval(1000, TimeUnit.SECONDS).take(3).delay(3000, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
			public void call(Long l) {
				System.out.println(l);				
			}
		});
		
		Thread.sleep(7000);
		System.out.println("done");
	}
	
}
