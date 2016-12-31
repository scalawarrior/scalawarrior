
import scala.scalajs.js
import scala.scalajs.js.Dynamic.global
import org.scalajs.jquery._
import upickle.default._
import com.scalawarrior.scalajs.createjs._
import Bootstrap._
import internal.GameStage
import org.scalajs.dom.ext.SessionStorage

object Main extends js.JSApp with GameRunner with EditorActions {

  val editor: Editor = new Editor(Seq(
    ("Save"    , "S"    , compile(s => {}) _),
    ("Complete", "Space", () => editor.complete())
  ), complete, new Logger(println))

  val loader = new LoadQueue(false)
  val path   = global.location.pathname.toString
  val level  = if(path.startsWith("/level/")) path.replaceFirst("^/level/", "").toInt else 1

  def main(): Unit = {
    val w = stage.canvas.width
    val h = stage.canvas.height

    // Pre-load images
    val manifest = js.Array(
      js.Dictionary("src" -> "background.png", "id" -> "background"),
      js.Dictionary("src" -> "ninja_blue.png", "id" -> "ninja_blue"),
      js.Dictionary("src" -> "ninja_red.png" , "id" -> "ninja_red"),
      js.Dictionary("src" -> "shuriken.png"  , "id" -> "shuriken"),
      js.Dictionary("src" -> "scalacat.png"  , "id" -> "warrior-animation")
    )

    loader.addEventListener("complete", (e: Object) => {
      val background = new Shape()
      background.graphics.beginBitmapFill(loader.getResult("background")).drawRect(0, 0, w, h)

      val spriteSheet = new SpriteSheet(js.Dictionary(
        "framerate"  -> 10,
        "images"     -> js.Array(loader.getResult("warrior-animation")),
        "frames"     -> js.Dictionary("regX" -> -20, "height" -> 100 * 2, "count" -> 64, "regY" -> 10, "width" -> 90 * 2),
        "animations" ->
          js.Dictionary(
            "walk" -> js.Dictionary(
              "frames" -> js.Array(0, 1)
            ),
            "damage" -> js.Dictionary(
              "frames" -> js.Array(5, 0)
            ),
            "attack" -> js.Dictionary(
              "frames" -> js.Array(0, 2, 3, 3)
            ),
            "attack-hit" -> js.Dictionary(
              "frames" -> js.Array(0, 2, 4, 4)
            )
          )
      ))
      val warrior = new Sprite(spriteSheet)

      // load stage
      stage.addChild(background)

      loadStage(level, warrior)

      Ticker.setFPS(60)
      Ticker.addEventListener("tick", stage)

      stage.update()

      jQuery("#run").click((e: AnyRef) => { compileAndRun(warrior) })

      if(path == "/"){
        SessionStorage.remove("source")
      } else {
        SessionStorage("source").foreach { source =>
          editor.sess.setValue(source)
        }
      }

      true
    })

    loader.loadManifest(manifest, true, "/assets/images/")
  }

  def loadStage(level: Int, warrior: Sprite): Unit = {
    jQuery.ajax(js.Dynamic.literal(
      url     = "/stage/" + level,
      `type`  = "GET",
      success = (data: js.Any, status: String, jqXHR: JQueryXHR) => {
        val gameStage = read[GameStage](jqXHR.responseText)
        gameStage.stage.zipWithIndex.foreach { case (x, i) =>
          if(x == "genin"){
            val ninjaSheet = new SpriteSheet(js.Dictionary(
              "framerate" -> 10,
              "images"    -> js.Array(loader.getResult("ninja_blue")),
              "frames"    -> js.Dictionary("regX" -> (-20 - (i * 125 - 80)), "height" -> 100 * 2, "count" -> 64, "regY" -> 10, "width" -> 90 * 2)
            ))
            val ninja = new Sprite(ninjaSheet)
            stage.addChild(ninja)
            objects(i) = Some(("genin", ninja))
          }
          if(x == "jonin"){
            val ninjaSheet = new SpriteSheet(js.Dictionary(
              "framerate" -> 10,
              "images"    -> js.Array(loader.getResult("ninja_red")),
              "frames"    -> js.Dictionary("regX" -> (-20 - (i * 125 - 80)), "height" -> 100 * 2, "count" -> 64, "regY" -> 10, "width" -> 90 * 2)
            ))
            val ninja = new Sprite(ninjaSheet)
            stage.addChild(ninja)
            objects(i) = Some(("jonin", ninja))
          }
        }
        stage.addChild(warrior)
      },
      error = (jqXHR: JQueryXHR, textStatus: String, errorThrow: String) => {
        global.alert(jqXHR.responseText)
      }
    ).asInstanceOf[JQueryAjaxSettings])
  }

}
