package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json._
import javax.inject.Inject

import compiler.ScalaJSCompiler
import internal.{Stages, Step}
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi

import scala.collection.mutable.ListBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class Application @Inject()(val messagesApi: MessagesApi)
    (implicit environment: Environment, webJarAssets: WebJarAssets) extends Controller with I18nSupport {

  case class RunForm(stage: Int, source: String)
  case class CompileForm(source: String)
  case class CompleteForm(source: String, flag: String, offset: Int)

  implicit val stepWrites = Json.writes[Step]

  val runForm = Form(
    mapping(
      "stage"  -> number(1, 2),
      "source" -> nonEmptyText()
    )(RunForm.apply)(RunForm.unapply)
  )

  val compileForm = Form(
    mapping(
      "source" -> nonEmptyText()
    )(CompileForm.apply)(CompileForm.unapply)
  )

  val completeForm = Form(
    mapping(
      "source" -> nonEmptyText(),
      "flag"   -> nonEmptyText(),
      "offset" -> number()
    )(CompleteForm.apply)(CompleteForm.unapply)
  )

  def index(level: Int) = Action {
    Ok(views.html.index(level))
  }
  
  def compile = Action { implicit request =>
    compileForm.bindFromRequest.fold(
      error => BadRequest(error.errorsAsJson),
      form  => {
        val logger = new CompilationLogger()
        ScalaJSCompiler.compile(form.source.getBytes("UTF-8"), logger).map { c =>
          val out = ScalaJSCompiler.export(ScalaJSCompiler.fastOpt(c))
          Ok(out).as("application/x-javascript")
        } getOrElse {
          BadRequest(logger.messages.mkString("\n"))
        }
      }
    )
  }

  def complete = Action { implicit request =>
    completeForm.bindFromRequest.fold(
      error => BadRequest(error.errorsAsJson),
      form  => {
        val f = ScalaJSCompiler.autocomplete(form.source, form.flag, form.offset)
        val r = Await.result(f, Duration.Inf)
        Ok(Json.toJson(r.map { case (key, value) => Map(key -> value) }))
      }
    )
  }

  def stage(level: Int) = Action { implicit request =>
    Ok(Json.obj("stage" -> Stages.Stage(level - 1).map {
      case None => ""
      case Some(x) => x.name
    }))
  }

}

class CompilationLogger extends Function1[String, Unit]{
  val messages = new ListBuffer[String]

  override def apply(message: String): Unit = {
    messages += message
  }
}
