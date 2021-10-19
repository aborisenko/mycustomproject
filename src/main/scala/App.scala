import SAXParser.Handlers.SberDealsXLSLParserHandler.Handler
import models.Deal
import zipParser.parser
import SAXParser.parse

import java.io.{File, FileOutputStream, FileWriter, InputStream, PrintWriter}
import scala.collection.mutable

object App {

   def convertToFileURL(filename:String):String = {
    var path = new File(filename).getAbsolutePath()
    if (File.separatorChar != '/') {
      path = path.replace(File.separatorChar, '/');
    }

//    if (!path.startsWith("/")) {
//      path = "/" + path;
//    }

     path
  }

  def main(args: Array[String]): Unit = {
    if( args.length != 1){ return }

    val list: mutable.ListBuffer[Deal] = mutable.ListBuffer.empty

    def f(in: InputStream): Unit = {
      val handler = new Handler(list)
      SAXParser.parse(in, handler)
    }

    parser(convertToFileURL(args.mkString("")), f)

    val fout:PrintWriter = new PrintWriter(new File("output1.txt"))

    try {
      list.foreach( d => {
        println(d)
        fout.println(d)
      } )
    }finally{
      fout.flush()
      fout.close()
    }

  }
}
