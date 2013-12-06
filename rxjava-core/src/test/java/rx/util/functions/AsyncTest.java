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

package rx.util.functions;

import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;
import rx.Observer;
import rx.concurrency.Schedulers;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Action2;
import rx.util.functions.Action3;
import rx.util.functions.Action4;
import rx.util.functions.Action5;
import rx.util.functions.Action6;
import rx.util.functions.Action7;
import rx.util.functions.Action8;
import rx.util.functions.Action9;
import rx.util.functions.ActionN;
import rx.util.functions.Func0;
import rx.util.functions.Func1;
import rx.util.functions.Func2;
import rx.util.functions.Func3;
import rx.util.functions.Func4;
import rx.util.functions.Func5;
import rx.util.functions.Func6;
import rx.util.functions.Func7;
import rx.util.functions.Func8;
import rx.util.functions.Func9;
import rx.util.functions.FuncN;

public class AsyncTest {
    @Mock
    Observer<Object> observer;
    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }
    @Test
    public void testAction0() {
        final AtomicInteger value = new AtomicInteger();
        Action0 action = new Action0() {
            @Override
            public void call() {
                value.incrementAndGet();
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call()
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(1, value.get());
    }
    @Test
    public void testAction0Error() {
        Action0 action = new Action0() {
            @Override
            public void call() {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call()
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testAction1() {
        final AtomicInteger value = new AtomicInteger();
        Action1<Integer> action = new Action1<Integer>() {
            @Override
            public void call(Integer t1) {
                value.set(t1);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(1, value.get());
    }
    @Test
    public void testAction1Error() {
        Action1<Integer> action = new Action1<Integer>() {
            @Override
            public void call(Integer t1) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testAction2() {
        final AtomicInteger value = new AtomicInteger();
        Action2<Integer, Integer> action = new Action2<Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2) {
                value.set(t1 | t2);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(3, value.get());
    }
    @Test
    public void testAction2Error() {
        Action2<Integer, Integer> action = new Action2<Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testAction3() {
        final AtomicInteger value = new AtomicInteger();
        Action3<Integer, Integer, Integer> action = new Action3<Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3) {
                value.set(t1 | t2 | t3);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(7, value.get());
    }
    @Test
    public void testAction3Error() {
        Action3<Integer, Integer, Integer> action = new Action3<Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testAction4() {
        final AtomicInteger value = new AtomicInteger();
        Action4<Integer, Integer, Integer, Integer> action = new Action4<Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4) {
                value.set(t1 | t2 | t3 | t4);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(15, value.get());
    }
    @Test
    public void testAction4Error() {
        Action4<Integer, Integer, Integer, Integer> action = new Action4<Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testAction5() {
        final AtomicInteger value = new AtomicInteger();
        Action5<Integer, Integer, Integer, Integer, Integer> action = new Action5<Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5) {
                value.set(t1 | t2 | t3 | t4 | t5);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(31, value.get());
    }
    @Test
    public void testAction5Error() {
        Action5<Integer, Integer, Integer, Integer, Integer> action = new Action5<Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testAction6() {
        final AtomicInteger value = new AtomicInteger();
        Action6<Integer, Integer, Integer, Integer, Integer, Integer> action = new Action6<Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6) {
                value.set(t1 | t2 | t3 | t4 | t5 | t6);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(63, value.get());
    }
    @Test
    public void testAction6Error() {
        Action6<Integer, Integer, Integer, Integer, Integer, Integer> action = new Action6<Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testAction7() {
        final AtomicInteger value = new AtomicInteger();
        Action7<Integer, Integer, Integer, Integer, Integer, Integer, Integer> action = new Action7<Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7) {
                value.set(t1 | t2 | t3 | t4 | t5 | t6 | t7);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(127, value.get());
    }
    @Test
    public void testAction7Error() {
        Action7<Integer, Integer, Integer, Integer, Integer, Integer, Integer> action = new Action7<Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testAction8() {
        final AtomicInteger value = new AtomicInteger();
        Action8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> action = new Action8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7, Integer t8) {
                value.set(t1 | t2 | t3 | t4 | t5 | t6 | t7 | t8);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64, 128)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(255, value.get());
    }
    @Test
    public void testAction8Error() {
        Action8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> action = new Action8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7, Integer t8) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64, 128)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testAction9() {
        final AtomicInteger value = new AtomicInteger();
        Action9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> action = new Action9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7, Integer t8, Integer t9) {
                value.set(t1 | t2 | t3 | t4 | t5 | t6 | t7 | t8 | t9);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64, 128, 256)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(511, value.get());
    }
    @Test
    public void testAction9Error() {
        Action9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> action = new Action9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public void call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7, Integer t8, Integer t9) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64, 128, 256)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testActionN() {
        final AtomicInteger value = new AtomicInteger();
        ActionN action = new ActionN() {
            @Override
            public void call(Object... args) {
                int i = 0;
                for (Object o : args) {
                    i = i | (Integer)o;
                }
                value.set(i);
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64, 128, 256, 512)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(null);
        verify(observer, times(1)).onCompleted();
        
        Assert.assertEquals(1023, value.get());
    }
    @Test
    public void testActionNError() {
        ActionN action = new ActionN() {
            @Override
            public void call(Object... args) {
                throw new RuntimeException("Forced failure");
            }
        };
        
        Async.toAsync(action, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64, 128, 256, 512)
                .subscribe(observer);
        
        verify(observer, times(1)).onError(any(Throwable.class));
        verify(observer, never()).onNext(null);
        verify(observer, never()).onCompleted();
    }
    @Test
    public void testFunc0() {
        Func0<Integer> func = new Func0<Integer>() {
            @Override
            public Integer call() {
                return 0;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call()
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(0);
        verify(observer, times(1)).onCompleted();
        
    }
    @Test
    public void testFunc1() {
        Func1<Integer, Integer> func = new Func1<Integer, Integer>() {
            @Override
            public Integer call(Integer t1) {
                return t1;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(1);
        verify(observer, times(1)).onCompleted();
    }
    @Test
    public void testFunc2() {
        Func2<Integer, Integer, Integer> func = new Func2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer t1, Integer t2) {
                return t1 | t2;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1, 2)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(3);
        verify(observer, times(1)).onCompleted();
    }
    @Test
    public void testFunc3() {
        Func3<Integer, Integer, Integer, Integer> func = new Func3<Integer, Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer t1, Integer t2, Integer t3) {
                return t1 | t2 | t3;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1, 2, 4)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(7);
        verify(observer, times(1)).onCompleted();
    }
    @Test
    public void testFunc4() {
        Func4<Integer, Integer, Integer, Integer, Integer> func = new Func4<Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer t1, Integer t2, Integer t3, Integer t4) {
                return t1 | t2 | t3 | t4;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1, 2, 4, 8)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(15);
        verify(observer, times(1)).onCompleted();
    }
    @Test
    public void testFunc5() {
        Func5<Integer, Integer, Integer, Integer, Integer, Integer> func = new Func5<Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5) {
                return t1 | t2 | t3 | t4 | t5;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1, 2, 4, 8, 16)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(31);
        verify(observer, times(1)).onCompleted();
    }
    @Test
    public void testFunc6() {
        Func6<Integer, Integer, Integer, Integer, Integer, Integer, Integer> func = new Func6<Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6) {
                return t1 | t2 | t3 | t4 | t5 | t6;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(63);
        verify(observer, times(1)).onCompleted();
    }
    @Test
    public void testFunc7() {
        Func7<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> func = new Func7<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7) {
                return t1 | t2 | t3 | t4 | t5 | t6 | t7;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(127);
        verify(observer, times(1)).onCompleted();
    }
    @Test
    public void testFunc8() {
        Func8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> func = new Func8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7, Integer t8) {
                return t1 | t2 | t3 | t4 | t5 | t6 | t7 | t8;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64, 128)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(255);
        verify(observer, times(1)).onCompleted();
    }
    @Test
    public void testFunc9() {
        Func9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> func = new Func9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer t1, Integer t2, Integer t3, Integer t4, Integer t5, Integer t6, Integer t7, Integer t8, Integer t9) {
                return t1 | t2 | t3 | t4 | t5 | t6 | t7 | t8 | t9;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64, 128, 256)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(511);
        verify(observer, times(1)).onCompleted();
    }
    @Test
    public void testFuncN() {
        FuncN<Integer> func = new FuncN<Integer>() {
            @Override
            public Integer call(Object... args) {
                int i = 0;
                for (Object o : args) {
                    i = i | (Integer)o;
                }
                return i;
            }
        };
        Async.toAsync(func, Schedulers.immediate())
                .call(1, 2, 4, 8, 16, 32, 64, 128, 256, 512)
                .subscribe(observer);
        
        verify(observer, never()).onError(any(Throwable.class));
        verify(observer, times(1)).onNext(1023);
        verify(observer, times(1)).onCompleted();
    }
}
