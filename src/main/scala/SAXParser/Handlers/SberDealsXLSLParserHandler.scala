package SAXParser.Handlers

import org.apache.poi.ss.usermodel.DateUtil
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.{Attributes, SAXException}

import models.Deal
import SAXParser.log

object SberDealsXLSLParserHandler {

  class Handler extends DefaultHandler {

    var list: List[Deal] = List.empty
    //    var tagContentBuffer = new StringBuffer
    var captureOpt: Option[String] = None
    var notTheFirstRow = false
    var deal: Deal = Deal()

    @throws[SAXException]
    override def startDocument(): Unit = {
      //      tagContentBuffer = new StringBuffer
      list = List.empty
      captureOpt = None
      notTheFirstRow = false
      //      println(s"startDocument")
    }

    @throws[SAXException]
    override def endDocument(): Unit = {
      //      println(s"endDocument")
      //        list.foreach(fout.println)
    }

    override def startElement(namespaceURI: String, key: String, qName: String, atts: Attributes) {
      val rOpt = Option(atts.getValue("r"))

      log.info(s"${this.getClass}.startElement, $key, $qName, $rOpt")

      /** первую строку (шапку) не сохраняем */
      if (notTheFirstRow) {

        captureOpt = (for {
          _ <- Option(qName).filter(_ == "c")
          newCapture <- rOpt.filter(key => key.charAt(0) >= 'A' && key.charAt(0) <= 'Z')
        } yield {

          log.info(s"${this.getClass}.startElement -> notTheFirstRow -> newCapture == ${newCapture.replaceAll("\\d+", "")}")

          newCapture.replaceAll("\\d+", "")
        }).orElse(captureOpt)

        //        println(s"captureOpt, $captureOpt")

      } else {
        /** если это row и уже не первый, то запоминаем */
        notTheFirstRow = (qName == "row") && rOpt.flatMap(_.toIntOption).exists(_ > 1)
      }

    }

    override def characters(ch: Array[Char], start: Int, length: Int) {
      val strOpt = Option(new String(ch, start, length).trim()).filter(_.nonEmpty)

      strOpt.tapEach { str =>
        captureOpt.tapEach { capture =>
          deal = capture match {
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
      }
    }

    override def endElement(uri: String, localName: String, qName: String): Unit = {

      /**
       * по идее characters должно пихать все в буфер и уже только в endElement мы можем перенести данные в параметр deal
       * потому что characters может быть очень много */

      //      captureOpt = if (qName == "с") {
      //        captureOpt.flatMap { _ =>
      //
      //          println(s"endElement $qName, $captureOpt -> None")
      //
      //          if (tagContentBuffer.length() > 0) {
      //            list.append(deal)
      //            deal = models.Deal()
      //            println(s"endElement erasing deal -> $deal")
      //          }
      //
      //          None
      //        }
      //      } else captureOpt

      /** так что временно по окончании row я должен запихать deal в список и очистить его */

      if (qName == "row" && deal != Deal()) {
        list = list.appended(deal)
        deal = Deal()
      }

    }
  }
}
