package demo.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, _}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.{ClientConnectionSettings, ConnectionPoolSettings}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.circe.Json
import io.circe.generic.auto._

object Routes extends ErrorAccumulatingCirceSupport with LazyLogging {

  case class MockDefinition(path: String, requests: Seq[Json], responses: Seq[Json])

  @volatile var state = Map.empty[String, Map[Json, Json]]

  val simpleSleepRoute: Route = {
    (get & parameter("sleep".as[Int].?(1))) { sleep: Int =>
      Thread.sleep(sleep * 1000)
      complete(HttpResponse.apply(StatusCodes.OK))
    }
  }

  val versionRoute: Route = {
    (get & extractRequest & extractHost) { case (rq, host) =>
      val statusString = s"${BuildInfo.version} runs on ${AppConfig.placement}"
      complete(HttpResponse(StatusCodes.OK).withEntity(statusString))
    }
  }

  def redirectRoute(implicit actorSystem: ActorSystem): Route = {
    (get & extractRequest) { rq =>
      val rqUrl = AppConfig.redirectUrl
      val conSettings = ConnectionPoolSettings(actorSystem)
        .withConnectionSettings(ClientConnectionSettings(actorSystem))
        .withMaxRetries(AppConfig.redirectRetries)
      val httpRq = HttpRequest(HttpMethods.GET, rqUrl)
      val resp = Http().singleRequest(httpRq, settings = conSettings)
      complete(resp)
    }
  }

  val routeWithCustomCode: Route =
    (get & parameter("code".as[Int]) & extractRequest) { case (code, rq) =>
      complete(HttpResponse(StatusCode.int2StatusCode(code)))
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

  def route(implicit actorSystem: ActorSystem): Route =
    pathEndOrSingleSlash {
      versionRoute ~
        fixedRoute
    } ~
      path("sleep") {
        simpleSleepRoute
      } ~
      path("redirect") {
        redirectRoute
      } ~
      path("responseWith") {
        routeWithCustomCode
      } ~
      dynamicRoute

}
