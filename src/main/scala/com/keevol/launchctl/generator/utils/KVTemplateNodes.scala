package com.keevol.launchctl.generator.utils

import com.keevol.javafx.controls.launchctl.LaunchdConfigKeys
import com.keevol.javafx.utils.{Images, Labels}
import com.keevol.launchctl.generator.controls.KVTemplateNode
import javafx.scene.Node
import javafx.scene.image.ImageView

import java.util

object KVTemplateNodes {

  val iconMap = new util.HashMap[String, String]()
  iconMap.put(LaunchdConfigKeys.Label.value(), "/icons/lc-label.png")
  iconMap.put(LaunchdConfigKeys.RunAtLoad.value(), "/icons/lc-load-run.png")
  iconMap.put(LaunchdConfigKeys.KeepAlive.value(), "/icons/lc-keepalive.png")
  iconMap.put(LaunchdConfigKeys.Program.value(), "/icons/lc-program.png")
  iconMap.put(LaunchdConfigKeys.ProgramArgs.value(), "/icons/lc-program-args.png")
  iconMap.put(LaunchdConfigKeys.WorkingDirectory.value(), "/icons/lc-working-dir.png")
  iconMap.put(LaunchdConfigKeys.Username.value(), "/icons/lc-user.png")
  iconMap.put(LaunchdConfigKeys.StandardOutputPath.value(), "/icons/lc-output-path.png")
  iconMap.put(LaunchdConfigKeys.StandardErrorPath.value(), "/icons/lc-err-path.png")
  iconMap.put(LaunchdConfigKeys.StartInterval.value(), "/icons/lc-schedule.png")
  iconMap.put(LaunchdConfigKeys.StartCalendarInterval.value(), "/icons/lc-job.png")
  iconMap.put(LaunchdConfigKeys.Custom.value(), "/icons/lc-custom.png")

  def getGraphicAsPerLabel(label: String = ""): ImageView = {
    val imageView = new ImageView(Images.from(iconMap.get(label)))
    imageView.setPreserveRatio(true)
    imageView.setFitHeight(36)
    imageView
  }

  def createTemplateNode(label: String = ""): Node = {
    new KVTemplateNode(getGraphicAsPerLabel(label), label)
  }
}