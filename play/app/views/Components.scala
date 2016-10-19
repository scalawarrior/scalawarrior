package views

import scalatags.Text.all._

object Components {

  def help(level: Int) = {
    div(
      (if(level >= 3){
        Seq(
          h5("Stage 3"),
          p(`class`:="recipient",
            "Check life by ", code("warrior.health"), " and recover by returning ", code("Action.Rest"), " in ", code("next()"), " method."
          )
        )
      } else Nil) ++
      (if(level >= 2){
        Seq(
          h5("Stage 2"),
          p(`class`:="recipient",
            "Check the front by ", code("warrior.feel"), ". It returns ", code("Space"), " has following methods:"
          ),
          ul(
            li(code("isEmpty")),
            li(code("isEnemy"))
          ),
          p(`class`:="recipient",
            "Further, attack enemy by returning ", code("Action.Attack"), " in ", code("next()"), " method."
          )
        )
      } else Nil) ++
      (if(level >= 1){
        Seq(
          h5("Stage 1"),
          p(`class`:="recipient",
            "Walk by returning ", code("Action.Walk"), " in ", code("next()"), " method."
          ),
          h5("Keyboard shortcut"),
          ul(
            li(code("Ctrl + S"), ": Compile"),
            li(code("Ctrl + Space"), ": Code completion")
          )
        )
      } else Nil)
    )
  }

  def gameover(level: Int) = {
    div(`class`:="modal", id:="gameOverModal", tabindex:="-1", role:="dialog", "aria-labelledby".attr:="staticModalLabel", "aria-hidden".attr:="true", "data-show".attr:="true", "data-keyboard".attr:="false", "data-backdrop".attr:="static")(
      div(`class`:="modal-dialog")(
        div(`class`:="modal-content")(
          div(`class`:="modal-body")(
            h4(`class`:="modal-title", "Game Over!")
          ),
          div(`class`:="modal-footer")(
            a(`href`:="/level/" + level, `class`:="btn btn-primary", "Retry this stage")
          )
        )
      )
    )
  }


  def clear(level: Int) = {
    div(`class`:="modal", id:="clearModal", tabindex:="-1", role:="dialog", "aria-labelledby".attr:="staticModalLabel", "aria-hidden".attr:="true", "data-show".attr:="true", "data-keyboard".attr:="false", "data-backdrop".attr:="static")(
      div(`class`:="modal-dialog")(
        div(`class`:="modal-content")(
          Seq(div(`class`:="modal-body")(
            h4(`class`:="modal-title", "Congratulations!")
          )) ++
          (if(level == 3){
            Seq(div(`class`:="modal-body")(
              p("You are ready for Scala programming. Go to the next step!"),
              a(href:="https://www.amazon.co.jp/exec/obidos/ASIN/0981531687",
                img(width:="200", src:="https://images-na.ssl-images-amazon.com/images/I/51y4d3qeABL._SX377_BO1,204,203,200_.jpg")
              )
            ))
          } else Nil) ++
          Seq(div(`class`:="modal-footer")(
            a(`href`:="/level/" + level, `class`:="btn btn-secondary", "Retry this stage"),
            if(level == 3){
              a(`href`:="/" + level, `class`:="btn btn-primary", "Go to first stage")
            } else {
              a(`href`:="/level/" + level + 1, `class`:="btn btn-primary", "Go to next stage")
            }
          ))
        )
      )
    )
  }

}
