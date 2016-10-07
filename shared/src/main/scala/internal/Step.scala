package internal

// TODO Should action be sealed object? Is it possible to deserialize from JSON in Scala.js side?
case class Step(action: String, pos: Int, life: Int)
