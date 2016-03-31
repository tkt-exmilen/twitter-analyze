package controllers

import play.api.mvc._

class TwitterAccessController extends Controller {
  def search = Action { request =>
    val params: Map[String, Seq[String]] = request.queryString
    val hashtag = params("hashtag").head
    Ok(<p>{hashtag}</p>).as(HTML)
  }
}
