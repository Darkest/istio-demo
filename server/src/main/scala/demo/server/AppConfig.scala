package demo.server

import com.typesafe.config.{Config, ConfigFactory}

import scala.jdk.CollectionConverters._

object AppConfig {

  lazy val config: Config = ConfigFactory.load(this.getClass.getClassLoader)

  lazy val redirectUrl: String = config.getString("server.redirect-url")
  lazy val host: String = config.getString("server.host")
  lazy val port: Int = config.getInt("server.port")
  lazy val redirectRetries: Int = config.getInt("server.endpoints.redirect.retries")
  lazy val placement: String = config.getString("server.placement")
  lazy val opentracingHeaders: Set[String] = config.getStringList("akka.opentracing.headers").asScala.toSet.map { s: String => s.toLowerCase }
  lazy val podName: String = System.getenv().asScala.getOrElse("POD_NAME", "unknown")

}
