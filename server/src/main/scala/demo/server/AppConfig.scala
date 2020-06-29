package demo.server

import com.typesafe.config.ConfigFactory

object AppConfig {

  lazy val config = ConfigFactory.load(this.getClass.getClassLoader)

  lazy val redirectUrl = config.getString("server.redirect-url")

}
