import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import js.{Dynamic => Dyn}
import scala.scalajs.js.Dynamic._
import JsVal.jsVal2jsAny
import scala.concurrent.Future
import scala.async.Async.{async, await}

/**
 * Everything related to setting up the Ace editor to
 * do exactly what we want.
 */
class Editor(bindings: Seq[(String, String, () => Any)],
             completions: () => Future[Seq[(String, String)]],
             implicit val logger: Logger) {
  lazy val Autocomplete = js.Dynamic.global.require("ace/autocomplete").Autocomplete
  def sess = editor.getSession()
  def aceDoc = sess.getDocument()
  def code = sess.getValue().asInstanceOf[String]
  def row = editor.getCursorPosition().row.asInstanceOf[Int]
  def column= editor.getCursorPosition().column.asInstanceOf[Int]

  def complete() = {
    if (!js.DynamicImplicits.truthValue(editor.completer))
      editor.completer = js.Dynamic.newInstance(Autocomplete)()
    js.Dynamic.global.window.ed = editor
    editor.completer.showPopup(editor)

    // needed for firefox on mac
    editor.completer.cancelContextMenu()

  }

  val editor: js.Dynamic = {
    val editor = Editor.initEditor

    for ((name, key, func) <- bindings){
      val binding = s"Ctrl-$key|Cmd-$key"
      editor.commands.addCommand(JsVal.obj(
        "name" -> name,
        "bindKey" -> JsVal.obj(
          "win" -> binding,
          "mac" -> binding,
          "sender" -> "editor|cli"
        ),
        "exec" -> func
      ))
    }

    editor.completers = js.Array(JsVal.obj(
      "getCompletions" -> {(editor: Dyn, session: Dyn, pos: Dyn, prefix: Dyn, callback: Dyn) => task * async {
        val things = await(completions()).map{ case (name, value) =>
          JsVal.obj(
            "value" -> value,
            "caption" -> (value + name)
          ).value
        }
        callback(null, js.Array(things:_*))
      }}
    ).value)

    editor.getSession().setTabSize(2)

    editor
  }
}
object Editor{
  def initEditorIn(id: String) = {
    val editor = global.ace.edit(id)
    editor.setTheme("ace/theme/monokai")
    //editor.renderer.setShowGutter(false)
    editor.setShowPrintMargin(false)
    editor
  }
  lazy val initEditor: js.Dynamic = {
    val editor = initEditorIn("editor")
    editor.getSession().setMode("ace/mode/scala")
    editor
  }
}