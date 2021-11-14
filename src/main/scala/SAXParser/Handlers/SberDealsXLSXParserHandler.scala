package SAXParser.Handlers

import org.apache.poi.ss.usermodel.DateUtil
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.{Attributes, SAXException}
import models.Deal
import SAXParser.log

import java.io.{File, PrintWriter}
import scala.collection.mutable

object SberDealsXLSXParserHandler {

  val dataXmlFileName = "xl/worksheets/sheet1.xml"

  trait DealsHandler {
    def addOne(deal: Deal)
  }

  class Handler( dealsHandler: DealsHandler ) extends DefaultHandler {

    var tagContentBuffer = new StringBuffer

    var captureOpt: Option[String] = None
    var notTheFirstRow = false
    var deal: Deal = Deal()

    @throws[SAXException]
    override def startDocument(): Unit = {
//      dealsHandler.clear()
      captureOpt = None
      notTheFirstRow = false
      tagContentBuffer = new StringBuffer
    }

    @throws[SAXException]
    override def endDocument(): Unit = {
    }

    override def startElement(namespaceURI: String, key: String, qName: String, atts: Attributes) {
      /** в аттрибуте "r" элемента указывается ексель индекс,
       * для "row" это порядковый номер от 1..
       * для "с" это столбецстрока, например A1 */
      val rOpt = Option(atts.getValue("r"))

//      log.info(s"${this.getClass}.startElement, $key, $qName, $rOpt")

      /** первую строку (шапку) не сохраняем */
      if (notTheFirstRow) {

        captureOpt = (for {
          _ <- Option(qName).filter(_ == "c") /** нужные ячейки <с r="A1"> */
          newCapture <- rOpt.filter(key => key.charAt(0) >= 'A' && key.charAt(0) <= 'Z')
        } yield {
//          log.info(s"${this.getClass}.startElement -> notTheFirstRow -> newCapture == ${newCapture.replaceAll("\\d+", "")}")

          /** раз нашли начало нового элемента который хотим спарсить, очистим по это дело буфер
           *  а так же отделим индекс столбца */
          tagContentBuffer = new StringBuffer
          newCapture.replaceAll("\\d+", "")
        }).orElse(captureOpt)

      } else {
        /** если это row и уже не первый, то запоминаем */
        notTheFirstRow = (qName == "row") && rOpt.flatMap(_.toIntOption).exists(_ > 1)
      }

    }

    override def characters(ch: Array[Char], start: Int, length: Int) {
      /** сохранением текста занимаемся только если мы внутри нужной ячейки */
      captureOpt.tapEach { _ =>
        tagContentBuffer.append(new String(ch, start, length))
      }
    }

    override def endElement(uri: String, localName: String, qName: String): Unit = {

      def setUpaDeal(str: String, key: String, deal: Deal): Deal ={
        key match {
          case "A" => deal.copy(contractNumber = Some(str))
          case "B" => deal.copy(dealNumber = str.toLongOption)
          case "C" => deal.copy(dealDate = str.toDoubleOption.map(DateUtil.getJavaDate(_, false)))
          case "D" => deal.copy(settlementDate = str.toDoubleOption.map(DateUtil.getJavaDate(_, false)))
          case "E" => deal.copy(instrumentCode = Some(str))
          case "F" => deal.copy(instrumentType = Some(str))
          case "G" => deal.copy(marketType = Some(str))
          case "H" => deal.copy(transaction = Some(str))
          case "I" => deal.copy(quantity = str.toLongOption)
          case "J" => deal.copy(price = str.toDoubleOption)
          case "K" => deal.copy(accumulatedCouponYield = str.toDoubleOption)
          case "L" => deal.copy(volume = str.toDoubleOption)
          case "M" => deal.copy(currency = Some(str))
          case "N" => deal.copy(rate = str.toDoubleOption)
          case "O" => deal.copy(systemCommission = str.toDoubleOption)
          case "P" => deal.copy(bankCommission = str.toDoubleOption)
          case "Q" => deal.copy(transactionAmount = str.toDoubleOption)
          case "R" => deal.copy(dealType = str.toDoubleOption)
          case _ => deal
        }
      }

      /** по окончании "c" я должен запихать накопленный буфер в поле объекта deal */

      if (qName == "c" && tagContentBuffer.length > 0) {
        captureOpt.tapEach { capture =>
          deal = setUpaDeal(tagContentBuffer.toString, capture, deal)
//          tagContentBuffer = new StringBuffer()
        }
      }

      /** по окончании "row" я должен запихать deal в список и очистить его */

      if (qName == "row" && deal != Deal()) {
        dealsHandler.addOne(deal)
        deal = Deal()
      }

    }
  }
}
