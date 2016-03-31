package controllers

import play.api._
import play.api.mvc._

class HelloController extends Controller {

  def index = Action { request =>
    val params: Map[String, Seq[String]] = request.queryString
    val name = params("name").head
    Ok(views.html.hello(name))
  }
}
