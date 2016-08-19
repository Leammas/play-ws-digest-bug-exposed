import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.asynchttpclient.Realm.AuthScheme
import org.asynchttpclient.{DefaultAsyncHttpClient, Realm}
import play.api.libs.ws.WSAuthScheme
import play.api.libs.ws.ahc.AhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()
  
  val user = "user1"
  val password = "user1"
  val url = "http://test.webdav.org/auth-digest/"

  requestAsync(false)
  requestAsync(true)
  requestWS()
  exit()

  def requestAsync(pree: Boolean) = {
    val c = new DefaultAsyncHttpClient()
    val realm = new Realm.Builder(user, password)
      .setUsePreemptiveAuth(pree)
      .setScheme(AuthScheme.DIGEST)
      .build()
    val r = c.prepareGet(url).setRealm(realm).execute()

    val response = r.get()
    println(s"Raw client with pree: ${pree.toString} - ${response.getStatusCode}")
    c.close()
  }

  def requestWS() = {
    val ws = AhcWSClient()
    val r = ws.url(url)
      .withAuth(user, password, WSAuthScheme.DIGEST)

    val f = r.get()

    f.map { response =>
      println(s"WS client - ${response.status}")
      ws.close()
    }
  }

  def exit() = {
    materializer.shutdown()
    system.terminate().foreach { _ =>
      scala.sys.exit()
    }
  }

}
