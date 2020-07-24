import com.typesafe.config.ConfigFactory

object AppConfig {

  lazy val config = ConfigFactory.load(this.getClass.getClassLoader)

  lazy val host = config.getString("server.host")
  lazy val port = config.getInt("server.port")
  lazy val placement = config.getString("server.placement")

}
