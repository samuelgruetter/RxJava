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
package rx.lang

/**
 * This package contains all classes that RxScala users need.
 *
 * It basically mirrors the structure of package `rx`, but some changes were made to make it more Scala-idiomatic.
 */
package object scala {
      
  // one trait for each method in Observable which takes a scheduler
  trait DefaultBufferScheduler
  trait DefaultWindowScheduler
  trait DefaultFromScheduler
  trait DefaultIntervalScheduler
  
  // one implicit conversion for each DefaultXxxScheduler
  implicit def toDefaultBufferScheduler(s: Scheduler) = new Scheduler(s) with DefaultBufferScheduler
  implicit def toDefaultWindowScheduler(s: Scheduler) = new Scheduler(s) with DefaultWindowScheduler
  implicit def toDefaultFromScheduler(s: Scheduler) = new Scheduler(s) with DefaultFromScheduler
  implicit def toDefaultIntervalScheduler(s: Scheduler) = new Scheduler(s) with DefaultIntervalScheduler
  
  import rx.lang.scala.schedulers.DefaultImplicits._
  
  /**
   * Placeholder for extension methods into Observable[T] from other types
   */
  implicit class ObservableExtensions[T](val source: Iterable[T]) extends AnyVal {
      def toObservable: Observable[T] = {  Observable.from(source) }
      def toObservable(scheduler: Scheduler): Observable[T] = {  Observable.from(source)(scheduler) }
  }

}
