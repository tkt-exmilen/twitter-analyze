package controllers

import awscala.s3
import com.amazonaws.services.s3.model.ObjectMetadata
import play.api.mvc._
import twitter4j._
import twitter4j.auth._
import play.api.cache._
import play.api.Play.current
import org.json4s._
import org.json4s.native.JsonMethods
import s3._
import java.io.{ByteArrayInputStream, File, InputStream}

class TwitterAccessController extends Controller {

  var twitter: Twitter = null

  def search = Action { request =>
    val params: Map[String, Seq[String]] = request.queryString
    var keyword = params("keyword").head

    // Twitter Search
    val query: Query = new Query()
    if (keyword == null || keyword == "") keyword = "ももくろ"
    query.setQuery(keyword)
    val result: QueryResult = twitter.search(query)
    val status = result.getTweets().get(0)
    val json = JsonMethods.parse(TwitterObjectFactory.getRawJSON(status))
    val text = json \ "text"

    // Save to S3
    val endpoint = System.getProperty("s3.endpoint")
    val bucketName = System.getProperty("s3.bucketName")
    implicit val s3: S3 = S3()
    s3.setEndpoint(endpoint)
    val buckets: Seq[Bucket] = s3.buckets
    val optBucket: Option[Bucket] = s3.bucket(bucketName)
    val key: String = bucketName + "/test"
    val metadata: ObjectMetadata = new ObjectMetadata()
    metadata.setContentLength(text.toString.length.toLong)
    optBucket match {
      case Some(bucket) =>
        println(endpoint)
        println(text)
        println(bucketName)
        println(key)
        bucket.putObject(key, new ByteArrayInputStream(text.toString.getBytes("utf-8")), metadata)
      case None =>
        None
    }
    println(endpoint)

    Ok(<p>{text}</p>).as(HTML)
  }

  def twitterLogin = Action { request =>
    val twitter: Twitter = (new TwitterFactory()).getInstance()
    val requestToken: RequestToken = twitter.getOAuthRequestToken("http://" + request.host + "/twitterOAuthCallback")
    Cache.set("twitter", twitter, 120)
    Cache.set("requestToken", requestToken, 120)

    Redirect(requestToken.getAuthorizationURL())
  }

  def twitterOAuthCallback = Action { request =>
    request.queryString.get("denied") match {
      case Some(denied) => Redirect(routes.TwitterAccessController.twitterLogout)
      case _ => {
        val getTwitter: Option[Twitter] = Cache.getAs[Twitter]("twitter")
        getTwitter match {
          case Some(twitter) => {
            val getRequestToken: Option[RequestToken] = Cache.getAs[RequestToken]("requestToken")
            getRequestToken match {
              case Some(requestToken) => {
                println(request)
                val authToken: String = request.queryString.get("oauth_token").get.head
                val authVerifier: String = request.queryString.get("oauth_verifier").get.head
                twitter.getOAuthAccessToken(requestToken, authVerifier)
                val user: User = twitter.showUser(twitter.getId())
                Cache.set("twitter_user", user, 4320)
                Cache.set("twitter_authToken", authToken, 4320)
                Cache.set("twitter_authVerifier", authVerifier, 4320)
                Cache.remove("twitter")
                Cache.remove("requestToken")

                this.twitter = twitter

              }
              case _ =>
            }
          }
          case _ =>
        }
        Redirect(routes.HomeController.index)
      }
    }
  }

  def twitterLogout = Action { request =>
    Cache.remove("twitter_user")
    Redirect(routes.HomeController.index)
  }
}


