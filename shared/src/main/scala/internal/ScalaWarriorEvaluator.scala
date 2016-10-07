package internal

import api._
import Stages._

object ScalaWarriorEvaluator {

  var currentStage: Array[Option[Object]] = null

  def init(level: Int): Unit = {
    currentStage = Stage(level - 1).toArray
  }

  def run(controller: ScalaWarriorController, pos: Int, life: Int): String = {
    val step = controller.next(new ScalaWarrior(life, pos, currentStage)) match {
      case Action.Walk => {
        currentStage(pos + 1) match {
          case None              => Step("walk", pos + 1, life)
          case Some(Goal)        => Step("goal", pos + 1, life)
          case Some(Enemy(_, _)) => {
            if(life <= 3){
              Step("walk-and-dead", pos, 0)
            } else {
              Step("walk-and-damage", pos, life)
            }
          }
        }
      }
      case Action.Attack => {
        currentStage(pos + 1) match {
          case Some(x: Enemy) => {
            val enemy = x.copy(life = x.life - 5)
            if(enemy.life <= 0){
              currentStage(pos + 1) = None
              Step("attack-and-kill", pos, life)
            } else {
              currentStage(pos + 1) = Some(enemy)
              Step("attack-and-hit", pos, life)
            }
          }
          case _ => Step("attack", pos, life)
        }
      }
      case Action.Rest => Step("stay", pos, if(life > 10) 20 else life + 10)
    }

    "{\"action\": \"" + step.action + "\", \"pos\": " + step.pos + ", \"life\": " + step.life + "}"
  }

}
