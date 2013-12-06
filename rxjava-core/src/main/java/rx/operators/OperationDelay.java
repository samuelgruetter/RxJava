/**
 * Copyright 2013 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.operators;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.concurrency.TestScheduler;
import rx.observables.ConnectableObservable;
import rx.util.functions.Func1;

public final class OperationDelay {

	public static <T> Observable<T> delayOrig(Observable<T> observable, final long delay, final TimeUnit unit, final Scheduler scheduler) {
        // observable.map(x => Observable.timer(t).map(_ => x).startItAlreadyNow()).concat()
    	Observable<Observable<T>> seqs = observable.map(new Func1<T, Observable<T>>() {
			public Observable<T> call(final T x) {
				ConnectableObservable<T> co = Observable.timer(delay, unit, scheduler).map(new Func1<Void, T>() {
					public T call(Void ignored) {
						return x;
					}
				}).replay();
				co.connect();
				return co;
			}
		});
    	return Observable.concat(seqs);
    }
    
	public static <T> Observable<T> delay(Observable<T> observable, final long delay, final TimeUnit unit, final Scheduler scheduler) {
        // observable.map(x => Observable.timer(t).map(_ => x).startItAlreadyNow()).concat()
    	Observable<Observable<T>> seqs = observable.map(new Func1<T, Observable<T>>() {
			public Observable<T> call(final T x) {
				System.out.println("map1");
				ConnectableObservable<T> co = Observable.timer(delay, unit, scheduler).map(new Func1<Void, T>() {
					public T call(Void ignored) {
						System.out.println("map2");
						return x;
					}
				}).replay();
				co.connect();
				return co;
			}
		});
    	return Observable.concat(seqs);
    }
    
	
    // copied from
    // https://github.com/jmhofer/RxJava/blob/e9027293dadf846b64f62e91da7c5c5850ed76f5/rxjava-core/src/main/java/rx/operators/OperationDelay.java
    // @Ignore
    public static class UnitTest {
        @Mock
        private Observer<Long> observer;
        @Mock
        private Observer<Long> observer2;

        private TestScheduler scheduler;

        @Before
        public void before() {
            initMocks(this);
            scheduler = new TestScheduler();
        }
        
        @Test
        public void testDelay() {
            Observable<Long> source = Observable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
            Observable<Long> delayed = OperationDelay.delay(source, 500L, TimeUnit.MILLISECONDS, scheduler);
            delayed.subscribe(observer);
            
            InOrder inOrder = inOrder(observer);
            scheduler.advanceTimeTo(1499L, TimeUnit.MILLISECONDS);
            verify(observer, never()).onNext(anyLong());
            verify(observer, never()).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));
            
            scheduler.advanceTimeTo(1500L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onNext(0L);
            inOrder.verify(observer, never()).onNext(anyLong());
            verify(observer, never()).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));

            scheduler.advanceTimeTo(2400L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, never()).onNext(anyLong());
            verify(observer, never()).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));
            
            scheduler.advanceTimeTo(2500L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onNext(1L);
            inOrder.verify(observer, never()).onNext(anyLong());
            verify(observer, never()).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));
            
            scheduler.advanceTimeTo(3400L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, never()).onNext(anyLong());
            verify(observer, never()).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));

            scheduler.advanceTimeTo(3500L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onNext(2L);
            verify(observer, times(1)).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));
        }

        @Test
        public void testLongDelay() {
            Observable<Long> source = Observable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
            Observable<Long> delayed = OperationDelay.delay(source, 5L, TimeUnit.SECONDS, scheduler);
            delayed.subscribe(observer);
            
            InOrder inOrder = inOrder(observer);
            
            scheduler.advanceTimeTo(5999L, TimeUnit.MILLISECONDS);
            verify(observer, never()).onNext(anyLong());
            verify(observer, never()).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));
            
            scheduler.advanceTimeTo(6000L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onNext(0L);
            scheduler.advanceTimeTo(6999L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, never()).onNext(anyLong());
            scheduler.advanceTimeTo(7000L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onNext(1L);
            scheduler.advanceTimeTo(7999L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, never()).onNext(anyLong());
            scheduler.advanceTimeTo(8000L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onNext(2L);
            inOrder.verify(observer, times(1)).onCompleted();
            inOrder.verify(observer, never()).onNext(anyLong());
            inOrder.verify(observer, never()).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));
        }
        
        @Test
        public void testDelayWithError() {
            Observable<Long> source = Observable.interval(1L, TimeUnit.SECONDS, scheduler).map(new Func1<Long, Long>() {
                @Override
                public Long call(Long value) {
                    if (value == 1L) {
                        throw new RuntimeException("error!");
                    }
                    return value; 
                }
            });
            Observable<Long> delayed = OperationDelay.delay(source, 1L, TimeUnit.SECONDS, scheduler);
            delayed.subscribe(observer);

            InOrder inOrder = inOrder(observer);
            
            scheduler.advanceTimeTo(1999L, TimeUnit.MILLISECONDS);
            verify(observer, never()).onNext(anyLong());
            verify(observer, never()).onCompleted();
            verify(observer, never()).onError(any(Throwable.class));
            
            scheduler.advanceTimeTo(2000L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onError(any(Throwable.class));
            inOrder.verify(observer, never()).onNext(anyLong());
            verify(observer, never()).onCompleted();
            
            scheduler.advanceTimeTo(5000L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, never()).onNext(anyLong());
            inOrder.verify(observer, never()).onError(any(Throwable.class));
            verify(observer, never()).onCompleted();
        }
        
        // TODO activate this test once https://github.com/Netflix/RxJava/issues/552 is fixed
        @Ignore
        @Test
        public void testDelayWithMultipleSubscriptions() {
            Observable<Long> source = Observable.interval(1L, TimeUnit.SECONDS, scheduler).take(3);
            Observable<Long> delayed = OperationDelay.delay(source, 500L, TimeUnit.MILLISECONDS, scheduler);
            delayed.subscribe(observer);
            delayed.subscribe(observer2);
            
            InOrder inOrder = inOrder(observer);
            InOrder inOrder2 = inOrder(observer2);
            
            scheduler.advanceTimeTo(1499L, TimeUnit.MILLISECONDS);
            verify(observer, never()).onNext(anyLong());
            verify(observer2, never()).onNext(anyLong());

            scheduler.advanceTimeTo(1500L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onNext(0L);
            inOrder2.verify(observer2, times(1)).onNext(0L);

            scheduler.advanceTimeTo(2499L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, never()).onNext(anyLong());
            inOrder2.verify(observer2, never()).onNext(anyLong());
            
            scheduler.advanceTimeTo(2500L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onNext(1L);
            inOrder2.verify(observer2, times(1)).onNext(1L);

            verify(observer, never()).onCompleted();
            verify(observer2, never()).onCompleted();

            scheduler.advanceTimeTo(3500L, TimeUnit.MILLISECONDS);
            inOrder.verify(observer, times(1)).onNext(2L);
            inOrder2.verify(observer2, times(1)).onNext(2L);
            inOrder.verify(observer, never()).onNext(anyLong());
            inOrder2.verify(observer2, never()).onNext(anyLong());
            inOrder.verify(observer, times(1)).onCompleted();
            inOrder2.verify(observer2, times(1)).onCompleted();

            verify(observer, never()).onError(any(Throwable.class));
            verify(observer2, never()).onError(any(Throwable.class));
        }
    }

}
