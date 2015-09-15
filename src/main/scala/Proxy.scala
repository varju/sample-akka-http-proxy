import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object Proxy extends App {

  implicit val system = ActorSystem("Proxy")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val proxy = Route { context =>
    val poolClientFlow = Http().cachedHostConnectionPool[Int]("httpbin.org", 80)
    val requestId = 42
    val handler = Source.single(context.request -> requestId)
      .via(poolClientFlow)
      .runWith(Sink.head)
      .flatMap(responseWithCode => context.complete(responseWithCode._1))
    handler
  }

  val binding = Http(system).bindAndHandle(handler = proxy, interface = "localhost", port = 9000)
}
