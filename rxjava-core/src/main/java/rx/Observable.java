/**
 * Copyright 2013 Netflix, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package rx;

import static rx.util.functions.Functions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import rx.joins.Pattern2;
import rx.joins.Plan0;
import rx.observables.BlockingObservable;
import rx.observables.ConnectableObservable;
import rx.observables.GroupedObservable;
import rx.operators.OperationAll;
import rx.operators.OperationAmb;
import rx.operators.OperationAny;
import rx.operators.OperationAsObservable;
import rx.operators.OperationAverage;
import rx.operators.OperationBuffer;
import rx.operators.OperationCache;
import rx.operators.OperationCast;
import rx.operators.OperationCombineLatest;
import rx.operators.OperationConcat;
import rx.operators.OperationDebounce;
import rx.operators.OperationDefaultIfEmpty;
import rx.operators.OperationDefer;
import rx.operators.OperationDelay;
import rx.operators.OperationDematerialize;
import rx.operators.OperationDistinct;
import rx.operators.OperationDistinctUntilChanged;
import rx.operators.OperationDoOnEach;
import rx.operators.OperationElementAt;
import rx.operators.OperationFilter;
import rx.operators.OperationFinally;
import rx.operators.OperationGroupBy;
import rx.operators.OperationGroupByUntil;
import rx.operators.OperationGroupJoin;
import rx.operators.OperationInterval;
import rx.operators.OperationJoin;
import rx.operators.OperationJoinPatterns;
import rx.operators.OperationMap;
import rx.operators.OperationMaterialize;
import rx.operators.OperationMerge;
import rx.operators.OperationMergeDelayError;
import rx.operators.OperationMinMax;
import rx.operators.OperationMulticast;
import rx.operators.OperationObserveOn;
import rx.operators.OperationOnErrorResumeNextViaFunction;
import rx.operators.OperationOnErrorResumeNextViaObservable;
import rx.operators.OperationOnErrorReturn;
import rx.operators.OperationOnExceptionResumeNextViaObservable;
import rx.operators.OperationParallel;
import rx.operators.OperationParallelMerge;
import rx.operators.OperationRepeat;
import rx.operators.OperationReplay;
import rx.operators.OperationRetry;
import rx.operators.OperationSample;
import rx.operators.OperationScan;
import rx.operators.OperationSequenceEqual;
import rx.operators.OperationSingle;
import rx.operators.OperationSkip;
import rx.operators.OperationSkipLast;
import rx.operators.OperationSkipUntil;
import rx.operators.OperationSkipWhile;
import rx.operators.OperationSubscribeOn;
import rx.operators.OperationSum;
import rx.operators.OperationSwitch;
import rx.operators.OperationSynchronize;
import rx.operators.OperationTake;
import rx.operators.OperationTakeLast;
import rx.operators.OperationTakeUntil;
import rx.operators.OperationTakeWhile;
import rx.operators.OperationThrottleFirst;
import rx.operators.OperationTimeInterval;
import rx.operators.OperationTimeout;
import rx.operators.OperationTimer;
import rx.operators.OperationTimestamp;
import rx.operators.OperationToMap;
import rx.operators.OperationToMultimap;
import rx.operators.OperationToObservableFuture;
import rx.operators.OperationToObservableIterable;
import rx.operators.OperationToObservableList;
import rx.operators.OperationToObservableSortedList;
import rx.operators.OperationUsing;
import rx.operators.OperationWindow;
import rx.operators.OperationZip;
import rx.operators.SafeObservableSubscription;
import rx.operators.SafeObserver;
import rx.plugins.RxJavaErrorHandler;
import rx.plugins.RxJavaObservableExecutionHook;
import rx.plugins.RxJavaPlugins;
import rx.schedulers.Schedulers;
import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;
import rx.subscriptions.Subscriptions;
import rx.util.OnErrorNotImplementedException;
import rx.util.Range;
import rx.util.TimeInterval;
import rx.util.Timestamped;
import rx.util.functions.Action0;
import rx.util.functions.Action1;
import rx.util.functions.Action2;
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
import rx.util.functions.Function;
import rx.util.functions.Functions;

/**
 * The Observable interface that implements the Reactive Pattern.
 * <p>
 * This interface provides overloaded methods for subscribing as well as
 * delegate methods to the various operators.
 * <p>
 * The documentation for this interface makes use of marble diagrams. The
 * following legend explains these diagrams:
 * <p>
 * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/legend.png">
 * <p>
 * For more information see the
 * <a href="https://github.com/Netflix/RxJava/wiki/Observable">RxJava Wiki</a>
 * 
 * @param <T> the type of the item emitted by the Observable
 */
public class Observable<T> {

    private final static ConcurrentHashMap<Class, Boolean> internalClassMap = new ConcurrentHashMap<Class, Boolean>();

    /**
     * Executed when 'subscribe' is invoked.
     */
    private final OnSubscribeFunc<T> onSubscribe;

    /**
     * Function interface for work to be performed when an {@link Observable}
     * is subscribed to via {@link Observable#subscribe(Observer)}
     * 
     * @param <T>
     */
    public static interface OnSubscribeFunc<T> extends Function {

        public Subscription onSubscribe(Observer<? super T> t1);

    }

    /**
     * Observable with Function to execute when subscribed to.
     * <p>
     * NOTE: Use {@link #create(OnSubscribeFunc)} to create an Observable
     * instead of this constructor unless you specifically have a need for
     * inheritance.
     * 
     * @param onSubscribe {@link OnSubscribeFunc} to be executed when
     *                    {@link #subscribe(Observer)} is called
     */
    protected Observable(OnSubscribeFunc<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    private final static RxJavaObservableExecutionHook hook = RxJavaPlugins.getInstance().getObservableExecutionHook();

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in
     * order to receive items and notifications from the Observable.
     * <p>
     * A typical implementation of {@code subscribe} does the following:
     * <ol>
     * <li>It stores a reference to the Observer in a collection object, such as
     *     a {@code List<T>} object.</li>
     * <li>It returns a reference to the {@link Subscription} interface. This
     *     enables Observers to unsubscribe, that is, to stop receiving items
     *     and notifications before the Observable stops sending them, which
     *     also invokes the Observer's {@link Observer#onCompleted onCompleted}
     *     method.</li>
     * </ol><p>
     * An <code>Observable&lt;T&gt;</code> instance is responsible for accepting
     * all subscriptions and notifying all Observers. Unless the documentation
     * for a particular <code>Observable&lt;T&gt;</code> implementation
     * indicates otherwise, Observers should make no assumptions about the order
     * in which multiple Observers will receive their notifications.
     * <p>
     * For more information see the
     * <a href="https://github.com/Netflix/RxJava/wiki/Observable">RxJava Wiki</a>
     * 
     * @param observer the Observer
     * @return a {@link Subscription} reference with which the {@link Observer}
     *         can stop receiving items before the Observable has finished
     *         sending them
     * @throws IllegalArgumentException if the {@link Observer} provided as the
     *                                  argument to {@code subscribe()} is
     *                                  {@code null}
     */
    public Subscription subscribe(Observer<? super T> observer) {
        // allow the hook to intercept and/or decorate
        OnSubscribeFunc<T> onSubscribeFunction = hook.onSubscribeStart(this, onSubscribe);
        // validate and proceed
        if (observer == null) {
            throw new IllegalArgumentException("observer can not be null");
        }
        if (onSubscribeFunction == null) {
            throw new IllegalStateException("onSubscribe function can not be null.");
            // the subscribe function can also be overridden but generally that's not the appropriate approach so I won't mention that in the exception
        }
        try {
            /**
             * See https://github.com/Netflix/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls to user code from within an operator"
             */
            if (isInternalImplementation(observer)) {
                Subscription s = onSubscribeFunction.onSubscribe(observer);
                if (s == null) {
                    // this generally shouldn't be the case on a 'trusted' onSubscribe but in case it happens
                    // we want to gracefully handle it the same as AtomicObservableSubscription does
                    return hook.onSubscribeReturn(this, Subscriptions.empty());
                } else {
                    return hook.onSubscribeReturn(this, s);
                }
            } else {
                SafeObservableSubscription subscription = new SafeObservableSubscription();
                subscription.wrap(onSubscribeFunction.onSubscribe(new SafeObserver<T>(subscription, observer)));
                return hook.onSubscribeReturn(this, subscription);
            }
        } catch (OnErrorNotImplementedException e) {
            // special handling when onError is not implemented ... we just rethrow
            throw e;
        } catch (Throwable e) {
            // if an unhandled error occurs executing the onSubscribe we will propagate it
            try {
                observer.onError(hook.onSubscribeError(this, e));
            } catch (OnErrorNotImplementedException e2) {
                // special handling when onError is not implemented ... we just rethrow
                throw e2;
            } catch (Throwable e2) {
                // if this happens it means the onError itself failed (perhaps an invalid function implementation)
                // so we are unable to propagate the error correctly and will just throw
                RuntimeException r = new RuntimeException("Error occurred attempting to subscribe [" + e.getMessage() + "] and then again while trying to pass to onError.", e2);
                hook.onSubscribeError(this, r);
                throw r;
            }
            return Subscriptions.empty();
        }
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in
     * order to receive items and notifications from the Observable.
     * <p>
     * A typical implementation of {@code subscribe} does the following:
     * <ol>
     * <li>It stores a reference to the Observer in a collection object, such as
     *     a {@code List<T>} object.</li>
     * <li>It returns a reference to the {@link Subscription} interface. This
     *     enables Observers to unsubscribe, that is, to stop receiving items
     *     and notifications before the Observable stops sending them, which
     *     also invokes the Observer's {@link Observer#onCompleted onCompleted}
     *     method.</li>
     * </ol><p>
     * An {@code Observable<T>} instance is responsible for accepting all
     * subscriptions and notifying all Observers. Unless the documentation for a
     * particular {@code Observable<T>} implementation indicates otherwise,
     * Observers should make no assumptions about the order in which multiple
     * Observers will receive their notifications.
     * <p>
     * For more information see the
     * <a href="https://github.com/Netflix/RxJava/wiki/Observable">RxJava Wiki</a>
     * 
     * @param observer the Observer
     * @param scheduler the {@link Scheduler} on which Observers subscribe to
     *                  the Observable
     * @return a {@link Subscription} reference with which Observers can stop
     *         receiving items and notifications before the Observable has
     *         finished sending them
     * @throws IllegalArgumentException if an argument to {@code subscribe()}
     *                                  is {@code null}
     */
    public Subscription subscribe(Observer<? super T> observer, Scheduler scheduler) {
        return subscribeOn(scheduler).subscribe(observer);
    }

    /**
     * Protects against errors being thrown from Observer implementations and
     * ensures onNext/onError/onCompleted contract compliance.
     * <p>
     * See https://github.com/Netflix/RxJava/issues/216 for a discussion on
     * "Guideline 6.4: Protect calls to user code from within an operator"
     */
    private Subscription protectivelyWrapAndSubscribe(Observer<? super T> o) {
        SafeObservableSubscription subscription = new SafeObservableSubscription();
        return subscription.wrap(subscribe(new SafeObserver<T>(subscription, o)));
    }

    /**
     * Subscribe and ignore all events.
     *  
     * @return 
     */
    public Subscription subscribe() {
        return protectivelyWrapAndSubscribe(new Observer<T>() {

            @Override
            public void onCompleted() {
                // do nothing
            }

            @Override
            public void onError(Throwable e) {
                throw new OnErrorNotImplementedException(e);
            }

            @Override
            public void onNext(T args) {
                // do nothing
            }

        });
    }
    
    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method
     * in order to receive items and notifications from the Observable.
     * 
     * @param onNext
     * @return 
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public Subscription subscribe(final Action1<? super T> onNext) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }

        /**
         * Wrapping since raw functions provided by the user are being invoked.
         * 
         * See https://github.com/Netflix/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls to user code from within an operator"
         */
        return protectivelyWrapAndSubscribe(new Observer<T>() {

            @Override
            public void onCompleted() {
                // do nothing
            }

            @Override
            public void onError(Throwable e) {
                throw new OnErrorNotImplementedException(e);
            }

            @Override
            public void onNext(T args) {
                onNext.call(args);
            }

        });
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in
     * order to receive items and notifications from the Observable.
     * 
     * @param onNext
     * @param scheduler
     * @return
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public Subscription subscribe(final Action1<? super T> onNext, Scheduler scheduler) {
        return subscribeOn(scheduler).subscribe(onNext);
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in
     * order to receive items and notifications from the Observable.
     * 
     * @param onNext
     * @param onError
     * @return
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        }

        /**
         * Wrapping since raw functions provided by the user are being invoked.
         * 
         * See https://github.com/Netflix/RxJava/issues/216 for discussion on
         * "Guideline 6.4: Protect calls to user code from within an operator"
         */
        return protectivelyWrapAndSubscribe(new Observer<T>() {

            @Override
            public void onCompleted() {
                // do nothing
            }

            @Override
            public void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public void onNext(T args) {
                onNext.call(args);
            }

        });
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in
     * order to receive items and notifications from the Observable.
     * 
     * @param onNext
     * @param onError
     * @param scheduler
     * @return
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, Scheduler scheduler) {
        return subscribeOn(scheduler).subscribe(onNext, onError);
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in
     * order to receive items and notifications from the Observable.
     * 
     * @param onNext
     * @param onError
     * @param onComplete
     * @return
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete) {
        if (onNext == null) {
            throw new IllegalArgumentException("onNext can not be null");
        }
        if (onError == null) {
            throw new IllegalArgumentException("onError can not be null");
        }
        if (onComplete == null) {
            throw new IllegalArgumentException("onComplete can not be null");
        }

        /**
         * Wrapping since raw functions provided by the user are being invoked.
         * 
         * See https://github.com/Netflix/RxJava/issues/216 for discussion on "Guideline 6.4: Protect calls to user code from within an operator"
         */
        return protectivelyWrapAndSubscribe(new Observer<T>() {

            @Override
            public void onCompleted() {
                onComplete.call();
            }

            @Override
            public void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public void onNext(T args) {
                onNext.call(args);
            }

        });
    }

    /**
     * An {@link Observer} must call an Observable's {@code subscribe} method in
     * order to receive items and notifications from the Observable.
     * 
     * @param onNext
     * @param onError
     * @param onComplete
     * @param scheduler
     * @return
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable#onnext-oncompleted-and-onerror">RxJava Wiki: onNext, onCompleted, and onError</a>
     */
    public Subscription subscribe(final Action1<? super T> onNext, final Action1<Throwable> onError, final Action0 onComplete, Scheduler scheduler) {
        return subscribeOn(scheduler).subscribe(onNext, onError, onComplete);
    }

    /**
      * Hides the identity of this observable.
      * @return an Observable hiding the identity of this Observable.
      */
     public Observable<T> asObservable() {
         return create(new OperationAsObservable<T>(this));
     }

    /**
     * Returns a {@link ConnectableObservable} that upon connection causes the
     * source Observable to push results into the specified subject.
     * 
     * @param subject the {@link Subject} for the {@link ConnectableObservable}
     *                to push source items into
     * @param <R> result type
     * @return a {@link ConnectableObservable} that upon connection causes the
     *         source Observable to push results into the specified
     *         {@link Subject}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablepublish-and-observablemulticast">RxJava Wiki: Observable.publish() and Observable.multicast()</a>
     */
    public <R> ConnectableObservable<R> multicast(Subject<? super T, ? extends R> subject) {
        return OperationMulticast.multicast(this, subject);
    }

    /**
     * Returns an observable sequence that contains the elements of a sequence 
     * produced by multicasting the source sequence within a selector function.
     * 
     * @param subjectFactory the subject factory
     * @param selector the selector function which can use the multicasted 
     *                 source sequence subject to the policies enforced by the 
     *                 created subject
     * @return the Observable sequence that contains the elements of a sequence
     *         produced by multicasting the source sequence within a selector
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablepublish-and-observablemulticast">RxJava: Observable.publish() and Observable.multicast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229708.aspx">MSDN: Observable.Multicast</a>
     */
    public <TIntermediate, TResult> Observable<TResult> multicast(
            final Func0<? extends Subject<? super T, ? extends TIntermediate>> subjectFactory, 
            final Func1<? super Observable<TIntermediate>, ? extends Observable<TResult>> selector) {
        return OperationMulticast.multicast(this, subjectFactory, selector);
    }

    /**
     * An Observable that never sends any information to an {@link Observer}.
     * 
     * This Observable is useful primarily for testing purposes.
     * 
     * @param <T> the type of item emitted by the Observable
     */
    private static class NeverObservable<T> extends Observable<T> {
        public NeverObservable() {
            super(new OnSubscribeFunc<T>() {

                @Override
                public Subscription onSubscribe(Observer<? super T> t1) {
                    return Subscriptions.empty();
                }

            });
        }
    }

    /**
     * An Observable that invokes {@link Observer#onError onError} when the
     * {@link Observer} subscribes to it.
     * 
     * @param <T> the type of item emitted by the Observable
     */
    private static class ThrowObservable<T> extends Observable<T> {

        public ThrowObservable(final Throwable exception) {
            super(new OnSubscribeFunc<T>() {

                /**
                 * Accepts an {@link Observer} and calls its
                 * {@link Observer#onError onError} method.
                 * 
                 * @param observer an {@link Observer} of this Observable
                 * @return a reference to the subscription
                 */
                @Override
                public Subscription onSubscribe(Observer<? super T> observer) {
                    observer.onError(exception);
                    return Subscriptions.empty();
                }

            });
        }

    }

    /**
     * Creates an Observable that will execute the given function when an
     * {@link Observer} subscribes to it.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/create.png">
     * <p>
     * Write the function you pass to <code>create</code> so that it behaves as
     * an Observable: It should invoke the Observer's
     * {@link Observer#onNext onNext}, {@link Observer#onError onError}, and
     * {@link Observer#onCompleted onCompleted} methods appropriately.
     * <p>
     * A well-formed Observable must invoke either the Observer's
     * <code>onCompleted</code> method exactly once or its <code>onError</code>
     * method exactly once.
     * <p>
     * See <a href="http://go.microsoft.com/fwlink/?LinkID=205219">Rx Design
     * Guidelines (PDF)</a> for detailed information.
     * 
     * @param <T> the type of the items that this Observable emits
     * @param func a function that accepts an {@code Observer<T>}, invokes its
     *             {@code onNext}, {@code onError}, and {@code onCompleted}
     *             methods as appropriate, and returns a {@link Subscription} to
     *             allow the Observer to cancel the subscription
     * @return an Observable that, when an {@link Observer} subscribes to it,
     *         will execute the given function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#create">RxJava Wiki: create()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.create.aspx">MSDN: Observable.Create</a>
     */
    public static <T> Observable<T> create(OnSubscribeFunc<T> func) {
        return new Observable<T>(func);
    }

    /**
     * Returns an Observable that emits no items to the {@link Observer} and
     * immediately invokes its {@link Observer#onCompleted onCompleted} method.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/empty.png">
     * 
     * @param <T> the type of the items (ostensibly) emitted by the Observable
     * @return an Observable that returns no data to the {@link Observer} and
     *         immediately invokes the {@link Observer}'s
     *         {@link Observer#onCompleted() onCompleted} method
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#empty-error-and-never">RxJava Wiki: empty()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229670.aspx">MSDN: Observable.Empty</a>
     */
    public static <T> Observable<T> empty() {
        return from(new ArrayList<T>());
    }

    /**
     * Returns an Observable that emits no items to the {@link Observer} and
     * immediately invokes its {@link Observer#onCompleted onCompleted} method
     * with the specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/empty.s.png">
     * 
     * @param scheduler the scheduler to call the
                        {@link Observer#onCompleted onCompleted} method
     * @param <T> the type of the items (ostensibly) emitted by the Observable
     * @return an Observable that returns no data to the {@link Observer} and
     *         immediately invokes the {@link Observer}'s
     *         {@link Observer#onCompleted() onCompleted} method with the
     *         specified scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#empty-error-and-never">RxJava Wiki: empty()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229066.aspx">MSDN: Observable.Empty Method (IScheduler)</a>
     */
    public static <T> Observable<T> empty(Scheduler scheduler) {
        return Observable.<T> empty().subscribeOn(scheduler);
    }

    /**
     * Returns an Observable that invokes an {@link Observer}'s
     * {@link Observer#onError onError} method when the Observer subscribes to
     * it.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/error.png">
     * 
     * @param exception the particular error to report
     * @param <T> the type of the items (ostensibly) emitted by the Observable
     * @return an Observable that invokes the {@link Observer}'s
     *         {@link Observer#onError onError} method when the Observer
     *         subscribes to it
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#empty-error-and-never">RxJava Wiki: error()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244299.aspx">MSDN: Observable.Throw</a>
     */
    public static <T> Observable<T> error(Throwable exception) {
        return new ThrowObservable<T>(exception);
    }

    /**
     * Returns an Observable that invokes an {@link Observer}'s
     * {@link Observer#onError onError} method with the specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/error.s.png">
     * 
     * @param exception the particular error to report
     * @param scheduler the scheduler to call the
     *                  {@link Observer#onError onError} method
     * @param <T> the type of the items (ostensibly) emitted by the Observable
     * @return an Observable that invokes the {@link Observer}'s
     *         {@link Observer#onError onError} method with the specified
     *         scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#empty-error-and-never">RxJava Wiki: error()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211711.aspx">MSDN: Observable.Throw</a>
     */
    public static <T> Observable<T> error(Throwable exception, Scheduler scheduler) {
        return Observable.<T> error(exception).subscribeOn(scheduler);
    }

    /**
     * Converts an {@link Iterable} sequence into an Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the entire iterable sequence is immediately emitted each time an
     * {@link Observer} subscribes. Since this occurs before the
     * {@link Subscription} is returned, it is not possible to unsubscribe from
     * the sequence before it completes.
     * 
     * @param iterable the source {@link Iterable} sequence
     * @param <T> the type of items in the {@link Iterable} sequence and the
     *            type of items to be emitted by the resulting Observable
     * @return an Observable that emits each item in the source {@link Iterable}
     *         sequence
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     */
    public static <T> Observable<T> from(Iterable<? extends T> iterable) {
        return from(iterable, Schedulers.immediate());
    }

    /**
     * Converts an {@link Iterable} sequence into an Observable with the
     * specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.s.png">
     * 
     * @param iterable the source {@link Iterable} sequence
     * @param scheduler the scheduler to emit the items of the iterable
     * @param <T> the type of items in the {@link Iterable} sequence and the
     *            type of items to be emitted by the resulting Observable
     * @return an Observable that emits each item in the source {@link Iterable}
     *         sequence with the specified scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212140.aspx">MSDN: Observable.ToObservable</a>
     */
    public static <T> Observable<T> from(Iterable<? extends T> iterable, Scheduler scheduler) {
        return create(OperationToObservableIterable.toObservableIterable(iterable, scheduler));
    }

    /**
     * Converts an Array into an Observable that emits the items in the Array.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the entire array is immediately emitted each time an
     * {@link Observer} subscribes. Since this occurs before the
     * {@link Subscription} is returned, it is not possible to unsubscribe from
     * the sequence before it completes.
     * 
     * @param items the source array
     * @param <T> the type of items in the Array and the type of items to be
     *            emitted by the resulting Observable
     * @return an Observable that emits each item in the source Array
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     */
    public static <T> Observable<T> from(T[] items) {
        return from(Arrays.asList(items));
    }

    /**
     * Converts an Array into an Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the entire array is immediately emitted each time an
     * {@link Observer} subscribes. Since this occurs before the
     * {@link Subscription} is returned, it is not possible to unsubscribe from
     * the sequence before it completes.
     *
     * @param items the source array
     * @param scheduler the scheduler to emit the items of the array
     * @param <T> the type of items in the Array and the type of items to be
     *            emitted by the resulting Observable
     * @return an Observable that emits each item in the source Array
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     */
    public static <T> Observable<T> from(T[] items, Scheduler scheduler) {
        return from(Arrays.asList(items), scheduler);
    }

