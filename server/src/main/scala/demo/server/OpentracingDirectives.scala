package demo.server

import akka.http.scaladsl.server.Directives._

object OpentracingDirectives {

  protected val opentracingHeaders: Set[String] = AppConfig.opentracingHeaders

  def propagateHeaders = {
    extractRequest.tflatMap { rq =>
      mapResponseHeaders { respHeaders =>
        respHeaders ++ rq._1.headers.filter(h => opentracingHeaders(h.lowercaseName()))
      }
    }
  }

}
