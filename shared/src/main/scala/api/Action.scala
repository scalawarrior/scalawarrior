package api

object Action {
  case object Walk extends Action
  case object Attack extends Action
  case object Rest extends Action
}

sealed trait Action