    /**
     * Converts an item into an Observable that emits that item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the item is immediately emitted each time an {@link Observer}
     * subscribes. Since this occurs before the {@link Subscription} is
     * returned, it is not possible to unsubscribe from the sequence before it
     * completes.
     * 
     * @param t1 the item
     * @param <T> the type of the item, and the type of the item to be
     *            emitted by the resulting Observable
     * @return an Observable that emits the item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     */
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1) {
        return from(Arrays.asList(t1));
    }

    /**
     * Converts a series of items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the items will be immediately emitted each time an {@link Observer}
     * subscribes. Since this occurs before the {@link Subscription} is
     * returned, it is not possible to unsubscribe from the sequence before it
     * completes.
     * 
     * @param t1 first item
     * @param t2 second item
     * @param <T> the type of items, and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1, T t2) {
        return from(Arrays.asList(t1, t2));
    }

    /**
     * Converts a series of items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the items will be immediately emitted each time an {@link Observer}
     * subscribes. Since this occurs before the {@link Subscription} is
     * returned, it is not possible to unsubscribe from the sequence before it
     * completes.
     *
     * @param t1 first item
     * @param t2 second item
     * @param t3 third item
     * @param <T> the type of items, and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1, T t2, T t3) {
        return from(Arrays.asList(t1, t2, t3));
    }

    /**
     * Converts a series of items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the items will be immediately emitted each time an {@link Observer}
     * subscribes. Since this occurs before the {@link Subscription} is
     * returned, it is not possible to unsubscribe from the sequence before it
     * completes.
     * 
     * @param t1 first item
     * @param t2 second item
     * @param t3 third item
     * @param t4 fourth item
     * @param <T> the type of items, and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1, T t2, T t3, T t4) {
        return from(Arrays.asList(t1, t2, t3, t4));
    }

    /**
     * Converts a series of items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the items will be immediately emitted each time an {@link Observer}
     * subscribes. Since this occurs before the {@link Subscription} is
     * returned, it is not possible to unsubscribe from the sequence before it
     * completes.
     * 
     * @param t1 first item
     * @param t2 second item
     * @param t3 third item
     * @param t4 fourth item
     * @param t5 fifth item
     * @param <T> the type of items, and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5) {
        return from(Arrays.asList(t1, t2, t3, t4, t5));
    }

    /**
     * Converts a series of items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the items will be immediately emitted each time an {@link Observer}
     * subscribes. Since this occurs before the {@link Subscription} is
     * returned, it is not possible to unsubscribe from the sequence before it
     * completes.
     * 
     * @param t1 first item
     * @param t2 second item
     * @param t3 third item
     * @param t4 fourth item
     * @param t5 fifth item
     * @param t6 sixth item
     * @param <T> the type of items, and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6));
    }

    /**
     * Converts a series of items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the items will be immediately emitted each time an {@link Observer}
     * subscribes. Since this occurs before the {@link Subscription} is
     * returned, it is not possible to unsubscribe from the sequence before it
     * completes.
     * 
     * @param t1 first item
     * @param t2 second item
     * @param t3 third item
     * @param t4 fourth item
     * @param t5 fifth item
     * @param t6 sixth item
     * @param t7 seventh item
     * @param <T> the type of items, and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6, T t7) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6, t7));
    }

    /**
     * Converts a series of items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the items will be immediately emitted each time an {@link Observer}
     * subscribes. Since this occurs before the {@link Subscription} is
     * returned, it is not possible to unsubscribe from the sequence before it
     * completes.
     * 
     * @param t1 first item
     * @param t2 second item
     * @param t3 third item
     * @param t4 fourth item
     * @param t5 fifth item
     * @param t6 sixth item
     * @param t7 seventh item
     * @param t8 eighth item
     * @param <T> the type of items, and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8));
    }

    /**
     * Converts a series of items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * Note: the items will be immediately emitted each time an {@link Observer}
     * subscribes. Since this occurs before the {@link Subscription} is
     * returned, it is not possible to unsubscribe from the sequence before it
     * completes.
     * 
     * @param t1 first item
     * @param t2 second item
     * @param t3 third item
     * @param t4 fourth item
     * @param t5 fifth item
     * @param t6 sixth item
     * @param t7 seventh item
     * @param t8 eighth item
     * @param t9 ninth item
     * @param <T> the type of items, and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9));
    }

    /**
     * Converts a series of items into an Observable that emits those items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.png">
     * <p>
     * @param t1 first item
     * @param t2 second item
     * @param t3 third item
     * @param t4 fourth item
     * @param t5 fifth item
     * @param t6 sixth item
     * @param t7 seventh item
     * @param t8 eighth item
     * @param t9 ninth item
     * @param t10 tenth item
     * @param <T> the type of items, and the type of items to be emitted by the
     *            resulting Observable
     * @return an Observable that emits each item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     * @deprecated Use {@link #from(Iterable)} instead such as {@code from(Arrays.asList(t1))}.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    // suppress unchecked because we are using varargs inside the method
    public static <T> Observable<T> from(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9, T t10) {
        return from(Arrays.asList(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10));
    }
    
    /**
     * Generates an Observable that emits a sequence of Integers within a
     * specified range.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/range.png">
     * <p>
     * @param start the value of the first Integer in the sequence
     * @param count the number of sequential Integers to generate
     * @return an Observable that emits a range of sequential Integers
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#range">RxJava Wiki: range()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229460.aspx">MSDN: Observable.Range</a>
     */
    public static Observable<Integer> range(int start, int count) {
        return from(Range.createWithCount(start, count));
    }

    /**
     * Generates an Observable that emits a sequence of Integers within a
     * specified range with the specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/range.s.png">
     *
     * @param start the value of the first Integer in the sequence
     * @param count the number of sequential Integers to generate
     * @param scheduler the scheduler to run the generator loop on
     * @return an Observable that emits a range of sequential Integers
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#range">RxJava Wiki: range()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211896.aspx">MSDN: Observable.Range</a>
     */
    public static Observable<Integer> range(int start, int count, Scheduler scheduler) {
        return from(Range.createWithCount(start, count), scheduler);
    }

    /**
     * Repeats the observable sequence indefinitely.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/repeat.png">
     *
     * @return an Observable that emits the items emitted by the source
     *         Observable repeatedly and in sequence
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#repeat">RxJava Wiki: repeat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229428.aspx">MSDN: Observable.Repeat</a>
     */
    public Observable<T> repeat() {
        return this.repeat(Schedulers.currentThread());
    }

    /**
     * Repeats the observable sequence indefinitely, on a particular scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/repeat.s.png">
     * 
     * @param scheduler the scheduler to send the values on.
     * @return an Observable that emits the items emitted by the source
     *         Observable repeatedly and in sequence
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#repeat">RxJava Wiki: repeat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229428.aspx">MSDN: Observable.Repeat</a>
     */
    public Observable<T> repeat(Scheduler scheduler) {
        return create(OperationRepeat.repeat(this, scheduler));
    }

    /**
     * Returns an Observable that calls an Observable factory to create its
     * Observable for each new Observer that subscribes. That is, for each
     * subscriber, the actual Observable is determined by the factory function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/defer.png">
     * <p>
     * The defer operator allows you to defer or delay emitting items from an
     * Observable until such time as an Observer subscribes to the Observable.
     * This allows an {@link Observer} to easily obtain updates or a refreshed
     * version of the sequence.
     * 
     * @param observableFactory the Observable factory function to invoke for
     *                          each {@link Observer} that subscribes to the
     *                          resulting Observable
     * @param <T> the type of the items emitted by the Observable
     * @return an Observable whose {@link Observer}s trigger an invocation of
     *         the given Observable factory function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#defer">RxJava Wiki: defer()</a>
     */
    public static <T> Observable<T> defer(Func0<? extends Observable<? extends T>> observableFactory) {
        return create(OperationDefer.defer(observableFactory));
    }

    /**
     * Returns an Observable that emits a single item and then completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/just.png">
     * <p>
     * To convert any object into an Observable that emits that object, pass
     * that object into the <code>just</code> method.
     * <p>
     * This is similar to the {@link #from(java.lang.Object[])} method, except
     * that <code>from()</code> will convert an {@link Iterable} object into an
     * Observable that emits each of the items in the Iterable, one at a time,
     * while the <code>just()</code> method converts an Iterable into an
     * Observable that emits the entire Iterable as a single item.
     * 
     * @param value the item to emit
     * @param <T> the type of that item
     * @return an Observable that emits a single item and then completes
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#just">RxJava Wiki: just()</a>
     * @deprecated Use {@link #from(T)}
     */
    @Deprecated
    public static <T> Observable<T> just(T value) {
        return from(Arrays.asList((value)));
    }

    /**
     * Returns an Observable that emits a single item and then completes, on a
     * specified scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/just.s.png">
     * <p>
     * This is a scheduler version of {@link Observable#just(Object)}.
     * 
     * @param value the item to emit
     * @param <T> the type of that item
     * @param scheduler the scheduler to emit the single item on
     * @return an Observable that emits a single item and then completes, on a
     *         specified scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#just">RxJava Wiki: just()</a>
     * @deprecated Use {@link #from(T)}
     */
    @Deprecated
    public static <T> Observable<T> just(T value, Scheduler scheduler) {
        return from(Arrays.asList((value)), scheduler);
    }

    /**
     * Flattens a sequence of Observables emitted by an Observable into one
     * Observable, without any transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine the items emitted by multiple Observables so that they
     * act like a single Observable, by using the {@code merge} method.
     * 
     * @param source an Observable that emits Observables
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the Observables emitted by the
     *         {@code source} Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public static <T> Observable<T> merge(Observable<? extends Observable<? extends T>> source) {
        return create(OperationMerge.merge(source));
    }

    /**
     * Flattens a series of Observables into one Observable, without any
     * transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they act
     * like a single Observable, by using the {@code merge} method.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2) {
        return create(OperationMerge.merge(t1, t2));
    }

    /**
     * Flattens a series of Observables into one Observable, without any
     * transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they act
     * like a single Observable, by using the {@code merge} method.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3) {
        return create(OperationMerge.merge(t1, t2, t3));
    }

    /**
     * Flattens a series of Observables into one Observable, without any
     * transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they act
     * like a single Observable, by using the {@code merge} method.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4) {
        return create(OperationMerge.merge(t1, t2, t3, t4));
    }

    /**
     * Flattens a series of Observables into one Observable, without any
     * transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they act
     * like a single Observable, by using the {@code merge} method.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5) {
        return create(OperationMerge.merge(t1, t2, t3, t4, t5));
    }

    /**
     * Flattens a series of Observables into one Observable, without any
     * transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they act
     * like a single Observable, by using the {@code merge} method.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @param t6 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6) {
        return create(OperationMerge.merge(t1, t2, t3, t4, t5, t6));
    }

    /**
     * Flattens a series of Observables into one Observable, without any
     * transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they act
     * like a single Observable, by using the {@code merge} method.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @param t6 an Observable to be merged
     * @param t7 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7) {
        return create(OperationMerge.merge(t1, t2, t3, t4, t5, t6, t7));
    }

    /**
     * Flattens a series of Observables into one Observable, without any
     * transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they act
     * like a single Observable, by using the {@code merge} method.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @param t6 an Observable to be merged
     * @param t7 an Observable to be merged
     * @param t8 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8) {
        return create(OperationMerge.merge(t1, t2, t3, t4, t5, t6, t7, t8));
    }

    /**
     * Flattens a series of Observables into one Observable, without any
     * transformation.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/merge.png">
     * <p>
     * You can combine items emitted by multiple Observables so that they act
     * like a single Observable, by using the {@code merge} method.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @param t6 an Observable to be merged
     * @param t7 an Observable to be merged
     * @param t8 an Observable to be merged
     * @param t9 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#merge">RxJava Wiki: merge()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> merge(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8, Observable<? extends T> t9) {
        return create(OperationMerge.merge(t1, t2, t3, t4, t5, t6, t7, t8, t9));
    }

    /**
     * Returns an Observable that emits the items emitted by two or more
     * Observables, one after the other.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param observables an Observable that emits Observables
     * @return an Observable that emits items that are the result of combining
     *         the items emitted by the {@code source} Observables, one after
     *         the other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    public static <T> Observable<T> concat(Observable<? extends Observable<? extends T>> observables) {
        return create(OperationConcat.concat(observables));
    }

    /**
     * Returns an Observable that emits the items emitted by two Observables,
     * one after the other.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1 an Observable to be concatenated
     * @param t2 an Observable to be concatenated
     * @return an Observable that emits items that are the result of combining
     *         the items emitted by the {@code source} Observables, one after
     *         the other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2) {
        return create(OperationConcat.concat(t1, t2));
    }

    /**
     * Returns an Observable that emits the items emitted by three Observables,
     * one after the other.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1 an Observable to be concatenated
     * @param t2 an Observable to be concatenated
     * @param t3 an Observable to be concatenated
     * @return an Observable that emits items that are the result of combining
     *         the items emitted by the {@code source} Observables, one after
     *         the other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3) {
        return create(OperationConcat.concat(t1, t2, t3));
    }

    /**
     * Returns an Observable that emits the items emitted by four Observables,
     * one after the other.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1 an Observable to be concatenated
     * @param t2 an Observable to be concatenated
     * @param t3 an Observable to be concatenated
     * @param t4 an Observable to be concatenated
     * @return an Observable that emits items that are the result of combining
     *         the items emitted by the {@code source} Observables, one after
     *         the other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4) {
        return create(OperationConcat.concat(t1, t2, t3, t4));
    }

    /**
     * Returns an Observable that emits the items emitted by five Observables,
     * one after the other.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1 an Observable to be concatenated
     * @param t2 an Observable to be concatenated
     * @param t3 an Observable to be concatenated
     * @param t4 an Observable to be concatenated
     * @param t5 an Observable to be concatenated
     * @return an Observable that emits items that are the result of combining
     *         the items emitted by the {@code source} Observables, one after
     *         the other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5));
    }

    /**
     * Returns an Observable that emits the items emitted by six Observables,
     * one after the other.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1 an Observable to be concatenated
     * @param t2 an Observable to be concatenated
     * @param t3 an Observable to be concatenated
     * @param t4 an Observable to be concatenated
     * @param t5 an Observable to be concatenated
     * @param t6 an Observable to be concatenated
     * @return an Observable that emits items that are the result of combining
     *         the items emitted by the {@code source} Observables, one after
     *         the other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5, t6));
    }

    /**
     * Returns an Observable that emits the items emitted by secven Observables,
     * one after the other.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1 an Observable to be concatenated
     * @param t2 an Observable to be concatenated
     * @param t3 an Observable to be concatenated
     * @param t4 an Observable to be concatenated
     * @param t5 an Observable to be concatenated
     * @param t6 an Observable to be concatenated
     * @param t7 an Observable to be concatenated
     * @return an Observable that emits items that are the result of combining
     *         the items emitted by the {@code source} Observables, one after
     *         the other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5, t6, t7));
    }

    /**
     * Returns an Observable that emits the items emitted by eight Observables,
     * one after the other.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1 an Observable to be concatenated
     * @param t2 an Observable to be concatenated
     * @param t3 an Observable to be concatenated
     * @param t4 an Observable to be concatenated
     * @param t5 an Observable to be concatenated
     * @param t6 an Observable to be concatenated
     * @param t7 an Observable to be concatenated
     * @param t8 an Observable to be concatenated
     * @return an Observable that emits items that are the result of combining
     *         the items emitted by the {@code source} Observables, one after
     *         the other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5, t6, t7, t8));
    }

    /**
     * Returns an Observable that emits the items emitted by nine Observables,
     * one after the other.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concat.png">
     * 
     * @param t1 an Observable to be concatenated
     * @param t2 an Observable to be concatenated
     * @param t3 an Observable to be concatenated
     * @param t4 an Observable to be concatenated
     * @param t5 an Observable to be concatenated
     * @param t6 an Observable to be concatenated
     * @param t7 an Observable to be concatenated
     * @param t8 an Observable to be concatenated
     * @param t9 an Observable to be concatenated
     * @return an Observable that emits items that are the result of combining
     *         the items emitted by the {@code source} Observables, one after
     *         the other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#concat">RxJava Wiki: concat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.concat.aspx">MSDN: Observable.Concat</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> concat(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8, Observable<? extends T> t9) {
        return create(OperationConcat.concat(t1, t2, t3, t4, t5, t6, t7, t8, t9));
    }

    /**
     * This behaves like {@link #merge(Observable)} except that if any of the
     * merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged
     * Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications,
     * {@code mergeDelayError} will only invoke the {@code onError} method of
     * its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items
     * from all of the source Observables without being interrupted by an error
     * notification from one of them.
     * 
     * @param source an Observable that emits Observables
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the Observables emitted by the
     *         {@code source} Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    public static <T> Observable<T> mergeDelayError(Observable<? extends Observable<? extends T>> source) {
        return create(OperationMergeDelayError.mergeDelayError(source));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable)} except that if
     * any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged
     * Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications,
     * {@code mergeDelayError} will only invoke the {@code onError} method of
     * its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items
     * from all of the source Observables without being interrupted by an error
     * notification from one of them.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable)}
     * except that if any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged
     * Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications,
     * {@code mergeDelayError} will only invoke the {@code onError} method of
     * its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items
     * from all of the source Observables without being interrupted by an error
     * notification from one of them.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3));
    }

    /**
     * This behaves like
     * {@link #merge(Observable, Observable, Observable, Observable)} except
     * that if any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged
     * Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications,
     * {@code mergeDelayError} will only invoke the {@code onError} method of
     * its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items
     * from all of the source Observables without being interrupted by an error
     * notification from one of them.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable)}
     * except that if any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged
     * Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications,
     * {@code mergeDelayError} will only invoke the {@code onError} method of
     * its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items
     * from all of the source Observables without being interrupted by an error
     * notification from one of them.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable, Observable)}
     * except that if any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged
     * Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications,
     * {@code mergeDelayError} will only invoke the {@code onError} method of
     * its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items
     * from all of the source Observables without being interrupted by an error
     * notification from one of them.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @param t6 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5, t6));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable, Observable, Observable)}
     * except that if any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged
     * Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications,
     * {@code mergeDelayError} will only invoke the {@code onError} method of
     * its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items
     * from all of the source Observables without being interrupted by an error
     * notification from one of them.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @param t6 an Observable to be merged
     * @param t7 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5, t6, t7));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable, Observable, Observable, Observable)}
     * except that if any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged
     * Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications,
     * {@code mergeDelayError} will only invoke the {@code onError} method of
     * its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items
     * from all of the source Observables without being interrupted by an error
     * notification from one of them.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @param t6 an Observable to be merged
     * @param t7 an Observable to be merged
     * @param t8 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5, t6, t7, t8));
    }

    /**
     * This behaves like {@link #merge(Observable, Observable, Observable, Observable, Observable, Observable, Observable, Observable, Observable)}
     * except that if any of the merged Observables notify of an error via
     * {@link Observer#onError onError}, {@code mergeDelayError} will refrain
     * from propagating that error notification until all of the merged
     * Observables have finished emitting items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeDelayError.png">
     * <p>
     * Even if multiple merged Observables send {@code onError} notifications,
     * {@code mergeDelayError} will only invoke the {@code onError} method of
     * its Observers once.
     * <p>
     * This method allows an Observer to receive all successfully emitted items
     * from all of the source Observables without being interrupted by an error
     * notification from one of them.
     * 
     * @param t1 an Observable to be merged
     * @param t2 an Observable to be merged
     * @param t3 an Observable to be merged
     * @param t4 an Observable to be merged
     * @param t5 an Observable to be merged
     * @param t6 an Observable to be merged
     * @param t7 an Observable to be merged
     * @param t8 an Observable to be merged
     * @param t9 an Observable to be merged
     * @return an Observable that emits items that are the result of flattening
     *         the items emitted by the {@code source} Observables
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#mergedelayerror">RxJava Wiki: mergeDelayError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229099.aspx">MSDN: Observable.Merge</a>
     */
    @SuppressWarnings("unchecked")
    // suppress because the types are checked by the method signature before using a vararg
    public static <T> Observable<T> mergeDelayError(Observable<? extends T> t1, Observable<? extends T> t2, Observable<? extends T> t3, Observable<? extends T> t4, Observable<? extends T> t5, Observable<? extends T> t6, Observable<? extends T> t7, Observable<? extends T> t8, Observable<? extends T> t9) {
        return create(OperationMergeDelayError.mergeDelayError(t1, t2, t3, t4, t5, t6, t7, t8, t9));
    }

    /**
     * Returns an Observable that never sends any items or notifications to an
     * {@link Observer}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/never.png">
     * <p>
     * This Observable is useful primarily for testing purposes.
     * 
     * @param <T> the type of items (not) emitted by the Observable
     * @return an Observable that never emits any items or sends any
     *         notifications to an {@link Observer}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#empty-error-and-never">RxJava Wiki: never()</a>
     */
    public static <T> Observable<T> never() {
        return new NeverObservable<T>();
    }

    /**
     * Given an Observable that emits Observables, returns an Observable that
     * emits the items emitted by the most recently emitted of those
     * Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/switchDo.png">
     * 
     * @param sequenceOfSequences the source Observable that emits Observables
     * @return an Observable that emits only the items emitted by the Observable
     *         most recently emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#switchonnext">RxJava Wiki: switchOnNext()</a>
     * @deprecated use {@link #switchOnNext}
     */
    @Deprecated
    public static <T> Observable<T> switchDo(Observable<? extends Observable<? extends T>> sequenceOfSequences) {
        return create(OperationSwitch.switchDo(sequenceOfSequences));
    }

    /**
     * Given an Observable that emits Observables, returns an Observable that
     * emits the items emitted by the most recently emitted of those
     * Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/switchDo.png">
     * 
     * @param sequenceOfSequences the source Observable that emits Observables
     * @return an Observable that emits only the items emitted by the Observable
     *         most recently emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#switchonnext">RxJava Wiki: switchOnNext()</a>
     */
    public static <T> Observable<T> switchOnNext(Observable<? extends Observable<? extends T>> sequenceOfSequences) {
        return create(OperationSwitch.switchDo(sequenceOfSequences));
    }
    
    /**
     * Given an Observable that emits Observables, returns an Observable that
     * emits the items emitted by the most recently emitted of those
     * Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/switchLatest.png">
     * 
     * @param sequenceOfSequences the source Observable that emits Observables
     * @return an Observable that emits only the items emitted by the Observable
     *         most recently emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#switchonnext">RxJava Wiki: switchOnNext()</a>
     * @see {@link #switchOnNext(Observable)}
     */
    public static <T> Observable<T> switchLatest(Observable<? extends Observable<? extends T>> sequenceOfSequences) {
        return create(OperationSwitch.switchDo(sequenceOfSequences));
    }

    
    /**
     * Accepts an Observable and wraps it in another Observable that ensures
     * that the resulting Observable is chronologically well-behaved.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/synchronize.png">
     * <p>
     * A well-behaved Observable does not interleave its invocations of the
     * {@link Observer#onNext onNext}, {@link Observer#onCompleted onCompleted},
     * and {@link Observer#onError onError} methods of its {@link Observer}s; it
     * invokes {@code onCompleted} or {@code onError} only once; and it never
     * invokes {@code onNext} after invoking either {@code onCompleted} or
     * {@code onError}. {@code synchronize} enforces this, and the Observable it
     * returns invokes {@code onNext} and {@code onCompleted} or {@code onError}
     * synchronously.
     * 
     * @return an Observable that is a chronologically well-behaved version of
     *         the source Observable, and that synchronously notifies its
     *         {@link Observer}s
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#synchronize">RxJava Wiki: synchronize()</a>
     */
    public Observable<T> synchronize() {
        return create(OperationSynchronize.synchronize(this));
    }

    /**
     * Accepts an Observable and wraps it in another Observable that ensures
     * that the resulting Observable is chronologically well-behaved. This is
     * accomplished by acquiring a mutual-exclusion lock for the object provided
     * as the lock parameter.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/synchronize.png">
     * <p>
     * A well-behaved Observable does not interleave its invocations of the
     * {@link Observer#onNext onNext}, {@link Observer#onCompleted onCompleted},
     * and {@link Observer#onError onError} methods of its {@link Observer}s; it
     * invokes {@code onCompleted} or {@code onError} only once; and it never
     * invokes {@code onNext} after invoking either {@code onCompleted} or
     * {@code onError}. {@code synchronize} enforces this, and the Observable it
     * returns invokes {@code onNext} and {@code onCompleted} or {@code onError}
     * synchronously.
     * 
     * @param lock the lock object to synchronize each observer call on
     * @return an Observable that is a chronologically well-behaved version of
     *         the source Observable, and that synchronously notifies its
     *         {@link Observer}s
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#synchronize">RxJava Wiki: synchronize()</a>
     */
    public Observable<T> synchronize(Object lock) {
        return create(OperationSynchronize.synchronize(this, lock));
    }

    /**
     * @deprecated use {@link #synchronize()} or {@link #synchronize(Object)}
     */
    @Deprecated
    public static <T> Observable<T> synchronize(Observable<T> source) {
        return create(OperationSynchronize.synchronize(source));
    }

    /**
     * Returns an Observable that emits an item each time interval, containing
     * a sequential number.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/interval.png">
     * 
     * @param interval interval size in time units (see below)
     * @param unit time units to use for the interval size
     * @return an Observable that emits an item each time interval
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#interval">RxJava Wiki: interval()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229027.aspx">MSDN: Observable.Interval</a>
     */
    public static Observable<Long> interval(long interval, TimeUnit unit) {
        return create(OperationInterval.interval(interval, unit));
    }

    /**
     * Returns an Observable that emits an item each time interval, containing
     * a sequential number.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/interval.s.png">
     * 
     * @param interval interval size in time units (see below)
     * @param unit time units to use for the interval size
     * @param scheduler the scheduler to use for scheduling the items
     * @return an Observable that emits an item each time interval
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#interval">RxJava Wiki: interval()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228911.aspx">MSDN: Observable.Interval</a>
     */
    public static Observable<Long> interval(long interval, TimeUnit unit, Scheduler scheduler) {
        return create(OperationInterval.interval(interval, unit, scheduler));
    }

    /**
     * Returns an Observable that emits one item after a given delay, and then
     * completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timer.png">
     * 
     * @param delay the initial delay before emitting a single 0L
     * @param unit time units to use for the interval size
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#timer">RxJava wiki: timer()</a>
     */
    public static Observable<Long> timer(long delay, TimeUnit unit) {
        return timer(delay, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * Returns an Observable that emits one item after a given delay, and then
     * completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timer.s.png">
     * 
     * @param delay the initial delay before emitting a single 0L
     * @param unit time units to use for the interval size
     * @param scheduler the scheduler to use for scheduling the item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#timer">RxJava wiki: timer()</a>
     */
    public static Observable<Long> timer(long delay, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationTimer.TimerOnce(delay, unit, scheduler));
    }
    
    /**
     * Return an Observable which emits a 0L after the {@code initialDelay} and
     * ever increasing numbers after each {@code period}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timer.p.png">
     * 
     * @param initialDelay the initial delay time to wait before emitting the
     *                     first value of 0L
     * @param period the time period after emitting the subsequent numbers
     * @param unit the time unit for both <code>initialDelay</code> and
     *             <code>period</code>
     * @return an Observable which emits a 0L after the {@code initialDelay} and
     *         ever increasing numbers after each {@code period}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#timer">RxJava Wiki: timer()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229435.aspx">MSDN: Observable.Timer</a>
     */
    public static Observable<Long> timer(long initialDelay, long period, TimeUnit unit) {
        return timer(initialDelay, period, unit, Schedulers.threadPoolForComputation());
    }
    
    /**
     * Return an Observable which emits a 0L after the {@code initialDelay} and
     * ever increasing numbers after each {@code period} while running on the
     * given {@code scheduler}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timer.ps.png">
     * 
     * @param initialDelay the initial delay time to wait before emitting the
     *                     first value of 0L
     * @param period the time period after emitting the subsequent numbers
     * @param unit the time unit for both <code>initialDelay</code> and
     *             <code>period</code>
     * @param scheduler the scheduler on which the waiting happens and value
     *                  emissions run
     * @return an Observable that emits a 0L after the {@code initialDelay} and
     *         ever increasing numbers after each {@code period} while running
     *         on the given {@code scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#timer">RxJava Wiki: timer()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229652.aspx">MSDN: Observable.Timer</a>
     */
    public static Observable<Long> timer(long initialDelay, long period, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationTimer.TimerPeriodically(initialDelay, period, unit, scheduler));
    }

    /**
     * Returns an Observable that emits the items emitted by the source
     * Observable shifted forward in time by a specified delay. Error
     * notifications from the source Observable are not delayed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delay.png">
     *
     * @param delay the delay to shift the source by
     * @param unit the {@link TimeUnit} in which <code>period</code> is defined
     * @return the source Observable, but shifted by the specified delay
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#delay">RxJava Wiki: delay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229810.aspx">MSDN: Observable.Delay</a>
     */
    public Observable<T> delay(long delay, TimeUnit unit) {
        return OperationDelay.delay(this, delay, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * Returns an Observable that emits the items emitted by the source
     * Observable shifted forward in time by a specified delay. Error
     * notifications from the source Observable are not delayed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delay.s.png">
     *
     * @param delay the delay to shift the source by
     * @param unit the {@link TimeUnit} in which <code>period</code> is defined
     * @param scheduler the {@link Scheduler} to use for delaying
     * @return the source Observable, but shifted by the specified delay
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#delay">RxJava Wiki: delay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229280.aspx">MSDN: Observable.Delay</a>
     */
    public Observable<T> delay(long delay, TimeUnit unit, Scheduler scheduler) {
        return OperationDelay.delay(this, delay, unit, scheduler);
    }

    /**
     * Return an Observable that delays the subscription to the source
     * Observable by a given amount of time.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delaySubscription.png">
     *
     * @param delay the time to delay the subscription
     * @param unit the time unit
     * @return an Observable that delays the subscription to the source
     *         Observable by the given amount
     */
    public Observable<T> delaySubscription(long delay, TimeUnit unit) {
        return delaySubscription(delay, unit, Schedulers.threadPoolForComputation());
    }
    
    /**
     * Return an Observable that delays the subscription to the source
     * Observable by a given amount of time, both waiting and subscribing on
     * a given Scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/delaySubscription.s.png">
     *
     * @param delay the time to delay the subscription
     * @param unit the time unit
     * @param scheduler the scheduler on which the waiting and subscription will
     *                  happen
     * @return an Observable that delays the subscription to the source
     *         Observable by a given amount, waiting and subscribing on the
     *         given Scheduler
     */
    public Observable<T> delaySubscription(long delay, TimeUnit unit, Scheduler scheduler) {
        return create(OperationDelay.delaySubscription(this, delay, unit, scheduler));
    }
    
    /**
     * Drops items emitted by an Observable that are followed by newer items
     * before a timeout value expires. The timer resets on each emission.
     * <p>
     * Note: If events keep firing faster than the timeout then no items will be
     * emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/debounce.png">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * 
     * @param timeout the time each item has to be "the most recent" of those
     *                emitted by the source {@link Observable} to ensure that
     *                it's not dropped
     * @param unit the {@link TimeUnit} for the timeout 
     * @return an {@link Observable} that filters out items that are too quickly
     *         followed by newer items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#throttlewithtimeout-or-debounce">RxJava Wiki: debounce()</a>
     * @see #throttleWithTimeout(long, TimeUnit)
     */
    public Observable<T> debounce(long timeout, TimeUnit unit) {
        return create(OperationDebounce.debounce(this, timeout, unit));
    }

    /**
     * Drops items emitted by an Observable that are followed by newer items
     * before a timeout value expires. The timer resets on each emission.
     * <p>
     * Note: If events keep firing faster than the timeout then no items will be
     * emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/debounce.s.png">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * 
     * @param timeout the time each item has to be "the most recent" of those
     *                emitted by the source {@link Observable} to ensure that
     *                it's not dropped
     * @param unit the unit of time for the specified timeout
     * @param scheduler the {@link Scheduler} to use internally to manage the
     *                  timers that handle the timeout for each event
     * @return an {@link Observable} that filters out items that are too quickly
     *         followed by newer items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#throttlewithtimeout-or-debounce">RxJava Wiki: debounce()</a>
     * @see #throttleWithTimeout(long, TimeUnit, Scheduler)
     */
    public Observable<T> debounce(long timeout, TimeUnit unit, Scheduler scheduler) {
        return create(OperationDebounce.debounce(this, timeout, unit, scheduler));
    }

    /**
     * Drops items emitted by an Observable that are followed by newer items
     * before a timeout value expires. The timer resets on each emission.
     * <p>
     * Note: If events keep firing faster than the timeout then no items will be
     * emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleWithTimeout.png">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * 
     * @param timeout the time each item has to be "the most recent" of those
     *                emitted by the source {@link Observable} to ensure that
     *                it's not dropped
     * @param unit the {@link TimeUnit} for the timeout
     * @return an {@link Observable} that filters out items that are too quickly
     *         followed by newer items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#throttlewithtimeout-or-debounce">RxJava Wiki: throttleWithTimeout()</a>
     * @see #debounce(long, TimeUnit)
     */
    public Observable<T> throttleWithTimeout(long timeout, TimeUnit unit) {
        return create(OperationDebounce.debounce(this, timeout, unit));
    }

    /**
     * Drops items emitted by an Observable that are followed by newer items
     * before a timeout value expires. The timer resets on each emission.
     * <p>
     * Note: If events keep firing faster than the timeout then no items will be
     * emitted by the resulting Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleWithTimeout.s.png">
     * <p>
     * Information on debounce vs throttle:
     * <p>
     * <ul>
     * <li><a href="http://drupalmotion.com/article/debounce-and-throttle-visual-explanation">Debounce and Throttle: visual explanation</a></li>
     * <li><a href="http://unscriptable.com/2009/03/20/debouncing-javascript-methods/">Debouncing: javascript methods</a></li>
     * <li><a href="http://www.illyriad.co.uk/blog/index.php/2011/09/javascript-dont-spam-your-server-debounce-and-throttle/">Javascript - don't spam your server: debounce and throttle</a></li>
     * </ul>
     * 
     * @param timeout the time each item has to be "the most recent" emitted by
     *                the {@link Observable} to ensure that it's not dropped
     * @param unit the {@link TimeUnit} for the timeout
     * @param scheduler the {@link Scheduler} to use internally to manage the
     *                  timers that handle the timeout for each item
     * @return an {@link Observable} that filters out items that are too quickly
     *         followed by newer items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#throttlewithtimeout-or-debounce">RxJava Wiki: throttleWithTimeout()</a>
     * @see #debounce(long, TimeUnit, Scheduler)
     */
    public Observable<T> throttleWithTimeout(long timeout, TimeUnit unit, Scheduler scheduler) {
        return create(OperationDebounce.debounce(this, timeout, unit, scheduler));
    }

    /**
     * Throttles by skipping items emitted by the source Observable until
     * <code>windowDuration</code> passes and then emitting the next item
     * emitted by the source Observable.
     * <p>
     * This differs from {@link #throttleLast} in that this only tracks passage
     * of time whereas {@link #throttleLast} ticks at scheduled intervals.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleFirst.png">
     * 
     * @param windowDuration time to wait before emitting another item after
     *                       emitting the last item
     * @param unit the unit of time for the specified timeout
     * @return an Observable that performs the throttle operation
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#throttlefirst">RxJava Wiki: throttleFirst()</a>
     */
    public Observable<T> throttleFirst(long windowDuration, TimeUnit unit) {
        return create(OperationThrottleFirst.throttleFirst(this, windowDuration, unit));
    }

    /**
     * Throttles by skipping items emitted by the source Observable until
     * <code>skipDuration</code> passes and then emitting the next item emitted
     * by the source Observable.
     * <p>
     * This differs from {@link #throttleLast} in that this only tracks passage
     * of time whereas {@link #throttleLast} ticks at scheduled intervals.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleFirst.s.png">
     * 
     * @param skipDuration time to wait before emitting another item after
     *                     emitting the last item
     * @param unit the unit of time for the specified timeout
     * @param scheduler the {@link Scheduler} to use internally to manage the
     *                  timers that handle timeout for each event
     * @return an Observable that performs the throttle operation
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#throttlefirst">RxJava Wiki: throttleFirst()</a>
     */
    public Observable<T> throttleFirst(long skipDuration, TimeUnit unit, Scheduler scheduler) {
        return create(OperationThrottleFirst.throttleFirst(this, skipDuration, unit, scheduler));
    }

    /**
     * Throttles by emitting the last item from the source Observable that falls
     * in each interval defined by <code>intervalDuration</code>.
     * <p>
     * This differs from {@link #throttleFirst} in that this ticks along at a
     * scheduled interval whereas {@link #throttleFirst} does not tick, it just
     * tracks passage of time.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleLast.png">
     * 
     * @param intervalDuration duration of windows within which the last item
     *                         emitted by the source Observable will be emitted
     * @param unit the unit of time for the specified interval
     * @return an Observable that performs the throttle operation
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#takelast">RxJava Wiki: throttleLast()</a>
     * @see #sample(long, TimeUnit)
     */
    public Observable<T> throttleLast(long intervalDuration, TimeUnit unit) {
        return sample(intervalDuration, unit);
    }

    /**
     * Throttles by emitting the last item in each interval defined by
     * <code>intervalDuration</code>.
     * <p>
     * This differs from {@link #throttleFirst} in that this ticks along at a
     * scheduled interval whereas {@link #throttleFirst} does not tick, it just
     * tracks passage of time.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/throttleLast.s.png">
     * 
     * @param intervalDuration duration of windows within which the last item
     *                         emitted by the source Observable will be emitted
     * @param unit the unit of time for the specified interval
     * @param scheduler the {@link Scheduler} to use internally to manage the
     *                  timers that handle timeout for each event
     * @return an Observable that performs the throttle operation
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#takelast">RxJava Wiki: throttleLast()</a>
     * @see #sample(long, TimeUnit, Scheduler)
     */
    public Observable<T> throttleLast(long intervalDuration, TimeUnit unit, Scheduler scheduler) {
        return sample(intervalDuration, unit, scheduler);
    }

    /**
     * Wraps each item emitted by a source Observable in a {@link Timestamped}
     * object.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timestamp.png">
     * 
     * @return an Observable that emits timestamped items from the source
     *         Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#timestamp">RxJava Wiki: timestamp()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229003.aspx">MSDN: Observable.Timestamp</a>
     */
    public Observable<Timestamped<T>> timestamp() {
        return create(OperationTimestamp.timestamp(this));
    }

    /**
     * Wraps each item emitted by a source Observable in a {@link Timestamped}
     * object with timestamps provided by the given Scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timestamp.s.png">
     * 
     * @param scheduler the {@link Scheduler} to use as a time source.
     * @return an Observable that emits timestamped items from the source
     *         Observable with timestamps provided by the given Scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#timestamp">RxJava Wiki: timestamp()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229003.aspx">MSDN: Observable.Timestamp</a>
     */
    public Observable<Timestamped<T>> timestamp(Scheduler scheduler) {
        return create(OperationTimestamp.timestamp(this, scheduler));
    }

    /**
     * Converts a {@link Future} into an Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.Future.png">
     * <p>
     * You can convert any object that supports the {@link Future} interface
     * into an Observable that emits the return value of the {@link Future#get}
     * method of that object, by passing the object into the {@code from}
     * method.
     * <p>
     * <em>Important note:</em> This Observable is blocking; you cannot
     * unsubscribe from it.
     * 
     * @param future the source {@link Future}
     * @param <T> the type of object that the {@link Future} returns, and also
     *            the type of item to be emitted by the resulting Observable
     * @return an Observable that emits the item from the source Future
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     */
    public static <T> Observable<T> from(Future<? extends T> future) {
        return create(OperationToObservableFuture.toObservableFuture(future));
    }

    /**
     * Converts a {@link Future} into an Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.Future.s.png">
     * <p>
     * You can convert any object that supports the {@link Future} interface
     * into an Observable that emits the return value of the {@link Future#get}
     * method of that object, by passing the object into the {@code from}
     * method.
     * <p>
     * 
     * @param future the source {@link Future}
     * @param scheduler the {@link Scheduler} to wait for the Future on. Use a
     *                  Scheduler such as {@link Schedulers#threadPoolForIO()}
     *                  that can block and wait on the future.
     * @param <T> the type of object that the {@link Future} returns, and also
     *            the type of item to be emitted by the resulting Observable
     * @return an Observable that emits the item from the source Future
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     */
    public static <T> Observable<T> from(Future<? extends T> future, Scheduler scheduler) {
        return create(OperationToObservableFuture.toObservableFuture(future)).subscribeOn(scheduler);
    }

    /**
     * Converts a {@link Future} into an Observable with timeout.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/from.Future.png">
     * <p>
     * You can convert any object that supports the {@link Future} interface
     * into an Observable that emits the return value of the {link Future#get}
     * method of that object, by passing the object into the {@code from}
     * method.
     * <p>
     * <em>Important note:</em> This Observable is blocking; you cannot
     * unsubscribe from it.
     * 
     * @param future the source {@link Future}
     * @param timeout the maximum time to wait before calling <code>get()</code>
     * @param unit the {@link TimeUnit} of the <code>timeout</code> argument
     * @param <T> the type of object that the {@link Future} returns, and also
     *            the type of item to be emitted by the resulting Observable
     * @return an Observable that emits the item from the source {@link Future}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Creating-Observables#from">RxJava Wiki: from()</a>
     */
    public static <T> Observable<T> from(Future<? extends T> future, long timeout, TimeUnit unit) {
        return create(OperationToObservableFuture.toObservableFuture(future, timeout, unit));
    }

    /**
     * Returns an Observable that emits a Boolean value that indicates whether
     * two sequences are equal by comparing the elements emitted by each
     * Observable pairwise.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sequenceEqual.png">
     * 
     * @param first the first Observable to compare
     * @param second the second Observable to compare
     * @param <T> the type of items emitted by each Observable
     * @return an Observable that emits a Boolean value that indicates whether
     *         two sequences are equal by comparing the elements pairwise
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#sequenceequal">RxJava Wiki: sequenceEqual()</a>
     */
    public static <T> Observable<Boolean> sequenceEqual(Observable<? extends T> first, Observable<? extends T> second) {
        return sequenceEqual(first, second, new Func2<T, T, Boolean>() {
            @Override
            public Boolean call(T first, T second) {
                if(first == null) {
                    return second == null;
                }
                return first.equals(second);
            }
        });
    }

    /**
     * Returns an Observable that emits a Boolean value that indicates whether
     * two sequences are equal by comparing the elements emitted by each
     * Observable pairwise based on the results of a specified equality
     * function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sequenceEqual.png">
     * 
     * @param first the first Observable to compare
     * @param second the second Observable to compare
     * @param equality a function used to compare items emitted by both
     *                 Observables
     * @param <T> the type of items emitted by each Observable
     * @return an Observable that emits a Boolean value that indicates whether
     *         two sequences are equal by comparing the elements pairwise
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#sequenceequal">RxJava Wiki: sequenceEqual()</a>
     */
    public static <T> Observable<Boolean> sequenceEqual(Observable<? extends T> first, Observable<? extends T> second, Func2<? super T, ? super T, Boolean> equality) {
        return OperationSequenceEqual.sequenceEqual(first, second, equality);
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations of two items emitted, in sequence, by
     * two other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted by {@code o1} and the first item emitted by
     * {@code o2}; the second item emitted by the new Observable will be the
     * result of the function applied to the second item emitted by {@code o1}
     * and the second item emitted by {@code o2}; and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will
     * invoke {@link Observer#onNext onNext} as many times as the number of
     * {@code onNext} invocations of the source Observable that emits the fewest
     * items.
     * 
     * @param o1 the first source Observable
     * @param o2 another source Observable
     * @param zipFunction a function that, when applied to an item emitted by
     *            each of the source Observables, results in an item that will
     *            be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <T1, T2, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Func2<? super T1, ? super T2, ? extends R> zipFunction) {
        return create(OperationZip.zip(o1, o2, zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations of three items emitted, in sequence, by
     * three other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted by {@code o1}, the first item emitted by
     * {@code o2}, and the first item emitted by {@code o3}; the second item
     * emitted by the new Observable will be the result of the function applied
     * to the second item emitted by {@code o1}, the second item emitted by
     * {@code o2}, and the second item emitted by {@code o3}; and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will
     * invoke {@link Observer#onNext onNext} as many times as the number of
     * {@code onNext} invocations of the source Observable that emits the fewest
     * items.
     * 
     * @param o1 the first source Observable
     * @param o2 a second source Observable
     * @param o3 a third source Observable
     * @param zipFunction a function that, when applied to an item emitted by
     *                    each of the source Observables, results in an item
     *                    that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <T1, T2, T3, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Func3<? super T1, ? super T2, ? super T3, ? extends R> zipFunction) {
        return create(OperationZip.zip(o1, o2, o3, zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations of four items emitted, in sequence, by
     * four other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted by {@code o1}, the first item emitted by
     * {@code o2}, the first item emitted by {@code o3}, and the first item
     * emitted by {@code 04}; the second item emitted by the new Observable will
     * be the result of the function applied to the second item emitted by each
     * of those Observables; and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will
     * invoke {@link Observer#onNext onNext} as many times as the number of
     * {@code onNext} invocations of the source Observable that emits the fewest
     * items.
     * 
     * @param o1 one source Observable
     * @param o2 a second source Observable
     * @param o3 a third source Observable
     * @param o4 a fourth source Observable
     * @param zipFunction a function that, when applied to an item emitted by
     *            each of the source Observables, results in an item that will
     *            be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <T1, T2, T3, T4, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Func4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> zipFunction) {
        return create(OperationZip.zip(o1, o2, o3, o4, zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations of five items emitted, in sequence, by
     * five other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted by {@code o1}, the first item emitted by
     * {@code o2}, the first item emitted by {@code o3}, the first item emitted
     * by {@code o4}, and the first item emitted by {@code o5}; the second item
     * emitted by the new Observable will be the result of the function applied
     * to the second item emitted by each of those Observables; and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will
     * invoke {@link Observer#onNext onNext} as many times as the number of
     * {@code onNext} invocations of the source Observable that emits the fewest
     * items.
     * 
     * @param o1 the first source Observable
     * @param o2 a second source Observable
     * @param o3 a third source Observable
     * @param o4 a fourth source Observable
     * @param o5 a fifth source Observable
     * @param zipFunction a function that, when applied to an item emitted by
     *                    each of the source Observables, results in an item
     *                    that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <T1, T2, T3, T4, T5, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Func5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> zipFunction) {
        return create(OperationZip.zip(o1, o2, o3, o4, o5, zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations of six items emitted, in sequence, by
     * six other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted each source Observable, the second item emitted
     * by the new Observable will be the result of the function applied to the
     * second item emitted by each of those Observables, and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will
     * invoke {@link Observer#onNext onNext} as many times as the number of
     * {@code onNext} invocations of the source Observable that emits the fewest
     * items.
     * 
     * @param o1 the first source Observable
     * @param o2 a second source Observable
     * @param o3 a third source Observable
     * @param o4 a fourth source Observable
     * @param o5 a fifth source Observable
     * @param o6 a sixth source Observable
     * @param zipFunction a function that, when applied to an item emitted by
     *                    each of the source Observables, results in an item
     *                    that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <T1, T2, T3, T4, T5, T6, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6,
            Func6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> zipFunction) {
        return create(OperationZip.zip(o1, o2, o3, o4, o5, o6, zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations of seven items emitted, in sequence, by
     * seven other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted each source Observable, the second item emitted
     * by the new Observable will be the result of the function applied to the
     * second item emitted by each of those Observables, and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will
     * invoke {@link Observer#onNext onNext} as many times as the number of
     * {@code onNext} invocations of the source Observable that emits the fewest
     * items.
     * 
     * @param o1 the first source Observable
     * @param o2 a second source Observable
     * @param o3 a third source Observable
     * @param o4 a fourth source Observable
     * @param o5 a fifth source Observable
     * @param o6 a sixth source Observable
     * @param o7 a seventh source Observable
     * @param zipFunction a function that, when applied to an item emitted by
     *                    each of the source Observables, results in an item
     *                    that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <T1, T2, T3, T4, T5, T6, T7, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7,
            Func7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> zipFunction) {
        return create(OperationZip.zip(o1, o2, o3, o4, o5, o6, o7, zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations of eight items emitted, in sequence, by
     * eight other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted each source Observable, the second item emitted
     * by the new Observable will be the result of the function applied to the
     * second item emitted by each of those Observables, and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will
     * invoke {@link Observer#onNext onNext} as many times as the number of
     * {@code onNext} invocations of the source Observable that emits the fewest
     * items.
     * 
     * @param o1 the first source Observable
     * @param o2 a second source Observable
     * @param o3 a third source Observable
     * @param o4 a fourth source Observable
     * @param o5 a fifth source Observable
     * @param o6 a sixth source Observable
     * @param o7 a seventh source Observable
     * @param o8 an eighth source Observable
     * @param zipFunction a function that, when applied to an item emitted by
     *                    each of the source Observables, results in an item
     *                    that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7, Observable<? extends T8> o8,
            Func8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> zipFunction) {
        return create(OperationZip.zip(o1, o2, o3, o4, o5, o6, o7, o8, zipFunction));
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations of nine items emitted, in sequence, by
     * nine other Observables.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted each source Observable, the second item emitted
     * by the new Observable will be the result of the function applied to the
     * second item emitted by each of those Observables, and so forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will
     * invoke {@link Observer#onNext onNext} as many times as the number of
     * {@code onNext} invocations of the source Observable that emits the fewest
     * items.
     * 
     * @param o1 the first source Observable
     * @param o2 a second source Observable
     * @param o3 a third source Observable
     * @param o4 a fourth source Observable
     * @param o5 a fifth source Observable
     * @param o6 a sixth source Observable
     * @param o7 a seventh source Observable
     * @param o8 an eighth source Observable
     * @param o9 a ninth source Observable
     * @param zipFunction a function that, when applied to an item emitted by
     *                    each of the source Observables, results in an item
     *                    that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Observable<R> zip(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7, Observable<? extends T8> o8,
            Observable<? extends T9> o9, Func9<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? extends R> zipFunction) {
        return create(OperationZip.zip(o1, o2, o3, o4, o5, o6, o7, o8, o9, zipFunction));
    }

    /**
     * Combines the given Observables, emitting an item that aggregates the
     * latest values of each of the source Observables each time an item is
     * received from any of the source Observables, where this aggregation is
     * defined by a given function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1 the first source Observable
     * @param o2 the second source Observable
     * @param combineFunction the aggregation function used to combine the
     *                        items emitted by the source Observables
     * @return an Observable whose emissions are the result of combining the
     *         emissions of the source Observables with the given aggregation
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public static <T1, T2, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Func2<? super T1, ? super T2, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, combineFunction));
    }

    /**
     * Combines the given Observables, emitting an item that aggregates the
     * latest values of each of the source Observables each time an item is
     * received from any of the source Observables, where this aggregation is
     * defined by a given function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1 the first source Observable
     * @param o2 the second source Observable
     * @param o3 the third source Observable
     * @param combineFunction the aggregation function used to combine the
     *                        items emitted by the source Observables
     * @return an Observable whose emissions are the result of combining the
     *         emissions of the source Observables with the given aggregation
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public static <T1, T2, T3, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Func3<? super T1, ? super T2, ? super T3, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, combineFunction));
    }

    /**
     * Combines the given Observables, emitting an item that aggregates the
     * latest values of each of the source Observables each time an item is
     * received from any of the source Observables, where this aggregation is
     * defined by a given function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1 the first source Observable
     * @param o2 the second source Observable
     * @param o3 the third source Observable
     * @param o4 the fourth source Observable
     * @param combineFunction the aggregation function used to combine the
     *                        items emitted by the source Observables
     * @return an Observable whose emissions are the result of combining the
     *         emissions of the source Observables with the given aggregation
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public static <T1, T2, T3, T4, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4,
            Func4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, combineFunction));
    }

    /**
     * Combines the given Observables, emitting an item that aggregates the
     * latest values of each of the source Observables each time an item is
     * received from any of the source Observables, where this aggregation is
     * defined by a given function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1 the first source Observable
     * @param o2 the second source Observable
     * @param o3 the third source Observable
     * @param o4 the fourth source Observable
     * @param o5 the fifth source Observable
     * @param combineFunction the aggregation function used to combine the
     *                        items emitted by the source Observables
     * @return an Observable whose emissions are the result of combining the
     *         emissions of the source Observables with the given aggregation
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public static <T1, T2, T3, T4, T5, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5,
            Func5<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, combineFunction));
    }

    /**
     * Combines the given Observables, emitting an item that aggregates the
     * latest values of each of the source Observables each time an item is
     * received from any of the source Observables, where this aggregation is
     * defined by a given function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1 the first source Observable
     * @param o2 the second source Observable
     * @param o3 the third source Observable
     * @param o4 the fourth source Observable
     * @param o5 the fifth source Observable
     * @param o6 the sixth source Observable
     * @param combineFunction the aggregation function used to combine the
     *                        items emitted by the source Observables
     * @return an Observable whose emissions are the result of combining the
     *         emissions of the source Observables with the given aggregation
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public static <T1, T2, T3, T4, T5, T6, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6,
            Func6<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, o6, combineFunction));
    }

    /**
     * Combines the given Observables, emitting an item that aggregates the
     * latest values of each of the source Observables each time an item is
     * received from any of the source Observables, where this aggregation is
     * defined by a given function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1 the first source Observable
     * @param o2 the second source Observable
     * @param o3 the third source Observable
     * @param o4 the fourth source Observable
     * @param o5 the fifth source Observable
     * @param o6 the sixth source Observable
     * @param o7 the seventh source Observable
     * @param combineFunction the aggregation function used to combine the
     *                        items emitted by the source Observables
     * @return an Observable whose emissions are the result of combining the
     *         emissions of the source Observables with the given aggregation
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public static <T1, T2, T3, T4, T5, T6, T7, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7,
            Func7<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, o6, o7, combineFunction));
    }

    /**
     * Combines the given Observables, emitting an item that aggregates the
     * latest values of each of the source Observables each time an item is
     * received from any of the source Observables, where this aggregation is
     * defined by a given function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1 the first source Observable
     * @param o2 the second source Observable
     * @param o3 the third source Observable
     * @param o4 the fourth source Observable
     * @param o5 the fifth source Observable
     * @param o6 the sixth source Observable
     * @param o7 the seventh source Observable
     * @param o8 the eighth source Observable
     * @param combineFunction the aggregation function used to combine the
     *                        items emitted by the source Observables
     * @return an Observable whose emissions are the result of combining the
     *         emissions of the source Observables with the given aggregation
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7, Observable<? extends T8> o8,
            Func8<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, o6, o7, o8, combineFunction));
    }

    /**
     * Combines the given Observables, emitting an item that aggregates the
     * latest values of each of the source Observables each time an item is
     * received from any of the source Observables, where this aggregation is
     * defined by a given function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/combineLatest.png">
     * 
     * @param o1 the first source Observable
     * @param o2 the second source Observable
     * @param o3 the third source Observable
     * @param o4 the fourth source Observable
     * @param o5 the fifth source Observable
     * @param o6 the sixth source Observable
     * @param o7 the seventh source Observable
     * @param o8 the eighth source Observable
     * @param o9 the ninth source Observable
     * @param combineFunction the aggregation function used to combine the
     *                        items emitted by the source Observables
     * @return an Observable whose emissions are the result of combining the
     *         emissions of the source Observables with the given aggregation
     *         function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#combinelatest">RxJava Wiki: combineLatest()</a>
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Observable<R> combineLatest(Observable<? extends T1> o1, Observable<? extends T2> o2, Observable<? extends T3> o3, Observable<? extends T4> o4, Observable<? extends T5> o5, Observable<? extends T6> o6, Observable<? extends T7> o7, Observable<? extends T8> o8, Observable<? extends T9> o9,
            Func9<? super T1, ? super T2, ? super T3, ? super T4, ? super T5, ? super T6, ? super T7, ? super T8, ? super T9, ? extends R> combineFunction) {
        return create(OperationCombineLatest.combineLatest(o1, o2, o3, o4, o5, o6, o7, o8, o9, combineFunction));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping buffers. It emits the current buffer and replaces it with
     * a new buffer when the Observable produced by the specified
     * <code>bufferClosingSelector</code> emits an item. It then uses the
     * <code>bufferClosingSelector</code> to create a new Observable to observe
     * for the end of the next buffer.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer1.png">
     * 
     * @param bufferClosingSelector the {@link Func0} which is used to produce
     *                              an {@link Observable} for every buffer
     *                              created. When this {@link Observable} emits
     *                              an item, <code>buffer()</code> emits the
     *                              associated buffer and replaces it with a new
     *                              one.
     * @return an {@link Observable} that emits connected, non-overlapping
     *         buffers when the current {@link Observable} created with the
     *         {@code bufferClosingSelector} argument emits an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public <TClosing> Observable<List<T>> buffer(Func0<? extends Observable<? extends TClosing>> bufferClosingSelector) {
        return create(OperationBuffer.buffer(this, bufferClosingSelector));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable emits buffers that it creates
     * when the specified <code>bufferOpenings</code> Observable emits an item,
     * and closes when the Observable returned from
     * <code>bufferClosingSelector</code> emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer2.png">
     * 
     * @param bufferOpenings the {@link Observable} that, when it emits an item,
     *                       causes a new buffer to be created
     * @param bufferClosingSelector the {@link Func1} that is used to produce
     *                              an {@link Observable} for every buffer
     *                              created. When this {@link Observable} emits
     *                              an item, the associated buffer is emitted.
     * @return an {@link Observable} that emits buffers that are created and
     *         closed when the specified {@link Observable}s emit items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public <TOpening, TClosing> Observable<List<T>> buffer(Observable<? extends TOpening> bufferOpenings, Func1<? super TOpening, ? extends Observable<? extends TClosing>> bufferClosingSelector) {
        return create(OperationBuffer.buffer(this, bufferOpenings, bufferClosingSelector));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping buffers, each containing <code>count</code> items. When
     * the source Observable completes or encounters an error, it emits the
     * current buffer is emitted, and propagates the notification from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer3.png">
     * 
     * @param count the maximum number of items in each buffer before it should
     *              be emitted
     * @return an {@link Observable} that emits connected, non-overlapping
     *         buffers, each containing at most "count" items from the source
     *         Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public Observable<List<T>> buffer(int count) {
        return create(OperationBuffer.buffer(this, count));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable emits buffers every
     * <code>skip</code> items, each containing <code>count</code> items. When
     * the source Observable completes or encounters an error, the resulting
     * Observable emits the current buffer and propagates the notification from
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer4.png">
     * 
     * @param count the maximum size of each buffer before it should be emitted
     * @param skip how many produced items need to be skipped before starting a
     *             new buffer. Note that when <code>skip</code> and
     *             <code>count</code> are equal, this is the same operation as
     *             {@link Observable#buffer(int)}.
     * @return an {@link Observable} that emits buffers every <code>skip</code>
     *         item and containing at most <code>count</code> items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public Observable<List<T>> buffer(int count, int skip) {
        return create(OperationBuffer.buffer(this, count, skip));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping buffers, each of a fixed duration specified by the
     * <code>timespan</code> argument. When the source Observable completes or
     * encounters an error, the resulting Observable emits the current buffer
     * and propagates the notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer5.png">
     * 
     * @param timespan the period of time each buffer collects items before it
     *                 should be emitted and replaced with a new buffer
     * @param unit the unit of time which applies to the <code>timespan</code>
     *             argument
     * @return an {@link Observable} that emits connected, non-overlapping
     *         buffers with a fixed duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public Observable<List<T>> buffer(long timespan, TimeUnit unit) {
        return create(OperationBuffer.buffer(this, timespan, unit));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping buffers, each of a fixed duration specified by the
     * <code>timespan</code> argument. When the source Observable completes or
     * encounters an error, the resulting Observable emits the current buffer
     * and propagates the notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer5.s.png">
     * 
     * @param timespan the period of time each buffer collects items before it
     *                 should be emitted and replaced with a new buffer
     * @param unit the unit of time which applies to the <code>timespan</code>
     *             argument
     * @param scheduler the {@link Scheduler} to use when determining the end
     *                  and start of a buffer
     * @return an {@link Observable} that emits connected, non-overlapping
     *         buffers with a fixed duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public Observable<List<T>> buffer(long timespan, TimeUnit unit, Scheduler scheduler) {
        return create(OperationBuffer.buffer(this, timespan, unit, scheduler));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping buffers, each of a fixed duration specified by the
     * <code>timespan</code> argument or a maximum size specified by the
     * <code>count</code> argument (whichever is reached first). When the source
     * Observable completes or encounters an error, the resulting Observable
     * emits the current buffer and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer6.png">
     * 
     * @param timespan the period of time each buffer collects items before it
     *                 should be emitted and replaced with a new buffer
     * @param unit the unit of time which applies to the <code>timespan</code>
     *             argument
     * @param count the maximum size of each buffer before it should be emitted
     * @return an {@link Observable} that emits connected, non-overlapping
     *         buffers of items emitted from the source Observable, after a
     *         fixed duration or when the buffer reaches maximum capacity
     *         (whichever occurs first)
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public Observable<List<T>> buffer(long timespan, TimeUnit unit, int count) {
        return create(OperationBuffer.buffer(this, timespan, unit, count));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping buffers, each of a fixed duration specified by the
     * <code>timespan</code> argument or a maximum size specified by the
     * <code>count</code> argument (whichever is reached first). When the source
     * Observable completes or encounters an error, the resulting Observable
     * emits the current buffer and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer6.s.png">
     * 
     * @param timespan the period of time each buffer collects items before it
     *                 should be emitted and replaced with a new buffer
     * @param unit the unit of time which applies to the <code>timespan</code>
     *             argument
     * @param count the maximum size of each buffer before it should be emitted
     * @param scheduler the {@link Scheduler} to use when determining the end
                        and start of a buffer
     * @return an {@link Observable} that emits connected, non-overlapping
     *         buffers of items emitted by the source Observable after a fixed
     *         duration or when the buffer reaches maximum capacity (whichever
     *         occurs first)
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public Observable<List<T>> buffer(long timespan, TimeUnit unit, int count, Scheduler scheduler) {
        return create(OperationBuffer.buffer(this, timespan, unit, count, scheduler));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable starts a new buffer
     * periodically, as determined by the <code>timeshift</code> argument. It
     * emits buffer after a fixed timespan, specified by the
     * <code>timespan</code> argument. When the source Observable completes or
     * encounters an error, it emits the current buffer and propagates the
     * notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer7.png">
     * 
     * @param timespan the period of time each buffer collects items before it
     *                 should be emitted
     * @param timeshift the period of time after which a new buffer will be
     *                  created
     * @param unit the unit of time that applies to the <code>timespan</code>
     *             and <code>timeshift</code> arguments
     * @return an {@link Observable} that emits new buffers of items emitted by
     *         the source Observable periodically after a fixed timespan has
     *         elapsed
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public Observable<List<T>> buffer(long timespan, long timeshift, TimeUnit unit) {
        return create(OperationBuffer.buffer(this, timespan, timeshift, unit));
    }

    /**
     * Creates an Observable that emits buffers of items it collects from the
     * source Observable. The resulting Observable starts a new buffer
     * periodically, as determined by the <code>timeshift</code> argument. It
     * emits each buffer after a fixed timespan, specified by the
     * <code>timespan</code> argument. When the source Observable completes or
     * encounters an error, the resulting Observable emits the current buffer
     * propagates the notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/buffer7.s.png">
     * 
     * @param timespan the period of time each buffer collects items before it
     *                 should be emitted
     * @param timeshift the period of time after which a new buffer will be
     *                  created
     * @param unit the unit of time that applies to the <code>timespan</code>
     *             and <code>timeshift</code> arguments
     * @param scheduler the {@link Scheduler} to use when determining the end
     *                  and start of a buffer
     * @return an {@link Observable} that emits new buffers of items emitted by
     *         the source Observable periodically after a fixed timespan has
     *         elapsed
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#buffer">RxJava Wiki: buffer()</a>
     */
    public Observable<List<T>> buffer(long timespan, long timeshift, TimeUnit unit, Scheduler scheduler) {
        return create(OperationBuffer.buffer(this, timespan, timeshift, unit, scheduler));
    }

    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping windows. It emits the current window and opens a new one
     * when the Observable produced by the specified
     * <code>closingSelector</code> emits an item. The
     * <code>closingSelector</code> then creates a new Observable to observe
     * for the end of the next window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window1.png">
     * 
     * @param closingSelector the {@link Func0} used to produce an
     *            {@link Observable} for every window created. When this
     *            {@link Observable} emits an item, <code>window()</code> emits
     *            the associated window and begins a new one.
     * @return an {@link Observable} that emits connected, non-overlapping
     *         windows when the current {@link Observable} created with the
     *         <code>closingSelector</code> argument emits an item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public <TClosing> Observable<Observable<T>> window(Func0<? extends Observable<? extends TClosing>> closingSelector) {
        return create(OperationWindow.window(this, closingSelector));
    }

    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable emits windows. These windows
     * contain those items emitted by the source Observable between the time
     * when the <code>windowOpenings</code> Observable emits an item and when
     * the Observable returned by <code>closingSelector</code> emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window2.png">
     * 
     * @param windowOpenings the {@link Observable} that, when it emits an item,
     *                       causes another window to be created
     * @param closingSelector a {@link Func1} that produces an 
     *                        {@link Observable} for every window created. When
     *                        this {@link Observable} emits an item, the
     *                        associated window is closed and emitted
     * @return an {@link Observable} that emits windows of items emitted by the
     *         source Observable that are governed by the specified
     *         {@link Observable}s emitting items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public <TOpening, TClosing> Observable<Observable<T>> window(Observable<? extends TOpening> windowOpenings, Func1<? super TOpening, ? extends Observable<? extends TClosing>> closingSelector) {
        return create(OperationWindow.window(this, windowOpenings, closingSelector));
    }
    
    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping windows, each containing <code>count</code> items. When
     * the source Observable completes or encounters an error, the resulting
     * Observable emits the current window and propagates the notification from
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window3.png">
     * 
     * @param count the maximum size of each window before it should be emitted
     * @return an {@link Observable} that emits connected, non-overlapping
     *         windows containing at most <code>count</code> items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public Observable<Observable<T>> window(int count) {
        return create(OperationWindow.window(this, count));
    }

    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable emits windows every
     * <code>skip</code> items, each containing <code>count</code> items.
     * When the source Observable completes or encounters an error, the
     * resulting Observable emits the current window and propagates the
     * notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window4.png">
     * 
     * @param count the maximum size of each window before it should be emitted
     * @param skip how many items need to be skipped before starting a new
     *             window. Note that if <code>skip</code> and <code>count</code>
     *             are equal this is the same operation as {@link #window(int)}.
     * @return an {@link Observable} that emits windows every "skipped"
     *         items containing at most <code>count</code> items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public Observable<Observable<T>> window(int count, int skip) {
        return create(OperationWindow.window(this, count, skip));
    }

    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping windows, each of a fixed duration specified by the
     * <code>timespan</code> argument. When the source Observable completes or
     * encounters an error, the resulting Observable emits the current window
     * and propagates the notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window5.png">
     * 
     * @param timespan the period of time each window collects items before it
     *                 should be emitted and replaced with a new window
     * @param unit the unit of time that applies to the <code>timespan</code>
     *             argument
     * @return an {@link Observable} that emits connected, non-overlapping
     *         windows with a fixed duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public Observable<Observable<T>> window(long timespan, TimeUnit unit) {
        return create(OperationWindow.window(this, timespan, unit));
    }

    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping windows, each of a fixed duration as specified by the
     * <code>timespan</code> argument. When the source Observable completes or
     * encounters an error, the resulting Observable emits the current window
     * and propagates the notification from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window5.s.png">
     * 
     * @param timespan the period of time each window collects items before it
     *                 should be emitted and replaced with a new window
     * @param unit the unit of time which applies to the <code>timespan</code>
     *             argument
     * @param scheduler the {@link Scheduler} to use when determining the end
     *                  and start of a window
     * @return an {@link Observable} that emits connected, non-overlapping
     *         windows with a fixed duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public Observable<Observable<T>> window(long timespan, TimeUnit unit, Scheduler scheduler) {
        return create(OperationWindow.window(this, timespan, unit, scheduler));
    }

    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping windows, each of a fixed duration as specified by the
     * <code>timespan</code> argument or a maximum size as specified by the
     * <code>count</code> argument (whichever is reached first). When the source
     * Observable completes or encounters an error, the resulting Observable
     * emits the current window and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window6.png">
     * 
     * @param timespan the period of time each window collects items before it
     *                 should be emitted and replaced with a new window
     * @param unit the unit of time that applies to the <code>timespan</code>
     *             argument
     * @param count the maximum size of each window before it should be emitted
     * @return an {@link Observable} that emits connected, non-overlapping
     *         windows after a fixed duration or when the window has reached
     *         maximum capacity (whichever occurs first)
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public Observable<Observable<T>> window(long timespan, TimeUnit unit, int count) {
        return create(OperationWindow.window(this, timespan, unit, count));
    }

    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable emits connected,
     * non-overlapping windows, each of a fixed duration specified by the
     * <code>timespan</code> argument or a maximum size specified by the
     * <code>count</code> argument (whichever is reached first). When the source
     * Observable completes or encounters an error, the resulting Observable
     * emits the current window and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window6.s.png">
     * 
     * @param timespan the period of time each window collects items before it
     *                 should be emitted and replaced with a new window
     * @param unit the unit of time which applies to the <code>timespan</code>
     *             argument
     * @param count the maximum size of each window before it should be emitted
     * @param scheduler the {@link Scheduler} to use when determining the end
     *                  and start of a window.
     * @return an {@link Observable} that emits connected non-overlapping
     *         windows after a fixed duration or when the window has reached
     *         maximum capacity (whichever occurs first).
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public Observable<Observable<T>> window(long timespan, TimeUnit unit, int count, Scheduler scheduler) {
        return create(OperationWindow.window(this, timespan, unit, count, scheduler));
    }

    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable starts a new window
     * periodically, as determined by the <code>timeshift</code> argument. It
     * emits each window after a fixed timespan, specified by the
     * <code>timespan</code> argument. When the source Observable completes or
     * Observable completes or encounters an error, the resulting Observable
     * emits the current window and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window7.png">
     * 
     * @param timespan the period of time each window collects items before it
     *                 should be emitted
     * @param timeshift the period of time after which a new window will be
     *                  created
     * @param unit the unit of time that applies to the <code>timespan</code>
     *             and <code>timeshift</code> arguments
     * @return an {@link Observable} that emits new windows periodically as a
     *         fixed timespan has elapsed
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public Observable<Observable<T>> window(long timespan, long timeshift, TimeUnit unit) {
        return create(OperationWindow.window(this, timespan, timeshift, unit));
    }

    /**
     * Creates an Observable that emits windows of items it collects from the
     * source Observable. The resulting Observable starts a new window
     * periodically, as determined by the <code>timeshift</code> argument. It
     * emits each window after a fixed timespan, specified by the
     * <code>timespan</code> argument. When the source Observable completes or
     * Observable completes or encounters an error, the resulting Observable
     * emits the current window and propagates the notification from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/window7.s.png">
     * 
     * @param timespan the period of time each window collects items before it
     *                 should be emitted
     * @param timeshift the period of time after which a new window will be
     *                  created
     * @param unit the unit of time that applies to the <code>timespan</code>
     *             and <code>timeshift</code> arguments
     * @param scheduler the {@link Scheduler} to use when determining the end
     *                  and start of a window
     * @return an {@link Observable} that emits new windows periodically as a
     *         fixed timespan has elapsed
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#window">RxJava Wiki: window()</a>
     */
    public Observable<Observable<T>> window(long timespan, long timeshift, TimeUnit unit, Scheduler scheduler) {
        return create(OperationWindow.window(this, timespan, timeshift, unit, scheduler));
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations of <i>n</i> items emitted, in sequence,
     * by <i>n</i> other Observables as provided by an Iterable.
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted by all of the source Observables; the second
     * item emitted by the new Observable will be the result of the function
     * applied to the second item emitted by each of those Observables; and so
     * forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will
     * invoke {@code onNext} as many times as the number of {@code onNext}
     * invokations of the source Observable that emits the fewest items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.o.png">
     * 
     * @param ws an Observable of source Observables
     * @param zipFunction a function that, when applied to an item emitted by
     *                    each of the source Observables, results in an item
     *                    that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <R> Observable<R> zip(Observable<? extends Observable<?>> ws, final FuncN<? extends R> zipFunction) {
        return ws.toList().mergeMap(new Func1<List<? extends Observable<?>>, Observable<? extends R>>() {
            @Override
            public Observable<R> call(List<? extends Observable<?>> wsList) {
                return create(OperationZip.zip(wsList, zipFunction));
            }
        });
    }

    /**
     * Returns an Observable that emits the results of a function of your
     * choosing applied to combinations items emitted, in sequence, by a
     * collection of other Observables.
     * <p>
     * {@code zip} applies this function in strict sequence, so the first item
     * emitted by the new Observable will be the result of the function applied
     * to the first item emitted by all of the source Observables; the second
     * item emitted by the new Observable will be the result of the function
     * applied to the second item emitted by each of those Observables; and so
     * forth.
     * <p>
     * The resulting {@code Observable<R>} returned from {@code zip} will invoke
     * {@code onNext} as many times as the number of {@code onNext} invokations
     * of the source Observable that emits the fewest items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/zip.png">
     * 
     * @param ws a collection of source Observables
     * @param zipFunction a function that, when applied to an item emitted by
     *                    each of the source Observables, results in an item
     *                    that will be emitted by the resulting Observable
     * @return an Observable that emits the zipped results
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#zip">RxJava Wiki: zip()</a>
     */
    public static <R> Observable<R> zip(Iterable<? extends Observable<?>> ws, FuncN<? extends R> zipFunction) {
        return create(OperationZip.zip(ws, zipFunction));
    }

    /**
     * Filter items emitted by an Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/filter.png">
     * 
     * @param predicate a function that evaluates the items emitted by the
     *                  source Observable, returning {@code true} if they pass
     *                  the filter
     * @return an Observable that emits only those items emitted by the source
     *         Observable that the filter evaluates as {@code true}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#filter-or-where">RxJava Wiki: filter()</a>
     */
    public Observable<T> filter(Func1<? super T, Boolean> predicate) {
        return create(OperationFilter.filter(this, predicate));
    }

    /**
     * Returns an Observable that emits all sequentially distinct items
     * emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/distinctUntilChanged.png">
     * 
     * @return an Observable that emits those items from the source Observable
     *         that are sequentially distinct
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#distinctuntilchanged">RxJava Wiki: distinctUntilChanged()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229494.aspx">MSDN: Observable.distinctUntilChanged</a>
     */
    public Observable<T> distinctUntilChanged() {
        return create(OperationDistinctUntilChanged.distinctUntilChanged(this));
    }

    /**
     * Returns an Observable that emits all items emitted by the source
     * Observable that are sequentially distinct according to a key selector
     * function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/distinctUntilChanged.key.png">
     * 
     * @param keySelector a function that projects an emitted item to a key
     *                    value that is used to decide whether an item is
     *                    sequentially distinct from another one or not
     * @return an Observable that emits those items from the source Observable
     *         whose keys are sequentially distinct
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#distinctuntilchanged">RxJava Wiki: distinctUntilChanged()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229508.aspx">MSDN: Observable.distinctUntilChanged</a>
     */
    public <U> Observable<T> distinctUntilChanged(Func1<? super T, ? extends U> keySelector) {
        return create(OperationDistinctUntilChanged.distinctUntilChanged(this, keySelector));
    }

    /**
     * Returns an Observable that emits all items emitted by the source
     * Observable that are distinct.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/distinct.png">
     * 
     * @return an Observable that emits only those items emitted by the source
     *         Observable that are distinct from each other
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#distinct">RxJava Wiki: distinct()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229764.aspx">MSDN: Observable.distinct</a>
     */
    public Observable<T> distinct() {
        return create(OperationDistinct.distinct(this));
    }

    /**
     * Returns an Observable that emits all items emitted by the source
     * Observable that are distinct according to a key selector function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/distinct.key.png">
     * 
     * @param keySelector a function that projects an emitted item to a key
     *                    value that is used to decide whether an item is
     *                    distinct from another one or not
     * @return an Observable that emits those items emitted by the source
     *         Observable that have distinct keys
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#distinct">RxJava Wiki: distinct()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244310.aspx">MSDN: Observable.distinct</a>
     */
    public <U> Observable<T> distinct(Func1<? super T, ? extends U> keySelector) {
        return create(OperationDistinct.distinct(this, keySelector));
    }

    /**
     * Returns an Observable that emits the item at a specified index in a
     * sequence of emissions from a source Observbable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/elementAt.png">
     * 
     * @param index the zero-based index of the item to retrieve
     * @return an Observable that emits the item at the specified position in
     *         the sequence of those emitted by the source Observable
     * @throws IndexOutOfBoundsException if <code>index</code> is greater than
     *                                   or equal to the number of items emitted
     *                                   by the source Observable
     * @throws IndexOutOfBoundsException if <code>index</code> is less than 0
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#elementat">RxJava Wiki: elementAt()</a>
     */
    public Observable<T> elementAt(int index) {
        return create(OperationElementAt.elementAt(this, index));
    }

    /**
     * Returns the item at a specified index in a sequence or the default item
     * if the index is out of range.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/elementAtOrDefault.png">
     * 
     * @param index the zero-based index of the item to retrieve
     * @param defaultValue the default item
     * @return an Observable that emits the item at the specified position in
     *         the source sequence, or the default item if the index is outside
     *         the bounds of the source sequence
     * @throws IndexOutOfBoundsException if <code>index</code> is less than 0
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#elementatordefault">RxJava Wiki: elementAtOrDefault()</a>
     */
    public Observable<T> elementAtOrDefault(int index, T defaultValue) {
        return create(OperationElementAt.elementAtOrDefault(this, index, defaultValue));
    }

    /**
     * Returns an {@link Observable} that emits <code>true</code> if any item
     * emitted by the source {@link Observable} satisfies a specified condition,
     * otherwise <code>false</code>. Note: this always emits <code>false</code>
     * if the source {@link Observable} is empty.
     * <p>
     * In Rx.Net this is the <code>any</code> operator but we renamed it in
     * RxJava to better match Java naming idioms.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/exists.png">
     * 
     * @param predicate the condition to test every item emitted by the source
     *                  Observable
     * @return a subscription function for creating the target Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#exists-and-isempty">RxJava Wiki: exists()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211993.aspx" >MSDN: Observable.Any</a> Note: the description in this page was wrong at the time of this writing.
     */
    public Observable<Boolean> exists(Func1<? super T, Boolean> predicate) {
        return create(OperationAny.exists(this, predicate));
    }

    /**
     * Returns an Observable that emits a Boolean that indicates whether the
     * source Observable emitted a specified item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/contains.png">
     * 
     * @param element the item to search for in the emissions from the source
     *                Observable
     * @return an Observable that emits <code>true</code> if the specified item
     *         is emitted by the source Observable, or <code>false</code> if the
     *         source Observable completes without emitting that item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#contains">RxJava Wiki: contains()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228965.aspx">MSDN: Observable.Contains</a>
     */
    public Observable<Boolean> contains(final T element) {
        return exists(new Func1<T, Boolean>() {
            public Boolean call(T t1) {
                return element == null ? t1 == null : element.equals(t1);
            }
        });
    }

    /**
     * Registers an {@link Action0} to be called when this Observable invokes
     * {@link Observer#onCompleted onCompleted} or
     * {@link Observer#onError onError}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/finallyDo.png">
     * 
     * @param action an {@link Action0} to be invoked when the source
     *               Observable finishes
     * @return an Observable that emits the same items as the source Observable,
     *         then invokes the {@link Action0}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#finallydo">RxJava Wiki: finallyDo()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212133.aspx">MSDN: Observable.Finally</a>
     */
    public Observable<T> finallyDo(Action0 action) {
        return create(OperationFinally.finallyDo(this, action));
    }

    /**
     * Creates a new Observable by applying a function that you supply to each
     * item emitted by the source Observable, where that function returns an
     * Observable, and then merging those resulting Observables and emitting the
     * results of this merger.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/flatMap.png">
     * <p>
     * Note: {@code mapMany} and {@code flatMap} are equivalent.
     * 
     * @param func a function that, when applied to an item emitted by the
     *             source Observable, returns an Observable
     * @return an Observable that emits the result of applying the
     *         transformation function to each item emitted by the source
     *         Observable and merging the results of the Observables obtained
     *         from this transformation.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#mapmany-or-flatmap-and-mapmanydelayerror">RxJava Wiki: flatMap()</a>
     * @see #mapMany(Func1)
     */
    public <R> Observable<R> flatMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        return mergeMap(func);
    }
    
    /**
     * Creates a new Observable by applying a function that you supply to each
     * item emitted by the source Observable, where that function returns an
     * Observable, and then merging those resulting Observables and emitting the
     * results of this merger.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mergeMap.png">
     * 
     * @param func a function that, when applied to an item emitted by the
     *             source Observable, returns an Observable
     * @return an Observable that emits the result of applying the
     *         transformation function to each item emitted by the source
     *         Observable and merging the results of the Observables obtained
     *         from this transformation.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#mapmany-or-flatmap-and-mapmanydelayerror">RxJava Wiki: flatMap()</a>
     * @see #flatMap(Func1)
     */
    public <R> Observable<R> mergeMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        return merge(map(func));
    }
    
    /**
     * Creates a new Observable by applying a function that you supply to each
     * item emitted by the source Observable, where that function returns an
     * Observable, and then concatting those resulting Observables and emitting
     * the results of this concat.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/concatMap.png">
     * 
     * @param func a function that, when applied to an item emitted by the
     *             source Observable, returns an Observable
     * @return an Observable that emits the result of applying the
     *         transformation function to each item emitted by the source
     *         Observable and concatting the results of the Observables obtained
     *         from this transformation.
     */
    public <R> Observable<R> concatMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        return concat(map(func));
    }
    
    /**
     * Creates a new Observable by applying a function that you supply to each
     * item emitted by the source Observable resulting in an Observable of
     * Observables. Then a {@link #switchLatest(Observable)} /
     * {@link #switchOnNext(Observable)} is applied.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/switchMap.png">
     * 
     * @param func a function that, when applied to an item emitted by the
     *             source Observable, returns an Observable
     * @return an Observable that emits the result of applying the
     *         transformation function to each item emitted by the source
     *         Observable and then switch
     */
    public <R> Observable<R> switchMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        return switchOnNext(map(func));
    }

    /**
     * Filter items emitted by an Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/where.png">
     * 
     * @param predicate a function that evaluates an item emitted by the source
     *                  Observable, returning {@code true} if it passes the
     *                  filter
     * @return an Observable that emits only those items emitted by the source
     *         Observable that the filter evaluates as {@code true}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#filter-or-where">RxJava Wiki: where()</a>
     * @see #filter(Func1)
     */
    @Deprecated
    public Observable<T> where(Func1<? super T, Boolean> predicate) {
        return filter(predicate);
    }

    /**
     * Returns an Observable that applies the given function to each item
     * emitted by an Observable and emits the results of these function
     * applications.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/map.png">
     * 
     * @param func a function to apply to each item emitted by the Observable
     * @return an Observable that emits the items from the source Observable,
     *         transformed by the given function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#map">RxJava Wiki: map()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244306.aspx">MSDN: Observable.Select</a>
     */
    public <R> Observable<R> map(Func1<? super T, ? extends R> func) {
        return create(OperationMap.map(this, func));
    }

    /**
     * Returns an Observable that applies the given function to each item
     * emitted by an Observable and emits the results of these function
     * applications.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mapWithIndex.png">
     * 
     * @param func a function to apply to each item emitted by the Observable
     *             that takes the index of the emitted item as additional
     *             parameter
     * @return an Observable that emits the items from the source Observable,
     *         transformed by the given function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#mapwithindex">RxJava Wiki: mapWithIndex()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244311.aspx">MSDN: Observable.Select</a>
     * @deprecated just use zip with {@link Observable#range(int)}
     */
    @Deprecated
    public <R> Observable<R> mapWithIndex(Func2<? super T, Integer, ? extends R> func) {
        return create(OperationMap.mapWithIndex(this, func));
    }

    /**
     * Creates a new Observable by applying a function that you supply to each
     * item emitted by the source Observable, where that function returns an
     * Observable, and then merging those resulting Observables and emitting
     * the results of this merger.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/mapMany.png">
     * <p>
     * Note: <code>mapMany</code> and <code>flatMap</code> are equivalent.
     * 
     * @param func a function that, when applied to an item emitted by the
     *             source Observable, returns an Observable
     * @return an Observable that emits the result of applying the
     *         transformation function to each item emitted by the source
     *         Observable and merging the results of the Observables obtained
     *         from this transformation.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#mapmany-or-flatmap-and-mapmanydelayerror">RxJava Wiki: mapMany()</a>
     * @see #flatMap(Func1)
     * @deprecated
     */
    @Deprecated
    public <R> Observable<R> mapMany(Func1<? super T, ? extends Observable<? extends R>> func) {
        return mergeMap(func);
    }

    /**
     * Turns all of the emissions and notifications from a source Observable
     * into emissions marked with their original types within
     * {@link Notification} objects.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/materialize.png">
     * 
     * @return an Observable whose items are the result of materializing the
     *         items and notifications of the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#materialize">RxJava Wiki: materialize()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229453.aspx">MSDN: Observable.materialize</a>
     */
    public Observable<Notification<T>> materialize() {
        return create(OperationMaterialize.materialize(this));
    }

    /**
     * Asynchronously subscribes and unsubscribes Observers on the specified
     * {@link Scheduler}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/subscribeOn.png">
     * 
     * @param scheduler the {@link Scheduler} to perform subscription and
     *                  unsubscription actions on
     * @return the source Observable modified so that its subscriptions and
     *         unsubscriptions happen on the specified {@link Scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#subscribeon">RxJava Wiki: subscribeOn()</a>
     */
    public Observable<T> subscribeOn(Scheduler scheduler) {
        return create(OperationSubscribeOn.subscribeOn(this, scheduler));
    }

    /**
     * Asynchronously notify {@link Observer}s on the specified
     * {@link Scheduler}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/observeOn.png">
     * 
     * @param scheduler the {@link Scheduler} to notify {@link Observer}s on
     * @return the source Observable modified so that its {@link Observer}s are
     *         notified on the specified {@link Scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#observeon">RxJava Wiki: observeOn()</a>
     */
    public Observable<T> observeOn(Scheduler scheduler) {
        return create(OperationObserveOn.observeOn(this, scheduler));
    }

    /**
     * Returns an Observable that reverses the effect of
     * {@link #materialize materialize} by transforming the {@link Notification}
     * objects emitted by the source Observable into the items or notifications
     * they represent.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/dematerialize.png">
     * 
     * @return an Observable that emits the items and notifications embedded in
     *         the {@link Notification} objects emitted by the source Observable
     * @throws Throwable if the source Observable is not of type
     *                   {@code Observable<Notification<T>>}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#dematerialize">RxJava Wiki: dematerialize()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229047.aspx">MSDN: Observable.dematerialize</a>
     */
    @SuppressWarnings("unchecked")
    public <T2> Observable<T2> dematerialize() {
        return create(OperationDematerialize.dematerialize((Observable<? extends Notification<? extends T2>>) this));
    }

    /**
     * Instruct an Observable to pass control to another Observable rather than
     * invoking {@link Observer#onError onError} if it encounters an error.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/onErrorResumeNext.png">
     * <p>
     * By default, when an Observable encounters an error that prevents it from
     * emitting the expected item to its {@link Observer}, the Observable
     * invokes its Observer's <code>onError</code> method, and then quits
     * without invoking any more of its Observer's methods. The
     * <code>onErrorResumeNext</code> method changes this behavior. If you pass
     * a function that returns an Observable (<code>resumeFunction</code>) to
     * <code>onErrorResumeNext</code>, if the original Observable encounters an
     * error, instead of invoking its Observer's <code>onError</code> method, it
     * will instead relinquish control to the Observable returned from
     * <code>resumeFunction</code>, which will invoke the Observer's
     * {@link Observer#onNext onNext} method if it is able to do so. In such a
     * case, because no Observable necessarily invokes <code>onError</code>, the
     * Observer may never know that an error happened.
     * <p>
     * You can use this to prevent errors from propagating or to supply fallback
     * data should errors be encountered.
     * 
     * @param resumeFunction a function that returns an Observable that will
     *                       take over if the source Observable encounters an
     *                       error
     * @return the original Observable, with appropriately modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#onerrorresumenext">RxJava Wiki: onErrorResumeNext()</a>
     */
    public Observable<T> onErrorResumeNext(final Func1<Throwable, ? extends Observable<? extends T>> resumeFunction) {
        return create(OperationOnErrorResumeNextViaFunction.onErrorResumeNextViaFunction(this, resumeFunction));
    }

    /**
     * Instruct an Observable to pass control to another Observable rather than
     * invoking {@link Observer#onError onError} if it encounters an error.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/onErrorResumeNext.png">
     * <p>
     * By default, when an Observable encounters an error that prevents it from
     * emitting the expected item to its {@link Observer}, the Observable
     * invokes its Observer's <code>onError</code> method, and then quits
     * without invoking any more of its Observer's methods. The
     * <code>onErrorResumeNext</code> method changes this behavior. If you pass
     * another Observable (<code>resumeSequence</code>) to an Observable's
     * <code>onErrorResumeNext</code> method, if the original Observable
     * encounters an error, instead of invoking its Observer's
     * <code>onError</code> method, it will instead relinquish control to
     * <code>resumeSequence</code> which will invoke the Observer's
     * {@link Observer#onNext onNext} method if it is able to do so. In such a
     * case, because no Observable necessarily invokes <code>onError</code>, the
     * Observer may never know that an error happened.
     * <p>
     * You can use this to prevent errors from propagating or to supply fallback
     * data should errors be encountered.
     * 
     * @param resumeSequence a function that returns an Observable that will
     *                       take over if the source Observable encounters an
     *                       error
     * @return the original Observable, with appropriately modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#onerrorresumenext">RxJava Wiki: onErrorResumeNext()</a>
     */
    public Observable<T> onErrorResumeNext(final Observable<? extends T> resumeSequence) {
        return create(OperationOnErrorResumeNextViaObservable.onErrorResumeNextViaObservable(this, resumeSequence));
    }

    /**
     * Instruct an Observable to pass control to another Observable rather than
     * invoking {@link Observer#onError onError} if it encounters an error of
     * type {@link java.lang.Exception}.
     * <p>
     * This differs from {@link #onErrorResumeNext} in that this one does not
     * handle {@link java.lang.Throwable} or {@link java.lang.Error} but lets
     * those continue through.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/onExceptionResumeNextViaObservable.png">
     * <p>
     * By default, when an Observable encounters an error that prevents it from
     * emitting the expected item to its {@link Observer}, the Observable
     * invokes its Observer's <code>onError</code> method, and then quits
     * without invoking any more of its Observer's methods. The
     * <code>onErrorResumeNext</code> method changes this behavior. If you pass
     * another Observable (<code>resumeSequence</code>) to an Observable's
     * <code>onErrorResumeNext</code> method, if the original Observable
     * encounters an error, instead of invoking its Observer's
     * <code>onError</code> method, it will instead relinquish control to
     * <code>resumeSequence</code> which will invoke the Observer's
     * {@link Observer#onNext onNext} method if it is able to do so. In such a
     * case, because no Observable necessarily invokes <code>onError</code>,
     * the Observer may never know that an error happened.
     * <p>
     * You can use this to prevent errors from propagating or to supply fallback
     * data should errors be encountered.
     * 
     * @param resumeSequence a function that returns an Observable that will
     *                       take over if the source Observable encounters an
     *                       error
     * @return the original Observable, with appropriately modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#onexceptionresumenextviaobservable">RxJava Wiki: onExceptionResumeNextViaObservable()</a>
     */
    public Observable<T> onExceptionResumeNext(final Observable<? extends T> resumeSequence) {
        return create(OperationOnExceptionResumeNextViaObservable.onExceptionResumeNextViaObservable(this, resumeSequence));
    }

    /**
     * Instruct an Observable to emit an item (returned by a specified function)
     * rather than invoking {@link Observer#onError onError} if it encounters an
     * error.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/onErrorReturn.png">
     * <p>
     * By default, when an Observable encounters an error that prevents it from
     * emitting the expected item to its {@link Observer}, the Observable
     * invokes its Observer's <code>onError</code> method, and then quits
     * without invoking any more of its Observer's methods. The
     * <code>onErrorReturn</code> method changes this behavior. If you pass a
     * function (<code>resumeFunction</code>) to an Observable's
     * <code>onErrorReturn</code> method, if the original Observable encounters
     * an error, instead of invoking its Observer's <code>onError</code> method,
     * it will instead emit the return value of <code>resumeFunction</code>.
     * <p>
     * You can use this to prevent errors from propagating or to supply fallback
     * data should errors be encountered.
     * 
     * @param resumeFunction a function that returns an item that the new
     *                       Observable will emit if the source Observable
     *                       encounters an error
     * @return the original Observable with appropriately modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#onerrorreturn">RxJava Wiki: onErrorReturn()</a>
     */
    public Observable<T> onErrorReturn(Func1<Throwable, ? extends T> resumeFunction) {
        return create(OperationOnErrorReturn.onErrorReturn(this, resumeFunction));
    }

    /**
     * Returns an Observable that applies a function of your choosing to the
     * first item emitted by a source Observable, then feeds the result of that
     * function along with the second item emitted by the source Observable into
     * the same function, and so on until all items have been emitted by the
     * source Observable, and emits the final result from the final call to your
     * function as its sole item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/reduce.png">
     * <p>
     * This technique, which is called "reduce" here, is sometimes called
     * "aggregate," "fold," "accumulate," "compress," or "inject" in other
     * programming contexts. Groovy, for instance, has an <code>inject</code>
     * method that does a similar operation on lists.
     * 
     * @param accumulator an accumulator function to be invoked on each item
     *                    emitted by the source Observable, whose result will
     *                    be used in the next accumulator call
     * @return an Observable that emits a single item that is the result of
     *         accumulating the output from the source Observable
     * @throws IllegalArgumentException if the source Observable emits no items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#reduce">RxJava Wiki: reduce()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229154.aspx">MSDN: Observable.Aggregate</a>
     * @see <a href="http://en.wikipedia.org/wiki/Fold_(higher-order_function)">Wikipedia: Fold (higher-order function)</a>
     */
    public Observable<T> reduce(Func2<T, T, T> accumulator) {
        /*
         * Discussion and confirmation of implementation at https://github.com/Netflix/RxJava/issues/423#issuecomment-27642532
         * 
         * It should use last() not takeLast(1) since it needs to emit an error if the sequence is empty.
         */
        return create(OperationScan.scan(this, accumulator)).last();
    }

    /**
     * Returns an Observable emits the count of the total number of items
     * emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/count.png">
     * 
     * @return an Observable that emits the number of elements emitted by the
     *         source Observable as its single item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#count-and-longcount">RxJava Wiki: count()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229470.aspx">MSDN: Observable.Count</a>
     * @see #longCount()
     */
    public Observable<Integer> count() {
        return reduce(0, new Func2<Integer, T, Integer>() {
            @Override
            public Integer call(Integer t1, T t2) {
                return t1 + 1;
            }
        });
    }

    /**
     * Returns an Observable that emits the sum of all the Integers emitted by
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.png">
     * 
     * @param source source Observable to compute the sum of
     * @return an Observable that emits the sum of all the Integers emitted by
     *         the source Observable as its single item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public static Observable<Integer> sumInteger(Observable<Integer> source) {
        return OperationSum.sum(source);
    }
    
    @Deprecated
    public static Observable<Integer> sum(Observable<Integer> source) {
        return OperationSum.sum(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Longs emitted by the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.png">
     * 
     * @param source source Observable to compute the sum of
     * @return an Observable that emits the sum of all the Longs emitted by the
     *         source Observable as its single item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public static Observable<Long> sumLong(Observable<Long> source) {
        return OperationSum.sumLongs(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Floats emitted by the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.png">
     * 
     * @param source source Observable to compute the sum of
     * @return an Observable that emits the sum of all the Floats emitted by the
     *         source Observable as its single item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public static Observable<Float> sumFloat(Observable<Float> source) {
        return OperationSum.sumFloats(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Doubles emitted by
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.png">
     * 
     * @param source source Observable to compute the sum of
     * @return an Observable that emits the sum of all the Doubles emitted by
     *         the source Observable as its single item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public static Observable<Double> sumDouble(Observable<Double> source) {
        return OperationSum.sumDoubles(source);
    }

    /**
     * Create an Observable that extracts an integer from each of the items
     * emitted by the source Observable via a function you specify, and then
     * emits the sum of these integers.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.f.png">
     * 
     * @param valueExtractor the function to extract an integer from each item
     *                       emitted by the source Observable
     * @return an Observable that emits the integer sum of the integer values
     *         corresponding to the items emitted by the source Observable
     *         transformed by the provided function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public Observable<Integer> sumInteger(Func1<? super T, Integer> valueExtractor) {
        return create(new OperationSum.SumIntegerExtractor<T>(this, valueExtractor));
    }

    /**
     * Create an Observable that extracts a long from each of the items emitted
     * by the source Observable via a function you specify, and then emits the
     * sum of these longs.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.f.png">
     * 
     * @param valueExtractor the function to extract a long from each item
     *                       emitted by the source Observable
     * @return an Observable that emits the long sum of the integer values
     *         corresponding to the items emitted by the source Observable
     *         transformed by the provided function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public Observable<Long> sumLong(Func1<? super T, Long> valueExtractor) {
        return create(new OperationSum.SumLongExtractor<T>(this, valueExtractor));
    }

    /**
     * Create an Observable that extracts a float from each of the items emitted
     * by the source Observable via a function you specify, and then emits the
     * sum of these floats.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.f.png">
     * 
     * @param valueExtractor the function to extract a float from each item
     *                       emitted by the source Observable
     * @return an Observable that emits the float sum of the integer values
     *         corresponding to the items emitted by the source Observable
     *         transformed by the provided function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public Observable<Float> sumFloat(Func1<? super T, Float> valueExtractor) {
        return create(new OperationSum.SumFloatExtractor<T>(this, valueExtractor));
    }

    /**
     * Create an Observable that extracts a double from each of the items
     * emitted by the source Observable via a function you specify, and then
     * emits the sum of these doubles.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sum.f.png">
     * 
     * @param valueExtractor the function to extract a double from each item
     *                       emitted by the source Observable
     * @return an Observable that emits the double sum of the integer values
     *         corresponding to the items emitted by the source Observable
     *         transformed by the provided function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public Observable<Double> sumDouble(Func1<? super T, Double> valueExtractor) {
        return create(new OperationSum.SumDoubleExtractor<T>(this, valueExtractor));
    }
    
    /**
     * Returns an Observable that computes the average of the Integers emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.png">
     * 
     * @param source source observable to compute the average of
     * @return an Observable that emits the average of all the Integers emitted
     *         by the source Observable as its single item
     * @throws IllegalArgumentException if the source Observable emits no items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public static Observable<Integer> averageInteger(Observable<Integer> source) {
        return OperationAverage.average(source);
    }
    
    @Deprecated
    public static Observable<Integer> average(Observable<Integer> source) {
        return OperationAverage.average(source);
    }

    /**
     * Returns an Observable that computes the average of the Longs emitted by
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.png">
     * 
     * @param source source Observable to compute the average of
     * @return an Observable that emits the average of all the Longs emitted by
     *         the source Observable as its single item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public static Observable<Long> averageLong(Observable<Long> source) {
        return OperationAverage.averageLongs(source);
    }

    /**
     * Returns an Observable that computes the average of the Floats emitted by
     * the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.png">
     * 
     * @param source source Observable to compute the average of
     * @return an Observable that emits the average of all the Floats emitted by
     *         the source Observable as its single item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public static Observable<Float> averageFloat(Observable<Float> source) {
        return OperationAverage.averageFloats(source);
    }

    /**
     * Returns an Observable that emits the average of the Doubles emitted
     * by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.png">
     * 
     * @param source source Observable to compute the average of
     * @return an Observable that emits the average of all the Doubles emitted
     *         by the source Observable as its single item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public static Observable<Double> averageDouble(Observable<Double> source) {
        return OperationAverage.averageDoubles(source);
    }

    /**
     * Create an Observable that transforms items emitted by the source
     * Observable into integers by using a function you provide and then emits
     * the integer average of the complete sequence of transformed values.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.f.png">
     * 
     * @param valueExtractor the function to transform an item emitted by the
     *                       source Observable into an integer
     * @return an Observable that emits the integer average of the complete
     *         sequence of items emitted by the source Observable when
     *         transformed into integers by the specified function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public Observable<Integer> averageInteger(Func1<? super T, Integer> valueExtractor) {
        return create(new OperationAverage.AverageIntegerExtractor<T>(this, valueExtractor));
    }

    /**
     * Create an Observable that transforms items emitted by the source
     * Observable into longs by using a function you provide and then emits
     * the long average of the complete sequence of transformed values.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.f.png">
     * 
     * @param valueExtractor the function to transform an item emitted by the
     *                       source Observable into a long
     * @return an Observable that emits the long average of the complete
     *         sequence of items emitted by the source Observable when
     *         transformed into longs by the specified function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public Observable<Long> averageLong(Func1<? super T, Long> valueExtractor) {
        return create(new OperationAverage.AverageLongExtractor<T>(this, valueExtractor));
    }

    /**
     * Create an Observable that transforms items emitted by the source
     * Observable into floats by using a function you provide and then emits
     * the float average of the complete sequence of transformed values.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.f.png">
     * 
     * @param valueExtractor the function to transform an item emitted by the
     *                       source Observable into a float
     * @return an Observable that emits the float average of the complete
     *         sequence of items emitted by the source Observable when
     *         transformed into floats by the specified function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public Observable<Float> averageFloat(Func1<? super T, Float> valueExtractor) {
        return create(new OperationAverage.AverageFloatExtractor<T>(this, valueExtractor));
    }

    /**
     * Create an Observable that transforms items emitted by the source
     * Observable into doubles by using a function you provide and then emits
     * the double average of the complete sequence of transformed values.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/average.f.png">
     * 
     * @param valueExtractor the function to transform an item emitted by the
     *                       source Observable into a double
     * @return an Observable that emits the double average of the complete
     *         sequence of items emitted by the source Observable when
     *         transformed into doubles by the specified function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#averageinteger-averagelong-averagefloat-and-averagedouble">RxJava Wiki: averageDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.average.aspx">MSDN: Observable.Average</a>
     */
    public Observable<Double> averageDouble(Func1<? super T, Double> valueExtractor) {
        return create(new OperationAverage.AverageDoubleExtractor<T>(this, valueExtractor));
    }

    /**
     * Returns an Observable that emits the minimum item emitted by the source
     * Observable. If there is more than one such item, it returns the
     * last-emitted one.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/min.png">
     *
     * @param source an Observable to determine the minimum item of
     * @return an Observable that emits the minimum item emitted by the source
     *         Observable
     * @throws IllegalArgumentException if the source is empty
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229715.aspx">MSDN: Observable.Min</a>
     */
    public static <T extends Comparable<? super T>> Observable<T> min(Observable<T> source) {
        return OperationMinMax.min(source);
    }

    /**
     * Returns an Observable that emits the minimum item emitted by the source
     * Observable, according to a specified comparator. If there is more than
     * one such item, it returns the last-emitted one.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/min.png">
     *
     * @param comparator the comparer used to compare elements
     * @return an Observable that emits the minimum item according to the
     *         specified comparator
     * @throws IllegalArgumentException if the source is empty
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#min">RxJava Wiki: min()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229095.aspx">MSDN: Observable.Min</a>
     */
    public Observable<T> min(Comparator<? super T> comparator) {
        return OperationMinMax.min(this, comparator);
    }

    /**
     * Returns an Observable that emits a List of items emitted by the source
     * Observable that have the minimum key value. For a source Observable that
     * emits no items, the resulting Observable emits an empty List.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/minBy.png">
     *
     * @param selector the key selector function
     * @return an Observable that emits a List of the items from the source
     *         Observable that had the minimum key value
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#minby">RxJava Wiki: minBy()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228970.aspx">MSDN: Observable.MinBy</a>
     */
    public <R extends Comparable<? super R>> Observable<List<T>> minBy(Func1<T, R> selector) {
        return OperationMinMax.minBy(this, selector);
    }

    /**
     * Returns an Observable that emits a List of items emitted by the source
     * Observable that have the minimum key value according to a given
     * comparator function. For a source Observable that emits no items, the
     * resulting Observable emits an empty List.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/minBy.png">
     *
     * @param selector the key selector function
     * @param comparator the comparator used to compare key values
     * @return an Observable that emits a List of the items emitted by the
     *         source Observable that had the minimum key value according to the
     *         specified comparator
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#minby">RxJava Wiki: minBy()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228970.aspx">MSDN: Observable.MinBy</a>
     */
    public <R> Observable<List<T>> minBy(Func1<T, R> selector, Comparator<? super R> comparator) {
        return OperationMinMax.minBy(this, selector, comparator);
    }

    /**
     * Returns an Observable that emits the maximum item emitted by the source
     * Observable. If there is more than one item with the same maximum value,
     * it emits the last-emitted of these.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/max.png">
     *
     * @param source an Observable to scan for the maximum emitted item
     * @return an Observable that emits this maximum item from the source
     * @throws IllegalArgumentException if the source is empty
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#max">RxJava Wiki: max()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211837.aspx">MSDN: Observable.Max</a>
     */
    public static <T extends Comparable<? super T>> Observable<T> max(Observable<T> source) {
        return OperationMinMax.max(source);
    }

    /**
     * Returns an Observable that emits the maximum item emitted by the source
     * Observable, according to the specified comparator. If there is more than
     * one item with the same maximum value, it emits the last-emitted of these.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/max.png">
     *
     * @param comparator the comparer used to compare items
     * @return an Observable that emits the maximum item emitted by the source
     *         Observable, according to the specified comparator
     * @throws IllegalArgumentException if the source is empty
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#max">RxJava Wiki: max()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211635.aspx">MSDN: Observable.Max</a>
     */
    public Observable<T> max(Comparator<? super T> comparator) {
        return OperationMinMax.max(this, comparator);
    }

    /**
     * Returns an Observable that emits a List of items emitted by the source
     * Observable that have the maximum key value. For a source Observable that
     * emits no items, the resulting Observable emits an empty List.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/maxBy.png">
     *
     * @param selector the key selector function
     * @return an Observable that emits a List of those items emitted by the
     *         source Observable that had the maximum key value
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#maxby">RxJava Wiki: maxBy()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229058.aspx">MSDN: Observable.MaxBy</a>
     */
    public <R extends Comparable<? super R>> Observable<List<T>> maxBy(Func1<T, R> selector) {
        return OperationMinMax.maxBy(this, selector);
    }

    /**
     * Returns an Observable that emits a List of items emitted by the source
     * Observable that have the maximum key value according to a specified
     * comparator. For a source Observable that emits no items, the resulting
     * Observable emits an empty List.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/maxBy.png">
     *
     * @param selector the key selector function
     * @param comparator the comparator used to compare key values
     * @return an Observable that emits a List of those items emitted by the
     *         source Observable that had the maximum key value according to the
     *         specified comparator
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#maxby">RxJava Wiki: maxBy()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244330.aspx">MSDN: Observable.MaxBy</a>
     */
    public <R> Observable<List<T>> maxBy(Func1<T, R> selector, Comparator<? super R> comparator) {
        return OperationMinMax.maxBy(this, selector, comparator);
    }

    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription
     * to the underlying Observable that will replay all of its items and
     * notifications to any future {@link Observer}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.png">
     * 
     * @return a {@link ConnectableObservable} that upon connection causes the
     *         source Observable to emit items to its {@link Observer}s
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     */
    public ConnectableObservable<T> replay() {
        return OperationMulticast.multicast(this, ReplaySubject.<T> create());
    }
    
    /**
     * Returns a {@link ConnectableObservable} that shares a single subscription
     * to the underlying Observable that will replay all of its items and
     * notifications to any future {@link Observer} on the given scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.s.png">
     *
     * @param scheduler the scheduler on which the Observers will observe the
     *                  emitted items
     * @return a {@link ConnectableObservable} that shares a single subscription
     *         to the source Observable that will replay all of its items and
     *         notifications to any future {@link Observer} on the given
     *         scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211699.aspx">MSDN: Observable.Replay</a>
     */
    public ConnectableObservable<T> replay(Scheduler scheduler) {
         return OperationMulticast.multicast(this, OperationReplay.createScheduledSubject(ReplaySubject.<T>create(), scheduler));
    }

    /**
     * Returns a connectable observable sequence that shares a single
     * subscription to the source Observable that replays at most
     * {@code bufferSize} items emitted by that Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.n.png">
     * 
     * @param bufferSize the buffer size
     * @return a connectable observable sequence that shares a single 
     *         subscription to the source Observable and replays at most
     *         {@code bufferSize} items emitted by that Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211976.aspx">MSDN: Observable.Replay</a>
     */
    public ConnectableObservable<T> replay(int bufferSize) {
        return OperationMulticast.multicast(this, OperationReplay.<T>replayBuffered(bufferSize));
    }

    /**
     * Returns a connectable observable sequence that shares a single 
     * subscription to the source Observable and replays at most
     * {@code bufferSize} items emitted by that Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.ns.png">
     * 
     * @param bufferSize the buffer size
     * @param scheduler the scheduler on which the Observers will observe the
     *                  emitted items
     * @return a connectable observable sequence that shares a single 
     *         subscription to the source Observable and replays at most
     *         {@code bufferSize} items emitted by that Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229814.aspx">MSDN: Observable.Replay</a>
     */
    public ConnectableObservable<T> replay(int bufferSize, Scheduler scheduler) {
        return OperationMulticast.multicast(this, 
                OperationReplay.createScheduledSubject(
                OperationReplay.<T>replayBuffered(bufferSize), scheduler));
    }
    
    /**
     * Returns a connectable observable sequence that shares a single 
     * subscription to the source Observable and replays all items emitted by
     * that Observable within a time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.t.png">
     * 
     * @param time the window length
     * @param unit the window length time unit
     * @return a connectable observable sequence that shares a single 
     *         subscription to the source Observable and that replays all items
     *         emitted by that Observable during the window defined by
     *         {@code time} and {@code unit}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229232.aspx">MSDN: Observable.Replay</a>
     */
    public ConnectableObservable<T> replay(long time, TimeUnit unit) {
        return replay(time, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * Returns a connectable observable sequence that shares a single 
     * subscription to the source Observable and replays all items emitted by
     * that Observable within a time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.ts.png">
     * 
     * @param time the window length
     * @param unit the window length time unit
     * @param scheduler the scheduler that is used as a time source for the
     *                  window
     * @return a connectable observable sequence that shares a single 
     *         subscription to the source Observable and replays all items
     *         emitted by that Observable within the window defined by
     *         {@code time} and {@code unit}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211811.aspx">MSDN: Observable.Replay</a>
     */
    public ConnectableObservable<T> replay(long time, TimeUnit unit, Scheduler scheduler) {
        return OperationMulticast.multicast(this, OperationReplay.<T>replayWindowed(time, unit, -1, scheduler));
    }

    /**
     * Returns a connectable observable sequence that shares a single 
     * subscription to the underlying sequence replaying {@code bufferSize}
     * notifications within window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.nt.png">
     * 
     * @param bufferSize the buffer size
     * @param time the window length
     * @param unit the window length time unit
     * @return Returns a connectable observable sequence that shares a single 
     *         subscription to the underlying sequence replaying bufferSize notifications within window
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229874.aspx">MSDN: Observable.Replay</a>
     */
    public ConnectableObservable<T> replay(int bufferSize, long time, TimeUnit unit) {
         return replay(bufferSize, time, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * Returns a connectable observable sequence that shares a single 
     * subscription to the underlying sequence and that replays a maximum of
     * {@code bufferSize} items that are emitted within the window defined by
     * {@code time} and {@code unit}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/replay.nts.png">
     * 
     * @param bufferSize the buffer size
     * @param time the window length
     * @param unit the window length time unit
     * @param scheduler the scheduler that is used as a time source for the
     *                  window
     * @return a connectable observable sequence that shares a single 
     *         subscription to the underlying sequence that replays a maximum of
     *         {@code bufferSize} items that are emitted within the window
     *         defined by {@code time} and {@code unit}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211759.aspx">MSDN: Observable.Replay</a>
     */
    public ConnectableObservable<T> replay(int bufferSize, long time, TimeUnit unit, Scheduler scheduler) {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize < 0");
        }
        return OperationMulticast.multicast(this, OperationReplay.<T>replayWindowed(time, unit, bufferSize, scheduler));
    }
    
    /**
     * Returns an observable sequence that is the result of invoking the
     * selector on a connectable observable sequence that shares a single
     * subscription to the underlying sequence and starts with initial value.
     * 
     * @param <R> the return element type
     * @param selector the selector function which can use the multicasted 
     *                 this sequence as many times as needed, without causing 
     *                 multiple subscriptions to this sequence
     * @return an observable sequence that is the result of invoking the
     *         selector on a connectable observable sequence that shares a
     *         single subscription to the underlying sequence and starts with
     *         initial value
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229653.aspx">MSDN: Observable.Replay</a>
     */
    public <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public Subject<T, T> call() {
                return ReplaySubject.create();
            }
        }, selector);
    }

    /**
     * Returns an observable sequence that is the result of invoking the 
     * selector on a connectable observable sequence that shares a single 
     * subscription to the underlying sequence replaying all notifications.
     * 
     * @param <R> the return element type
     * @param selector the selector function which can use the multicasted 
     *                 this sequence as many times as needed, without causing 
     *                 multiple subscriptions to this sequence
     * @param scheduler the scheduler where the replay is observed
     * @return an observable sequence that is the result of invoking the 
     *         selector on a connectable observable sequence that shares a
     *         single subscription to the underlying sequence replaying all
     *         notifications
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211644.aspx">MSDN: Observable.Replay</a>
     */
    public <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final Scheduler scheduler) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public Subject<T, T> call() {
                return OperationReplay.createScheduledSubject(ReplaySubject.<T>create(), scheduler);
            }
        }, selector);
    }

    /**
     * Returns an observable sequence that is the result of invoking the 
     * selector on a connectable observable sequence that shares a single 
     * subscription to the underlying sequence replaying {@code bufferSize}
     * notifications.
     * 
     * @param <R> the return element type
     * @param selector the selector function which can use the multicasted 
     *                 this sequence as many times as needed, without causing 
     *                 multiple subscriptions to this sequence
     * @param bufferSize the buffer size
     * @return an observable sequence that is the result of invoking the 
     *         selector on a connectable observable sequence that shares a
     *         single subscription to the underlying sequence replaying
     *         {@code bufferSize} notifications
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211675.aspx">MSDN: Observable.Replay</a>
     */
    public <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final int bufferSize) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public Subject<T, T> call() {
                return OperationReplay.replayBuffered(bufferSize);
            }
        }, selector);
    }

    /**
     * Returns an observable sequence that is the result of invoking the 
     * selector on a connectable observable sequence that shares a single 
     * subscription to the underlying sequence replaying {@code bufferSize}
     * notifications.
     * 
     * @param <R> the return element type
     * @param selector the selector function which can use the multicasted 
     *                 this sequence as many times as needed, without causing 
     *                 multiple subscriptions to this sequence
     * @param bufferSize the buffer size
     * @param scheduler the scheduler where the replay is observed
     * @return an observable sequence that is the result of invoking the 
     *         selector on a connectable observable sequence that shares a
     *         single subscription to the underlying sequence replaying
     *         {@code bufferSize} notifications
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229928.aspx">MSDN: Observable.Replay</a>
     */
    public <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final int bufferSize, final Scheduler scheduler) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public Subject<T, T> call() {
                return OperationReplay.<T>createScheduledSubject(OperationReplay.<T>replayBuffered(bufferSize), scheduler);
            }
        }, selector);
    }

    /**
     * Returns an observable sequence that is the result of invoking the
     * selector on a connectable observable sequence that shares a single 
     * subscription to the underlying sequence replaying all notifications
     * within window.
     * 
     * @param <R> the return element type
     * @param selector the selector function which can use the multicasted 
     *                 this sequence as many times as needed, without causing 
     *                 multiple subscriptions to this sequence
     * @param time the window length
     * @param unit the window length time unit
     * @return an observable sequence that is the result of invoking the
     *         selector on a connectable observable sequence that shares a
     *         single subscription to the underlying sequence replaying all
     *         notifications within window
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229526.aspx">MSDN: Observable.Replay</a>
     */
    public <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, long time, TimeUnit unit) {
        return replay(selector, time, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * Returns an observable sequence that is the result of invoking the 
     * selector on a connectable observable sequence that shares a single 
     * subscription to the underlying sequence replaying all notifications
     * within window.
     * 
     * @param <R> the return element type
     * @param selector the selector function which can use the multicasted 
     *                 this sequence as many times as needed, without causing 
     *                 multiple subscriptions to this sequence
     * @param time the window length
     * @param unit the window length time unit
     * @param scheduler the scheduler that is used as a time source for the
     *                  window
     * @return an observable sequence that is the result of invoking the 
     *         selector on a connectable observable sequence that shares a
     *         single subscription to the underlying sequence replaying all
     *         notifications within window
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244327.aspx">MSDN: Observable.Replay</a>
     */
    public <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final long time, final TimeUnit unit, final Scheduler scheduler) {
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public Subject<T, T> call() {
                return OperationReplay.replayWindowed(time, unit, -1, scheduler);
            }
        }, selector);
    }

    /**
     * Returns an observable sequence that is the result of invoking the 
     * selector on a connectable observable sequence that shares a single 
     * subscription to the underlying sequence replaying {@code bufferSize}
     * notifications within window.
     * 
     * @param <R> the return element type
     * @param selector the selector function which can use the multicasted 
     *                 this sequence as many times as needed, without causing 
     *                 multiple subscriptions to this sequence
     * @param bufferSize the buffer size
     * @param time the window length
     * @param unit the window length time unit
     * @return an observable sequence that is the result of invoking the 
     *         selector on a connectable observable sequence that shares a
     *         single subscription to the underlying sequence replaying
     *         {@code bufferSize} notifications within window
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228952.aspx">MSDN: Observable.Replay</a>
     */
    public <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, int bufferSize, long time, TimeUnit unit) {
        return replay(selector, bufferSize, time, unit, Schedulers.threadPoolForComputation());
    }


    /**
     * Returns an observable sequence that is the result of invoking the 
     * selector on a connectable observable sequence that shares a single 
     * subscription to the underlying sequence replaying {@code bufferSize}
     * notifications within window.
     * 
     * @param <R> the return element type
     * @param selector the selector function which can use the multicasted 
     *                 this sequence as many times as needed, without causing 
     *                 multiple subscriptions to this sequence
     * @param bufferSize the buffer size
     * @param time the window length
     * @param unit the window length time unit
     * @param scheduler the scheduler which is used as a time source for the
     *                  window
     * @return an observable sequence that is the result of invoking the 
     *         selector on a connectable observable sequence that shares a
     *         single subscription to the underlying sequence replaying
     *         {@code bufferSize} notifications within window
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablereplay">RxJava Wiki: replay()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229404.aspx">MSDN: Observable.Replay</a>
     */
    public <R> Observable<R> replay(Func1<? super Observable<T>, ? extends Observable<R>> selector, final int bufferSize, final long time, final TimeUnit unit, final Scheduler scheduler) {
        if (bufferSize < 0) {
            throw new IllegalArgumentException("bufferSize < 0");
        }
        return OperationMulticast.multicast(this, new Func0<Subject<T, T>>() {
            @Override
            public Subject<T, T> call() {
                return OperationReplay.replayWindowed(time, unit, bufferSize, scheduler);
            }
        }, selector);
    }
    
    /**
     * Retry subscription to the source Observable when it calls
     * <code>onError</code> up to a certain number of retries.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/retry.png">
     * <p>
     * If the source Observable calls {@link Observer#onError}, this method will
     * resubscribe to the source Observable for a maximum of
     * <code>retryCount</code> resubscriptions.
     * <p>
     * Any and all items emitted by the source Observable will be emitted by
     * the resulting Observable, even those emitted during failed subscriptions.
     * For example, if an Observable fails at first but emits [1, 2] then
     * succeeds the second time and emits [1, 2, 3, 4, 5] then the complete
     * sequence of emissions and notifications would be
     * [1, 2, 1, 2, 3, 4, 5, <i>onCompleted</i>].
     * 
     * @param retryCount number of retry attempts before failing
     * @return the source Observable modified with retry logic
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#retry">RxJava Wiki: retry()</a>
     */
    public Observable<T> retry(int retryCount) {
        return create(OperationRetry.retry(this, retryCount));
    }

    /**
     * Retry subscription to the source Observable whenever it calls
     * <code>onError</code> (infinite retry count).
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/retry.png">
     * <p>
     * If the source Observable calls {@link Observer#onError}, this method will
     * resubscribe to the source Observable.
     * <p>
     * Any and all items emitted by the source Observable will be emitted by
     * the resulting Observable, even those emitted during failed subscriptions.
     * For example, if an Observable fails at first but emits [1, 2] then
     * succeeds the second time and emits [1, 2, 3, 4, 5] then the complete
     * sequence of emissions and notifications would be
     * [1, 2, 1, 2, 3, 4, 5, <i>onCompleted</i>].
     * 
     * @return the source Observable modified with retry logic
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Error-Handling-Operators#retry">RxJava Wiki: retry()</a>
     */
    public Observable<T> retry() {
        return create(OperationRetry.retry(this));
    }

    /**
     * This method has similar behavior to {@link #replay} except that this
     * auto-subscribes to the source Observable rather than returning a
     * {@link ConnectableObservable}.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/cache.png">
     * <p>
     * This is useful when you want an Observable to cache responses and you
     * can't control the subscribe/unsubscribe behavior of all the
     * {@link Observer}s.
     * <p>
     * When you call {@code cache()}, it does not yet subscribe to the
     * source Observable. This only happens when {@code subscribe} is called
     * the first time on the Observable returned by {@code cache()}.
     * <p>
     * Note: You sacrifice the ability to unsubscribe from the origin when you
     * use the <code>cache()</code> operator so be careful not to use this
     * operator on Observables that emit an infinite or very large number of
     * items that will use up memory.
     * 
     * @return an Observable that, when first subscribed to, caches all of its
     *         items and notifications for the benefit of subsequent observers
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#cache">RxJava Wiki: cache()</a>
     */
    public Observable<T> cache() {
        return create(OperationCache.cache(this));
    }

    /**
     * Perform work in parallel by sharding an {@code Observable<T>} on a
     * {@link Schedulers#threadPoolForComputation()} {@link Scheduler} and
     * return an {@code Observable<R>} with the output.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/parallel.png">
     * 
     * @param f a {@link Func1} that applies Observable operators to
     *          {@code Observable<T>} in parallel and returns an
     *          {@code Observable<R>}
     * @return an Observable with the output of the {@link Func1} executed on a
     *         {@link Scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#parallel">RxJava Wiki: parallel()</a>
     */
    public <R> Observable<R> parallel(Func1<Observable<T>, Observable<R>> f) {
        return OperationParallel.parallel(this, f);
    }

    /**
     * Perform work in parallel by sharding an {@code Observable<T>} on a
     * {@link Scheduler} and return an {@code Observable<R>} with the output.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/parallel.png">
     * 
     * @param f a {@link Func1} that applies Observable operators to
     *          {@code Observable<T>} in parallel and returns an
     *          {@code Observable<R>}
     * @param s a {@link Scheduler} to perform the work on
     * @return an Observable with the output of the {@link Func1} executed on a
     *         {@link Scheduler}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#parallel">RxJava Wiki: parallel()</a>
     */
    public <R> Observable<R> parallel(final Func1<Observable<T>, Observable<R>> f, final Scheduler s) {
        return OperationParallel.parallel(this, f, s);
    }

    /**
     * Merges an <code>Observable&lt;Observable&lt;T&gt;&gt;</code> to
     * <code>Observable&lt;Observable&lt;T&gt;&gt;</code> with the number of
     * inner Observables defined by <code>parallelObservables</code>.
     * <p>
     * For example, if the original
     * <code>Observable&lt;Observable&lt;T&gt;&gt;</code> has 100 Observables to
     * be emitted and <code>parallelObservables</code> is 8, the 100 will be
     * grouped onto 8 output Observables.
     * <p>
     * This is a mechanism for efficiently processing <i>n</i> number of
     * Observables on a smaller <i>m</i> number of resources (typically CPU
     * cores).
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/parallelMerge.png">
     * 
     * @param parallelObservables the number of Observables to merge into
     * @return an Observable of Observables constrained in number by
     *         <code>parallelObservables</code>
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#parallelmerge">RxJava Wiki: parallelMerge()</a>
     */
    public static <T> Observable<Observable<T>> parallelMerge(Observable<Observable<T>> source, int parallelObservables) {
        return OperationParallelMerge.parallelMerge(source, parallelObservables);
    }
    
    /**
     * Merges an <code>Observable&lt;Observable&lt;T&gt;&gt;</code> to
     * <code>Observable&lt;Observable&lt;T&gt;&gt;</code> with the number of
     * inner Observables defined by <code>parallelObservables</code> and runs
     * each Observable on the defined Scheduler.
     * <p>
     * For example, if the original
     * <code>Observable&lt;Observable&lt;T&gt;&gt;</code> has 100 Observables to
     * be emitted and <code>parallelObservables</code> is 8, the 100 will be
     * grouped onto 8 output Observables.
     * <p>
     * This is a mechanism for efficiently processing <i>n</i> number of
     * Observables on a smaller <i>m</i> number of resources (typically CPU
     * cores).
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/parallelMerge.png">
     * 
     * @param parallelObservables the number of Observables to merge into
     * @param scheduler the Scheduler to run each Observable on
     * @return an Observable of Observables constrained in number by
     *         <code>parallelObservables</code>
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#parallelmerge">RxJava Wiki: parallelMerge()</a>
     */
    public static <T> Observable<Observable<T>> parallelMerge(Observable<Observable<T>> source, int parallelObservables, Scheduler scheduler) {
        return OperationParallelMerge.parallelMerge(source, parallelObservables, scheduler);
    }
    
    /**
     * Returns a {@link ConnectableObservable}, which waits until its
     * {@link ConnectableObservable#connect connect} method is called before it
     * begins emitting items to those {@link Observer}s that have subscribed to
     * it.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/publishConnect.png">
     * 
     * @return a {@link ConnectableObservable} that upon connection causes the
     *         source Observable to emit items to its {@link Observer}s
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablepublish-and-observablemulticast">RxJava Wiki: publish()</a>
     */
    public ConnectableObservable<T> publish() {
        return OperationMulticast.multicast(this, PublishSubject.<T> create());
    }

    /**
     * Returns a {@link ConnectableObservable} that emits only the last item
     * emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/publishLast.png">
     * 
     * @return a {@link ConnectableObservable}
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Connectable-Observable-Operators#observablepublishlast">RxJava Wiki: publishLast()</a>
     */
    public ConnectableObservable<T> publishLast() {
        return OperationMulticast.multicast(this, AsyncSubject.<T> create());
    }

    /**
     * Synonymous with <code>reduce()</code>.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/aggregate.png">
     *
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#reduce">RxJava Wiki: reduce()</a>
     * @see #reduce(Func2)
     * @deprecated use #reduce(Func2)
     */
    @Deprecated
    public Observable<T> aggregate(Func2<T, T, T> accumulator) {
        return reduce(accumulator);
    }

    /**
     * Returns an Observable that applies a function of your choosing to the
     * first item emitted by a source Observable, then feeds the result of that
     * function along with the second item emitted by an Observable into the
     * same function, and so on until all items have been emitted by the source
     * Observable, emitting the final result from the final call to your
     * function as its sole item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/reduceSeed.png">
     * <p>
     * This technique, which is called "reduce" here, is sometimec called
     * "aggregate," "fold," "accumulate," "compress," or "inject" in other
     * programming contexts. Groovy, for instance, has an <code>inject</code>
     * method that does a similar operation on lists.
     * 
     * @param initialValue the initial (seed) accumulator value
     * @param accumulator an accumulator function to be invoked on each item
     *                    emitted by the source Observable, the result of which
     *                    will be used in the next accumulator call
     * @return an Observable that emits a single item that is the result of
     *         accumulating the output from the items emitted by the source
     *         Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#reduce">RxJava Wiki: reduce()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229154.aspx">MSDN: Observable.Aggregate</a>
     * @see <a href="http://en.wikipedia.org/wiki/Fold_(higher-order_function)">Wikipedia: Fold (higher-order function)</a>
     */
    public <R> Observable<R> reduce(R initialValue, Func2<R, ? super T, R> accumulator) {
        return create(OperationScan.scan(this, initialValue, accumulator)).takeLast(1);
    }
    
    /**
     * Collect values into a single mutable data structure.
     * <p>
     * A simplified version of `reduce` that does not need to return the state on each pass.
     * <p>
     * 
     * @param state
     * @param collector
     * @return
     */
    public <R> Observable<R> collect(R state, final Action2<R, ? super T> collector) {
        Func2<R, T, R> accumulator = new Func2<R, T, R>() {

            @Override
            public R call(R state, T value) {
                collector.call(state, value);
                return state;
            }

        };
        return reduce(state, accumulator);
    }
    
    /**
     * Synonymous with <code>reduce()</code>.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/aggregateSeed.png">
     * 
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#reduce">RxJava Wiki: reduce()</a>
     * @see #reduce(Object, Func2)
     * @deprecated use #reduce(Object, Func2)
     */
    @Deprecated
    public <R> Observable<R> aggregate(R initialValue, Func2<R, ? super T, R> accumulator) {
        return reduce(initialValue, accumulator);
    }
    
    /**
     * Returns an Observable that applies a function of your choosing to the
     * first item emitted by a source Observable, then feeds the result of that
     * function along with the second item emitted by an Observable into the
     * same function, and so on until all items have been emitted by the source
     * Observable, emitting the result of each of these iterations.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/scan.png">
     * <p>
     * This sort of function is sometimes called an accumulator.
     * 
     * @param accumulator an accumulator function to be invoked on each item
     *                    emitted by the source Observable, whose result will be
     *                    emitted to {@link Observer}s via
     *                    {@link Observer#onNext onNext} and used in the next
     *                    accumulator call
     * @return an Observable that emits the results of each call to the
     *         accumulator function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#scan">RxJava Wiki: scan()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211665.aspx">MSDN: Observable.Scan</a>
     */
    public Observable<T> scan(Func2<T, T, T> accumulator) {
        return create(OperationScan.scan(this, accumulator));
    }

    /**
     * Returns an Observable that emits the results of sampling the items
     * emitted by the source Observable at a specified time interval.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sample.png">
     * 
     * @param period the sampling rate
     * @param unit the {@link TimeUnit} in which <code>period</code> is defined
     * @return an Observable that emits the results of sampling the items
     *         emitted by the source Observable at the specified time interval
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#sample-or-throttlelast">RxJava Wiki: sample()</a>
     */
    public Observable<T> sample(long period, TimeUnit unit) {
        return create(OperationSample.sample(this, period, unit));
    }

    /**
     * Returns an Observable that emits the results of sampling the items
     * emitted by the source Observable at a specified time interval.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sample.s.png">
     * 
     * @param period the sampling rate
     * @param unit the {@link TimeUnit} in which <code>period</code> is defined
     * @param scheduler the {@link Scheduler} to use when sampling
     * @return an Observable that emits the results of sampling the items
     *         emitted by the source Observable at the specified time interval
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#sample-or-throttlelast">RxJava Wiki: sample()</a>
     */
    public Observable<T> sample(long period, TimeUnit unit, Scheduler scheduler) {
        return create(OperationSample.sample(this, period, unit, scheduler));
    }
    
    /**
     * Return an Observable that emits the results of sampling the items emitted
     * by this Observable when the <code>sampler</code> Observable emits an item
     * or completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/sample.o.png">
     * 
     * @param sampler the Observable to use for sampling the source Observable
     * @return an Observable that emits the results of sampling the items
     *         emitted by this Observable whenever the <code>sampler</code>
     *         Observable emits an item or completes
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#sample-or-throttlelast">RxJava Wiki: sample()</a>
     */
    public <U> Observable<T> sample(Observable<U> sampler) {
        return create(new OperationSample.SampleWithObservable<T, U>(this, sampler));
    }
    
    /**
     * Returns an Observable that applies a function of your choosing to the
     * first item emitted by a source Observable, then feeds the result of that
     * function along with the second item emitted by an Observable into the
     * same function, and so on until all items have been emitted by the source
     * Observable, emitting the result of each of these iterations.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/scanSeed.png">
     * <p>
     * This sort of function is sometimes called an accumulator.
     * <p>
     * Note that when you pass a seed to <code>scan()</code> the resulting
     * Observable will emit that seed as its first emitted item.
     * 
     * @param initialValue the initial (seed) accumulator item
     * @param accumulator an accumulator function to be invoked on each item
     *                    emitted by the source Observable, whose result will be
     *                    emitted to {@link Observer}s via
     *                    {@link Observer#onNext onNext} and used in the next
     *                    accumulator call
     * @return an Observable that emits the results of each call to the
     *         accumulator function
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#scan">RxJava Wiki: scan()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211665.aspx">MSDN: Observable.Scan</a>
     */
    public <R> Observable<R> scan(R initialValue, Func2<R, ? super T, R> accumulator) {
        return create(OperationScan.scan(this, initialValue, accumulator));
    }

    /**
     * Returns an Observable that emits a Boolean that indicates whether all of
     * the items emitted by the source Observable satisfy a condition.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/all.png">
     * 
     * @param predicate a function that evaluates an item and returns a Boolean
     * @return an Observable that emits <code>true</code> if all items emitted
     *         by the source Observable satisfy the predicate; otherwise,
     *         <code>false</code>
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#all">RxJava Wiki: all()</a>
     */
    public Observable<Boolean> all(Func1<? super T, Boolean> predicate) {
        return create(OperationAll.all(this, predicate));
    }

    /**
     * Returns an Observable that skips the first <code>num</code> items emitted
     * by the source Observable and emits the remainder.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skip.png">
     * 
     * @param num the number of items to skip
     * @return an Observable that is identical to the source Observable except
     *         that it does not emit the first <code>num</code> items that the
     *         source Observable emits
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#skip">RxJava Wiki: skip()</a>
     */
    public Observable<T> skip(int num) {
        return create(OperationSkip.skip(this, num));
    }

    /**
     * Create an Observable that skips values before the given time ellapses.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skip.t.png">
     * 
     * @param time the length of the time window
     * @param unit the time unit
     * @return an Observable that skips values before the given time ellapses
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#skip">RxJava Wiki: skip()</a>
     */
    public Observable<T> skip(long time, TimeUnit unit) {
        return skip(time, unit, Schedulers.threadPoolForComputation());
    }
 
    /**
     * Create an Observable that skips values before the given time elapses
     * while waiting on the given scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skip.ts.png">
     * 
     * @param time the length of the time window
     * @param unit the time unit
     * @param scheduler the scheduler where the timed wait happens
     * @return an Observable that skips values before the given time elapses
     *         while waiting on the given scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#skip">RxJava Wiki: skip()</a>
     */
    public Observable<T> skip(long time, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationSkip.SkipTimed<T>(this, time, unit, scheduler));
    }
 
    /**
     * If the Observable completes after emitting a single item, return an
     * Observable containing that item. If it emits more than one item or no
     * item, throw an IllegalArgumentException.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/single.png">
     * 
     * @return an Observable that emits the single item emitted by the source
     *         Observable that matches the predicate
     * @throws IllegalArgumentException if the source emits more than one item
     *                                  or no items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#single-and-singleordefault">RxJava Wiki: single()</a>
     * @see MSDN: <code>Observable.singleAsync()</code>
     */
    public Observable<T> single() {
        return create(OperationSingle.<T> single(this));
    }

    /**
     * If the Observable completes after emitting a single item that matches a
     * predicate, return an Observable that emits that item. If the source
     * Observable emits more than one such item or no such items, throw an
     * IllegalArgumentException.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/single.p.png">
     * 
     * @param predicate a predicate function to evaluate items emitted by the
     *                  source Observable
     * @return an Observable that emits the single item emitted by the source
     *         Observable that matches the predicate
     * @throws IllegalArgumentException if the source Observable emits more than
     *                                  one item or no items matching the
     *                                  predicate
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#single-and-singleordefault">RxJava Wiki: single()</a>
     * @see MSDN: <code>Observable.singleAsync()</code>
     */
    public Observable<T> single(Func1<? super T, Boolean> predicate) {
        return filter(predicate).single();
    }

    /**
     * If the source Observable completes after emitting a single item, return
     * an Observable that emits that item. If the source Observable is empty,
     * return an Observable that emits a default item. If the source Observable
     * emits more than one item, throw an IllegalArgumentException.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/singleOrDefault.png">
     * 
     * @param defaultValue a default value to emit if the source Observable
     *                     emits no item
     * @return an Observable that emits the single item emitted by the source
     *         Observable, or default value if the source Observable is empty
     * @throws IllegalArgumentException if the source emits more than one item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#single-and-singleordefault">RxJava Wiki: single()</a>
     * @see MSDN: <code>Observable.singleOrDefaultAsync()</code>
     */
    public Observable<T> singleOrDefault(T defaultValue) {
        return create(OperationSingle.<T> singleOrDefault(this, defaultValue));
    }

    /**
     * If the Observable completes after emitting a single item that matches a
     * predicate, return an Observable that emits that item. If the source
     * Observable emits no such item, return an Observable that emits a default
     * item. If the source Observable emits more than one such item, throw an
     * IllegalArgumentException.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/singleOrDefault.p.png">
     * 
     * @param defaultValue a default value to emit if the source Observable
     *                     emits no matching items
     * @param predicate a predicate function to evaluate items emitted by the
     *                  source Observable
     * @return an Observable that emits the single item emitted by the source
     *         Observable that matches the predicate, or the default item if no
     *         emitted item matches the predicate
     * @throws IllegalArgumentException if the source emits more than one item
     *                                  matching the predicate
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#single-and-singleordefault">RxJava Wiki: single()</a>
     * @see MSDN: <code>Observable.singleOrDefaultAsync()</code>
     */
    public Observable<T> singleOrDefault(T defaultValue, Func1<? super T, Boolean> predicate) {
        return filter(predicate).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the
     * source Observable, or an <code>IllegalArgumentException</code> if the
     * source {@link Observable} is empty.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/first.png">
     * 
     * @return an Observable that emits only the very first item from the
     *         source, or an <code>IllegalArgumentException</code> if the source {@link Observable} is empty.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#first">RxJava Wiki: first()</a>
     * @see MSDN: <code>Observable.firstAsync()</code>
     */
    public Observable<T> first() {
        return take(1).single();
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the
     * source Observable that satisfies a given condition, or an
     * <code>IllegalArgumentException</code> if no such items are emitted.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/firstN.png">
     * 
     * @param predicate the condition any source emitted item has to satisfy
     * @return an Observable that emits only the very first item satisfying the
     *         given condition from the source, or an <code>IllegalArgumentException</code> if no such items are emitted.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#first">RxJava Wiki: first()</a>
     * @see MSDN: <code>Observable.firstAsync()</code>
     */
    public Observable<T> first(Func1<? super T, Boolean> predicate) {
        return takeFirst(predicate).single();
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the
     * source Observable, or a default item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/firstOrDefault.png">
     * 
     * @param defaultValue the default item to emit if the source Observable
     *                     doesn't emit anything
     * @return an Observable that emits only the very first item from the
     *         source, or a default item if the source Observable completes
     *         without emitting a single item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#firstordefault">RxJava Wiki: firstOrDefault()</a>
     * @see MSDN: <code>Observable.firstOrDefaultAsync()</code>
     */
    public Observable<T> firstOrDefault(T defaultValue) {
        return take(1).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the
     * source Observable that satisfies a given condition, or a default item
     * otherwise.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/firstOrDefaultN.png">
     * 
     * @param predicate the condition any source emitted item has to satisfy
     * @param defaultValue the default item to emit if the source Observable
     *        doesn't emit anything that satisfies the given condition
     * @return an Observable that emits only the very first item from the source
     *         that satisfies the given condition, or a default item otherwise
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#firstordefault">RxJava Wiki: firstOrDefault()</a>
     * @see MSDN: <code>Observable.firstOrDefaultAsync()</code>
     */
    public Observable<T> firstOrDefault(T defaultValue, Func1<? super T, Boolean> predicate) {
        return takeFirst(predicate).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that emits the items emitted by the source
     * Observable or a specified default item if the source Observable is empty.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/defaultIfEmpty.png">
     * 
     * @param defaultValue the item to emit if the source Observable emits no
     *                     items
     * @return an Observable that emits either the specified default item if the
     *         source Observable emits no items, or the items emitted by the
     *         source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#defaultifempty">RxJava Wiki: defaultIfEmpty()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229624.aspx">MSDN: Observable.DefaultIfEmpty</a>
     */
    public Observable<T> defaultIfEmpty(T defaultValue) {
        return create(OperationDefaultIfEmpty.defaultIfEmpty(this, defaultValue));
    }

    /**
     * Returns an Observable that emits only the first <code>num</code> items
     * emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/take.png">
     * <p>
     * This method returns an Observable that will invoke a subscribing
     * {@link Observer}'s {@link Observer#onNext onNext} function a maximum of
     * <code>num</code> times before invoking
     * {@link Observer#onCompleted onCompleted}.
     * 
     * @param num the number of items to emit
     * @return an Observable that emits only the first <code>num</code> items
     *         emitted by the source Observable, or all of the items from the
     *         source Observable if that Observable emits fewer than
     *         <code>num</code> items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#take">RxJava Wiki: take()</a>
     */
    public Observable<T> take(final int num) {
        return create(OperationTake.take(this, num));
    }

    /**
     * Create an Observable that emits the emitted items from the source
     * Observable before the time runs out.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/take.t.png">
     * 
     * @param time the length of the time window
     * @param unit the time unit
     * @return an Observable that emits the emitted items from the source
     *         Observable before the time runs out
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#take">RxJava Wiki: take()</a>
     */
    public Observable<T> take(long time, TimeUnit unit) {
        return take(time, unit, Schedulers.threadPoolForComputation());
    }
     
    /**
     * Create an Observable that emits the emitted items from the source
     * Observable before the time runs out, waiting on the given scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/take.ts.png">
     * 
     * @param time the length of the time window
     * @param unit the time unit
     * @param scheduler the scheduler used for time source
     * @return an Observable that emits the emitted items from the source
     *         Observable before the time runs out, waiting on the given
     *         scheduler
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#take">RxJava Wiki: take()</a>
     */
    public Observable<T> take(long time, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationTake.TakeTimed<T>(this, time, unit, scheduler));
    }
 
    /**
     * Returns an Observable that emits items emitted by the source Observable
     * so long as a specified condition is true.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeWhile.png">
     * 
     * @param predicate a function that evaluates an item emitted by the source
     *                  Observable and returns a Boolean
     * @return an Observable that emits the items from the source Observable so
     *         long as each item satisfies the condition defined by
     *         <code>predicate</code>
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#takewhile-and-takewhilewithindex">RxJava Wiki: takeWhile()</a>
     */
    public Observable<T> takeWhile(final Func1<? super T, Boolean> predicate) {
        return create(OperationTakeWhile.takeWhile(this, predicate));
    }

    /**
     * Returns an Observable that emits the items emitted by a source Observable
     * so long as a given predicate remains true, where the predicate can
     * operate on both the item and its index relative to the complete sequence
     * of items.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeWhileWithIndex.png">
     * 
     * @param predicate a function to test each item emitted by the source
     *                  Observable for a condition; the second parameter of the
     *                  function represents the index of the source item
     * @return an Observable that emits items from the source Observable so long
     *         as the predicate continues to return <code>true</code> for each
     *         item, then completes
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#takewhile-and-takewhilewithindex">RxJava Wiki: takeWhileWithIndex()</a>
     */
    public Observable<T> takeWhileWithIndex(final Func2<? super T, ? super Integer, Boolean> predicate) {
        return create(OperationTakeWhile.takeWhileWithIndex(this, predicate));
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeFirst.png">
     * 
     * @return an Observable that emits only the very first item from the
     *         source, or an empty Observable if the source Observable completes
     *         without emitting a single item
     * @deprecated Use <code>take(1)</code> directly.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#first">RxJava Wiki: first()</a>
     * @see MSDN: <code>Observable.firstAsync()</code>
     */
    @Deprecated
    public Observable<T> takeFirst() {
        return take(1);
    }

    /**
     * Returns an Observable that emits only the very first item emitted by the
     * source Observable that satisfies a given condition.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeFirstN.png">
     * 
     * @param predicate the condition any source emitted item has to satisfy
     * @return an Observable that emits only the very first item satisfying the
     *         given condition from the source, or an empty Observable if the
     *         source Observable completes without emitting a single matching
     *         item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#first">RxJava Wiki: first()</a>
     * @see MSDN: <code>Observable.firstAsync()</code>
     */
    public Observable<T> takeFirst(Func1<? super T, Boolean> predicate) {
        return filter(predicate).take(1);
    }

    /**
     * Returns an Observable that emits only the last <code>count</code> items
     * emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.n.png">
     * 
     * @param count the number of items to emit from the end of the sequence
     *              emitted by the source Observable
     * @return an Observable that emits only the last <code>count</code> items
     *         emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#takelast">RxJava Wiki: takeLast()</a>
     */
    public Observable<T> takeLast(final int count) {
        return create(OperationTakeLast.takeLast(this, count));
    }

    /**
     * Return an Observable which emits the items from the source Observable
     * that were emitted not before it completed minus a time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.t.png">
     * 
     * @param time the length of the time window, relative to the completion of
     *             the source Observable
     * @param unit the time unit
     * @return an Observable that emits the items from the source Observable
     *         that were emitted not before it completed minus a time window
     */
    public Observable<T> takeLast(long time, TimeUnit unit) {
        return takeLast(time, unit, Schedulers.threadPoolForComputation());
    }
    
    /**
     * Return an Observable that emits the items from the source Observable that
     * were emitted not before the source Observable completed minus a time
     * window, where the timing information is provided by the given scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.ts.png">
     * 
     * @param time the length of the time window, relative to the completion of
     *             the source Observable
     * @param unit the time unit
     * @param scheduler the Scheduler that provides the timestamps for the
     *                  Observed items
     * @return an Observable that emits the items from the source Observable
     *         that were emitted not before it completed minus a time window,
     *         where the timing information is provided by the given Scheduler
     */
    public Observable<T> takeLast(long time, TimeUnit unit, Scheduler scheduler) {
        return create(OperationTakeLast.takeLast(this, time, unit, scheduler));
    }
    
    /**
     * Return an Observable that emits at most a specified number of items from
     * the source Observable that were emitted not before it completed minus a
     * time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.tn.png">
     * 
     * @param count the maximum number of items to emit
     * @param time the length of the time window, relative to the completion of
     *             the source Observable
     * @param unit the time unit
     * @return an Observable that emits at most {@code count} items from the
     *         source Observable which were emitted not before it completed
     *         minus a time window
     */
    public Observable<T> takeLast(int count, long time, TimeUnit unit) {
        return takeLast(count, time, unit, Schedulers.threadPoolForComputation());
    }

    /**
     * Return an Observable that emits at most a specified number of items from
     * the source Observable that were emitted not before it completed minus a
     * time window, where the timing information is provided by a given
     * scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLast.tns.png">
     * 
     * @param count the maximum number of items to emit
     * @param time the length of the time window, relative to the completion of
     *             the source Observable
     * @param unit the time unit
     * @param scheduler the Scheduler that provides the timestamps for the
     *                  observed items
     * @return an Observable that emits at most {@code count} items from the
     *         source Observable which were emitted not before it completed
     *         minus a time window, where the timing information is provided by
     *         the given {@code scheduler}
     */
    public Observable<T> takeLast(int count, long time, TimeUnit unit, Scheduler scheduler) {
        if (count < 0) {
            throw new IllegalArgumentException("count >= 0 required");
        }
        return create(OperationTakeLast.takeLast(this, count, time, unit, scheduler));
    }

    /**
     * Return an Observable that emits single List containing the last
     * {@code count} elements emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.png">
     * 
     * @param count the number of items to take last
     * @return an Observable that emits a single list containing the last
     *         {@code count} elements emitted by the source Observable
     */
    public Observable<List<T>> takeLastBuffer(int count) {
        return takeLast(count).toList();
    }
    
    /**
     * Return an Observable that emits single List containing items that were
     * emitted by the source Observable not before it completed minus a time
     * window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.t.png">
     *
     * @param time the length of the time window, relative to the completion of
     *             the source Observable
     * @param unit the time unit
     * @return an Observable that emits single list containing items that were
     *         were emitted by the source Observable not before it completed
     *         minus a time window
     */
    public Observable<List<T>> takeLastBuffer(long time, TimeUnit unit) {
        return takeLast(time, unit).toList();
    }

    /**
     * Return an Observable that emits single List containing items that were
     * emitted by the source Observable not before it completed minus a time
     * window, where the timing information is provided by the given Scheduler.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.ts.png">
     *
     * @param time the length of the time window, relative to the completion of
     *             the source Observable
     * @param unit the time unit
     * @param scheduler the Scheduler that provides the timestamps for the
     *                  observed items
     * @return an Observable that emits single list containing items that were
     *         were emitted by the source Observable not before it completed
     *         minus a time window, where the timing information is provided by
     *         the given scheduler
     */
    public Observable<List<T>> takeLastBuffer(long time, TimeUnit unit, Scheduler scheduler) {
        return takeLast(time, unit, scheduler).toList();
    }

    /**
     * Return an Observable that emits a single List containing at most
     * {@code count} items from the source Observable that were emitted not
     * before it completed minus a time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.tn.png">
     *
     * @param count the maximum number of items to emit
     * @param time the length of the time window, relative to the completion of
     *             the source Observable
     * @param unit the time unit
     * @return an Observable that emits a single List containing at most
     *         {@code count} items emitted by the source Observable not before
     *         it completed minus a time window
     */
    public Observable<List<T>> takeLastBuffer(int count, long time, TimeUnit unit) {
        return takeLast(count, time, unit).toList();
    }

    /**
     * Return an Observable that emits a single List containing at most
     * {@code count} items from the source Observable that were emitted not
     * before it completed minus a time window.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeLastBuffer.tns.png">
     *
     * @param count the maximum number of items to emit
     * @param time the length of the time window, relative to the completion of
     *             the source Observable
     * @param unit the time unit
     * @param scheduler the scheduler that provides the timestamps for the
     *                  observed items
     * @return an Observable that emits a single List containing at most
     *         {@code count} items emitted by the source Observable not before
     *         it completed minus a time window
     */
    public Observable<List<T>> takeLastBuffer(int count, long time, TimeUnit unit, Scheduler scheduler) {
        return takeLast(count, time, unit, scheduler).toList();
    }
    

    /**
     * Returns an Observable that emits the items from the source Observable
     * only until the <code>other</code> Observable emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/takeUntil.png">
     * 
     * @param other the Observable whose first emitted item will cause
     *              <code>takeUntil</code> to stop emitting items from the
     *              source Observable
     * @param <E> the type of items emitted by <code>other</code>
     * @return an Observable that emits the items of the source Observable until
     *         such time as <code>other</code> emits its first item
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#takeuntil">RxJava Wiki: takeUntil()</a>
     */
    public <E> Observable<T> takeUntil(Observable<? extends E> other) {
        return OperationTakeUntil.takeUntil(this, other);
    }

    /**
     * Returns an Observable that bypasses all items from the source Observable
     * as long as the specified condition holds true, but emits all further
     * source items as soon as the condition becomes false.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipWhileWithIndex.png">
     * 
     * @param predicate a function to test each item emitted from the source
     *                  Observable for a condition. It receives the emitted item
     *                  as the first parameter and the index of the emitted item
     *                  as a second parameter.
     * @return an Observable that emits all items from the source Observable as
     *         soon as the condition becomes false
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#skipwhile-and-skipwhilewithindex">RxJava Wiki: skipWhileWithIndex()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211631.aspx">MSDN: Observable.SkipWhile</a>
     */
    public Observable<T> skipWhileWithIndex(Func2<? super T, Integer, Boolean> predicate) {
        return create(OperationSkipWhile.skipWhileWithIndex(this, predicate));
    }

    /**
     * Returns an Observable that bypasses all items from the source Observable
     * as long as a specified condition holds true, but emits all further
     * source items as soon as the condition becomes false.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipWhile.png">
     * 
     * @param predicate a function to test each item emitted from the source
     *                  Observable for a condition
     * @return an Observable that emits all items from the source Observable as
     *         soon as the condition becomes false
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#skipwhile-and-skipwhilewithindex">RxJava Wiki: skipWhile()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229685.aspx">MSDN: Observable.SkipWhile</a>
     */
    public Observable<T> skipWhile(Func1<? super T, Boolean> predicate) {
        return create(OperationSkipWhile.skipWhile(this, predicate));
    }

    /**
     * Bypasses a specified number of items at the end of an Observable
     * sequence.
     * <p>
     * This operator accumulates a queue long enough to store the first
     * <code>count</code> items. As more items are received, items are taken
     * from the front of the queue and emitted by the returned Observable. This
     * causes such items to be delayed.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipLast.png">
     * 
     * @param count number of items to bypass at the end of the source sequence
     * @return an Observable that emits the items emitted by the source
     *         Observable except for the bypassed ones at the end
     * @throws IndexOutOfBoundsException if <code>count</code> is less than zero
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#skiplast">RxJava Wiki: skipLast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211750.aspx">MSDN: Observable.SkipLast</a>
     */
    public Observable<T> skipLast(int count) {
        return create(OperationSkipLast.skipLast(this, count));
    }

    /**
     * Create an Observable that skips values emitted in a time window before
     * the source completes.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipLast.t.png">
     *
     * @param time the length of the time window
     * @param unit the time unit
     * @return an Observable that skips values emitted in a time window before
     *         the source completes
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#skiplast">RxJava Wiki: skipLast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211750.aspx">MSDN: Observable.SkipLast</a>
     */
    public Observable<T> skipLast(long time, TimeUnit unit) {
        return skipLast(time, unit, Schedulers.threadPoolForComputation());
    }
    
    /**
     * Create an Observable that skips values emitted in a time window before
     * the source completes by using the given scheduler as time source.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipLast.ts.png">
     *
     * @param time the length of the time window
     * @param unit the time unit
     * @param scheduler the scheduler used for time source
     * @return an Observable that skips values emitted in a time window before
     *         the source completes by using the given scheduler as time source
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#skiplast">RxJava Wiki: skipLast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211750.aspx">MSDN: Observable.SkipLast</a>
     */
    public Observable<T> skipLast(long time, TimeUnit unit, Scheduler scheduler) {
        return create(new OperationSkipLast.SkipLastTimed<T>(this, time, unit, scheduler));
    }
 
    /**
     * Returns an Observable that emits a single item, a list composed of all
     * the items emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toList.png">
     * <p>
     * Normally, an Observable that returns multiple items will do so by
     * invoking its {@link Observer}'s {@link Observer#onNext onNext} method for
     * each such item. You can change this behavior, instructing the Observable
     * to compose a list of all of these items and then to invoke the Observer's
     * <code>onNext</code> function once, passing it the entire list, by calling
     * the Observable's <code>toList</code> method prior to calling its
     * {@link #subscribe} method.
     * <p>
     * Be careful not to use this operator on Observables that emit infinite or
     * very large numbers of items, as you do not have the option to
     * unsubscribe.
     * 
     * @return an Observable that emits a single item: a List containing all of
     *         the items emitted by the source Observable.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tolist">RxJava Wiki: toList()</a>
     */
    public Observable<List<T>> toList() {
        return create(OperationToObservableList.toObservableList(this));
    }

    /**
     * Return an Observable that emits the items emitted by the source
     * Observable, in a sorted order (each item emitted by the Observable must
     * implement {@link Comparable} with respect to all other items in the
     * sequence).
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toSortedList.png">
     * 
     * @throws ClassCastException if any item emitted by the Observable does not
     *                            implement {@link Comparable} with respect to
     *                            all other items emitted by the Observable
     * @return an Observable that emits the items from the source Observable in
     *         sorted order
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tosortedlist">RxJava Wiki: toSortedList()</a>
     */
    public Observable<List<T>> toSortedList() {
        return create(OperationToObservableSortedList.toSortedList(this));
    }

    /**
     * Return an Observable that emits the items emitted by the source
     * Observable, in a sorted order based on a specified comparison function
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toSortedList.f.png">
     * 
     * @param sortFunction a function that compares two items emitted by the
     *                     source Observable and returns an Integer that
     *                     indicates their sort order
     * @return an Observable that emits the items from the source Observable in
     *         sorted order
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tosortedlist">RxJava Wiki: toSortedList()</a>
     */
    public Observable<List<T>> toSortedList(Func2<? super T, ? super T, Integer> sortFunction) {
        return create(OperationToObservableSortedList.toSortedList(this, sortFunction));
    }

    /**
     * Emit a specified set of items before beginning to emit items from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param values Iterable of the items you want the modified Observable to
     *               emit first
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(Iterable<T> values) {
        return concat(Observable.<T> from(values), this);
    }

    /**
     * Emit a specified set of items with the specified scheduler before
     * beginning to emit items from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.s.png">
     * 
     * @param values iterable of the items you want the modified Observable to
     *               emit first
     * @param scheduler the scheduler to emit the prepended values on
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229372.aspx">MSDN: Observable.StartWith</a>
     */
    public Observable<T> startWith(Iterable<T> values, Scheduler scheduler) {
        return concat(from(values, scheduler), this);
    }

    /**
     * Emit a specified array of items with the specified scheduler before
     * beginning to emit items from the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.s.png">
     *
     * @param values the items you want the modified Observable to emit first
     * @param scheduler the scheduler to emit the prepended values on
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229372.aspx">MSDN: Observable.StartWith</a>
     */
    public Observable<T> startWith(T[] values, Scheduler scheduler) {
        return startWith(Arrays.asList(values), scheduler);
    }

    /**
     * Emit a specified item before beginning to emit items from the source
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1 item to emit
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(T t1) {
        return concat(Observable.<T> from(t1), this);
    }

    /**
     * Emit a specified set of items before beginning to emit items from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1 first item to emit
     * @param t2 second item to emit
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(T t1, T t2) {
        return concat(Observable.<T> from(t1, t2), this);
    }

    /**
     * Emit a specified set of items before beginning to emit items from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1 first item to emit
     * @param t2 second item to emit
     * @param t3 third item to emit
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(T t1, T t2, T t3) {
        return concat(Observable.<T> from(t1, t2, t3), this);
    }

    /**
     * Emit a specified set of items before beginning to emit items from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1 first item to emit
     * @param t2 second item to emit
     * @param t3 third item to emit
     * @param t4 fourth item to emit
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(T t1, T t2, T t3, T t4) {
        return concat(Observable.<T> from(t1, t2, t3, t4), this);
    }

    /**
     * Emit a specified set of items before beginning to emit items from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1 first item to emit
     * @param t2 second item to emit
     * @param t3 third item to emit
     * @param t4 fourth item to emit
     * @param t5 fifth item to emit
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(T t1, T t2, T t3, T t4, T t5) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5), this);
    }

    /**
     * Emit a specified set of items before beginning to emit items from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1 first item to emit
     * @param t2 second item to emit
     * @param t3 third item to emit
     * @param t4 fourth item to emit
     * @param t5 fifth item to emit
     * @param t6 sixth item to emit
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(T t1, T t2, T t3, T t4, T t5, T t6) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5, t6), this);
    }

    /**
     * Emit a specified set of items before beginning to emit items from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1 first item to emit
     * @param t2 second item to emit
     * @param t3 third item to emit
     * @param t4 fourth item to emit
     * @param t5 fifth item to emit
     * @param t6 sixth item to emit
     * @param t7 seventh item to emit
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(T t1, T t2, T t3, T t4, T t5, T t6, T t7) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5, t6, t7), this);
    }

    /**
     * Emit a specified set of items before beginning to emit items from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1 first item to emit
     * @param t2 second item to emit
     * @param t3 third item to emit
     * @param t4 fourth item to emit
     * @param t5 fifth item to emit
     * @param t6 sixth item to emit
     * @param t7 seventh item to emit
     * @param t8 eighth item to emit 
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5, t6, t7, t8), this);
    }

    /**
     * Emit a specified set of items before beginning to emit items from the
     * source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/startWith.png">
     * 
     * @param t1 first item to emit
     * @param t2 second item to emit
     * @param t3 third item to emit
     * @param t4 fourth item to emit
     * @param t5 fifth item to emit
     * @param t6 sixth item to emit
     * @param t7 seventh item to emit
     * @param t8 eighth item to emit 
     * @param t9 ninth item to emit
     * @return an Observable that exhibits the modified behavior
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#startwith">RxJava Wiki: startWith()</a>
     */
    public Observable<T> startWith(T t1, T t2, T t3, T t4, T t5, T t6, T t7, T t8, T t9) {
        return concat(Observable.<T> from(t1, t2, t3, t4, t5, t6, t7, t8, t9), this);
    }

    /**
     * Groups the items emitted by an Observable according to a specified
     * criterion, and emits these grouped items as {@link GroupedObservable}s,
     * one GroupedObservable per group.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupBy.png">
     * 
     * @param keySelector a function that extracts the key from an item
     * @param elementSelector a function to map a source item to an item in a
     *                        {@link GroupedObservable}
     * @param <K> the key type
     * @param <R> the type of items emitted by the resulting
     *            {@link GroupedObservable}s
     * @return an Observable that emits {@link GroupedObservable}s, each of
     *         which corresponds to a unique key value and emits items
     *         representing items from the source Observable that share that key
     *         value
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#groupby-and-groupbyuntil">RxJava Wiki: groupBy</a>
     */
    public <K, R> Observable<GroupedObservable<K, R>> groupBy(final Func1<? super T, ? extends K> keySelector, final Func1<? super T, ? extends R> elementSelector) {
        return create(OperationGroupBy.groupBy(this, keySelector, elementSelector));
    }

    /**
     * Groups the items emitted by an Observable according to a specified
     * criterion, and emits these grouped items as {@link GroupedObservable}s,
     * one GroupedObservable per group.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupBy.png">
     * 
     * @param keySelector a function that extracts the key for each item
     * @param <K> the key type
     * @return an Observable that emits {@link GroupedObservable}s, each of
     *         which corresponds to a unique key value and emits items
     *         representing items from the source Observable that share that key
     *         value
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#groupby-and-groupbyuntil">RxJava Wiki: groupBy</a>
     */
    public <K> Observable<GroupedObservable<K, T>> groupBy(final Func1<? super T, ? extends K> keySelector) {
        return create(OperationGroupBy.groupBy(this, keySelector));
    }

    /**
     * Return an Observable that correlates two sequences when they overlap and
     * groups the results.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupJoin.png">
     * 
     * @param right the other Observable to correlate items from this Observable
     *              with
     * @param leftDuration function that returns an Observable whose emissions
     *                     indicate the duration of the values of this
     *                     Observable
     * @param rightDuration function that returns an Observable whose emissions
     *                      indicate the duration of the values of the
     *                      <code>right</code> Observable
     * @param resultSelector function that takes an item emitted by each source
     *                       Observable and returns the value to be emitted by
     *                       the resulting Observable
     * @return an Observable that emits grouped items based on overlapping
     *         durations from this and another Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#join-and-groupjoin">RxJava Wiiki: groupJoin</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244235.aspx">MSDN: Observable.GroupJoin</a>
     */
    public <T2, D1, D2, R> Observable<R> groupJoin(Observable<T2> right, Func1<? super T, ? extends Observable<D1>> leftDuration, 
            Func1<? super T2, ? extends Observable<D2>> rightDuration,
            Func2<? super T, ? super Observable<T2>, ? extends R> resultSelector) {
        return create(new OperationGroupJoin<T, T2, D1, D2, R>(this, right, leftDuration, rightDuration, resultSelector));
    }
    
    /**
     * Returns an Observable that emits <code>true</code> if the source
     * Observable is empty, otherwise <code>false</code>.
     * <p>
     * In Rx.Net this is negated as the <code>any</code> operator but we renamed
     * this in RxJava to better match Java naming idioms.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/isEmpty.png">
     * 
     * @return an Observable that emits a Boolean
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#exists-and-isempty">RxJava Wiki: isEmpty()</a>
     * @see <a href= "http://msdn.microsoft.com/en-us/library/hh229905.aspx" >MSDN: Observable.Any</a>
     */
    public Observable<Boolean> isEmpty() {
        return create(OperationAny.isEmpty(this));
    }

    /**
     * Returns an Observable that emits the last item emitted by the source or
     * notifies observers of an <code>IllegalArgumentException</code> if the
     * source Observable is empty.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/last.png">
     * 
     * @return an Observable that emits the last item from the source Observable
     *         or notifies observers of an error
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observable-Operators#last">RxJava Wiki: last()</a>
     * @see MSDN: <code>Observable.lastAsync()</code>
     */
    public Observable<T> last() {
        return takeLast(1).single();
    }

    /**
     * Returns an Observable that emits only the last item emitted by the source
     * Observable that satisfies a given condition, or an
     * IllegalArgumentException if no such items are emitted.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/last.p.png">
     * 
     * @param predicate the condition any source emitted item has to satisfy
     * @return an Observable that emits only the last item satisfying the given
     *         condition from the source, or an IllegalArgumentException if no
     *         such items are emitted
     * @throws IllegalArgumentException if no such itmes are emmited
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observable-Operators#last">RxJava Wiki: last()</a>
     * @see MSDN: <code>Observable.lastAsync()</code>
     */
    public Observable<T> last(Func1<? super T, Boolean> predicate) {
        return filter(predicate).takeLast(1).single();
    }

    /**
     * Returns an Observable that emits only the last item emitted by the source
     * Observable, or a default item if the source is empty.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/lastOrDefault.png">
     * 
     * @param defaultValue the default item to emit if the source Observable is
     *                     empty
     * @return an Observable that emits only the last item from the source, or a
     *         default item if the source is empty
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#lastOrDefault">RxJava Wiki: lastOrDefault()</a>
     * @see MSDN: <code>Observable.lastOrDefaultAsync()</code>
     */
    public Observable<T> lastOrDefault(T defaultValue) {
        return takeLast(1).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that emits only the last item emitted by the source
     * Observable that satisfies a given condition, or a default item otherwise.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/lastOrDefault.p.png">
     * 
     * @param defaultValue the default item to emit if the source Observable
     *                     doesn't emit anything that satisfies the given
     *                     condition
     * @param predicate the condition any source emitted item has to satisfy
     * @return an Observable that emits only the last item from the source that
     *         satisfies the given condition, or a default item otherwise
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#lastOrDefault">RxJava Wiki: lastOrDefault()</a>
     * @see MSDN: <code>Observable.lastOrDefaultAsync()</code>
     */
    public Observable<T> lastOrDefault(T defaultValue, Func1<? super T, Boolean> predicate) {
        return filter(predicate).takeLast(1).singleOrDefault(defaultValue);
    }

    /**
     * Returns an Observable that counts the total number of items emitted by
     * the source Observable and emits this count as a 64-bit long.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/longCount.png">
     * 
     * @return an Observable that emits the number of items emitted by the
     *         source Observable as its single, 64-bit long item 
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#count-and-longcount">RxJava Wiki: count()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229120.aspx">MSDN: Observable.LongCount</a>
     * @see #count()
     */
    public Observable<Long> longCount() {
        return reduce(0L, new Func2<Long, T, Long>() {
            @Override
            public Long call(Long t1, T t2) {
                return t1 + 1;
            }
        });
    }
    
    /**
     * Converts an Observable into a {@link BlockingObservable} (an Observable
     * with blocking operators).
     * 
     * @return a <code>BlockingObservable</code> version of this Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Blocking-Observable-Operators">RxJava Wiki: Blocking Observable Operators</a>
     */
    public BlockingObservable<T> toBlockingObservable() {
        return BlockingObservable.from(this);
    }

    /**
     * Converts the items emitted by an Observable to the specified type.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/cast.png">
     * 
     * @param klass the target class type which the items will be converted to
     * @return an Observable that emits each item from the source Observable
     *         converted to the specified type
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#cast">RxJava Wiki: cast()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211842.aspx">MSDN: Observable.Cast</a>
     */
    public <R> Observable<R> cast(final Class<R> klass) {
        return create(OperationCast.cast(this, klass));
    }

    /**
     * Filters the items emitted by an Observable based on the specified type.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/ofClass.png">
     * 
     * @param klass the class type to filter the items emitted by the source
     *              Observable
     * @return an Observable that emits items from the source Observable of
     *         type <code>klass</code>.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#oftype">RxJava Wiki: ofType()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229380.aspx">MSDN: Observable.OfType</a>
     */
    public <R> Observable<R> ofType(final Class<R> klass) {
        return filter(new Func1<T, Boolean>() {
            public Boolean call(T t) {
                return klass.isInstance(t);
            }
        }).cast(klass);
    }

    /**
     * Ignores all items emitted by an Observable and only calls
     * <code>onCompleted</code> or <code>onError</code>.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/ignoreElements.png">
     * 
     * @return an empty Observable that only calls <code>onCompleted</code> or
     *         <code>onError</code>
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#ignoreelements">RxJava Wiki: ignoreElements()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229242.aspx">MSDN: Observable.IgnoreElements</a>
     */
    public Observable<T> ignoreElements() {
        return filter(alwaysFalse());
    }

    /**
     * Applies a timeout policy for each item emitted by the Observable, using
     * the specified scheduler to run timeout timers. If the next item isn't
     * observed within the specified timeout duration starting from its
     * predecessor, observers are notified of a <code>TimeoutException</code>.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout.1.png">
     *
     * @param timeout maximum duration between items before a timeout occurs
     * @param timeUnit the unit of time which applies to the
     *                 <code>timeout</code> argument.
     * @return the source Observable modified to notify observers of a
     *         <code>TimeoutException</code> in case of a timeout
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#timeout">RxJava Wiki: timeout()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh244283.aspx">MSDN: Observable.Timeout</a>
     */
    public Observable<T> timeout(long timeout, TimeUnit timeUnit) {
        return create(OperationTimeout.timeout(this, timeout, timeUnit));
    }

    /**
     * Applies a timeout policy for each item emitted by the Observable, using
     * the specified scheduler to run timeout timers. If the next item isn't
     * observed within the specified timeout duration starting from its
     * predecessor, a specified fallback Observable produces future items and
     * notifications from that point on.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout.2.png">
     *
     * @param timeout maximum duration between items before a timeout occurs
     * @param timeUnit the unit of time which applies to the
     *                 <code>timeout</code> argument
     * @param other fallback Observable to use in case of a timeout
     * @return the source Observable modified to switch to the fallback
     *         Observable in case of a timeout
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#timeout">RxJava Wiki: timeout()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229512.aspx">MSDN: Observable.Timeout</a>
     */
    public Observable<T> timeout(long timeout, TimeUnit timeUnit, Observable<? extends T> other) {
        return create(OperationTimeout.timeout(this, timeout, timeUnit, other));
    }

    /**
     * Applies a timeout policy for each item emitted by the Observable, using
     * the specified scheduler to run timeout timers. If the next item isn't
     * observed within the specified timeout duration starting from its
     * predecessor, the observer is notified of a <code>TimeoutException</code>.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout.1s.png">
     *
     * @param timeout maximum duration between items before a timeout occurs
     * @param timeUnit the unit of time which applies to the
     *                 <code>timeout</code> argument
     * @param scheduler Scheduler to run the timeout timers on
     * @return the source Observable modified to notify observers of a
     *         <code>TimeoutException</code> in case of a timeout
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#timeout">RxJava Wiki: timeout()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh228946.aspx">MSDN: Observable.Timeout</a>
     */
    public Observable<T> timeout(long timeout, TimeUnit timeUnit, Scheduler scheduler) {
        return create(OperationTimeout.timeout(this, timeout, timeUnit, scheduler));
    }

    /**
     * Applies a timeout policy for each item emitted by the Observable, using
     * the specified scheduler to run timeout timers. If the next item isn't
     * observed within the specified timeout duration starting from its
     * predecessor, a specified fallback Observable sequence produces future
     * items and notifications from that point on.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeout.2s.png">
     *
     * @param timeout maximum duration between items before a timeout occurs
     * @param timeUnit the unit of time which applies to the
     *                 <code>timeout</code> argument
     * @param other Observable to use as the fallback in case of a timeout
     * @param scheduler Scheduler to run the timeout timers on
     * @return the source Observable modified so that it will switch to the
     *         fallback Observable in case of a timeout
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#timeout">RxJava Wiki: timeout()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211676.aspx">MSDN: Observable.Timeout</a>
     */
    public Observable<T> timeout(long timeout, TimeUnit timeUnit, Observable<? extends T> other, Scheduler scheduler) {
        return create(OperationTimeout.timeout(this, timeout, timeUnit, other, scheduler));
    }

    /**
     * Records the time interval between consecutive items emitted by an
     * Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeInterval.png">
     * 
     * @return an Observable that emits time interval information items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#timeinterval">RxJava Wiki: timeInterval()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212107.aspx">MSDN: Observable.TimeInterval</a>
     */
    public Observable<TimeInterval<T>> timeInterval() {
        return create(OperationTimeInterval.timeInterval(this));
    }

    /**
     * Records the time interval between consecutive items emitted by an
     * Observable, using the specified Scheduler to compute time intervals.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/timeInterval.s.png">
     * 
     * @param scheduler Scheduler used to compute time intervals
     * @return an Observable that emits time interval information items
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#timeinterval">RxJava Wiki: timeInterval()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212107.aspx">MSDN: Observable.TimeInterval</a>
     */
    public Observable<TimeInterval<T>> timeInterval(Scheduler scheduler) {
        return create(OperationTimeInterval.timeInterval(this, scheduler));
    }

    /**
     * Constructs an Observable that creates a dependent resource object.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/using.png">
     *
     * @param resourceFactory the factory function to obtain a resource object
     *                        that depends on the Observable
     * @param observableFactory the factory function to obtain an Observable
     * @return the Observable whose lifetime controls the lifetime of the
     *         dependent resource object
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#using">RxJava Wiki: using()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229585.aspx">MSDN: Observable.Using</a>
     */
    public static <T, RESOURCE extends Subscription> Observable<T> using(Func0<RESOURCE> resourceFactory, Func1<RESOURCE, Observable<T>> observableFactory) {
        return create(OperationUsing.using(resourceFactory, observableFactory));
    }

    /**
     * Given multiple Observables, return the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     *
     * @param o1 an Observable competing to react first
     * @param o2 an Observable competing to react first
     * @return an Observable that reflects whichever of the given Observables
     *         reacted first
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2) {
        return create(OperationAmb.amb(o1, o2));
    }

    /**
     * Given multiple Observables, return the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     *
     * @param o1 an Observable competing to react first
     * @param o2 an Observable competing to react first
     * @param o3 an Observable competing to react first
     * @return an Observable that reflects whichever of the given Observables
     *         reacted first
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3) {
        return create(OperationAmb.amb(o1, o2, o3));
    }

    /**
     * Given multiple Observables, return the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     *
     * @param o1 an Observable competing to react first
     * @param o2 an Observable competing to react first
     * @param o3 an Observable competing to react first
     * @param o4 an Observable competing to react first
     * @return an Observable that reflects whichever of the given Observables
     *         reacted first
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4) {
        return create(OperationAmb.amb(o1, o2, o3, o4));
    }

    /**
     * Given multiple Observables, return the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     *
     * @param o1 an Observable competing to react first
     * @param o2 an Observable competing to react first
     * @param o3 an Observable competing to react first
     * @param o4 an Observable competing to react first
     * @param o5 an Observable competing to react first
     * @return an Observable that reflects whichever of the given Observables
     *         reacted first
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5));
    }

    /**
     * Given multiple Observables, return the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     *
     * @param o1 an Observable competing to react first
     * @param o2 an Observable competing to react first
     * @param o3 an Observable competing to react first
     * @param o4 an Observable competing to react first
     * @param o5 an Observable competing to react first
     * @param o6 an Observable competing to react first
     * @return an Observable that reflects whichever of the given Observables
     *         reacted first
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5, Observable<? extends T> o6) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5, o6));
    }

    /**
     * Given multiple Observables, return the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     *
     * @param o1 an Observable competing to react first
     * @param o2 an Observable competing to react first
     * @param o3 an Observable competing to react first
     * @param o4 an Observable competing to react first
     * @param o5 an Observable competing to react first
     * @param o6 an Observable competing to react first
     * @param o7 an Observable competing to react first
     * @return an Observable that reflects whichever of the given Observables
     *         reacted first
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5, Observable<? extends T> o6, Observable<? extends T> o7) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5, o6, o7));
    }

    /**
     * Given multiple Observables, return the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     *
     * @param o1 an Observable competing to react first
     * @param o2 an Observable competing to react first
     * @param o3 an Observable competing to react first
     * @param o4 an Observable competing to react first
     * @param o5 an Observable competing to react first
     * @param o6 an Observable competing to react first
     * @param o7 an Observable competing to react first
     * @param o8 an observable competing to react first
     * @return an Observable that reflects whichever of the given Observables
     *         reacted first
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5, Observable<? extends T> o6, Observable<? extends T> o7, Observable<? extends T> o8) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5, o6, o7, o8));
    }

    /**
     * Given multiple Observables, return the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     *
     * @param o1 an Observable competing to react first
     * @param o2 an Observable competing to react first
     * @param o3 an Observable competing to react first
     * @param o4 an Observable competing to react first
     * @param o5 an Observable competing to react first
     * @param o6 an Observable competing to react first
     * @param o7 an Observable competing to react first
     * @param o8 an Observable competing to react first
     * @param o9 an Observable competing to react first
     * @return an Observable that reflects whichever of the given Observables
     *         reacted first
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229733.aspx">MSDN: Observable.Amb</a>
     */
    public static <T> Observable<T> amb(Observable<? extends T> o1, Observable<? extends T> o2, Observable<? extends T> o3, Observable<? extends T> o4, Observable<? extends T> o5, Observable<? extends T> o6, Observable<? extends T> o7, Observable<? extends T> o8, Observable<? extends T> o9) {
        return create(OperationAmb.amb(o1, o2, o3, o4, o5, o6, o7, o8, o9));
    }

    /**
     * Given multiple Observables, return the one that first emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/amb.png">
     *
     * @param sources Observable sources competing to react first
     * @return an Observable that reflects whichever of the given Observables
     *         reacted first
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Conditional-and-Boolean-Operators#amb">RxJava Wiki: amb()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229115.aspx">MSDN: Observable.Amb</a>
     */
    public static <T> Observable<T> amb(Iterable<? extends Observable<? extends T>> sources) {
        return create(OperationAmb.amb(sources));
    }

    /**
     * Invokes an action for each item emitted by the Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnEach.png">
     *
     * @param observer the action to invoke for each item emitted by the source
     *                 Observable
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#dooneach">RxJava Wiki: doOnEach()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229307.aspx">MSDN: Observable.Do</a>
     */
    public Observable<T> doOnEach(Observer<? super T> observer) {
        return create(OperationDoOnEach.doOnEach(this, observer));
    }

    /**
     * Invokes an action if the source Observable calls <code>onError</code>.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnError.png">
     *
     * @param onError the action to invoke if the source Observable calls
     *                <code>onError</code>
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#doonerror">RxJava Wiki: doOnError()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229804.aspx">MSDN: Observable.Do</a>
     */
    public Observable<T> doOnError(final Action1<Throwable> onError) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public void onNext(T args) { }

        };


        return create(OperationDoOnEach.doOnEach(this, observer));
    }
    
    /**
     * Invokes an action when the source Observable calls
     * <code>onCompleted</code>.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnCompleted.png">
     *
     * @param onCompleted the action to invoke when the source Observable calls
     *                    <code>onCompleted</code>
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#dooncompleted">RxJava Wiki: doOnCompleted()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229804.aspx">MSDN: Observable.Do</a>
     */
    public Observable<T> doOnCompleted(final Action0 onCompleted) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onCompleted() {
                onCompleted.call();
            }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onNext(T args) { }

        };


        return create(OperationDoOnEach.doOnEach(this, observer));
    }
    
    
    /**
     * Invokes an action when the source Observable calls
     * <code>onNext</code>.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnNext.png">
     *
     * @param onNext the action to invoke when the source Observable calls
     *               <code>onNext</code>
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#dooneach">RxJava Wiki: doOnNext()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229804.aspx">MSDN: Observable.Do</a>
     */
    public Observable<T> doOnNext(final Action1<? super T> onNext) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onCompleted() { }

            @Override
            public void onError(Throwable e) { }

            @Override
            public void onNext(T args) { 
                onNext.call(args);
            }

        };

        return create(OperationDoOnEach.doOnEach(this, observer));
    }
    
    /**
     * Invokes an action for each item emitted by the Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/doOnEach.png">
     *
     * @param observer the action to invoke for each item emitted by the source
     *                 Observable
     * @return the source Observable with the side-effecting behavior applied
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Observable-Utility-Operators#dooneach">RxJava Wiki: doOnEach()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229307.aspx">MSDN: Observable.Do</a>
     */
    public Observable<T> doOnEach(final Action1<Notification<? super T>> onNotification) {
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onCompleted() {
                onNotification.call(new Notification<T>());
            }

            @Override
            public void onError(Throwable e) {
                onNotification.call(new Notification<T>(e));
            }

            @Override
            public void onNext(T v) {
                onNotification.call(new Notification<T>(v));
            }

        };

        return create(OperationDoOnEach.doOnEach(this, observer));
    }

    /**
     * Whether a given {@link Function} is an internal implementation inside
     * rx.* packages or not.
     * <p>
     * For why this is being used see
     * https://github.com/Netflix/RxJava/issues/216 for discussion on
     * "Guideline 6.4: Protect calls to user code from within an operator"
     * <p>
     * Note: If strong reasons for not depending on package names comes up then
     * the implementation of this method can change to looking for a marker
     * interface.
     * 
     * @param o
     * @return {@code true} if the given function is an internal implementation,
     *         and {@code false} otherwise.
     */
    private boolean isInternalImplementation(Object o) {
        if (o == null) {
            return true;
        }
        // prevent double-wrapping (yeah it happens)
        if (o instanceof SafeObserver) {
            return true;
        }

        Class<?> clazz = o.getClass();
        if (internalClassMap.containsKey(clazz)) {
            //don't need to do reflection
            return internalClassMap.get(clazz);
        } else {
            // we treat the following package as "internal" and don't wrap it
            Package p = o.getClass().getPackage(); // it can be null
            Boolean isInternal = (p != null && p.getName().startsWith("rx.operators"));
            internalClassMap.put(clazz, isInternal);
            return isInternal;
        }
    }

    /**
     * Creates a pattern that matches when both Observables emit an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param second Observable to match with the source Observable
     * @return Pattern object that matches when both Observables emit an item
     * @throws NullPointerException if <code>right</code> is null
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: and()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229153.aspx">MSDN: Observable.And</a>
     */
    public <T2> Pattern2<T, T2> and(Observable<T2> right) {
        return OperationJoinPatterns.and(this, right);
    }

    /**
     * Matches when the Observable has an available item and projects the item
     * by invoking the selector function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param selector selector that will be invoked for items emitted by the
     *                 source Observable
     * @return a Plan that produces the projected results, to be fed (with other
     *         Plans) to the {@link #when} operator
     * @throws NullPointerException if <code>selector</code> is null
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: then()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211662.aspx">MSDN: Observable.Then</a>
     */
    public <R> Plan0<R> then(Func1<T, R> selector) {
        return OperationJoinPatterns.then(this, selector);
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param plans a series of plans created by use of the {@link #then}
     *              operator on patterns
     * @return an Observable that emits the results from matching several
     *         patterns
     * @throws NullPointerException if <code>plans</code> is null
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    public static <R> Observable<R> when(Plan0<R>... plans) {
        return create(OperationJoinPatterns.when(plans));
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param plans a series of plans created by use of the {@link #then}
     *              operator on patterns
     * @return an Observable that emits the results from matching several
     *         patterns
     * @throws NullPointerException if <code>plans</code> is null
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229558.aspx">MSDN: Observable.When</a>
     */
    public static <R> Observable<R> when(Iterable<? extends Plan0<R>> plans) {
        if (plans == null) {
            throw new NullPointerException("plans");
        }
        return create(OperationJoinPatterns.when(plans));
    }

    /**
     * Joins the results from a pattern.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param p1 the plan to join
     * @return an Observable that emits the results from matching a pattern
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public static <R> Observable<R> when(Plan0<R> p1) {
        return create(OperationJoinPatterns.when(p1));
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param p1 a plan
     * @param p2 a plan
     * @return an Observable that emits the results from matching several
     *         patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2) {
        return create(OperationJoinPatterns.when(p1, p2));
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param p1 a plan
     * @param p2 a plan
     * @param p3 a plan
     * @return an Observable that emits the results from matching several
     *         patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3) {
        return create(OperationJoinPatterns.when(p1, p2, p3));
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param p1 a plan
     * @param p2 a plan
     * @param p3 a plan
     * @param p4 a plan
     * @return an Observable that emits the results from matching several
     *         patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4));
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param p1 a plan
     * @param p2 a plan
     * @param p3 a plan
     * @param p4 a plan
     * @param p5 a plan
     * @return an Observable that emits the results from matching several
     *         patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5));
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param p1 a plan
     * @param p2 a plan
     * @param p3 a plan
     * @param p4 a plan
     * @param p5 a plan
     * @param p6 a plan
     * @return an Observable that emits the results from matching several
     *         patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5, Plan0<R> p6) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5, p6));
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param p1 a plan
     * @param p2 a plan
     * @param p3 a plan
     * @param p4 a plan
     * @param p5 a plan
     * @param p6 a plan
     * @param p7 a plan
     * @return an Observable that emits the results from matching several
     *         patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5, Plan0<R> p6, Plan0<R> p7) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5, p6, p7));
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param p1 a plan
     * @param p2 a plan
     * @param p3 a plan
     * @param p4 a plan
     * @param p5 a plan
     * @param p6 a plan
     * @param p7 a plan
     * @param p8 a plan
     * @return an Observable that emits the results from matching several
     *         patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5, Plan0<R> p6, Plan0<R> p7, Plan0<R> p8) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5, p6, p7, p8));
    }

    /**
     * Joins together the results from several patterns.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/and_then_when.png">
     *
     * @param p1 a plan
     * @param p2 a plan
     * @param p3 a plan
     * @param p4 a plan
     * @param p5 a plan
     * @param p6 a plan
     * @param p7 a plan
     * @param p8 a plan
     * @param p9 a plan
     * @return an Observable that emits the results from matching several
     *         patterns
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#and-then-and-when">RxJava Wiki: when()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229889.aspx">MSDN: Observable.When</a>
     */
    @SuppressWarnings("unchecked")
    public static <R> Observable<R> when(Plan0<R> p1, Plan0<R> p2, Plan0<R> p3, Plan0<R> p4, Plan0<R> p5, Plan0<R> p6, Plan0<R> p7, Plan0<R> p8, Plan0<R> p9) {
        return create(OperationJoinPatterns.when(p1, p2, p3, p4, p5, p6, p7, p8, p9));
    }

    /**
     * Correlates the items emitted by two Observables based on overlapping
     * durations.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/join_.png">
     *
     * @param right the second Observable to join items from
     * @param leftDurationSelector a function to select the duration of each 
     *                             item emitted by this Observable, used to
     *                             determine overlap
     * @param rightDurationSelector a function to select the duration of each
     *                              item emitted by the <code>right</code>
     *                              Observable, used to determine overlap
     * @param resultSelector a function that computes a result item for any two
     *                       overlapping items emitted by the two Observables
     * @return an Observable that emits result items computed from source items
     *         that have an overlapping duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Combining-Observables#join">RxJava Wiki: join()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229750.aspx">MSDN: Observable.Join</a>
     */
    public <TRight, TLeftDuration, TRightDuration, R> Observable<R> join(Observable<TRight> right, Func1<T, Observable<TLeftDuration>> leftDurationSelector,
            Func1<TRight, Observable<TRightDuration>> rightDurationSelector,
            Func2<T, TRight, R> resultSelector) {
        return create(new OperationJoin<T, TRight, TLeftDuration, TRightDuration, R>(this, right, leftDurationSelector, rightDurationSelector, resultSelector));
    }    
    
    /**
     * Return an Observable that emits a single HashMap containing all items
     * emitted by the source Observable, mapped by the keys returned by the
     * {@code keySelector} function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMap.png">
     * <p>
     * If a source item maps to the same key, the HashMap will contain the
     * latest of those items.
     * 
     * @param keySelector the function that extracts the key from the source
     *                    items to be used as keys in the HashMap
     * @return an Observable that emits a single HashMap containing the mapped
     *         items from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229137.aspx">MSDN: Observable.ToDictionary</a>
     */
    public <K> Observable<Map<K, T>> toMap(Func1<? super T, ? extends K> keySelector) {
        return create(OperationToMap.toMap(this, keySelector));
    }
    
    /**
     * Return an Observable that emits a single HashMap containing elements with
     * key and value extracted from the items emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMap.png">
     * <p>
     * If a source item maps to the same key, the HashMap will contain the
     * latest of those items.
     * 
     * @param keySelector the function that extracts the key from the source
     *                    items to be used as key in the HashMap
     * @param valueSelector the function that extracts the value from the source
     *                      items to be used as value in the HashMap
     * @return an Observable that emits a single HashMap containing the mapped
     *         items from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212075.aspx">MSDN: Observable.ToDictionary</a>
     */
    public <K, V> Observable<Map<K, V>> toMap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector) {
        return create(OperationToMap.toMap(this, keySelector, valueSelector));
    }
    
    /**
     * Return an Observable that emits a single Map, returned by the
     * <code>mapFactory</code> function, containing key and value extracted from
     * the items emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMap.png">
     * 
     * @param keySelector the function that extracts the key from the source
     *                    items to be used as key in the Map
     * @param valueSelector the function that extracts the value from the source
     *                      items to be used as value in the Map
     * @param mapFactory the function that returns an Map instance to be used
     * @return an Observable that emits a single Map containing the mapped
     *         items emitted by the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     */
    public <K, V> Observable<Map<K, V>> toMap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector, Func0<? extends Map<K, V>> mapFactory) {
        return create(OperationToMap.toMap(this, keySelector, valueSelector, mapFactory));
    }
    
    /**
     * Return an Observable that emits a single HashMap containing an ArrayList
     * of items, emitted by the source Observable and keyed by the
     * <code>keySelector</code> function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMultiMap.png">
     * 
     * @param keySelector the function that extracts the key from the source
     *                    items to be used as key in the HashMap
     * @return an Observable that emits a single HashMap containing an ArrayList
     *         of items mapped from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh212098.aspx">MSDN: Observable.ToLookup</a>
     */
    public <K> Observable<Map<K, Collection<T>>> toMultimap(Func1<? super T, ? extends K> keySelector) {
        return create(OperationToMultimap.toMultimap(this, keySelector));
    }
    
    /**
     * Return an Observable that emits a single HashMap containing an ArrayList
     * of values, extracted by the <code>valueSelector</code> function, emitted
     * by the source Observable and keyed by the <code>keySelector</code>
     * function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMultiMap.png">
     * 
     * @param keySelector the function that extracts the key from the source
     *                    items to be used as key in the HashMap
     * @param valueSelector the function that extracts the value from the source
     *                      items to be used as value in the Map
     * @return an Observable that emits a single HashMap containing an ArrayList
     *         of items mapped from the source Observable
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229101.aspx">MSDN: Observable.ToLookup</a>
     */
    public <K, V> Observable<Map<K, Collection<V>>> toMultimap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector) {
        return create(OperationToMultimap.toMultimap(this, keySelector, valueSelector));
    }
    
    /**
     * Return an Observable that emits a single Map, returned by the
     * <code>mapFactory</code> function, containing an ArrayList of values,
     * extracted by the <code>valueSelector</code> function, emitted by the
     * source Observable and keyed by the <code>keySelector</code> function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMultiMap.png">
     * 
     * @param keySelector the function that extracts the key from the source
     *                    items to be used as key in the Map
     * @param valueSelector the function that extracts the value from the source
     *                      items to be used as value in the Map
     * @param mapFactory the function that returns an Map instance to be used
     * @return an Observable that emits a single Map containing the list of
     *         mapped items from the source Observable.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     */
    public <K, V> Observable<Map<K, Collection<V>>> toMultimap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector, Func0<? extends Map<K, Collection<V>>> mapFactory) {
        return create(OperationToMultimap.toMultimap(this, keySelector, valueSelector, mapFactory));
    }

    /**
     * Return an Observable that emits a single Map, returned by the
     * <code>mapFactory</code> function, that contains a custom collection of
     * values, extracted by the <code>valueSelector</code> function, emitted by
     * the source Observable and keyed by the <code>keySelector</code> function.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/toMultiMap.png">
     * 
     * @param keySelector the function that extracts the key from the source
     *                    items to be used as key in the Map
     * @param valueSelector the function that extracts the value from the source
     *                      items to be used as value in the Map
     * @param mapFactory the function that returns an Map instance to be used
     * @param collectionFactory the function that returns a Collection instance
     *                          for a particular key to be used in the Map
     * @return an Observable that emits a single Map containing the collection
     *         of mapped items from the source Observable.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Mathematical-and-Aggregate-Operators#tomap-and-tomultimap">RxJava Wiki: toMap()</a>
     */
    public <K, V> Observable<Map<K, Collection<V>>> toMultimap(Func1<? super T, ? extends K> keySelector, Func1<? super T, ? extends V> valueSelector, Func0<? extends Map<K, Collection<V>>> mapFactory, Func1<? super K, ? extends Collection<V>> collectionFactory) {
        return create(OperationToMultimap.toMultimap(this, keySelector, valueSelector, mapFactory, collectionFactory));
    } 
    
    /**
     * Return an Observable that skips items from the source Observable until
     * the secondary Observable emits an item.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/skipUntil.png">
     * 
     * @param other the other Observable that has to emit an item before this
     *              Observable's elements are relayed
     * @return an Observable that skips items from the source Observable
     *         until the secondary Observable emits an item.
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Filtering-Observables#skipuntil">RxJava Wiki: skipUntil()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229358.aspx">MSDN: Observable.SkipUntil</a>
     */
    public <U> Observable<T> skipUntil(Observable<U> other) {
        return create(new OperationSkipUntil<T, U>(this, other));
    }

    /**
     * Groups the items emitted by an Observable according to a specified key
     * selector function until the duration Observable expires for the key.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupByUntil.png">
     *
     * @param keySelector a function to extract the key for each item
     * @param durationSelector a function to signal the expiration of a group
     * @return an Observable that emits grouped Observables, each of which
     *         corresponds to a key value and emits all items that share that
     *         same key value that were emitted during the key's duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#groupby-and-groupbyuntil">RxJava Wiki: groupByUntil()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211932.aspx">MSDN: Observable.GroupByUntil</a>
     */
    public <TKey, TDuration> Observable<GroupedObservable<TKey, T>> groupByUntil(Func1<? super T, ? extends TKey> keySelector, Func1<? super GroupedObservable<TKey, T>, ? extends Observable<? extends TDuration>> durationSelector) {
        return groupByUntil(keySelector, Functions.<T>identity(), durationSelector);
    }
    
    /**
     * Groups the items emitted by an Observable according to specified key and
     * value selector functions until the duration Observable expires for the
     * key.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/Netflix/RxJava/images/rx-operators/groupByUntil.png">
     *
     * @param keySelector a function to extract the key for each item
     * @param valueSelector a function to map each source item to an item
     *                      emitted by an Observable group
     * @param durationSelector a function to signal the expiration of a group
     * @return an Observable that emits grouped Observables, each of which
     *         corresponds to a key value and emits all items that share that
     *         same key value that were emitted during the key's duration
     * @see <a href="https://github.com/Netflix/RxJava/wiki/Transforming-Observables#groupby-and-groupbyuntil">RxJava Wiki: groupByUntil()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229433.aspx">MSDN: Observable.GroupByUntil</a>
     */
    public <TKey, TValue, TDuration> Observable<GroupedObservable<TKey, TValue>> groupByUntil(Func1<? super T, ? extends TKey> keySelector, Func1<? super T, ? extends TValue> valueSelector, Func1<? super GroupedObservable<TKey, TValue>, ? extends Observable<? extends TDuration>> durationSelector) {
        return create(new OperationGroupByUntil<T, TKey, TValue, TDuration>(this, keySelector, valueSelector, durationSelector));
    }

}
