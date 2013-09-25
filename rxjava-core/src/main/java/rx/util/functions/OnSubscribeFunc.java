package rx.util.functions;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

/**
 * Function interface for work to be performed when an {@link Observable} is subscribed to via {@link Observable#subscribe(Observer)}
 * 
 * @param <T>
 */
public interface OnSubscribeFunc<T> extends Function {

    public Subscription onSubscribe(Observer<? super T> t1);

}
