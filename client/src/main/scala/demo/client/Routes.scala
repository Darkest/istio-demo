package demo.client

import java.util.UUID

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.{ErrorAccumulatingCirceSupport, FailFastCirceSupport}
import demo.client.Streams._
import io.circe._
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._

class Routes(streams: Streams) extends FailFastCirceSupport{

  def addStream =
    (post & entity(as[Settings])) { settings =>
      complete(streams.startStream(settings).asJson)
    }

  def updateStream(id: UUID) =
    (post & entity(as[Settings])){ settings =>
      complete(streams.reconfigureStream(uuid = id, settings))
    }

  def stopStream(id: UUID) = {
    post{
      complete(streams.stopStream(id))
    }
  }

  def route =
    path("add"){
      addStream
    } ~
    path("update" ~ JavaUUID){ id =>
        updateStream(id)
      } ~
  path("stop" ~ JavaUUID){ id =>
      stopStream(id)
    }
}
