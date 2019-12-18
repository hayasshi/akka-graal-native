package com.github.vmencik.akkanative

import java.time.{Instant, ZoneId, ZonedDateTime}
import java.util.logging.LogManager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success



object Main extends LazyLogging {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  case class History(size: String, created_at: String)
  case class Response(size: String, histories: Seq[History])

  import scalikejdbc._
  ConnectionPool.singleton("jdbc:mariadb://127.0.0.1/mymysql", "mymysql", "mymysql")

  def main(args: Array[String]): Unit = {
    configureLogging()

    val config = ConfigFactory.load()
    implicit val system: ActorSystem = ActorSystem("graal", config)
    implicit val materializer: Materializer = ActorMaterializer()
    implicit val ec: ExecutionContext = system.dispatcher

    val route =
      path("graal-hp-size") {
        get {
          val result = graalHomepageSize.map { size =>
            val histories = DB.localTx { implicit session =>
              sql"SELECT * FROM access_history ORDER BY id DESC".map { rs =>
                History(
                  rs.long("size").toString,
                  ZonedDateTime.ofInstant(Instant.ofEpochMilli(rs.long("created_at_epoch_millis")), ZoneId.systemDefault()).toString
                )
              }.list().apply()
            }
            (size, histories)
          }
          onSuccess(result) { case (size, histories) =>
            complete(Response(size.toString, histories))
          }
        }
      }

    Http()
      .bindAndHandle(route,
                     config.getString("http.service.bind-to"),
                     config.getInt("http.service.port"))
      .andThen {
        case Success(binding) => logger.info(s"Listening at ${binding.localAddress}")
      }
  }

  private def graalHomepageSize(implicit ec: ExecutionContext,
                                system: ActorSystem,
                                mat: Materializer): Future[Int] = {
    Http().singleRequest(HttpRequest(uri = "https://www.graalvm.org"))
      .flatMap { resp =>
        resp.status match {
          case StatusCodes.OK =>
            resp.entity.dataBytes.runFold(0) { (cnt, chunk) =>
              cnt + chunk.size
            }
          case other =>
            resp.discardEntityBytes()
            throw new IllegalStateException(s"Unexpected status code $other")
        }
      }
      .map { size =>
        DB.localTx { implicit session =>
          sql"INSERT INTO access_history (size, created_at_epoch_millis) values ($size, ${java.time.Instant.now().toEpochMilli})".update.apply()
        }
        size
      }

  }

  private def configureLogging(): Unit = {
    val is = getClass.getResourceAsStream("/app.logging.properties")
    try {
      LogManager.getLogManager.reset()
      LogManager.getLogManager.readConfiguration(is)
    }
    finally is.close()
  }

}
