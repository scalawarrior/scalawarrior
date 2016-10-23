package views

import scalacss.Defaults._

object Styles extends StyleSheet.Inline {
  import dsl._

  val silkFont = fontFace("Silkscreen")(
    _.src("url(/assets/stylesheets/slkscr.ttf)")
  )

  val h1 = style(
    fontSize(60 px)
  )

  val silk = style(
    fontFamily(silkFont)
  )

  val logo = style(
    verticalAlign.bottom,
    width(60 px),
    paddingBottom(14 px)
  )

  val status = style(
    fontSize(40 %%)
  )

  val modalBody = style(
    fontSize(90 %%)
  )

  val help = style(
    position.absolute,
    float.right,
    marginTop(10 px),
    left(1010 px)
  )

  val editor = style(
    width(1000 px),
    height(300 px)
  )

  val buttons = style(
    margin(8 px),
    width(990 px)
  )

  val errorMessage = style(
    marginTop(8 px),
    padding(8 px)
  )

}
