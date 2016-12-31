
import scala.scalajs.js
import org.scalajs.jquery._
import upickle.default._
import com.scalawarrior.scalajs.createjs._
import Bootstrap._
import internal.Step

trait GameRunner {

  val objects = Array[Option[(String, Sprite)]](None, None, None, None, None, None, None)
  val stage = new Stage("demoCanvas")

  def nextStep(pos: Int, life: Int, warrior: Sprite): Unit = {
    val json = js.eval(s"ScalaWarriorRunner().run(${pos}, ${life})").asInstanceOf[String]
    val step = read[Step](json)
    playAnimation(step, warrior)
  }

  def enemyPhase(pos: Int, life: Int, warrior: Sprite): Unit = {
    def nextObject(life: Int, head: (Option[(String, Sprite)], Int), rest: Seq[(Option[(String, Sprite)], Int)]): Unit = {
      val (obj, i) = head
      obj match {
        case Some(("jonin", _)) => {
          // Attack of Enemy
          val sheet = new SpriteSheet(js.Dictionary(
            "framerate" -> 10,
            "images"    -> js.Array(Main.loader.getResult("shuriken")),
            "frames"    -> js.Dictionary("regX" -> (-20 - (i * 125)), "height" -> 24, "count" -> 64, "regY" -> -100, "width" -> 64)
          ))
          val shuriken = new Sprite(sheet)
          stage.addChild(shuriken)

          val tween = Tween.get(shuriken, js.Dynamic.literal(loop = false))
          val distance = i * 125 - warrior.x

          tween.to(js.Dynamic.literal(x = distance * -1), distance * 2).call { _: Tween =>
            stage.removeChild(shuriken)
            warrior.gotoAndStop("damage")

          }.to(js.Dynamic.literal(), 200).call { _: Tween =>
            warrior.gotoAndStop("0")

            val nextLife = if(life > 2) life - 2 else 0
            jQuery("#life").text(nextLife.toString)

            if (nextLife == 0) {
              jQuery("#gameOverModal").modal()
            } else {
              rest match {
                case Nil => nextStep(pos, nextLife, warrior)
                case x :: xs => nextObject(nextLife, x, xs)
              }
            }
          }
        }
        case _ => {
          rest match {
            case Nil => nextStep(pos, life, warrior)
            case x :: xs => nextObject(life, x, xs)
          }
        }
      }
    }

    val objectsWithIndex = objects.zipWithIndex
    nextObject(life, objectsWithIndex.head, objectsWithIndex.tail.toList)
  }

  def playAnimation(step: Step, warrior: Sprite): Unit = {
    var x = warrior.x
    val tween = Tween.get(warrior, js.Dynamic.literal(loop = false))
    val Step(action, pos, life) = step

    action match {
      case "walk" => {
        x = pos * 125
        tween.call { _: Tween =>
          warrior.gotoAndPlay("walk")
        }.to(js.Dynamic.literal(x = x), 500).call { _: Tween =>
          warrior.gotoAndStop("0")
        }.to(js.Dynamic.literal(x = x), 200).call { _: Tween =>
          enemyPhase(step.pos, step.life, warrior)
        }
      }
      case "goal" => {
        x = pos * 125
        tween.call { _: Tween =>
          warrior.gotoAndPlay("walk")
        }.to(js.Dynamic.literal(x = x), 500).call { _: Tween =>
          warrior.gotoAndStop("0")
          jQuery("#clearModal").modal()
        }
      }
      case "attack" => {
        tween.call { _: Tween =>
          warrior.gotoAndPlay("attack")
        }.to(js.Dynamic.literal(x = x), 500).call { _: Tween =>
          warrior.gotoAndStop("0")
        }.to(js.Dynamic.literal(x = x), 200).call { _: Tween =>
          enemyPhase(step.pos, step.life, warrior)
        }
      }
      case "attack-and-hit" => {
        tween.call { _: Tween =>
          warrior.gotoAndPlay("attack-hit")
        }.to(js.Dynamic.literal(x = x), 500).call { _: Tween =>
          warrior.gotoAndStop("0")
        }.to(js.Dynamic.literal(x = x), 200).call { _: Tween =>
          enemyPhase(step.pos, step.life, warrior)
        }
      }
      case "attack-and-kill" => {
        tween.call { _: Tween =>
          warrior.gotoAndPlay("attack-hit")
        }.to(js.Dynamic.literal(x = x), 500).call { _: Tween =>
          warrior.gotoAndStop("0")
          objects(pos + 1).foreach { sprite =>
            stage.removeChild(sprite._2)
            objects(pos + 1) = None
          }
        }.to(js.Dynamic.literal(x = x), 200).call { _: Tween =>
          enemyPhase(step.pos, step.life, warrior)
        }
      }
      case "walk-and-damage" => {
        tween.call { _: Tween =>
          warrior.gotoAndPlay("walk")
        }.to(js.Dynamic.literal(x = (pos + 1) * 125 - 40), 400).call { _: Tween =>
          jQuery("#life").text(life.toString)
          warrior.gotoAndStop("damage")
        }.to(js.Dynamic.literal(x = (pos + 1) * 125 - 40), 100).call { _: Tween =>
          warrior.gotoAndStop("0")
        }.to(js.Dynamic.literal(x = x), 400).to(js.Dynamic.literal(x = x), 200).call { _: Tween =>
          enemyPhase(step.pos, step.life - 2, warrior)
        }
      }
      case "walk-and-dead" => {
        tween.call { _: Tween =>
          warrior.gotoAndPlay("walk")
        }.to(js.Dynamic.literal(x = (pos + 1) * 125 - 40), 400).call { _: Tween =>
          jQuery("#life").text(life.toString)
          warrior.gotoAndStop("damage")
        }.to(js.Dynamic.literal(x = (pos + 1) * 125 - 40), 100).call { _: Tween =>
          warrior.gotoAndStop("0")
        }.to(js.Dynamic.literal(x = x), 400).call { _: Tween =>
          jQuery("#gameOverModal").modal()
        }
      }
      case "stay" => {
        tween.to(js.Dynamic.literal(x = x), 400).call { _: Tween =>
          jQuery("#life").text(life.toString)
          enemyPhase(step.pos, step.life, warrior)
        }
      }
    }
  }
}
