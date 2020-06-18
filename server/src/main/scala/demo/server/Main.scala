package demo.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.Await
import scala.concurrent.duration._

object Main extends App {

  implicit val system = ActorSystem("my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val bindingFuture = Http().bindAndHandle(Routes.route, "0.0.0.0", 8080)

  println(s"Server online at http://0.0.0.0:8080/")

  scala.sys.addShutdownHook {
    println("Terminating...")
    system.terminate()
    Await.result(system.whenTerminated, 30 seconds)
    println("Terminated... Bye")
  }
}
