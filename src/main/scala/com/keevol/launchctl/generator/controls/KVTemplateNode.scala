package com.keevol.launchctl.generator.controls

import com.keevol.javafx.utils.{DnD, Labels}
import javafx.geometry.{Insets, Pos}
import javafx.scene.{Cursor, Node}
import javafx.scene.image.ImageView
import javafx.scene.input.{ClipboardContent, Dragboard, TransferMode}
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory

/**
 * {{{
 *   _                                    _
 *  | |                                  | |
 *  | | __   ___    ___  __   __   ___   | |
 *  | |/ /  / _ \  / _ \ \ \ / /  / _ \  | |
 *  |   <  |  __/ |  __/  \ V /  | (_) | | |
 *  |_|\_\  \___|  \___|   \_/    \___/  |_|
 * }}}
 *
 * KEEp eVOLution!
 *
 * @author fq@keevol.com
 * @since 2017.5.12
 *
 *        Copyright 2017 © 杭州福强科技有限公司版权所有
 *        [[https://www.keevol.com]]
 */

class KVTemplateNode(graphic: Node, nodeType: String) extends VBox {
  private val logger = LoggerFactory.getLogger(classOf[KVTemplateNode])

  setPadding(new Insets(5))
  setSpacing(7)
  setAlignment(Pos.CENTER)

  getStyleClass.add("kv-template-node")

  getChildren.addAll(graphic, Labels.default(nodeType))

  DnD.onDrag(this) { dragboard =>
    val content = new ClipboardContent()
    logger.debug(s"put nodeType: ${nodeType} to dragboard.")
    content.putString(nodeType)
    dragboard.setContent(content)
    dragboard.setDragView(graphic.asInstanceOf[ImageView].getImage)
  }
}




