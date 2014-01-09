package rx.lang.scala.examples

import scala.concurrent.duration.DurationInt
import scala.concurrent.duration.Duration
import scala.language.postfixOps
import org.junit.Test
import org.scalatest.junit.JUnitSuite
import rx.lang.scala._
import rx.lang.scala.schedulers._
import rx.lang.scala.DefaultIntervalScheduler

class SchedulersExample extends JUnitSuite {

  def printObs[T](o: Observable[T]) {
    o.subscribe(v => println(s"onNext($v)"), err => println(s"onError($err)"), () => println("onCompleted"))
  }
  
  def sleep(d: Duration) {
    Thread.sleep(d.toMillis)
  }
  
  @Test def testWithDefaultImplicits() {
    import rx.lang.scala.schedulers.DefaultImplicits._
    
    printObs(Observable.interval(200 millis).take(10).buffer(300 millis))
    sleep(2500 millis)
    printObs(Observable.interval(200 millis).take(10).window(300 millis).flatMap(obs => obs.sum))
    sleep(2500 millis)
    printObs(Observable.from(List(1, 2, 3)))
  }
  
  @Test def testWithExplicitSchedulers() {
    printObs(Observable.interval(200 millis)(NewThreadScheduler()).take(10)
               .buffer(300 millis)(NewThreadScheduler()))
    sleep(2500 millis)
    printObs(Observable.interval(200 millis)(NewThreadScheduler()).take(10)
               .window(300 millis)(NewThreadScheduler())
               .flatMap(obs => obs.sum))
    sleep(2500 millis)
    printObs(Observable.from(List(1, 2, 3))(ThreadPoolForIOScheduler()))
    sleep(100 millis)
  }
  
  @Test def testWithCustomImplicits() {    
    val testScheduler = TestScheduler()
    
    implicit val myImplicitSchedulerForBufferAndWindow = new Scheduler(testScheduler) 
      with DefaultBufferScheduler with DefaultWindowScheduler with DefaultIntervalScheduler  
    implicit val myImplicitSchedulerForFrom = new Scheduler(ImmediateScheduler())
      with DefaultFromScheduler
      
    printObs(Observable.interval(200 millis).take(10).buffer(300 millis))
    printObs(Observable.interval(200 millis).take(10).window(300 millis).flatMap(obs => obs.sum))
    printObs(Observable.from(List(1, 2, 3)))
    
    for (t <- 0 to 2500) {
      testScheduler.advanceTimeBy(1 millis)
    }
  }
  
  
}