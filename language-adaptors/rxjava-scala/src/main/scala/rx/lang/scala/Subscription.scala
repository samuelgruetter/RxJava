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

package rx.lang.scala

import rx.lang.scala.subscriptions._
import rx.lang.scala.ImplicitFunctionConversions._

/**
 * Subscriptions are returned from all `Observable.subscribe` methods to allow unsubscribing.
 *
 * This interface is the equivalent of `IDisposable` in the .NET Rx implementation.
 */
trait Subscription {

  private [scala] val asJavaSubscription: rx.Subscription

  /**
   * Call this method to stop receiving notifications on the Observer that was registered when
   * this Subscription was received.
   */
  def unsubscribe(): Unit = asJavaSubscription.unsubscribe()

  /**
   * Checks if the subscription is unsubscribed.
   */
  def isUnsubscribed: Boolean

}

object Subscription {

  /**
   * Creates an [[rx.lang.scala.Subscription]] from an [[rx.Subscription]].
   * This one is not called `apply`, because it's dangerous to have this together with `apply(=>Unit)`,
   * since everything can be converted to Unit.
   */
  private[scala] def fromJava(subscription: rx.Subscription): Subscription = subscription match {
    case x: rx.subscriptions.BooleanSubscription => new BooleanSubscription(x)
    case x: rx.subscriptions.CompositeSubscription => new CompositeSubscription(x)
    case x: rx.subscriptions.MultipleAssignmentSubscription => new MultipleAssignmentSubscription(x)
    case x: rx.subscriptions.SerialSubscription => new SerialSubscription(x)
    case x: rx.Subscription => apply { x.unsubscribe }
  }

  /**
   * Creates an [[rx.lang.scala.Subscription]] that invokes the specified action when unsubscribed.
   */
  def apply(u: => Unit): Subscription = {
    fromJava(rx.subscriptions.Subscriptions.create(scalaFunction0ProducingUnitToAction0(() => u)))
  }

  /**
   * Creates an empty [[rx.lang.scala.Subscription]].
   */
  def apply(): Subscription = Subscription {}

}
