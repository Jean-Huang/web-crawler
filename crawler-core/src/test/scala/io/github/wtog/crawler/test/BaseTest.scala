package io.github.wtog.crawler.test

import java.util.concurrent.atomic.AtomicInteger

import io.github.wtog.crawler.processor.{Page, PageProcessor, RequestSetting}
import io.github.wtog.crawler.rest.Server
import io.github.wtog.crawler.test.server.TestMockServer
import io.github.wtog.utils.ConfigUtils
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * @author : tong.wang
  * @since : 5/16/18 9:19 PM
  * @version : 1.0.0
  */
trait BaseTest extends FunSuite with Matchers with BeforeAndAfterAll {

  lazy val port = ConfigUtils.getIntOpt("crawler.server.port").getOrElse(19000)

  override def beforeAll() = {
    System.getProperty("config.resource", "application-test.conf")
    System.getProperty("log4j.resource", "log4j2-test.xml")
    if (!Server.running)
      TestMockServer.start
  }

  lazy val requestSettingTest = RequestSetting(
    domain = "www.baidu.com",
    url = Some("https://www.baidu.com/s?wd=wtog%20web-crawler")
  )

  case class TestProcessor(requestSettingTest: RequestSetting = requestSettingTest) extends PageProcessor {
    override def requestSetting = requestSettingTest

    /**
      * the target urls for processor to crawl
      *
      * @return
      */
    override def targetUrls: List[String] = ???

    override protected def doProcess(page: Page): Unit = ???
  }

  case class LocalProcessor() extends PageProcessor {

    val link = new AtomicInteger(0)

    override def targetUrls: List[String] = List(s"http://localhost:${port}")

    override protected def doProcess(page: Page): Unit = {
      assert(page.isDownloadSuccess)
      page.addTargetRequest(s"${page.url}?id=${link.incrementAndGet()}")
    }

    override def requestSetting: RequestSetting = {
      RequestSetting(
        domain = "localhost",
        url = Some(s"http://localhost:${port}/mock/list")
      )
    }
  }

  def await[T](future: => Future[T]) = Await.result(future, 1 minute)
}