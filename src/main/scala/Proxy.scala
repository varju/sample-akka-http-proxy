import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}

object Proxy extends App {

  implicit val system = ActorSystem("Proxy")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val proxy = Route { context =>
    val request = context.request
    val flow = Http(system).outgoingConnection("httpbin.org", 80)
    val handler = Source.single(context.request)
      .map(r => r.withHeaders(RawHeader("x-authenticated", "someone")))
      .via(flow)
      .runWith(Sink.head)
      .flatMap(context.complete(_))
    handler
  }

  val binding = Http(system).bindAndHandle(handler = proxy, interface = "localhost", port = 9000)
}
