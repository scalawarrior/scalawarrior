package api

import internal.Stages
import Stages.{Enemy, Goal}

class ScalaWarrior(life: Int, pos: Int, stage: Seq[Option[Object]]) {

  lazy val health: Int = life

  lazy val distance: Int = 7 - pos

  def feel: Space = new Space(stage(pos + 1))

}


class Space(obj: Option[Object]) {

  lazy val isEmpty: Boolean = obj.isEmpty

  lazy val isGoal: Boolean = obj match {
    case Some(Goal) => true
    case _ => false
  }

  lazy val isEnemy: Boolean = obj match {
    case Some(Enemy(_, _)) => true
    case _ => false
  }

}