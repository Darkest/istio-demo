import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.DebuggingDirectives
import swagger.users.UsersResource

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

object Main extends App {

  implicit val system = ActorSystem("my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  val host = AppConfig.host
  val port = AppConfig.port

  val requestHandler: Route = DebuggingDirectives.logRequestResult("req/resp", Logging.InfoLevel) {
    UsersResource.routes(new Account)
  }

  val bindingFuture = Http().bindAndHandle(requestHandler, host, port)

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
