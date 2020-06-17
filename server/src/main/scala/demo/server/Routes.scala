package demo.server

import akka.http.scaladsl.model.{HttpResponse, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.Json
import io.circe.generic.auto._

object Routes extends ErrorAccumulatingCirceSupport {

  case class MockDefinition(path: String, requests: Seq[Json], responses: Seq[Json])
  @volatile var state = Map.empty[String, Map[Json, Json]]

  val simpleSleepRoute: Route = {
    (get & parameter("sleep".as[Int].?(1))) { sleep: Int =>
      Thread.sleep(sleep * 1000)
      complete(HttpResponse.apply(StatusCodes.OK))
    }
  }

  // fixed route to update state
  val fixedRoute: Route = post {
    entity(as[MockDefinition]) { mock =>
      val mapping = mock.requests.zip(mock.responses).toMap
      state = state + (mock.path -> mapping)
      complete("ok")
    }
  }

  // dynamic routing based on current state
  val dynamicRoute: Route = ctx => {
    val routes = state.map {
      case (segment, responses) =>
        post {
          path(segment) {
            entity(as[Json]) { input =>
              complete(responses.get(input))
            }
          }
        }
    }
    concat(routes.toList: _*)(ctx)
  }

  val route =
    pathEndOrSingleSlash{
      fixedRoute
    } ~
      path("sleep") {
        simpleSleepRoute
      } ~
      dynamicRoute

}
