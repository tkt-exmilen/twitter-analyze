package controllers

import play.api.mvc._
import twitter4j._
import twitter4j.auth._
import play.api.cache._
import play.api.Play.current


class TwitterAccessController extends Controller {
  def search = Action { request =>
    val params: Map[String, Seq[String]] = request.queryString
    val hashtag = params("hashtag").head
    Ok(<p>{hashtag}</p>).as(HTML)
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
                var authToken: String = request.queryString.get("oauth_token").get.head
                var authVerifier: String = request.queryString.get("oauth_verifier").get.head
                twitter.getOAuthAccessToken(requestToken, authVerifier)
                var user: User = twitter.showUser(twitter.getId())
                Cache.set("twitter_user", user, 4320)
                Cache.remove("twitter")
                Cache.remove("requestToken")
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
