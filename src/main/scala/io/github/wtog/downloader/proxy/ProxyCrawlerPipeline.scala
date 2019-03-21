package io.github.wtog.downloader.proxy

import io.github.wtog.pipeline.Pipeline

/**
 * @author : tong.wang
 * @since : 6/2/18 11:57 PM
 * @version : 1.0.0
 */
object ProxyCrawlerPipeline extends Pipeline {

  implicit def MapToProxyDTO(map: Map[String, Any]): ProxyDTO = {
    ProxyDTO(
      map("host").asInstanceOf[String],
      map.getOrElse("port", "80").toString.toInt,
      map.get("username").map(_.asInstanceOf[String]),
      map.get("password").map(_.asInstanceOf[String]))
  }

  override def process(pageResultItem: (String, Map[String, Any])): Unit = {
    val (_, result) = pageResultItem

    ProxyProvider.proxyList.offer(result)
  }
}
