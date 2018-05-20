package wt.downloader.proxy

/**
  * @author : tong.wang
  * @since : 5/20/18 11:08 AM
  * @version : 1.0.0
  */
object ProxyProvider {
  def getProxy(): ProxyDTO = ProxyDTO("", port = 80, "", "")
}

case class ProxyDTO(host: String, port: Int, username: String, password: String)
