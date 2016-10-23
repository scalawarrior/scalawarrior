import Main._
import com.scalawarrior.scalajs.createjs.Sprite
import org.scalajs.dom.ext.SessionStorage
import org.scalajs.jquery.{JQueryAjaxSettings, JQueryXHR, _}

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.Dynamic._

trait EditorActions {

  def compile(f: String => Unit)(): Unit = {
    val source = editor.sess.getValue().asInstanceOf[String]
    SessionStorage.update("source", source)
    jQuery("#status").html("<img src=\"/assets/images/indicator.gif\"> Compiling...")

    val completeSource =
      s"""${source}
         |
         |@scala.scalajs.js.annotation.ScalaJSDefined
         |@scala.scalajs.js.annotation.JSExport
         |object ScalaWarriorRunner extends scala.scalajs.js.Object {
         |  val warrior = new ScalaWarriorController()
         |
         |  def init(): Unit = {
         |    internal.ScalaWarriorEvaluator.init(${level})
         |  }
         |
         |  def run(pos: Int, life: Int): String = {
         |    internal.ScalaWarriorEvaluator.run(warrior, pos, life)
         |  }
         |}
       """.stripMargin


    jQuery.ajax(js.Dynamic.literal(
      url     = "/compile",
      `type`  = "POST",
      data    = js.Dynamic.literal("source" -> completeSource),
      success = (data: js.Any, status: String, jqXHR: JQueryXHR) => {
        jQuery("#status").text("Done.")
        f(jqXHR.responseText)
      },
      error = (jqXHR: JQueryXHR, textStatus: String, errorThrow: String) => {
        jQuery("#status").html("Error: <pre class=\"" + views.Styles.errorMessage.htmlClass + "\">" + jqXHR.responseText + "</pre>")
      }
    ).asInstanceOf[JQueryAjaxSettings])
  }

  def compileAndRun(warrior: Sprite): Unit = {
    compile(source => {
      js.eval(source)
      js.eval("ScalaWarriorRunner().init()")
      nextStep(0, 20, warrior)
    })
  }

  def complete(): Future[Seq[(String, String)]] = {
    val source = editor.sess.getValue().asInstanceOf[String]

    val intOffset = editor.column + source.split("\n")
      .take(editor.row)
      .map(_.length + 1)
      .sum

    val flag = if(source.take(intOffset).endsWith(".")) "member" else "scope"
    val p = Promise[Seq[(String, String)]]

    jQuery.ajax(js.Dynamic.literal(
      url     = "/complete",
      `type`  = "POST",
      data    = js.Dictionary(
        "source" -> source,
        "flag"   -> flag,
        "offset" -> intOffset
      ),
      success = (data: js.Any, status: String, jqXHR: JQueryXHR) => {
        val res = upickle.read[Seq[Map[String, String]]](jqXHR.responseText)
        p.success(res.map(x => x.map { case (key, value) => key -> value }.head))
      },
      error = (jqXHR: JQueryXHR, textStatus: String, errorThrow: String) => {
        global.alert(jqXHR.responseText)
      }
    ).asInstanceOf[JQueryAjaxSettings])

    p.future
  }


}
