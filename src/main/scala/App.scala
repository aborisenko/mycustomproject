import zipOpener.open

import java.io.File

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

    open(convertToFileURL(args.mkString("")))
  }
}
