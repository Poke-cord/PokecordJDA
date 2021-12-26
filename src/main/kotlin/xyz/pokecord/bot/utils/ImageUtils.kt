package xyz.pokecord.bot.utils

import java.awt.image.BufferedImage
import java.net.URL
import javax.imageio.ImageIO

object ImageUtils {
  fun BufferedImage.flipHorizontally() = BufferedImage(width, height, type).apply {
    graphics.drawImage(this@flipHorizontally, width, 0, -width, height, null);
  }

  fun loadImage(imageName: String) = loadImage(this::class.java.getResource("/images/$imageName")!!)

  fun loadImage(url: URL) = ImageIO.read(url)!!
}
