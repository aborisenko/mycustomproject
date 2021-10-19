import SAXParser.Handlers.SberDealsXLSLParserHandler.Handler

import java.io.InputStream
import java.util.logging.Logger
import javax.xml.parsers.SAXParserFactory

package object SAXParser {
  val log = Logger.getLogger(this.getClass.toString)

  def parse(in: InputStream) = {
    val factory = SAXParserFactory.newInstance
    //    factory.setNamespaceAware(true);
    val saxParser = factory.newSAXParser();
    saxParser.parse(in, new Handler());
  }

}
