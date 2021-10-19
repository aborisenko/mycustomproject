import SAXParser.Handlers.SberDealsXLSLParserHandler.Handler
import org.xml.sax.helpers.DefaultHandler

import java.io.InputStream
import java.util.logging.Logger
import javax.xml.parsers.SAXParserFactory

package object SAXParser {
  val log = Logger.getLogger(this.getClass.toString)

  def parse(in: InputStream, handler: DefaultHandler) = {
    val factory = SAXParserFactory.newInstance
    //    factory.setNamespaceAware(true);
    val saxParser = factory.newSAXParser();
    saxParser.parse(in, handler);
  }

}
