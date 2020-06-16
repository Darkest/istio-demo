package demo.client


import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream._
import akka.stream.scaladsl.{Keep, Sink, Source}
import com.typesafe.scalalogging.LazyLogging
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import demo.client.Streams.{Service, Settings}
import io.circe.Json

import scala.concurrent.duration.Duration
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}


object Streams extends ErrorAccumulatingCirceSupport{
  case class Settings(msgPerSec: Int, path: String, method: String, payload: Option[Json])
  case class Service(uuid: UUID, settings: Settings, killSwitch: UniqueKillSwitch)
}

class Streams(implicit actorSystem: ActorSystem) extends LazyLogging{

  val streamsRunning: scala.collection.concurrent.Map[UUID, Service] =
    new java.util.concurrent.ConcurrentHashMap[UUID, Service].asScala


  def stopStream(uuid: UUID): Boolean = {
    streamsRunning.get(uuid) match {
      case Some(service) =>
        service.killSwitch.shutdown()
        streamsRunning.remove(uuid, service)
      case None => false
    }
  }

  def startStream(settings: Settings): UUID = {
    val uuid = UUID.randomUUID()
    val streamKs = this.stream(settings)
    val service = Service( uuid, settings, streamKs)
    uuid
  }

  def reconfigureStream(uuid: UUID, settings: Settings): UUID = {
    stopStream(uuid)
    startStream(settings)
  }

  private val decider: Supervision.Decider = {
    case exc: StreamTcpException =>
      logger.error(exc.getMessage)
      Supervision.Resume
    case _ => Supervision.Stop
  }

  private def stream(settings: Settings): UniqueKillSwitch =
    Source.apply(1 to Int.MaxValue).log("Request number: ").withAttributes(Attributes
      .logLevels(onElement = Logging.WarningLevel, onFinish = Logging.InfoLevel, onFailure = Logging.InfoLevel))
      .throttle(1, Duration(1, TimeUnit.SECONDS))
      .map(_ => HttpRequest(HttpMethods.getForKeyCaseInsensitive(settings.method).get, Uri(settings.path)))
      .log("Http request")
      .withAttributes(Attributes
        .logLevels(onElement = Logging.WarningLevel, onFinish = Logging.InfoLevel, onFailure = Logging.InfoLevel))
      .mapAsync(4)(rq => Http().singleRequest(rq))
      .viaMat(KillSwitches.single)(Keep.right)
      .log("Http response")
      .withAttributes(Attributes
        .logLevels(onElement = Logging.WarningLevel, onFinish = Logging.InfoLevel, onFailure = Logging.InfoLevel))
      .to(Sink.onComplete {
        case Success(done) => println(s"Completed: $done")
        case Failure(ex) => println(s"Failed: ${ex.getMessage}")
      })
      .withAttributes(ActorAttributes.supervisionStrategy(decider))
      .run()
}
