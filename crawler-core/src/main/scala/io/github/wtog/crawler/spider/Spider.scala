package io.github.wtog.crawler.spider

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.{ AtomicBoolean, AtomicInteger }

import akka.actor.{ ActorRef, PoisonPill, Props }
import io.github.wtog.crawler.actor.ActorManager
import io.github.wtog.crawler.downloader.{ ChromeHeadlessConfig, ChromeHeadlessDownloader, DownloaderActorReceiver }
import io.github.wtog.crawler.dto.DownloadEvent
import io.github.wtog.crawler.processor.PageProcessor
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

/**
  * @author : tong.wang
  * @since : 4/10/18 11:34 AM
  * @version : 1.0.0
  */
case class Spider(pageProcessor: PageProcessor) {

  private lazy val logger = LoggerFactory.getLogger(classOf[Spider])

  private var downloaderActorPath = ""

  val running: AtomicBoolean = new AtomicBoolean(false)

  val name: String = pageProcessor.name

  def start(): Unit =
    if (!running.getAndSet(true)) {
//      if (pageProcessor.downloader.isInstanceOf[ChromeHeadlessDownloader.type] && ChromeHeadlessConfig.chromeDriverNotExecutable) {
//        throw new IllegalStateException("""
//            |cant find chrome driver to execute.
//            |choose one chrome driver from https://npm.taobao.org/mirrors/chromedriver/70.0.3538.16/ to download and install into your system
//          """.stripMargin)
//      }

      val downloaderActor: ActorRef = getDownloadActor
      execute(downloaderActor)
      SpiderPool.addSpider(this)
    }

  private def getDownloadActor: ActorRef = {
    downloaderActorPath = s"downloader-${name}-${System.currentTimeMillis()}"
    val downloaderActor = ActorManager.getNewSystemActor(
      "downloader-dispatcher",
      downloaderActorPath,
      props = Props[DownloaderActorReceiver]
    )
    downloaderActor
  }

  def restart(): Unit = {
    if (running.get()) {
      this.stop()
    }

    start()
  }

  def stop(): Unit =
    if (running.getAndSet(false)) {
      ActorManager.getExistedActor(downloaderActorPath) ! PoisonPill
      SpiderPool.removeSpider(this)
      this.CrawlMetric.clean()
    }

  private def execute(downloaderActor: ActorRef): Future[Unit] =
    Future {
      this.pageProcessor.targetRequests.foreach { url ⇒
        downloaderActor ! DownloadEvent(
          spider = this,
          request = pageProcessor.requestSetting.withRequestUri(url)
        )
        TimeUnit.MILLISECONDS.sleep(this.pageProcessor.requestSetting.sleepTime.toMillis)
      }
    }

  object CrawlMetric {
    private val downloadPageSuccessNum = new AtomicInteger(0)
    private val downloadPageFailedNum  = new AtomicInteger(0)
    private val processPageSuccessNum  = new AtomicInteger(0)

    def downloadedPageSum: Int = downloadPageSuccessNum.get() + downloadPageFailedNum.get()

    def downloadSuccessCounter: Int = downloadPageSuccessNum.getAndIncrement()

    def downloadFailedCounter: Int = downloadPageFailedNum.getAndIncrement()

    def processedSuccessCounter: Int = processPageSuccessNum.getAndIncrement()

    def clean(): Unit = {
      downloadPageSuccessNum.set(0)
      downloadPageFailedNum.set(0)
      processPageSuccessNum.set(0)
    }

    def record(success: Boolean, url: String): Unit = {
      if (logger.isDebugEnabled())
        logger.debug(s"downloaded: ${success} ${url}")
      if (success) downloadSuccessCounter
      else downloadFailedCounter
    }

    def metricInfo(): Map[String, Any] = Map(
      "spider"     -> name,
      "total"      -> downloadedPageSum,
      "downloaded" -> downloadSuccessCounter,
      "processed"  -> processPageSuccessNum.get()
    )

  }

  override def toString: String = s"spider-${name}: ${CrawlMetric.downloadedPageSum}"
}
