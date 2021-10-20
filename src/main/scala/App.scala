import Helper.convertToFileURL
import SAXParser.Handlers.SberDealsXLSXParserHandler._
import models.Deal
import xlsxOpener._

import java.io.{File, PrintWriter}
import scala.List
import scala.collection.mutable
import scala.util.{Failure, Success}

object App {


  def main(args: Array[String]): Unit = {
    if( args.length != 1){ return }


//    openAll( convertToFileURL( args.mkString("")), (str: String) => str.contains("worksheet") && str.endsWith("xml") ){
//      xmlFile =>
//        val handler = new Handler(list)
//        SAXParser.parse( xmlFile, handler)
//    }

    xlsxOpener.echoNames(convertToFileURL( args(0))) map( _.foreach(println) )

    xlsxOpener.openOne( convertToFileURL( args.mkString("")), dataXmlFileName ){
      xmlFile =>
        val list: mutable.ListBuffer[Deal] = mutable.ListBuffer.empty
        val handler = new Handler(list)
        SAXParser.parse( xmlFile, handler)
        list.toList
    }.fold(
      e => println(e.getMessage),
      list => list.map(println)
    )

  }
}
