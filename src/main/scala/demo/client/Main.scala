package demo.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import demo.server.Routes

import scala.io.StdIn

object Main extends App {

//  val lc: LoggerContext = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
//  StatusPrinter.print(lc)


  implicit val system = ActorSystem("my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher
  val routes = new Routes(new Streams)


  val bindingFuture = Http().bindAndHandle(routes.route , "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")

  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
