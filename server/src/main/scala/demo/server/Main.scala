package demo.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

object Main extends App {

  implicit val system = ActorSystem("my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val host = AppConfig.host
  val port = AppConfig.port
  val bindingFuture = Http().bindAndHandle(Routes.route, host, port)

  println("Environment variables:")
  System.getenv().asScala.foreach { case (k, v) => println(s"$k -> $v") }
  println("End of environment variables")

  println(s"Server online at http://${host}:$port/")

  scala.sys.addShutdownHook {
    println("Terminating...")
    system.terminate()
    Await.result(system.whenTerminated, 30 seconds)
    println("Terminated... Bye")
  }
}
