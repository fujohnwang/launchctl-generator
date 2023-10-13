package com.keevol.launchctl.generator

import com.keevol.javafx.KFXApplication
import com.keevol.javafx.splash.KeevolSplashScreenLoader
import com.keevol.javafx.utils.{Keevol, Labels}
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.stage.Stage


class LaunchctlGenerator extends KFXApplication {
   def layoutStage(primaryStage: Stage): Unit = {
    primaryStage.setTitle("Launchctl Daemon Configuration Generator")

    val layout = new BorderPane()
    layout.setCenter(Labels.title("XXXXasj;dfk;jasd "))

    val scene = new Scene(layout)
    scene.getStylesheets.add("/css/style.css")
    primaryStage.setScene(scene)
  }
}


object LaunchctlGenerator {
  def main(args: Array[String]): Unit = {
    KFXApplication.launch(classOf[LaunchctlGenerator], args)
  }
}

object LaunchctlGeneratorIDELauncher {
  def main(args: Array[String]): Unit = {
    LaunchctlGenerator.main(args)
  }
}