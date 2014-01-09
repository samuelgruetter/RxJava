package rx.lang.scala

package object schedulers {
  
  object DefaultImplicits {
    implicit val computationScheduler = new Scheduler(ThreadPoolForComputationScheduler()) 
                                        with DefaultBufferScheduler 
                                        with DefaultWindowScheduler
                                        with DefaultIntervalScheduler
    implicit val immediateScheduler =   new Scheduler(ImmediateScheduler())
                                        with DefaultFromScheduler
  }

}