package demo.server

import com.typesafe.config.ConfigFactory

object AppConfig {

  lazy val config = ConfigFactory.load(this.getClass.getClassLoader)

  lazy val redirectUrl = config.getString("server.redirect-url")
  lazy val host = config.getString("server.host")
  lazy val port = config.getInt("server.port")
  lazy val redirectRetries = config.getInt("server.endpoints.redirect.retries")

}
