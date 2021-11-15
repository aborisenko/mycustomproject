import SAXParser.Handlers.SberDealsXLSXParserHandler._
import models.Deal
import Helper._

import java.io.{BufferedOutputStream, File, FileNotFoundException, FileOutputStream, PrintStream}
import scala.util.control.NonFatal
import scala.util.{Failure, Try, Using}

case class UnknownCommandException( message: String) extends Exception
case class UnknownException(message: String) extends Exception
case class PrintHelp( message: String =
                      s"""--cmd - echo, echopage or sber_deals
                         |--input - path to xlsx input file
                         |--output - path to output file or it's going to write results to the system console
                         |--page - in case you want to specify the page ot parse, using 'xl/worksheets/sheet1.xml' by default
                         |--help - to print this sheet
                         |""".stripMargin) extends Exception

case class Task( command: Option[String] = None, file: Option[String] = None, output: Option[String] = None, page: Option[String] = None){

  override def toString: String = {
    s"""Task(
       |\tcommand     = $ANSI_GREEN ${command.getOrElse("")} $ANSI_RESET
       |\tinput file  = $ANSI_GREEN ${file.getOrElse("")} $ANSI_RESET
       |\toutput file = $ANSI_GREEN ${output.getOrElse("")} $ANSI_RESET
       |\tpage file   = $ANSI_GREEN ${page.getOrElse("")} $ANSI_RESET
       |""".stripMargin
  }
}

object App {

  def splitArgsIntoTuple(s: String):(Option[String],Option[String]) = {
    val res = s.split("=").toList
    assert(res.length <= 2 && res.nonEmpty)
    (res.headOption, res.tail.headOption)
  }

  def argsIntoTask(taskE: Either[Exception,Task], arg: (Option[String],Option[String])): Either[Exception,Task] = {
    taskE flatMap { task =>
      arg match {
        case (Some("--cmd"), opt1@Some(_)) => Right(task.copy(command = opt1))
        case (Some("--input"), opt2:Some[String]) => Right(task.copy(file = opt2))
        case (Some("--output"), opt3:Some[String]) => Right(task.copy(output = opt3))
        case (Some("--page"), opt4:Some[String]) => Right(task.copy(page = opt4))
        case (Some("--help"), _) => Left( PrintHelp())
        case _ => Left( PrintHelp())
      }
    }
  }

  def main(args: Array[String]): Unit = {

    val taskE: Either[Exception, Task] =
      args.map(splitArgsIntoTuple).foldLeft[Either[Exception, Task]](Right(Task()))(argsIntoTask)

    println(taskE.getOrElse(""))

    for {
      task <- taskE
      filename <- task.file.toRight(new FileNotFoundException("filename must be specified"))
      inputF = new File(filename)
    } yield Using(
      task.output match {
        case Some(filename) => new PrintStream(new BufferedOutputStream(new FileOutputStream(filename)))
        case None => System.out
      }
    ){ output: PrintStream =>

      task.command match {
        case Some("echo") => xlsxOpener.getNames(inputF)( output.println )
        case Some("echopage") => xlsxOpener.openOne(filename, task.page.getOrElse(dataXmlFileName)) { xmlFile =>

            val b = new Array[Byte](1024)
            Iterator.continually{
              val len = xmlFile.read(b)
              output.write(b, 0, len)
              len
            }.takeWhile(_ > 0).foreach(_ => ())

        }
        case Some("sber_deals") => xlsxOpener.openOne(filename, task.page.getOrElse(dataXmlFileName)) { xmlFile =>

            val dealsHandler = new DealsHandler {
              def addOne(deal: Deal): Unit = output.println(deal.toString)
            }

            val handler = new Handler(dealsHandler)
            SAXParser.parse(xmlFile, handler)
        }
        case _ => Failure( UnknownCommandException(task.command.getOrElse("command must be specified, try --help instead")))
      }

    }.recoverWith {
      case NonFatal(e) =>
        System.err.println(e)
        Failure(e)
    }.flatten.toEither

  }
}

