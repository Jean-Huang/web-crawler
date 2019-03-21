package io.github.wtog.downloader.proxy.crawler

import io.github.wtog.processor.{ Page, RequestSetting }
import scala.concurrent.duration._

/**
  * @author : tong.wang
  * @since : 6/7/18 11:27 PM
  * @version : 1.0.0
  */
case class Data5UPageProcessor() extends ProxyProcessorTrait {

  override def doProcess(page: Page): Unit = {
    val ipRow  = page.dom(".wlist > ul > li:nth-child(2) .l2")
    val ipSize = ipRow.size()

    (0 until ipSize).foreach(i ⇒ {
      val ip   = ipRow.get(i).select("span:nth-child(1)").text()
      val port = ipRow.get(i).select("span:nth-child(2)").text()

      page.addPageResultItem(Map("host" -> ip, "port" -> port))
    })

  }

  override def cronExpression: Option[String] = Some("*/5 * * ? * *")

  override def requestSetting: RequestSetting = RequestSetting(domain = "www.data5u.com", sleepTime = 2 second)

  override def targetUrls: List[String] = List("http://www.data5u.com")

}
