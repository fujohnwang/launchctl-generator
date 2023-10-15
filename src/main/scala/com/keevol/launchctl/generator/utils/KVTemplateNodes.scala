package com.keevol.launchctl.generator.utils

import com.keevol.javafx.utils.{Images, Labels}
import com.keevol.launchctl.generator.controls.KVTemplateNode
import javafx.scene.Node
import javafx.scene.image.ImageView

object KVTemplateNodes {
  def getGraphicAsPerLabel(label: String = ""): Node = {
    val imageView = new ImageView(Images.from("/images/lc_logo.jpg"))
    imageView.setPreserveRatio(true)
    imageView.setFitHeight(36)
    imageView
  }

  def createNode(label: String = ""): Node = {
    new KVTemplateNode(getGraphicAsPerLabel(label), label)
  }
}