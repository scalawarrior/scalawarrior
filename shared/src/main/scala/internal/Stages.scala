package internal

/**
 * Define stages.
 */
object Stages {

  val Stage = Seq[Seq[Option[Object]]](
    Seq(None, None, None, None, None, None, None, Some(Goal)),
    Seq(None, None, None, None, Some(Enemy("genin", 12)), None, None, Some(Goal)),
    Seq(None, None, None, Some(Enemy("genin", 12)), None, None, Some(Enemy("jonin", 12)), Some(Goal))
  )

  sealed trait Object {
    val name: String
  }

  case class Enemy(name: String, life: Int) extends Object

  object Goal extends Object {
    val name = "goal"
  }

}
