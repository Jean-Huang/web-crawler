package io.github.wtog.test

import java.util.concurrent.TimeUnit

import io.github.wtog.downloader.ChromeHeadlessDownloader
import io.github.wtog.example.{BaiduPageProcessor, ZhihuAnswerPageProcessor}
import io.github.wtog.pipeline.Pipeline
import io.github.wtog.spider.Spider

/**
  * @author : tong.wang
  * @since : 2019-03-03 21:23
  * @version : 1.0.0
  */
class SpiderTest extends BaseTest {

  case object BaiduPageSpecProcessor extends BaiduPageProcessor() {
    override def pipelines = Set(BaiduPageSpecPipeline())
  }

  case class BaiduPageSpecPipeline() extends Pipeline {
    override def process[R](pageResultItem: (String, R)): Unit = {}
  }

  test("crawl baidu start") {
    Spider(name = "baidu", pageProcessor = BaiduPageSpecProcessor).start()
  }

  test("crawl with chrome headless") {
    Spider(name = "zhihu", pageProcessor = ZhihuAnswerPageProcessor(), downloader = ChromeHeadlessDownloader).start()
    TimeUnit.SECONDS.sleep(15)
  }
}